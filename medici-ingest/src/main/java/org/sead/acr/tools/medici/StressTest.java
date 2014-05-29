package org.sead.acr.tools.medici;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.query.OrderBy;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;


import edu.illinois.ncsa.cet.search.impl.LuceneTextIndex;
import edu.illinois.ncsa.mmdb.web.server.search.SearchableThingIdGetter;
import edu.illinois.ncsa.mmdb.web.server.search.SearchableThingTextExtractor;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

/*Various stress tests
 *  
 *  @author myersjd@umich.edu
 */

public class StressTest extends MediciToolBase {

    private static Resource FRBR_EO         = Resource.uriRef("http://purl.org/vocab/frbr/core#embodimentOf");
    private static Resource SHA1_DIGEST     = Resource.uriRef("http://sead-data.net/terms/hasSHA1Digest");

    private static long     max             = 9223372036854775807l;
    private static boolean  merge           = false;

    private static long     globalFileCount = 0l;

    private static BeanSession bs = null;

    protected static LuceneTextIndex<String> search = null;
    
    public static void main(String[] args) throws Exception {

        init("stress-log-", true);
        for (String arg : args) {
            println("Arg is : " + arg);
            if (arg.equalsIgnoreCase("-listonly")) {
                listonly = true;
                println("List Only Mode");
            } else if (arg.equals("-merge")) {
                merge = true;
                println("Merge mode ON");

            } else if (arg.startsWith("-limit")) {
                max = Long.parseLong(arg.substring(6));
                println("Max ingest file count: " + max);
            }
        }
        
      /*  
        File folder = new File("\\tmp\\lucene");
        folder.mkdirs();

        println("Lucene search index directory = " + folder.getAbsolutePath());
        Directory dir = FSDirectory.open(folder);
        IndexWriter.unlock(dir);
        search = new LuceneTextIndex<String>(dir);
        search.setTextExtractor(new SearchableThingTextExtractor());
        search.setIdGetter(new SearchableThingIdGetter());
        int j = 0;
        while (true) {
            println("Index Queueing");
        indexFullTextAll();
        println("Indexing");

        consumeFullTextIndexQueue();
       
       // expireBeans();
        println("Completed ITERATION: " + j++);
        
        }
      */  
     /*   
        int i=0;
        while (true) {
            i+=findBeans();
            println("Total: " + i);
            flushLog();

        }
       */ 
        
    }

    static Object fetchBean(String uri) throws OperatorException, ClassNotFoundException {
        //lots of beansessions!!!
        if(bs == null) {
            bs = createBeanSession();
        }
        try {
            return bs.fetchBean(Resource.uriRef(uri));
        } finally {
            bs.close();
            bs=null;
        }
    }

    private static int findBeans() throws OperatorException, IOException, ClassNotFoundException {
        Unifier uf = new Unifier();

        uf.addPattern("db", Rdf.TYPE, new DatasetBeanUtil(beansession).getType());
        uf.setColumnNames("db");
        context.perform(uf);
        Resource id = null;
       int i=0;
       Table<Resource> t = (Table<Resource>) unifyExcludeDeleted(uf, "db");
        for (Tuple<Resource> row : t ) {
            fetchBean(row.get(0).getString());
            println("Fetched: " + row.get(0).getString());
            i++;
        }
        return i;
    }

    static List<String> indexQueue   = new LinkedList<String>();
    static List<String> deindexQueue = new LinkedList<String>();

    /**
     * Queue a dataset for full-text (re)indexing
     * 
     * @param datasetUri
     */
    public static void indexFullText(String datasetUri) {
        synchronized (indexQueue) {
            synchronized (deindexQueue) {
                if (!indexQueue.contains(datasetUri)) {
                    indexQueue.add(0, datasetUri);
                }
                if (deindexQueue.contains(datasetUri)) {
                    deindexQueue.remove(datasetUri);
                }
            }
        }
    }

    /**
     * Queue a dataset for full-text deindexing
     * 
     * @param datasetUri
     */
    public static void deindexFullText(String datasetUri) {
        synchronized (deindexQueue) {
            synchronized (indexQueue) {
                if (!deindexQueue.contains(datasetUri)) {
                    deindexQueue.add(0, datasetUri);
                }
                if (indexQueue.contains(datasetUri)) {
                    indexQueue.remove(datasetUri);
                }
            }
        }
    }

    public static int indexFullTextAll() {
        int i = 0;
        int batchSize = 100;
        while (true) {
            Unifier u = new Unifier();
            u.setColumnNames("d", "replaced", "date");
            u.addPattern("d", Rdf.TYPE, Cet.DATASET);
            u.addPattern("d", Dc.DATE, "date");
            u.addPattern("d", DcTerms.IS_REPLACED_BY, "replaced", true);
            u.setLimit(batchSize);
            u.setOffset(i);
            u.addOrderByDesc("date");
            try {
                context.perform(u);
                int n = 0;
                for (Tuple<Resource> row : u.getResult() ) {
                    n++;
                    i++;
                    String d = row.get(0).getString();
                    Resource r = row.get(1);
                    if (Rdf.NIL.equals(r)) { // deleted
                        deindexFullText(d);
                    } else {
                        indexFullText(d);
                    }
                }
                if (n < batchSize) {
                    println("queued " + i + " datasets for full-text reindexing @ " + new Date());
                    break;
                }
            } catch (OperatorException x) {
                x.printStackTrace();
                // FIXME deal with busy state
            }
        }
        int j = 0;
        while (true) {
            Unifier u = new Unifier();
            u.setColumnNames("d", "replaced", "date");
            u.addPattern("d", Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE);
            u.addPattern("d", DcTerms.DATE_CREATED, "date");
            u.addPattern("d", DcTerms.IS_REPLACED_BY, "replaced", true);
            u.setLimit(batchSize);
            u.setOffset(j);
            u.addOrderByDesc("date");
            try {
                context.perform(u);
                int n = 0;
                for (Tuple<Resource> row : u.getResult() ) {
                    n++;
                    j++;
                    String d = row.get(0).getString();
                    Resource r = row.get(1);
                    if (Rdf.NIL.equals(r)) { // deleted
                        deindexFullText(d);
                    } else {
                        indexFullText(d);
                    }
                }
                if (n < batchSize) {
                    println("queued " + j + " collections for full-text reindexing @ " + new Date());
                    break;
                }
            } catch (OperatorException x) {
                x.printStackTrace();
                // FIXME deal with busy state
            }
        }
        return i + j;
    }

