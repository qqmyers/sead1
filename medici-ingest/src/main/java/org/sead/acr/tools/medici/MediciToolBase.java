package org.sead.acr.tools.medici;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.ContentStoreContext;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.kernel.impl.HashFileContext;
import org.tupeloproject.kernel.impl.MemoryContext;
import org.tupeloproject.mysql.MysqlContext;
import org.tupeloproject.mysql.NewMysqlContext;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

/*Minimal setup required to run tools local to a medici instance
 * 
 */
public class MediciToolBase {

    private static Log                log      = LogFactory.getLog(FileCollectionIngester.class);
    protected static BeanSession      beansession;
    protected static MimeMap          mimemap;
    protected static PersonBean       creator;

    protected static Context          context;

    private static PrintWriter        pw       = null;

    protected static boolean          listonly = false;

    protected static final Properties props    = new Properties();

    protected static void println(String s) {
        System.out.println(s);
        if (pw != null)
            pw.println(s);
        return;
    }

    protected static void init(String prefix, boolean useBeanSession) throws IOException, ClassNotFoundException, OperatorException {
        File outputFile = new File(prefix + System.currentTimeMillis() + ".txt");
        init(outputFile, useBeanSession);
    }

    protected static void init(File outputFile, boolean useBeanSession) throws IOException, ClassNotFoundException, OperatorException {

        // load properties

        props.load(new FileInputStream("server.properties"));

        try {
            pw = new PrintWriter(new FileWriter(outputFile));
        } catch (Exception e) {
            println(e.getMessage());
        }

        setContext(props);

        if (useBeanSession == true) {
            // create beansession
            beansession = createBeanSession(props);

            // MimeMap
            MimeMap.initializeContext(beansession.getContext());
            mimemap = new MimeMap(beansession.getContext());

            // creator
            creator(props);
            println(String.format("Creator    : %s <%s> [%s]", creator.getName(), creator.getEmail(), creator.getUri()));
        }
    }

    protected static void flushLog() {
        if (pw != null) {
            pw.flush();
        }

    }
    
    protected static void closeLog() {
        if (pw != null) {
            pw.flush();
            pw.close();
        }

    }

    protected static void creator(Properties props) throws OperatorException {
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
                    if (!listonly) {
                        beansession.save(creator);
                    }
                    return;
                }
            }
        }

        creator = PersonBeanUtil.getAnonymous();
        if (!listonly) {
            beansession.save(creator);
        }
    }

    protected static BeanSession createBeanSession(Properties props) throws OperatorException, ClassNotFoundException {

        return CETBeans.createBeanSession(context);
    }

    private static void setContext(Properties props) {
        // create the context
        context = null;
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
    }

    protected static HashMap<String, String> getConfigOptions() {
        HashMap<String, String> options = new HashMap<String, String>();
        Unifier uf = new Unifier();
        uf.addPattern("entry", Rdf.TYPE, Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/mmdb/Configuration"));
        uf.addPattern("entry", Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/key"), "key");
        uf.addPattern("entry", Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/mmdb/configuration/value"), "val");

        uf.setColumnNames("key", "val");

        try {
            context.perform(uf);
            for (Tuple t : uf.getResult()) {
                options.put(t.get(0).toString(), t.get(1).toString());
            }
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return options;
    }
}
