/*
 *
 * Copyright 2015 University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 * @author myersjd@umich.edu
 */

package org.sead.acr.tools.medici;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.index.Terms;
import org.tupeloproject.kernel.BlobFetcher;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleFetcher;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleReader;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.kernel.events.TriplesWrittenEvent;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

/*Verifies that metadata related to Dataset content and the content itself match. Currently this means:
 * The filesize metadata matches the number of byes
 * THe sha1 hash value recorded as metadata matches the hash calculated from the bytes
 * There is 1 and only 1 file size and 1 and only 1 sha1 hash value.
 * 
 * This class simply documents the issue(s) and make no changes, but it does record a list of metadata 
 * changes necessary to a) update the metadata, or b) delete the dataset that can be run through MetadataRepair 
 * 
 * Options
 * -metadataonly - just verify the existience of 1 and only 1 size and sha1, don't check blobs
 * -limit<N> - # of Datasets to process
 * -skip<N> - # to skip before starting
 * -deletedonly - just check items that are deleted
 * 
 * The rest of the argument list may the word All, or a list of identifiers for collections or datasets to check. 
 * (Checking a collection means recursively checking all datasets within it.)
 */

public class MetadataAnalysis extends MediciToolBase {

    private static long        max                = 9223372036854775807l;
    private static long        skip               = 0l;
    private static long        addCount           = 0l;
    private static long        processCount       = 0l;
    private static boolean     collections        = false;

    // Stats:
    private static long        totalMetadata      = 0l;
    private static long        totalRelationships = 0l;

    private static PrintWriter csvPw              = null;

    public static void main(String[] args) throws Exception {

        init("metadata-log-", false); // No beansession needed

        File csvFile = new File("metadata-log-" + System.currentTimeMillis() + ".csv");
        try {
            csvPw = new PrintWriter(new FileWriter(csvFile));
        } catch (Exception e) {
            println(e.getMessage());
        }

        for (String arg : args) {
            println("Arg is : " + arg);
            if (arg.equalsIgnoreCase("-collections")) {
                collections = true;
                println("Metadata Only Mode");
            } else if (arg.startsWith("-limit")) {
                max = Long.parseLong(arg.substring(6));
                println("Max dataset count: " + max);
            } else if (arg.startsWith("-skip")) {
                skip = Long.parseLong(arg.substring(5));
                println("Skip dataset count: " + skip);
            }
        }

        // go through arguments
        for (String arg : args) {
            if (!((arg.equalsIgnoreCase("-collections")) || (arg.startsWith("-limit")) || (arg.startsWith("-skip")) || arg.equalsIgnoreCase("-deletedonly") || arg
                    .equalsIgnoreCase("-addmissinghashes"))) {
                if (arg.equalsIgnoreCase("All")) {
                    addToDatasetList(null);
                } else {
                    try {
                        addToDatasetList(Resource.uriRef(arg));

                    } catch (Exception e) {
                        println("Unable to retrieve datasets for argument: " + arg + ", Error is " + e.getMessage());
                    }
                }
            }
        }
        println("Starting Metadata Analysis: ");
        println(datasets.size() + " Datasets to test");
        loadTerms();
        loadRelationships();
        processCount = 0l;
        checkDatasets();
        println("Metadata Analysis Complete: Final Stats");
        println(datasets.size() + " Datasets tested");
        println("Total metadata entries: " + totalMetadata);
        println("Total relationship entries: " + totalRelationships);
        flushLog();
        csvPw.flush();
        csvPw.close();
    }

