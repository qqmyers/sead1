package org.sead.acr.tools.medici;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import org.tupeloproject.kernel.BlobWriter;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/*This app recursively uploads all files/subdirs from a given set of paths in a file system local to a Medici instance. 
 * (Based on a ~2012 version of edu.illinois.ncsa.medici.ingest.MediciIngester (Kooper, Malviya). (This app handles deleted 
 * collections and files, but does not have all of the options that later versions of MediciIngester has. It also implements 
 * a limit, which is useful give the known issue that beansession does not release old beans and saves slow down as more than 
 * 5-10K files are uploaded in one execution.)
 * 
 * Note: this ingester uses the http://purl.org/vocab/frbr/core#embodimentOf predicate to record the original relative path 
 * as metadata on the uploaded collections and files, and then uses this (and hasPart relationships to parents) to identify 
 * matching collections and files. NB: It will not find/match collections and files entered through the GUI unless their 
 * frbr metadata has been set manually 
 * 
 * Options include:
 * -listOnly - just print the directory structure read and compare with the found structure in Medici
 * -limit<long> - only upload limit datasets before halting (matches don't count so repeatedly calling the app 
 *                with -limitx will upload x additional datasets each time
 * -merge - do not create new collections. Since this app writes hasPart relationships for subcollections and datasets as it goes, 
 *          if a top-level collection is not found (the frbr path doesn't match), it would create a new one and then link all 
 *          subcollections to it. This option causes the app to skip any parts of the dir tree where a matching collection can't 
 *          be found.
 * (Todo) -requireFRBRPath -          
 *          
 *  Example:
 *  
 *                            java -cp mi.jar org.sead.acr.tools.FileCollectionIngester -listOnly -limit10 -merge subdir1 subdir2
 *                            
 *  The app requires a server.properties file in the current directory to discover the medici/tupelo storage context to use
 *  
 *  Results are written to the console and a timestamped ingest-log* file.
 *  
 *  @author myersjd@umich.edu
 */

public class FileCollectionIngester extends MediciToolBase {

    private static Resource FRBR_EO         = Resource.uriRef("http://purl.org/vocab/frbr/core#embodimentOf");
    private static Resource SHA1_DIGEST     = Resource.uriRef("http://sead-data.net/terms/hasSHA1Digest");

    private static long     max             = 9223372036854775807l;
    private static boolean  merge           = false;

    private static long     globalFileCount = 0l;

    public static void main(String[] args) throws Exception {

        init("ingest-log-", true);
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

        // go through arguments
        for (String arg : args) {
            if (!((arg.equalsIgnoreCase("-listonly")) || (arg.equals("-merge")) || (arg.startsWith("-limit")))) {

                File file = new File(arg);
                if (file.isDirectory()) {
                    uploadCollection(file, "", null);
                } else {

                    if (globalFileCount < max) {
                        uploadFile(file, "");
                    }
                }
            }
        }
        flushLog();
    }

