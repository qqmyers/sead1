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
 * Recursive decent through all UriRefs to show the full tree of metadata/blobs associated with a  dataset. 
 * Includes links where the dataset is the top-level subject or the object as well as reified info, 
 * i.e. statements about metadata that are relationships.
 * Decent truncated at references to other datasets/collections/tags/People/Relationships/etc that may be used elsewhere.
 * 
 *  This is ~ the Concise Bounded Description of the Dataset 
 *  
 *  -verbose : show blob/triple level info
 *  -dodelete : actually remove blobs/triples rather than listing them (i.e. it's listonly by default)
 *  -processactivedata : process data that has not been marked as deleted
 *  -limit<x> : process a maximium of <x> datasets
 *  
 *  WARNIGN/NOTE: To avoid accidental removal of all data (-dodelete and =processactivedata), 
 *  this combination of flags only works with a specified list of datasets, not with an empty list
 *  (which means look for all datasets)
 * 
 * @author myersjd@umich.edu
 *                               
 */

package org.sead.acr.tools.medici;

import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

public class TrueDelete extends NodeProcessorBase {

    public static void main(String[] args) throws Exception {

        init("truedelete-log-", false); // No beansession needed

        for (String arg : args) {
            println("Arg is : " + arg);
            if (arg.equalsIgnoreCase("-verbose")) {
                verbose = true;
                println("Verbose Mode");
            } else if (arg.equalsIgnoreCase("-dodelete")) {
                doDelete = true;
                println("Delete Mode");
            } else if (arg.equalsIgnoreCase("-processactivedata")) {
                processActiveData = true;
                println("Active Data Mode");
            } else if (arg.startsWith("-limit")) {
                max = Long.parseLong(arg.substring(6));
                println("Max dataset count: " + max);
            }

        }

        boolean global = true;

        for (String arg : args) {
            if ((!(arg.equalsIgnoreCase("-verbose")) && (!arg.equalsIgnoreCase("-dodelete")) && (!arg.equalsIgnoreCase("-processactivedata")) && (!arg.startsWith("-limit")))) {
                global = false;

                try {
                    UriRef ds = Resource.uriRef(arg);
                    if (numberProcessed < max) {

                        if (process(ds) == true) {
                            numberProcessed++;
                        } else {
                            // Skip non-deleted entries in the list
                            println(ds.toString() + " not processed (not deleted?)");
                        }

                        println("Stats after " + numberProcessed + " datasets: " + totalBlobs + " blobs, " + totalBytes + " bytes, " + totalTriples + " triples");
                    }
                } catch (Exception e) {
                    println("Could not parse arg: " + arg);
                    e.printStackTrace();
                }
            }
        }

        if (global) {
            // List all deleted datasets and then process them
            Unifier uf = new Unifier();
            uf.addPattern("s", Rdf.TYPE, Cet.DATASET);
            //only do active data in list/not delete mode
            if (!processActiveData || doDelete) {
                uf.addPattern("s", DcTerms.IS_REPLACED_BY, "r");
            }
            uf.setColumnNames("s");
            context.perform(uf);
            for (Tuple<Resource> row : uf.getResult()) {
                if (numberProcessed < max) {

                    if (process((UriRef) row.get(0)) == true) {
                        numberProcessed++;
                    } else {
                        // Should not happen since we only list deleted items
                        // when global ==true
                        println("ERROR: " + ((UriRef) row.get(0)).toString() + " not processed (not deleted?)");
                    }

                    println("Stats after " + numberProcessed + " datasets: " + totalBlobs + " blobs, " + totalBytes + " bytes, " + totalTriples + " triples");
                }
            }
        }
        if (verbose) {
            println("Predicates Followed:");
            for (UriRef u : predicatesFollowed) {
                println("  " + u.toString());
            }
        }
        flushLog();
        System.exit(0);
    }
}