    // consume ft index queue
    public synchronized static void consumeFullTextIndexQueue() {
        boolean logged = false;
        if (search != null) {
            // copy the queues, so we don't block
            List<String> toDeindex = new LinkedList<String>();
            List<String> toIndex = new LinkedList<String>();
            List<String> moreToDeindex = new LinkedList<String>();

            synchronized (deindexQueue) {
                synchronized (indexQueue) {
                    toIndex.addAll(indexQueue);
                    toDeindex.addAll(deindexQueue);
                    toDeindex.addAll(indexQueue);
                    indexQueue.clear();
                    deindexQueue.clear();
                }
            }
            if (toDeindex.size() > 0) {
                if (!logged) {
                    println("starting full-text reindexing @ " + new Date());
                    logged = true;
                }
                println("deindexing " + toDeindex.size() + " deleted dataset(s) @ " + new Date());
                for (String datasetUri : toDeindex ) {
                    Unifier uf = new Unifier();
                    uf.addPattern(Resource.uriRef(datasetUri), MMDB.METADATA_HASSECTION, "section");
                    uf.setColumnNames("section");
                    try {
                        context.perform(uf);
                        Set<String> sections = new HashSet<String>();
                        for (Tuple<Resource> row : uf.getResult() ) {
                            sections.add(row.get(0).getString());
                        }
                        moreToDeindex.addAll(sections);
                        //getSearch().deindex(sections);
                    } catch (OperatorException e) {
                        println("Could not find/remove sections." + e.getMessage());
                    }
                }
                toDeindex.addAll(moreToDeindex);
                search.deindex(toDeindex);
                println("deindexed " + toDeindex.size() + " deleted dataset(s) @ " + new Date());
            }
            if (toIndex.size() > 0) {
                if (!logged) {
                    println("starting full-text reindexing @ " + new Date());
                    logged = true;
                }
                long then = System.currentTimeMillis();
                println("indexing " + toIndex.size() + " dataset(s) @ " + new Date());
                /*                for (String datasetUri : toIndex ) {
                                    Unifier uf = new Unifier();
                                    uf.addPattern(Resource.uriRef(datasetUri), MMDB.METADATA_HASSECTION, "section");
                                    uf.setColumnNames("section");
                                    try {
                                        getContext().perform(uf);
                                        Set<String> sections = new HashSet<String>();
                                        for (Tuple<Resource> row : uf.getResult() ) {
                                            sections.add(row.get(0).getString());
                                        }
                                        getSearch().deindex(sections);
                                    } catch (OperatorException e) {
                                        log.warn("Could not find/remove sections.", e);
                                    }
                    */
                search.indexAll(toIndex);
                //           }
                long elapsed = System.currentTimeMillis() - then;
                double minutes = elapsed / 60000.0;
                println("indexed " + toIndex.size() + " dataset(s) in " + minutes + " minutes");
            }
        }
        ((LuceneTextIndex<String>) search).refreshIndexSearcher();
    }

    /**
     * Perform a unifier, but exclude anything that is marked as deleted
     * (dcterms:isReplacedBy rdf:nil).
     * 
     * @param u
     *            the unifier
     * @param subjectVar
     *            which variable will be bound to the possibly-deleted item
     * @return the results, with deleted items excluded
     * @throws OperatorException
     */
    public static Table<Resource> unifyExcludeDeleted(Unifier u, String subjectVar) throws OperatorException {
        List<String> newColumnNames = new LinkedList<String>(u.getColumnNames());
        newColumnNames.add("_ued");
        u.setColumnNames(newColumnNames);
        /*
        LinkedList<org.tupeloproject.rdf.query.Pattern> pats = u.getPatterns();
        for (org.tupeloproject.rdf.query.Pattern pat : pats ) {
            log.debug(pat.toString());
        }
        */
        u.addPattern(subjectVar, Resource.uriRef("http://purl.org/dc/terms/isReplacedBy"), "_ued", true);
        if (u.getOffset() != 0 || u.getLimit() != Unifier.UNLIMITED) {
            //Ordering in all cases would simplify logic in FilteredIterator and avoid having to retrieve deleted items (after the first one)
            // - is it worth the cost?
            List<OrderBy> newOrderBy = new LinkedList<OrderBy>();
            OrderBy ued = new OrderBy();
            ued.setAscending(true); // FIXME should be false when SQL contexts order correctly, i.e., when TUP-481 is fixed
            ued.setName("_ued");
            newOrderBy.add(ued);
            for (OrderBy ob : u.getOrderBy() ) {
                newOrderBy.add(ob);
            }
            u.setOrderBy(newOrderBy);
        }
        context.perform(u);
        int cix = u.getColumnNames().size() - 1;

        return new FilteredTable<Resource>(u.getResult(), cix);
    }

}
