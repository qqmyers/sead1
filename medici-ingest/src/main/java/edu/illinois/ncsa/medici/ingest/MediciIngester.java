package edu.illinois.ncsa.medici.ingest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.imageio.stream.FileImageInputStream;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.BlobWriter;
import org.tupeloproject.kernel.ContentStoreContext;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.beans.SaveBeanPreprocessor;
import org.tupeloproject.kernel.impl.HashFileContext;
import org.tupeloproject.kernel.impl.MemoryContext;
import org.tupeloproject.mysql.MysqlContext;
import org.tupeloproject.mysql.NewMysqlContext;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;

import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

public class MediciIngester {
    private static Log         log = LogFactory.getLog(MediciIngester.class);

    private static BeanSession beansession;
    private static MimeMap     mimemap;
    private static PersonBean  creator;
    private static String      stringContext;
    private static String      server;

    public static void main(String[] args) throws Exception {
        // load properties
        final Properties props = new Properties();
        props.load(new FileInputStream("server.properties"));

        // create beansession
        beansession = createBeanSession(props);

        // MimeMap
        MimeMap.initializeContext(beansession.getContext());
        mimemap = new MimeMap(beansession.getContext());

        // add special hook
        DatasourceBeanPreprocessor.setDatasourceBeanPreprocessor(beansession);

        // creator
        creator(props);
        System.out.println(String.format("Creator    : %s <%s> [%s]", creator.getName(), creator.getEmail(), creator.getUri()));

        // extractor
        server = props.getProperty("extractor.url");
        // launch the job
        if (!server.endsWith("/")) { //$NON-NLS-1$
            server += "/"; //$NON-NLS-1$
        }
        server += "extractor/extract"; //$NON-NLS-1$
        stringContext = URLEncoder.encode(CETBeans.contextToNTriples(beansession.getContext()), "UTF-8"); //$NON-NLS-1$

        // go through arguments
        for (String arg : args) {
            File file = new File(arg);
            if (file.isDirectory()) {
                uploadCollection(file);
            } else {
                uploadFile(file);
            }
        }
    }

    private static void uploadCollection(File dir) throws OperatorException, IOException {
        CollectionBean collection = new CollectionBean();
        collection.setCreationDate(new Date(dir.lastModified()));
        collection.setCreator(creator);
        collection.setLabel(dir.getName());
        collection.setLastModifiedDate(new Date(dir.lastModified()));
        collection.setTitle(dir.getName());
        beansession.save(collection);

        System.out.println(String.format("Collection : %s ", collection.getTitle()));

        Collection<DatasetBean> beans = new HashSet<DatasetBean>();
        for (File file : dir.listFiles()) {
            if (file.getName().startsWith(".")) {
                continue;
            }
            if (file.isFile()) {
                beans.add(uploadFile(file));
            }
        }
        new CollectionBeanUtil(beansession).addBeansToCollection(collection, beans);
    }

    private static DatasetBean uploadFile(File file) throws OperatorException, IOException {
        final DatasetBean dataset = new DatasetBean();
        dataset.setCreator(creator);
        dataset.setDate(new Date());
        dataset.setFilename(file.getName());
        dataset.setLabel(file.getName());
        dataset.setMimeType(mimemap.getContentTypeFor(file.getName()));
        dataset.setSize(file.length());
        dataset.setTitle(file.getName());

        InputStream is = new FileInputStream(file) {
            long total = 0;

            public int read(byte[] b) throws IOException {
                int count = super.read(b);
                total += count;
                int perc = (int) (total * 100 / dataset.getSize());
                System.out.print(String.format("Dataset    : %s [%d, %s] %d%%\r", dataset.getTitle(), dataset.getSize(), dataset.getMimeType(), perc));
                return count;
            };
        };

        BlobWriter bw = new BlobWriter();
        bw.setInputStream(is);
        bw.setSubject(Resource.uriRef(dataset.getUri()));
        beansession.getContext().perform(bw);
        is.close();

        beansession.save(dataset);
        System.out.println(String.format("Dataset    : %s [%d, %s] 100%%", dataset.getTitle(), dataset.getSize(), dataset.getMimeType()));
        
        try {
            callExtractor(dataset);
        } catch (Exception e) {
            e.printStackTrace();
            server = null;
        }

        return dataset;
    }

