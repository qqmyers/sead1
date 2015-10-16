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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.util.Tuple;

/*Checks/repairs collection structure to match path information provided by the bulk uploader.
 * (Useful validation tool, but originally developed to repair problems caused by Uploader 
 * bug/crashes that did not allow it to finish writing collection relationships.
 * 
 */

public class CheckCollections extends MediciToolBase {

    private static long max          = 9223372036854775807l;
    private static long skip         = 0l;
    private static long addCount     = 0l;
    private static long processCount = 0l;

    public static void main(String[] args) throws Exception {

        init("check-collection-log-", false); // No beansession needed

        for (String arg : args) {
            println("Arg is : " + arg);
            if (arg.equalsIgnoreCase("-listonly")) {
                listonly = true;
                println("List Only Mode");
            } else if (arg.startsWith("-limit")) {
                max = Long.parseLong(arg.substring(6));
                println("Max triple count: " + max);
            } else if (arg.startsWith("-skip")) {
                skip = Long.parseLong(arg.substring(5));
                println("Skip triple count: " + skip);
            }
        }

        // go through arguments
        for (String arg : args) {
            if (!((arg.equalsIgnoreCase("-listonly")) || (arg.startsWith("-limit")) || (arg.startsWith("-skip")))) {

                File file = new File(arg);
                checkDatasetList(file);
            }
        }
        flushLog();
    }

    private static HashMap<String, UriRef> parents = new HashMap<String, UriRef>();

    private static void checkDatasetList(File file) throws OperatorException, IOException {
        int icr = 0;
        TripleWriter tw = null;
        UriRef frbr = Resource.uriRef("http://purl.org/vocab/frbr/core#embodimentOf");
        BufferedReader br;
        br = new BufferedReader(new FileReader(file));
        while (br.ready()) {

            String nextLine = br.readLine();
            if (processCount >= skip) {
                if (processCount < max) {
                    if (icr == 0) {
                        tw = new TripleWriter();
                    }
                    Unifier uf = new Unifier();
                    UriRef data = Resource.uriRef(nextLine);
                    uf.addPattern(data, frbr, "path");
                    uf.addPattern("parent", DcTerms.HAS_PART, data, true);
                    uf.setColumnNames("path", "parent");
                    context.perform(uf);
                    Tuple<Resource> t = uf.getFirstRow();
                    if (t.get(1) == null) {
                        // Missing collection
                        String pathString = t.get(0).toString();
                        pathString = pathString.substring(0, pathString.lastIndexOf("/"));
                        if (!parents.containsKey(pathString)) {
                            Unifier uf2 = new Unifier();
                            uf2.addPattern("coll", frbr, Resource.literal(pathString));
                            uf2.addColumnName("coll");
                            context.perform(uf2);
                            Tuple<Resource> first =  uf2.getFirstRow();
                            if(first!=null) {
                            UriRef newParent = (UriRef) first.get(0);
                            
                                parents.put(pathString, newParent);
                            }
                        }
                        if (parents.containsKey(pathString)) {
                            tw.add(parents.get(pathString), DcTerms.HAS_PART, data);
                            addCount++;
                            icr++;
                        }
                    }
                }
            }
            processCount++;

            if (icr == 100) {
                icr = 0;
                println("Current Count: " + addCount + "to fix out of " + processCount);
                writeTriples(tw);
                flushLog();
            }
        }

        if (icr != 0) {
            println("Final Count: " + addCount + "fixed out of " + processCount);
            writeTriples(tw);
        }
        flushLog();
        br.close();
    }

    private static void writeTriples(TripleWriter tw) {
        Set<Triple> add = tw.getToAdd();
        println("To Add:");
        for (Triple t : add) {
            println(t.toString());
        }
        if (!listonly) {
            try {
                context.perform(tw);
            } catch (Exception e) {
                println(e.getMessage());
                println(e.getStackTrace().toString());
                flushLog();
                System.exit(0);
            }
        }
    }
}
