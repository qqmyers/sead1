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

import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Literal;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.util.Tuple;

public class RemoveOrphans extends NodeProcessorBase {

    
    public static void main(String[] args) throws Exception {

        init("removeorphans-log-", false); // No beansession needed

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
            }

        }
        processActiveData=true; //Non-dataset/collection orphans are never marked as deleted

        Set<Literal> typeSet = new HashSet<Literal>();
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewVideoBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewImageBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.gis.GeoPointBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.AnnotationBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewTabularDataBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewMultiTabularDataBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewPyramidBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewMultiVideoBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewDocumentBean"));
        typeSet.add(Resource.literal("edu.uiuc.ncsa.cet.bean.PreviewMultiImageBean"));

        UriRef classnameRef = Resource.uriRef("tag:tupeloproject.org,2006:/2.0/beans/2.0/propertyValueImplementationClassName");

        for (Literal type : typeSet) {
            // List all orphans without parents and then process them
            Unifier uf = new Unifier();
            uf.addPattern("s", classnameRef, type);
            uf.addPattern("d", "p", "s", true);

            uf.setColumnNames("s", "d");
            context.perform(uf);
            for (Tuple<Resource> row : uf.getResult()) {
                if (numberProcessed < max) {
                    if (row.get(1) == null) {
                        println("Orphaned " + type.toString() + " : " + row.get(0).toString());

                        process((UriRef) row.get(0));
                        numberProcessed++;

                        println("Stats after " + numberProcessed + " orphans: " + totalBlobs + " blobs, " + totalBytes + " bytes, " + totalTriples + " triples");
                    }
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
