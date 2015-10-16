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

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;

import org.tupeloproject.kernel.BlobFetcher;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
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
 * -ignoredeleted - don't bother with items that are deleted
 * 
 * The rest of the argument list may the word All, or a list of identifiers for collections or datasets to check. 
 * (Checking a collection means recursively checking all datasets within it.)
 */

public class FixityCheck extends MediciToolBase {

    private static long    max                 = 9223372036854775807l;
    private static long    skip                = 0l;
    private static long    addCount            = 0l;
    private static long    processCount        = 0l;
    private static boolean metadataonly        = false;
    private static boolean deletedonly       = false;

    // Stats:
    private static long    totalMissingHash    = 0l;
    private static long    totalMultipleHash   = 0l;
    private static long    totalMissingLength  = 0l;
    private static long    totalMultipleLength = 0l;
    private static long    totalMissingFiles   = 0l;
    private static long    totalLengthMismatch = 0l;
    private static long    totalHashMismatch   = 0l;
    private static long    totalBadId          = 0l;

    public static void main(String[] args) throws Exception {

        init("fixitycheck-log-", false); // No beansession needed

        for (String arg : args) {
            println("Arg is : " + arg);
            if (arg.equalsIgnoreCase("-metadataonly")) {
                metadataonly = true;
                println("Metadata Only Mode");
            } else if (arg.equalsIgnoreCase("-deletedonly")) {
                deletedonly = true;
                println("Ignore Deleted Mode");
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
            if (!((arg.equalsIgnoreCase("-metadataonly")) || (arg.startsWith("-limit")) || (arg.startsWith("-skip")) || arg.equalsIgnoreCase("-deletedonly"))) {
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
        println("Starting Fixity Checks: ");
        println(datasets.size() + " Datasets to test");

        checkDatasets();
        println("Fixity Check Complete: Final Stats");
        println(datasets.size() + " Datasets tested");
        println("Total with Bad IDs: " + totalBadId);
        println("Total with Missing Length: " + totalMissingLength);
        println("Total with Multiple Lengths: " + totalMultipleLength);
        println("Total with Missing SHA1 Hash: " + totalMissingHash);
        println("Total with Multiple SHA1 Hashes: " + totalMultipleHash);
        println("Total with Missing Files: " + totalMissingFiles);
        println("Total with Mismatched Length: " + totalLengthMismatch);
        println("Total with Mismatched Hash: " + totalHashMismatch);
        flushLog();
    }

    private static HashSet<UriRef> datasets = new HashSet<UriRef>();

    private static void addToDatasetList(UriRef id) throws OperatorException, IOException {
        if (id == null) {
            Unifier uf = new Unifier();
            uf.addPattern("data", Rdf.TYPE, Cet.DATASET);
            uf.addColumnName("data");
            if (deletedonly == true) {
                uf.addPattern("data", DcTerms.IS_REPLACED_BY, "del", true);
                uf.addColumnName("del");
            }

            context.perform(uf);
            for (Tuple t : uf.getResult()) {
                if (!deletedonly || t.get(1) != null) {
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
        if (deletedonly == true) {
            uf.addPattern("data", DcTerms.IS_REPLACED_BY, "del", true);
            uf.addColumnName("del");
        }
        try {
            context.perform(uf);
            for (Tuple<Resource> data : uf.getResult()) {
                if (!deletedonly || data.get(1) != null) {
                    datasets.add((UriRef) data.get(0));
                }
            }
            Unifier uf2 = new Unifier();
            uf2.addPattern(col, Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE);
            uf2.addPattern(col, DcTerms.HAS_PART, "coll");
            uf2.addPattern("coll", Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE);
            uf2.addColumnName("coll");
            if (deletedonly == true) {
                uf2.addPattern("coll", DcTerms.IS_REPLACED_BY, "del", true);
                uf2.addColumnName("del");
            }
            context.perform(uf2);

            for (Tuple<Resource> coll : uf2.getResult()) {
                if (!deletedonly || coll.get(1) != null) {

                    addDatasetsForCollection((UriRef) coll.get(0));
                }
            }
        } catch (Exception e) {
            System.err.println("Error for " + col.toString() + ". " + e.getMessage());
        }

    }

    private final static UriRef hasFilesize = Resource.uriRef("tag:tupeloproject.org,2006:/2.0/files/length");
    private final static UriRef hasSHA1     = Resource.uriRef("http://sead-data.net/terms/hasSHA1Digest");

    private static void checkDatasets() {

        for (UriRef ds : datasets) {
            println("Processing: " + ds.toString());
            Unifier uf = new Unifier();
            uf.addPattern(ds, Dc.IDENTIFIER, "id");
            uf.addPattern(ds, Dc.TITLE, "title", true);
            uf.addPattern(ds, hasFilesize, "len", true);
            uf.addPattern(ds, hasSHA1, "sha1", true);
            uf.setColumnNames("id", "title", "len", "sha1");
            InputStream is = null;

            try {
                context.perform(uf);

                ArrayList<Long> lengths = new ArrayList<Long>();
                ArrayList<String> hashes = new ArrayList<String>();

                for (Tuple<Resource> t : uf.getResult()) {
                    if (t.get(0).toString().equals(ds.toString())) {
                        if (t.get(1) != null) {
                            println("Found: " + t.get(1).toString());
                        } else {
                            println("Found: <NO TITLE>");
                        }
                        if (t.get(2) != null) {
                            String size = t.get(2).toString();
                            long x = Long.parseLong(size);
                            if (x < -3) {
                                x += Math.pow(2, 32);
                            }
                            lengths.add(x);
                        }
                        if (t.get(3) != null) {
                            hashes.add(t.get(3).toString());
                        }
                    } else {
                        totalBadId++;
                        println("BAD DATASET: ID does not match identifier metadata");
                    }

                }
                if (lengths.size() != 1) {
                    if (lengths.isEmpty()) {
                        totalMissingLength++;
                        println("BAD METADATA: Missing file length.");
                    } else {
                        totalMultipleLength++;
                        println("BAD METADATA: Multiple file lengths.");
                    }
                } else {
                    println("Size: " + lengths.get(0));
                }
                if (hashes.size() != 1) {
                    if (hashes.isEmpty()) {
                        totalMissingHash++;
                        println("BAD METADATA: Missing sha1 hash.");
                    } else {
                        totalMultipleHash++;
                        println("BAD METADATA: Multiple sha1 hashes.");
                    }
                } else {
                    println("SHA1: " + hashes.get(0));
                }
                // Now check actual dataset bytes:
                if (!metadataonly) {
                    BlobFetcher bf = new BlobFetcher(ds);
                    context.perform(bf);
                    MessageDigest sha1 = null;
                    is = bf.getInputStream();

                    sha1 = MessageDigest.getInstance("SHA1");
                    is = new DigestInputStream(is, sha1);
                    byte b[] = new byte[9192];
                    long actLength = 0l;
                    while (is.available() != 0) {
                        int read = is.read(b);
                        if (read == -1) {
                            break;
                        } else {
                            actLength += read;
                        }
                    }
                    byte[] digest = sha1.digest();
                    String actualHash = asHex(digest);

                    if ((!lengths.isEmpty()) && !(lengths.contains(actLength))) {
                        totalLengthMismatch++;
                        println("CORRUPT FILE: length as stored is " + actLength);
                    }
                    if ((!hashes.isEmpty()) && !(hashes.contains(actualHash))) {
                        totalHashMismatch++;
                        println("CORRUPT FILE: hash as stored is " + actualHash);
                    }

                }
            } catch (NoSuchAlgorithmException e1) {
                println("Config error: No SHA1 algorithm!");
            } catch (Exception e) {
                if (e.getMessage().equals("file not found")) {
                    totalMissingFiles++;
                }
                println("Failed in test for " + ds.toString() + ", " + e.getMessage());
            } finally {

                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            println("______________\n\n");
        }
    }

    // Copied from edu.illinois.ncsa.mmdb.server.util.BeanFiller
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static String asHex(byte[] buf) {
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }

}