    private static int uploadCollection(File dir, String path, Resource parent) throws OperatorException, IOException {
        path += "/" + dir.getName();

        // Fixme - could check title and parent hasPart relationships only
        // (instead of relying on frbr metadata, e.g. requiring
        // that the top-level dir ingested matches (by dc:title) a top level
        // collection (no parent) in Medici
        // (or specifying the ID of the starting collection in Medici to match
        // against)
        Unifier uf = new Unifier();
        uf.addPattern("coll", Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE);
        uf.addPattern("coll", Resource.uriRef("http://purl.org/vocab/frbr/core#embodimentOf"), Resource.literal(path));
        uf.addPattern("coll", Resource.uriRef("http://purl.org/dc/terms/isReplacedBy"), "_ued", true);
        if (parent != null) {
            uf.addPattern(parent, Resource.uriRef("http://purl.org/dc/terms/hasPart"), "coll");
        }
        uf.setColumnNames("coll", "_ued");
        context.perform(uf);
        Resource id = null;
        for (Tuple<Resource> row : uf.getResult()) {
            if (row.get(1) == null) {
                id = row.get(0);
            }
        }

        CollectionBean collection = null;
        if (id != null) {
            collection = new CollectionBeanUtil(beansession).get(id, false);
            println(String.format("Found Collection : %s ", collection.getTitle()) + " for " + dir.getName());

        } else if (merge == false) {
            collection = new CollectionBean();

            collection.setCreationDate(new Date(dir.lastModified()));
            collection.setCreator(creator);
            collection.setTitle(dir.getName());
            println(String.format("Created Collection : %s ", collection.getTitle()));

        } else {
            println("No match for collection (Skipping): " + dir.getName());
        }

        int numberOfFiles = 0;

        if (collection != null) {
            if (!listonly) {
                collection.setLastModifiedDate(new Date(dir.lastModified()));
                beansession.save(collection);

                if (parent != null) {
                    beansession.getContext().addTriple(parent, DcTerms.HAS_PART, Resource.uriRef(collection.getUri()));
                }
            }
            Collection<DatasetBean> beans = new HashSet<DatasetBean>();

            for (File file : dir.listFiles()) {
                if (file.getName().startsWith(".")) {
                    continue;
                }
                if (file.isDirectory()) {
                    uploadCollection(file, path, Resource.uriRef(collection.getUri()));
                } else {

                    if (globalFileCount < max) {
                        numberOfFiles += 1;
                        // fileStats[1] += file.length();
                        DatasetBean db = uploadFile(file, path);
                        if (db != null) {
                            beans.add(db);
                        }
                    }
                }
            }
            if (!listonly) {
                collection.setMemberCount(numberOfFiles);

                new CollectionBeanUtil(beansession).addBeansToCollection(collection, beans);
                beansession.getContext().addTriple(Resource.uriRef(collection.getUri()), FRBR_EO, path);
            }
            println("Collection: " + path + " ---- Number of files:" + numberOfFiles);
        }
        return numberOfFiles;
    }

    private static DatasetBean uploadFile(File file, String path) throws OperatorException, IOException {

        Unifier uf = new Unifier();
        uf.addPattern("db", Rdf.TYPE, new DatasetBeanUtil(beansession).getType());
        uf.addPattern("db", Resource.uriRef("http://purl.org/vocab/frbr/core#embodimentOf"), Resource.literal(path));
        uf.addPattern("db", Resource.uriRef("http://purl.org/dc/elements/1.1/title"), Resource.literal(file.getName()));
        uf.addPattern("db", Resource.uriRef("http://purl.org/dc/terms/isReplacedBy"), "_ued", true);
        uf.setColumnNames("db", "_ued");
        context.perform(uf);
        Resource id = null;
        for (Tuple<Resource> row : uf.getResult()) {
            if (row.get(1) == null) {
                id = row.get(0);
            }
        }

        if (id != null) {
            println("Skipping <" + id + ">: " + path + "/" + file.getName());
            return null;
        }
        globalFileCount++;
        if (!listonly) {
            final DatasetBean dataset = new DatasetBean();
            dataset.setCreator(creator);
            dataset.setDate(new Date(file.lastModified()));
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
                    int perc = 0;
                    if (dataset.getSize() != 0)
                        perc = (int) (total * 100 / dataset.getSize());
                    System.out.print(String.format("Dataset    : %s [%d, %s] %d%%\r", dataset.getTitle(), dataset.getSize(), dataset.getMimeType(), perc));
                    return count;
                };
            };
            byte[] digest = null;
            try {
                MessageDigest sha1 = MessageDigest.getInstance("SHA1");
                DigestInputStream dis = new DigestInputStream(is, sha1);

                BlobWriter bw = new BlobWriter();
                bw.setInputStream(dis);
                bw.setSubject(Resource.uriRef(dataset.getUri()));
                beansession.getContext().perform(bw);
                dis.close();
                is.close();
                digest = sha1.digest();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            beansession.save(dataset);

            beansession.getContext().addTriple(Resource.uriRef(dataset.getUri()), FRBR_EO, path + "/" + file.getName());
            if (digest != null) {
                beansession.getContext().addTriple(Resource.uriRef(dataset.getUri()), SHA1_DIGEST, digest);
            }
            println(String.format("Dataset    : %s [%d, %s] %d%%", dataset.getTitle(), dataset.getSize(), dataset.getMimeType(), 100));

            return dataset;
        } else {
            println(String.format("Dataset    : %s [%d, %s] %d%%", file.getName(), file.length(), mimemap.getContentTypeFor(file.getName()), 100));

            return null;
        }
    }

}
