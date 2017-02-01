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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.tupeloproject.rdf.terms.Rdfs;
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

    private static long        processCount       = 0l;
    static private Set<UriRef> terms              = new HashSet<UriRef>();
    static private Set<UriRef> relationships      = new HashSet<UriRef>();

    // Stats:
    private static long        totalMetadata      = 0l;
    private static long        totalRelationships = 0l;
    private static long        totalTags          = 0l;

    private static PrintWriter csvPw              = null;

    public static void main(String[] args) throws Exception {

        init("metadata2-log-", false); // No beansession needed

        File csvFile = new File("metadata2-log-" + System.currentTimeMillis() + ".csv");
        try {
            csvPw = new PrintWriter(new FileWriter(csvFile));
        } catch (Exception e) {
            println(e.getMessage());
        }
        println("Starting Metadata Analysis: ");
        getGoodList();
        checkTerms();
        checkRelationships();
        checkTags();
        processCount = 0l;
        println("Metadata Analysis Complete: Final Stats");
        println("Total metadata entries: " + totalMetadata);
        println("Total relationship entries: " + totalRelationships);
        println("Total tag entries: " + totalTags);
        flushLog();
        csvPw.flush();
        csvPw.close();
    }

    private static void checkRelationships() {

        Unifier uf = new Unifier();
        uf.addPattern("rel", Rdf.TYPE, Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/mmdb/Relationship"));
        uf.addColumnName("rel");

        try {
            context.perform(uf);
            println("Relationships:");
            for (Tuple t : uf.getResult()) {
                println(((Resource) t.get(0)).toString());
                relationships.add((UriRef) t.get(0));
            }
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        println("\n\n");

        for (UriRef t : relationships) {
            long relCount = getTriples(t);
            println("Found " + relCount + " entries");
            totalRelationships += relCount;
        }
    }

    private static long getTriples(UriRef t) {

        println("Retrieving: " + t.toString());
        Unifier uf2 = new Unifier();
        uf2.addPattern("sub", t, "val");
        uf2.addColumnName("sub");
        uf2.addColumnName("val");
        long entryCount = 0l;
        try {
            context.perform(uf2);

            for (Tuple<Resource> entry : uf2.getResult()) {
                UriRef subject = (UriRef) entry.get(0);
                if (datasets.contains(subject)) {
                    printRow(subject.toString(), t.toString(), ((Resource) entry.get(1)).toString());
                    entryCount++;
                }
            }
        } catch (OperatorException e) {
            println("Failed to find entries for: " + t.toString());
            e.printStackTrace();
        }
        return entryCount;
    }

    private static void checkTerms() {

        Unifier uf = new Unifier();
        uf.addPattern("term", Rdf.TYPE, Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/userMetadataField"));
        uf.addColumnName("term");

        try {
            context.perform(uf);
            println("Terms:");
            for (Tuple<Resource> t : uf.getResult()) {
                if (!((Resource) t.get(0)).equals(Resource.uriRef("http://purl.org/dc/terms/hasPart"))) {
                    println(((Resource) t.get(0)).toString());
                    terms.add((UriRef) t.get(0));
                }
            }
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        println("\n\n");

        for (UriRef t : terms) {
            long termCount = getTriples(t);
            println("Found " + termCount + " entries");
            totalMetadata += termCount;
        }
    }

    private static void checkTags() {

        Unifier uf = new Unifier();
        uf.addPattern("item", Resource.uriRef("http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag"), "tag");
        uf.addPattern("tag", Resource.uriRef("http://www.holygoat.co.uk/owl/redwood/0.1/tags/name"), "name");
        uf.setColumnNames("item", "name");

        Map<String, Integer> tagCounts = new HashMap<String, Integer>();
        try {
            context.perform(uf);
            println("Tags:");
            for (Tuple<Resource> t : uf.getResult()) {
                println(t.get(0).toString() + ":" + t.get(1).toString());
                UriRef subject = (UriRef)t.get(0);
                if (datasets.contains(subject)) {
                    String tag = t.get(1).toString();
                    printRow(subject.toString(), "http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag", tag);
                    if (tagCounts.containsKey(tag)) {
                        tagCounts.put(tag, tagCounts.get(tag) + 1);
                    } else {
                        tagCounts.put(tag, 1);
                    }
                }
            }

        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        println("\nTag Summary");
        tagCounts = sortByValue(tagCounts);
        for (String tag : tagCounts.keySet()) {
            long count = tagCounts.get(tag);
            println("Tag: " + tag + " : " + count + " entries");
            totalTags += count;
        }
    }

    private static HashSet<UriRef> datasets = new HashSet<UriRef>();

    private static void getGoodList() throws OperatorException, IOException {

        Unifier uf = new Unifier();
        uf.addPattern("data", Rdf.TYPE, Cet.DATASET);
        uf.addPattern("data", DcTerms.IS_REPLACED_BY, "del", true);
        uf.addColumnName("data");
        uf.addColumnName("del");

        context.perform(uf);
        for (Tuple<Resource> t : uf.getResult()) {
            if (t.get(1) == null) {
                datasets.add((UriRef) t.get(0));
                processCount++;
            }
        }
        println("Found " + processCount + " datasets");

        Unifier uf2 = new Unifier();
        uf2.addPattern("data", Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE);
        uf2.addPattern("data", DcTerms.IS_REPLACED_BY, "del", true);
        uf2.addColumnName("data");
        uf2.addColumnName("del");

        long colCount = 0l;
        context.perform(uf2);
        for (Tuple<Resource> t : uf2.getResult()) {
            if (t.get(1) == null) {
                datasets.add((UriRef) t.get(0));
                colCount++;
            }
        }
        println("Found " + colCount + " collections.");
        processCount += colCount;
    }

    static long count      = 0l;

    static int  printCount = 0;

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
            s = s.replace("\"", "\"\"");
            s = "\"" + s + "\"";
        }
        return s;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