    private static void creator(Properties props) throws OperatorException {
        Set<String> keys = new HashSet<String>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith("user.")) { //$NON-NLS-1$
                String pre = key.substring(0, key.lastIndexOf(".")); //$NON-NLS-1$
                if (!keys.contains(pre)) {
                    keys.add(pre);
                    String fullname = props.getProperty(pre + ".fullname");
                    String email = props.getProperty(pre + ".email");
                    String username = props.getProperty(pre + ".username", email);

                    creator = new PersonBean();
                    creator.setUri(PersonBeanUtil.getPersonID(username));
                    creator.setEmail(email);
                    creator.setLabel(fullname);
                    creator.setName(fullname);
                    beansession.save(creator);
                    return;
                }
            }
        }

        creator = PersonBeanUtil.getAnonymous();
        beansession.save(creator);
    }

    private static void callExtractor(DatasetBean dataset) throws Exception {
        if (server == null) {
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        
        // create the body of the message
        sb.append("context="); //$NON-NLS-1$
        sb.append(stringContext);
        sb.append("&dataset="); //$NON-NLS-1$
        sb.append(URLEncoder.encode(dataset.getUri(), "UTF-8")); //$NON-NLS-1$

        URL url = new URL(server);
        URLConnection conn = url.openConnection();
        conn.setReadTimeout(1000);
        if (conn.getReadTimeout() != 1000) {
            log.info("Could not set read timeout! (set to " + conn.getReadTimeout() + ").");
        }

        // send post
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(sb.toString());
        wr.flush();
        wr.close();

        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            log.debug(line);
        }
        rd.close();
    }

    private static BeanSession createBeanSession(Properties props) throws OperatorException, ClassNotFoundException {
        // create the context
        Context context = null;
        if ("mysql".equals(props.getProperty("context.type"))) {
            MysqlContext mc = new MysqlContext();
            mc.setUser(props.getProperty("mysql.user"));
            mc.setPassword(props.getProperty("mysql.password"));
            mc.setSchema(props.getProperty("mysql.schema"));
            mc.setHost(props.getProperty("mysql.host"));
            try {
                mc.connect();
                context = mc;
            } catch (SQLException e) {
                log.error("Could not connect to database.", e);
            }

        } else if ("newmysql".equals(props.getProperty("context.type"))) {
            NewMysqlContext mc = new NewMysqlContext();
            mc.setUsername(props.getProperty("mysql.user"));
            mc.setPassword(props.getProperty("mysql.password"));
            mc.setDatabase(props.getProperty("mysql.schema"));
            mc.setHostname(props.getProperty("mysql.host"));
            if (mc.open()) {
                context = mc;
            } else {
                log.error("Could not connect to database.");
            }

            // } else if ("psql".equals(props.getProperty("context.type"))) {
            // PostgresqlContext mc = new PostgresqlContext();
            // mc.setUser(props.getProperty("psql.user"));
            // mc.setPassword(props.getProperty("psql.password"));
            // mc.setSchema(props.getProperty("psql.schema"));
            // mc.setHost(props.getProperty("psql.host"));
            // try {
            // mc.connect();
            // context = mc;
            // } catch (SQLException e) {
            // log.error("Could not connect to database.", e);
            // }
            // jdbc = mc.getJdbcUrl();
            // } else if ("newpsql".equals(props.getProperty("context.type"))) {
            // NewPostgresqlContext mc = new NewPostgresqlContext();
            // mc.setUsername(props.getProperty("psql.user"));
            // mc.setPassword(props.getProperty("psql.password"));
            // mc.setDatabase(props.getProperty("psql.schema"));
            // mc.setHostname(props.getProperty("psql.host"));
            // if (mc.open()) {
            // context = mc;
            // } else {
            // log.error("Could not connect to database.");
            // }
            // jdbc = mc.getJdbcUrl();
        }

        if (context == null) {
            log.error("Could not connect to persistent storage, creating memory database.");
            context = new MemoryContext();
        }

        if (props.containsKey("hfc.path")) {
            File hfcpath = new File(props.getProperty("hfc.path"));
            if (!hfcpath.exists()) {
                hfcpath.mkdirs();
            }
            log.info("HFC :" + hfcpath.getAbsolutePath());
            if (hfcpath.isDirectory()) {
                HashFileContext hfc = new HashFileContext();
                hfc.setDepth(3);
                hfc.setDirectory(hfcpath);
                ContentStoreContext csc = new ContentStoreContext();
                csc.setMetadataContext(context);
                csc.setDataContext(hfc);
                context = csc;
            }
        }

        return CETBeans.createBeanSession(context);
    }
}
