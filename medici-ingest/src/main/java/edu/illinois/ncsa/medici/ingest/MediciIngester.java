package edu.illinois.ncsa.medici.ingest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.BlobWriter;
import org.tupeloproject.kernel.ContentStoreContext;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.SubjectRemover;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.kernel.impl.HashFileContext;
import org.tupeloproject.kernel.impl.MemoryContext;
import org.tupeloproject.mysql.MysqlContext;
import org.tupeloproject.mysql.NewMysqlContext;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

public class MediciIngester {
    private static Log         log            = LogFactory.getLog(MediciIngester.class);

    private static boolean     CALL_EXTRACTOR = false;
    private static boolean     USE_DATABASE   = false;

    private static Resource    DCTERMS_ID     = Resource.uriRef("http://purl.org/dc/terms/identifier");
    private static Resource    FRBRCORE_ID     = Resource.uriRef("http://purl.org/vocab/frbr/core#embodimentOf");

    private static BeanSession beansession;
    private static MimeMap     mimemap;
    private static PersonBean  creator;
    private static String      stringContext;
    private static String      server;
    
    private static boolean     ingestOnlyMissing = false;
    private static boolean     deleteCollectionAndChilds = false;
    private static boolean     countDatasetWithinCollection = false;
    private static Map<String, ArrayList<String>> ingestedCollections = new HashMap<String, ArrayList<String>>();
    private static int dataSetCount = 0;

