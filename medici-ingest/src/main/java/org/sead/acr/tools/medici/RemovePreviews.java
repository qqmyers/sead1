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
 * Look for any entries of the following types that are not associated with a Dataset or Collection:
 * Should not happen in general...
 * 
 * edu.uiuc.ncsa.cet.bean.PreviewVideoBean
 * edu.uiuc.ncsa.cet.bean.PreviewImageBean
 * edu.uiuc.ncsa.cet.bean.gis.GeoPointBean
 * edu.uiuc.ncsa.cet.bean.AnnotationBean
 * edu.uiuc.ncsa.cet.bean.PreviewTabularDataBean
 * edu.uiuc.ncsa.cet.bean.PreviewMultiTabularDataBean
 * edu.uiuc.ncsa.cet.bean.PreviewPyramidBean
 * edu.uiuc.ncsa.cet.bean.PreviewMultiVideoBean
 * edu.uiuc.ncsa.cet.bean.PreviewDocumentBean
 * edu.uiuc.ncsa.cet.bean.PreviewMultiImageBean
 *  
 *  -verbose : show blob/triple level info
 *  -dodelete : actually remove blobs/triples rather than listing them (i.e. it's listonly by default)
 *  -limit<x> : process a maximium of <x> datasets
 *  
 * @author myersjd@umich.edu
 *                               
 */

package org.sead.acr.tools.medici;

import java.util.HashSet;
import java.util.Set;

import org.tupeloproject.kernel.TripleFetcher;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Literal;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

public class RemovePreviews extends NodeProcessorBase {

    static boolean includeSmall  = false;
    static boolean includeViewed = false;

    public static void main(String[] args) throws Exception {

        init("removepreviews-log-", false); // No beansession needed

        for (String arg : args) {
            println("Arg is : " + arg);
            if (arg.equalsIgnoreCase("-verbose")) {
                verbose = true;
                println("Verbose Mode");
            } else if (arg.equalsIgnoreCase("-dodelete")) {
                doDelete = true;
                println("Delete Mode");
            } else if (arg.startsWith("-limit")) {
                max = Long.parseLong(arg.substring(6));
                println("Max dataset count: " + max);
            } else if (arg.equalsIgnoreCase("-includesmall")) {
                includeSmall = true;
                println("Include Small Previews Mode");
            } else if (arg.equalsIgnoreCase("-includeviewed")) {
                includeViewed = true;
                println("Include Viewed Mode");
            }
        }

        processActiveData = true; // Don't expect to want to delete previews of
                                  // deleted items (without just removing them
                                  // altogether)

        Set<Literal> typeSet = new HashSet<Literal>();
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewVideoBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewImageBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewTabularDataBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewMultiTabularDataBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewPyramidBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewMultiVideoBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewDocumentBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewMultiImageBean"));
        // FixMe - Ignoring edu.uiuc.ncsa.cet.bean.PreviewGeoserverBean until we
        // add code to remove the geoserver entries

        UriRef classnameRef = Resource.uriRef("tag:tupeloproject.org,2006:/2.0/beans/2.0/propertyValueImplementationClassName");

        Unifier uf = new Unifier();
        uf.addPattern("d", Rdf.TYPE, Cet.DATASET);
        uf.setColumnNames("d");
        context.perform(uf);

        // Find all datasets
        for (Tuple<Resource> row : uf.getResult()) {
            // Filter out those with views if desired
            if (!includeViewed) {
                TripleMatcher tm = new TripleMatcher();
                tm.setSubject(row.get(0));
                tm.setPredicate(MMDB.VIEWED_BY);
                context.perform(tm);
                if (!(tm.getResult().isEmpty())) {
                    // Skip any datasets that have been viewed
                    continue;
                }
            }
            // Now get Previews
            Unifier previews = new Unifier();
            previews.addPattern(row.get(0), Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/hasPreview"), "prev");
            previews.addPattern("prev", classnameRef, "type");
            previews.setColumnNames("prev", "type");
            context.perform(previews);
            for (Tuple<Resource> prev : previews.getResult()) {
                boolean small = false;
                // Filter out small one if desired
                if ((!includeSmall) && (prev.get(1).toString().equals("edu.uiuc.ncsa.cet.bean.PreviewImageBean"))) {
                    TripleFetcher prevMeta = new TripleFetcher();
                    prevMeta.setSubject(prev.get(0));
                    context.perform(prevMeta);
                    int size = 0;

                    for (Triple t : prevMeta.getResult()) {
                        if ((t.getPredicate().toString().equalsIgnoreCase("http://cet.ncsa.uiuc.edu/2007/hasImageHeight"))) {
                            size += Integer.parseInt(t.getObject().toString());
                        }
                        if ((t.getPredicate().toString().equalsIgnoreCase("http://cet.ncsa.uiuc.edu/2007/hasImageWidth"))) {
                            size += Integer.parseInt(t.getObject().toString());
                        }
                    }
                    // Thumbnails at 200x200 and Medium at 600x800 are both
                    // considered small
                    if (size < 1500) {
                        small = true;
                    }
                }
                if (small) {
                    continue;
                }
                // Have one we should process
                if (numberProcessed < max) {

                    println("Processing " + prev.get(1).toString() + " for dataset: " + row.get(0) + " : " + prev.get(0));

                    process((UriRef) prev.get(0));
                    numberProcessed++;

                    println("Stats after " + numberProcessed + " previews: " + totalBlobs + " blobs, " + totalBytes + " bytes, " + totalTriples + " triples");
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