    private static void loadRelationships() {

        Unifier uf = new Unifier();
        uf.addPattern("rel", Rdf.TYPE, Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/mmdb/Relationship"));
        uf.addColumnName("rel");

        try {
            context.perform(uf);
            println("Relationships:");
            for (Tuple t : uf.getResult()) {
                println(((Resource) t.get(0)).toString());
                relationships.add((Resource) t.get(0));
            }
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        println("\n\n");
    }

    private static void loadTerms() {

        Unifier uf = new Unifier();
        uf.addPattern("term", Rdf.TYPE, Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/userMetadataField"));
        uf.addColumnName("term");

        try {
            context.perform(uf);
            println("Terms:");
            for (Tuple t : uf.getResult()) {
                if (!((Resource) t.get(0)).equals(Resource.uriRef("http://purl.org/dc/terms/hasPart"))) {
                    println(((Resource) t.get(0)).toString());
                    terms.add((Resource) t.get(0));
                }
            }
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        println("\n\n");
    }

    private static HashSet<UriRef> datasets = new HashSet<UriRef>();

    private static void addToDatasetList(UriRef id) throws OperatorException, IOException {
        if (id == null) {
            Unifier uf = new Unifier();
            if (collections) {
                uf.addPattern("data", Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE);
            } else {
                uf.addPattern("data", Rdf.TYPE, Cet.DATASET);
            }
            uf.addColumnName("data");
            uf.addPattern("data", DcTerms.IS_REPLACED_BY, "del", true);
            uf.addColumnName("del");

            context.perform(uf);
            for (Tuple t : uf.getResult()) {

                if (t.get(1) == null) {
                    processCount++;

                    if (processCount > skip) {
                        addCount++;
                        if (addCount <= max) {
                            datasets.add((UriRef) t.get(0));
                        }
                    }
                }
            }

        } else {
            // Decide if it's a dataset or collections
            TripleMatcher tMatcher = new TripleMatcher();
            tMatcher.match(id, Rdf.TYPE, Cet.DATASET);
            context.perform(tMatcher);
            if (tMatcher.getResult().size() != 0) {
                processCount++;
                if (processCount > skip) {
                    addCount++;
                    if (addCount <= max) {
                        // Note - ignoring ignoredelete flag if you specifically
                        // ask for this dataset
                        datasets.add(id);
                    }
                }
            } else {
                // Recursively get Dataset children of Collection
                addDatasetsForCollection(id);
            }

        }
    }

    private static void addDatasetsForCollection(UriRef col) {
        Unifier uf = new Unifier();
        uf.addPattern(col, Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE);
        uf.addPattern(col, DcTerms.HAS_PART, "data");
        uf.addPattern("data", Rdf.TYPE, Cet.DATASET);

        uf.addColumnName("data");
        uf.addPattern("data", DcTerms.IS_REPLACED_BY, "del", true);
        uf.addColumnName("del");

        try {
            context.perform(uf);
            for (Tuple<Resource> data : uf.getResult()) {
                if (data.get(1) == null) {
                    processCount++;

                    if (processCount > skip) {
                        addCount++;
                        if (addCount <= max) {
                            datasets.add((UriRef) data.get(0));
                        }
                    }

                }
            }
            Unifier uf2 = new Unifier();
            uf2.addPattern(col, Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE);
            uf2.addPattern(col, DcTerms.HAS_PART, "coll");
            uf2.addPattern("coll", Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE);
            uf2.addColumnName("coll");
            uf2.addPattern("coll", DcTerms.IS_REPLACED_BY, "del", true);
            uf2.addColumnName("del");
            context.perform(uf2);

            for (Tuple<Resource> coll : uf2.getResult()) {
                if (coll.get(1) == null) {

                    addDatasetsForCollection((UriRef) coll.get(0));
                }
            }
        } catch (Exception e) {
            System.err.println("Error for " + col.toString() + ". " + e.getMessage());
        }

    }

    private final static UriRef  hasFilesize   = Resource.uriRef("tag:tupeloproject.org,2006:/2.0/files/length");
    private final static UriRef  hasSHA1       = Resource.uriRef("http://sead-data.net/terms/hasSHA1Digest");

    static private Set<Resource> terms         = new HashSet<Resource>();
    static private Set<Resource> relationships = new HashSet<Resource>();

    static long                  count         = 0l;

    private static void checkDatasets() {

        for (UriRef ds : datasets) {
            // println("Processing: " + ds.toString());
            processCount++;
            count++;

            TripleFetcher tf = new TripleFetcher();
            tf.setSubject(ds);
            try {
                context.perform(tf);

                for (Triple t : tf.getResult()) {
                    if (terms.contains(t.getPredicate())) {
                        printRow(t.getSubject().toString(), t.getPredicate().toString(), t.getObject().toString());
                        totalMetadata++;
                    }
                    if (relationships.contains(t.getPredicate())) {
                        printRow(t.getSubject().toString(), t.getPredicate().toString(), t.getObject().toString());
                        totalRelationships++;
                    }

                }
            } catch (OperatorException e) {
                println("Could not process " + ds.toString() + ": " + e.getMessage());
            }

            if (count == 100) {
                count = 0;
                println("\nProcessed Count: " + processCount + "\n");
            }
        }
    }

    static int printCount = 0;

    private static void printRow(String s, String p, String o) {
        s = csvEscapeString(s);
        p = csvEscapeString(p);
        o = csvEscapeString(o);

        if (csvPw != null) {
            if (o.length() != 0) {

                csvPw.println(s + "," + p + "," + o);
            } else {
                println("Zero length value for :" + s + " : " + p);
            }
        }
        printCount++;
        if (printCount == 100) {
            printCount = 0;
            csvPw.flush();
        }
        return;

    }

    private static String csvEscapeString(String s) {
        if (s.contains(",") || s.contains("\n") || s.contains("\"")) {
            s.replace("\"", "\"\"");
            s = "\"" + s + "\"";
        }
        return s;
    }
}