    public static void main(String[] args) throws Exception {
        // load properties
        final Properties props = new Properties();
        props.load(new FileInputStream("server.properties"));
        
        /*InputStream in = MediciIngester.class.getClassLoader().getResourceAsStream("server.properties");
        
        // load properties
        final Properties props = new Properties();
        props.load(in);*/

        // create beansession
        beansession = createBeanSession(props);

        // MimeMap
        MimeMap.initializeContext(beansession.getContext());
        mimemap = new MimeMap(beansession.getContext());

        // add special hook
        if (USE_DATABASE) {
            DatasourceBeanPreprocessor.setDatasourceBeanPreprocessor(beansession);
        }

        // creator
        creator(props);
        System.out.println(String.format("Creator    : %s <%s> [%s]", creator.getName(), creator.getEmail(), creator.getUri()));

        // extractor
        server = props.getProperty("extractor.url");
        // launch the job
        if ((server != null) && !server.endsWith("/")) { //$NON-NLS-1$
            server += "/"; //$NON-NLS-1$
        }
        server += "extractor/extract"; //$NON-NLS-1$
        stringContext = URLEncoder.encode(CETBeans.contextToNTriples(beansession.getContext()), "UTF-8"); //$NON-NLS-1$
        
        
        //This loop checks for the operation that needs to be performed based on the second argument 
        for(String arg: args){
            
            if(arg.equals("missing")){
                ingestOnlyMissing = true;
                break;
            }else if(arg.equals("delete")){                
                Scanner user_input = new Scanner( System.in );
                System.out.println("Are you sure you want to delete Collection and all its child? (Y/N)");
                String yesOrNo = user_input.next();
                if(yesOrNo!=null && yesOrNo.equalsIgnoreCase("Y")){
                    deleteCollectionAndChilds = true;
                    break;
                }else{
                    System.out.println("Exiting");
                    System.exit(0);
                }
            }else if(arg.equals("count")){
                countDatasetWithinCollection = true;
                break;
            }
            
        }

        // go through arguments
        // Based on the operation that needs to be performed respective methods are invoked
        if(ingestOnlyMissing){
            //Enters into this only when the missing collection or dataset needs to be ingested
            Unifier uf = new Unifier();
            uf.addPattern("c", Rdf.TYPE, Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/Collection"));
            uf.addPattern("c", FRBRCORE_ID, "t");
            uf.setColumnNames("c", "t");
            
            beansession.getContext().perform(uf);
            
            for (Tuple<Resource> row : uf.getResult()) {
                    if(ingestedCollections.get(row.get(1).getString())==null){
                        ArrayList<String> a = new ArrayList<String>();
                        a.add(row.get(0).getString());
                        ingestedCollections.put(row.get(1).getString(), a);
                    }else{
                        ArrayList<String> a = ingestedCollections.get(row.get(1).getString());
                        a.add(row.get(0).getString());
                        ingestedCollections.put(row.get(1).getString(), a);

                        System.out.println(row.get(1).getString());
                    }
            }
            System.out.println(ingestedCollections.size());
            
            for (String arg : args) {
                File file = new File(arg);
                if (file.isDirectory()) {
                    uploadMissingCollection(file, "", null);
                } else {
                    uploadFile(file, "");
                }
            }
            
        }else if(countDatasetWithinCollection){
            //Enters when the number of datasets is to be counted, argument should be the tagURI of the collection
            datasetCountWithinCollection(args[0]);
            
            System.out.println("Total number of datasets in given collection: "+dataSetCount);
            
        }else if(deleteCollectionAndChilds){
            
            //Enters when the collection and all its child needs to be deregistered (once the dataset is ingested it can't be deleted only deregistered) from medici
            //Argument should be tagURI of the Collection
            deleteDatasets(args[0]);
            
        } else {
                        
            //Enters when the collection is to be ingested, argument should be the absolute path to the collection
            for (String arg : args) {
                File file = new File(arg);
                if (file.isDirectory()) {
                    uploadCollection(file, "", null);
                } else {
                    uploadFile(file, "");
                }
            }
            
        }
    }

    private static int uploadCollection(File dir, String path, Resource parent) throws OperatorException, IOException {
        path += "/" + dir.getName();

        CollectionBean collection = new CollectionBean();
        collection.setCreationDate(new Date(dir.lastModified()));
        collection.setCreator(creator);
        collection.setLastModifiedDate(new Date(dir.lastModified()));
        collection.setTitle(path);
        beansession.save(collection);

        if (parent != null) {
            beansession.getContext().addTriple(parent, DcTerms.HAS_PART, Resource.uriRef(collection.getUri()));
        }

        System.out.println(String.format("Collection : %s ", collection.getTitle()));

        Collection<DatasetBean> beans = new HashSet<DatasetBean>();

        int numberOfFiles = 0;

        for (File file : dir.listFiles()) {
            if (file.getName().startsWith(".")) {
                continue;
            }
            if (file.isDirectory()) {
                uploadCollection(file, path, Resource.uriRef(collection.getUri()));
            } else {
                numberOfFiles += 1;
                beans.add(uploadFile(file, path));
            }
        }
        collection.setMemberCount(numberOfFiles);

        new CollectionBeanUtil(beansession).addBeansToCollection(collection, beans);
        beansession.getContext().addTriple(Resource.uriRef(collection.getUri()), DCTERMS_ID, path);

        log.info("Collection: " + path + " ---- Number of files:" + numberOfFiles);

        return numberOfFiles;
    }
    
    private static int uploadMissingCollection(File dir, String path, Resource parent) throws OperatorException, IOException {
        path += "/" + dir.getName();
        //String collectionPath = "/" + dir.getName();
        Resource collectionUri = null;

        CollectionBean collection = new CollectionBean();
        collection.setCreationDate(new Date(dir.lastModified()));
        collection.setCreator(creator);
        collection.setLastModifiedDate(new Date(dir.lastModified()));
        collection.setTitle(path);
        if(ingestedCollections.get(path)!=null) {
            ArrayList<String> a = ingestedCollections.get(path);
            //collectionUri = Resource.uriRef(ingestedCollections.get(path).get(0));
            if(parent!=null){
                boolean isPresent = false;
                for(String tagURI: a){
                    
                    TripleMatcher tf = new TripleMatcher();
                    tf.setSubject(parent);
                    tf.setPredicate(DcTerms.HAS_PART);
                    tf.setObject(Resource.uriRef(tagURI));
                    beansession.getContext().perform(tf);
                    if(tf.getResult()!=null && tf.getResult().size()!=0){
                        isPresent = true;
                        collectionUri = Resource.uriRef(tagURI);
                        //System.out.println("Already present");
                        break;
                    }
                }
                if(!isPresent){
                    beansession.save(collection);
                    if (parent != null) {
                        beansession.getContext().addTriple(parent, DcTerms.HAS_PART, Resource.uriRef(collection.getUri()));
                    }
                    System.out.println(String.format("Will be added Collection : %s ", collection.getTitle()));
                    System.out.println(collection.getUri());
                }
            }else{
                collectionUri = Resource.uriRef(ingestedCollections.get(path).get(0));
                System.out.println("Is it here");
            }
            
        }else{
            beansession.save(collection);
            if (parent != null) {
                beansession.getContext().addTriple(parent, DcTerms.HAS_PART, Resource.uriRef(collection.getUri()));
            }
            System.out.println(String.format("Will be added  23 Collection : %s ", collection.getTitle()));
            System.out.println(collection.getUri());
        }        

        System.out.println(String.format("Collection : %s ", collection.getTitle()));

        Collection<DatasetBean> beans = new HashSet<DatasetBean>();

        int numberOfFiles = 0;

        for (File file : dir.listFiles()) {
            if (file.getName().startsWith(".")) {
                continue;
            }
            if (file.isDirectory()) {
                if(collectionUri!=null)
                    uploadMissingCollection(file, path, collectionUri);
                else
                    uploadMissingCollection(file, path, Resource.uriRef(collection.getUri()));
                
                
            } else {
                if(collectionUri!=null){                
                    TripleMatcher tf = new TripleMatcher();                
                    tf.setPredicate(Dc.TITLE);
                    tf.setObject(Resource.literal(file.getName()));
                    beansession.getContext().perform(tf);
                    
                    if(tf.getResult()!=null && tf.getResult().size()!=0){
                        boolean isPresent = false;
                        for (Tuple<Resource> row : tf.getResult()) {                            
                                TripleMatcher tf1 = new TripleMatcher();
                                tf1.setSubject(collectionUri);
                                tf1.setPredicate(DcTerms.HAS_PART);
                                tf1.setObject(Resource.uriRef(row.get(0).getString()));
                                beansession.getContext().perform(tf1);
                                
                                if(tf1.getResult()!=null && tf1.getResult().size()!=0){
                                    isPresent = true;
                                    break;
                                }                              
                        }
                        if(!isPresent){
                            beans.add(uploadFile(file, path));
                        }  
                    }else{
                        beans.add(uploadFile(file, path));
                    }
                }else{
                    beans.add(uploadFile(file, path));
                }
            }
        }
        collection.setMemberCount(numberOfFiles);

        if(collectionUri==null) {            
            new CollectionBeanUtil(beansession).addBeansToCollection(collection, beans);
            beansession.getContext().addTriple(Resource.uriRef(collection.getUri()), FRBRCORE_ID, path);
            log.info("Collection: " + path + " ---- Number of files:" + numberOfFiles);
        }else{
            collection.setUri(collectionUri.getString());
            new CollectionBeanUtil(beansession).addBeansToCollection(collection, beans);
            beansession.getContext().addTriple(Resource.uriRef(collection.getUri()), FRBRCORE_ID, path);
            log.info("Collection: " + path + " ---- Number of files:" + numberOfFiles);
        }

        return numberOfFiles;
    }
    private static DatasetBean uploadFile(File file, String path) throws OperatorException, IOException {
        final DatasetBean dataset = new DatasetBean();
        dataset.setCreator(creator);
        dataset.setDate(new Date(file.lastModified()));
        dataset.setFilename(file.getName());
        dataset.setLabel(file.getName());
        dataset.setMimeType(mimemap.getContentTypeFor(file.getName().toLowerCase())); //Converting to lowercase to fix mime type bug
        dataset.setSize(file.length());
        dataset.setTitle(file.getName());

        InputStream is = new FileInputStream(file) {
            long total = 0;

            public int read(byte[] b) throws IOException {
                int count = super.read(b);
                total += count;
                int perc = 0;
                if (dataset.getSize() != 0)
                    perc = (int) (total * 100 / dataset.getSize());
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
        beansession.getContext().addTriple(Resource.uriRef(dataset.getUri()), DCTERMS_ID, path);
        System.out.println(String.format("Dataset    : %s [%d, %s] %d%%", dataset.getTitle(), dataset.getSize(), dataset.getMimeType(), 100));

        if (CALL_EXTRACTOR) {
            try {
                callExtractor(dataset);
            } catch (Exception e) {
                e.printStackTrace();
                CALL_EXTRACTOR = false;
            }
        }
        return dataset;
    }    
    
    private static void datasetCountWithinCollection(String tagURI) throws OperatorException{
        TripleMatcher tf = new TripleMatcher();
        tf.setSubject(Resource.uriRef(tagURI));
        tf.setPredicate(DcTerms.HAS_PART);
        beansession.getContext().perform(tf);
        DatasetBean dataset = null;
        for (Tuple<Resource> row : tf.getResult()) {
            if(row.get(2).getString().contains("tag:cet.ncsa.uiuc.edu,2008:/bean/Dataset")){
                dataset = (DatasetBean)beansession.fetchBean(Resource.uriRef(row.get(2).getString()));
                if(dataset.getTitle().contains(".JPG") && dataset.getMimeType().contains("application/octet-stream")){
                    System.out.println(dataset.getMimeType());
                    dataset.setMimeType("image/jpeg");
                    beansession.save(dataset);
                    dataSetCount++;
                }else{
                    System.out.println(dataset.getMimeType());
                    System.out.println(dataset.getTitle());
                }
                dataset = null;
                if(dataSetCount%500==0){
                    System.gc();
                }
            }else{
                datasetCountWithinCollection(row.get(2).getString());
            }
        }
    }
    
    
    private static void deleteDatasets(String tagURI) throws OperatorException{
        TripleMatcher tf = new TripleMatcher();
        tf.setSubject(Resource.uriRef(tagURI));
        tf.setPredicate(DcTerms.HAS_PART);
        beansession.getContext().perform(tf);
        
        for (Tuple<Resource> row : tf.getResult()) {
                deleteDatasets(row.get(2).getString());                
                //System.out.println(row.get(0).getString()+"--->"+row.get(1).getString()+"--->"+row.get(2).getString());
        }
        
        SubjectRemover s = new SubjectRemover();
        s.setSubject(Resource.uriRef(tagURI));
        beansession.getContext().perform(s);
        beansession.delete(Resource.uriRef(tagURI));
        beansession.deregister(Resource.uriRef(tagURI));
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
