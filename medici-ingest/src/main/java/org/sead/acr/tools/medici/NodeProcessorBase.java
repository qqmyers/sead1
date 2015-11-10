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
 * Recursive decent through all UriRefs to show or delete the full tree of metadata/blobs associated with a  node. 
 * Includes links where the node (currently only affects datasets and collections)is the top-level subject or the object as well as reified info, 
 * i.e. statements about metadata that are relationships.
 * Decent truncated at references to other datasets/collections/tags/People/Relationships/etc that may be used elsewhere.
 * 
 *  This is ~ the Concise Bounded Description of the Node 
 *  
 *  -verbose : show blob/triple level info
 *  -dodelete : actually remove blobs/triples rather than listing them (i.e. it's listonly by default)
 *  -processactivedata : process data that has not been marked as deleted
 *  -limit<x> : process a maximium of <x> datasets
 *  
 * @author myersjd@umich.edu
 *                               
 */

package org.sead.acr.tools.medici;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.tupeloproject.kernel.BlobChecker;
import org.tupeloproject.kernel.BlobRemover;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleFetcher;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.BlankNode;
import org.tupeloproject.rdf.Literal;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.uiuc.ncsa.cet.bean.CETBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

public class NodeProcessorBase extends MediciToolBase {

    static long           totalBytes         = 0l;
    static long           totalTriples       = 0l;
    static long           totalBlobs         = 0l;
    protected static long max                = 9223372036854775807l;
    protected static long numberProcessed    = 0l;

    static Set<UriRef>    processedNodeRefs  = new HashSet<UriRef>();
    static Set<UriRef>    predicatesFollowed = new HashSet<UriRef>();

    static boolean        verbose            = false;
    static boolean        doDelete           = false;
    static boolean        processActiveData  = false;

    static final String   GEOWORKSPACE       = "medici";             // used to
                                                                      // remove
                                                                      // geo
                                                                      // preview

    protected static boolean process(UriRef node) {

        try {
            if ("Not processed".equals(processNode(node, 0, null, null))) {
                return false;
            } else {
                return true;
            }
        } catch (OperatorException e) {
            println("Error processing node: " + node.toString());
            e.printStackTrace();
        }
        return false;
    }

    private static String processNode(UriRef node, int depth, String predicate, UriRef parent) throws OperatorException {
        if (predicate != null) {
            if (processedNodeRefs.contains(node)) {
                if (verbose) {
                    println(offset(depth) + predicate + " <Link>" + node.toString());
                }
                return null;
            }
        }
        processedNodeRefs.add(node);

        TripleFetcher tFetcher = new TripleFetcher();
        tFetcher.setSubject(node);
        context.perform(tFetcher);
        Set<Triple> triples = tFetcher.getResult();
        if ((predicate != null) && isPerson(node, triples)) {
            return "Person";
        } else if ((predicate != null) && isDataSet(node, triples)) {
            return "DataSet";
        } else if ((predicate != null) && isCollection(node, triples)) {
            return "Collection";
        } else if ((predicate != null) && isTag(node, triples)) {
            return "Tag";
        } else if ((predicate != null) && isTopLevelMarker(node, triples)) {
            return "TopLevelMarker";
        } else if ((predicate != null) && isRelationship(node, triples)) {
            return "Relationship";
        }  else {

            // No predicate means top-level and we only process if it is deleted
            // or
            // the processactive flag is set
            if ((predicate == null) && !(processActiveData || triples.contains(new Triple(node, DcTerms.IS_REPLACED_BY, Rdf.NIL)))) {
                return "Not processed";
            }
            
            if ((predicate == null) && (isGeoPreview(node, triples))) {
                println("geoserver entry may exist");
                if (doDelete) {
                    TripleMatcher parentMatcher = new TripleMatcher();
                    parentMatcher.setPredicate(Cet.cet("hasPreview"));
                    parentMatcher.setObject(node);
                    context.perform(parentMatcher);
                    if (!parentMatcher.getResult().isEmpty()) {
                        UriRef dataset = (UriRef) parentMatcher.getResult().iterator().next().getSubject();
                        if (deleteGeoserverDataStore(dataset)) {
                            println("Deleted geoserver entry for :" + dataset.toString());
                        } else {
                            println("Failed to remove geoserver entry for :" + dataset.toString());
                        }
                    }
                }

            }
            
            // Assure node itself exists and has a size - add it's size to
            // the
            // total
            BlobChecker bc = new BlobChecker(node);
            context.perform(bc);
            if (verbose) {
                if (predicate != null) {
                    println(offset(depth) + predicate + " : " + node.toString() + " : " + triples.size());
                } else {
                    println(offset(depth) + node.toString() + " : " + triples.size());
                }
            }
            if (bc.exists()) {
                if (bc.getSize() <= 0) {
                    println("Bad File: Node Size reported as: " + bc.getSize());
                } else {
                    if (verbose) {
                        println(offset(depth) + "Blob: " + bc.getFile().getName() + " : " + bc.getSize());
                    }
                    totalBytes += bc.getSize();
                    totalBlobs++;
                    if (doDelete) {
                        BlobRemover br = new BlobRemover(node);
                        context.perform(br);
                    }
                }
            }

            String childOffset = offset(depth + 1);
            totalTriples += triples.size();

            if (predicate == null) {
                println("Node: " + node.toString() + " " + bc.getSize() + ", " + triples.size() + " triples.");
            }
            for (Triple t : triples) {
                predicatesFollowed.add((UriRef) t.getPredicate());

                Resource obj = t.getObject();
                if (obj instanceof Literal) {
                    if (verbose) {
                        println(childOffset + t.getPredicate().toString() + " : \"" + obj.toString() + "\"");
                    }
                } else if (obj instanceof BlankNode) {
                    if (verbose) {
                        println(childOffset + t.getPredicate().toString() + " : blank:" + obj.toString());
                    }
                } else if (obj instanceof UriRef) {
                    String type = processNode((UriRef) obj, depth + 1, t.getPredicate().toString(), node);
                    if (type != null) {
                        if (verbose) {
                            println(childOffset + t.getPredicate().toString() + " : " + type + ":" + obj.toString());
                        }
                    }
                } else {
                    if (verbose) {
                        println(childOffset + t.getPredicate().toString() + " : resource:" + obj.toString());
                    }
                }
            }
            // If this item has a blob or triples, check to see if anything
            // links to it
            if (bc.exists() || !triples.isEmpty()) {
                TripleMatcher incomingMatcher = new TripleMatcher();
                incomingMatcher.setObject(node);
                context.perform(incomingMatcher);
                Set<Triple> incomingTriples = incomingMatcher.getResult();
                totalTriples += incomingTriples.size();

                for (Triple t : incomingTriples) {
                    // Skip back links
                    if ((parent == null) || (!(t.getSubject().toString().equals(parent.toString())))) {
                        String type = processNode((UriRef) t.getSubject(), depth, "<-- " + t.getPredicate().toString() + " <-- ", null);
                        if (type != null) {
                            if (verbose) {
                                println(offset(depth) + "<-- " + t.getPredicate().toString() + " <--  : " + type + ":" + t.getSubject().toString());
                            }
                        }

                    }
                }
                if (doDelete) {
                    // Remove backlinks
                    if (!incomingTriples.isEmpty()) {
                        TripleWriter tw = new TripleWriter();
                        tw.removeAll(incomingTriples);
                        context.perform(tw);
                    }

                }
            }
            if (doDelete) {
                if (!triples.isEmpty()) {
                    TripleWriter tw = new TripleWriter();
                    tw.removeAll(triples);
                    context.perform(tw);
                }
            }
        }
        flushLog();
        return null;
    }

    private static boolean isGeoPreview(UriRef node, Set<Triple> triples) {
        return triples.contains(new Triple(node, Resource.uriRef("tag:tupeloproject.org,2006:/2.0/beans/2.0/propertyValueImplementationClassName"), Resource
                .literal("edu.uiuc.ncsa.cet.bean.PreviewGeoserverBean")));
    }

    private static boolean isCollection(UriRef node, Set<Triple> triples) {
        return triples.contains(new Triple(node, Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE));
    }

    private static boolean isDataSet(UriRef node, Set<Triple> triples) {
        return triples.contains(new Triple(node, Rdf.TYPE, Cet.DATASET));
    }

    private static boolean isPerson(UriRef node, Set<Triple> triples) {
        return triples.contains(new Triple(node, Rdf.TYPE, Resource.uriRef("http://xmlns.com/foaf/0.1/Person")));
    }

    private static boolean isTag(UriRef node, Set<Triple> triples) {
        return triples.contains(new Triple(node, Rdf.TYPE, Resource.uriRef("http://www.holygoat.co.uk/owl/redwood/0.1/tags/Tag")));
    }

    private static boolean isTopLevelMarker(UriRef node, Set<Triple> triples) {
        return node.toString().equals("http://sead-data.net/terms/acr/Top_Level");
    }

    private static boolean isRelationship(UriRef node, Set<Triple> triples) {
        // inverse relationships may not have a relationship type so check
        // 'inverseness'
        for (Triple t : triples) {
            if (t.getPredicate().toString().equals("http://www.w3.org/2002/07/owl#inverseOf")) {
                return true;
            }
        }
        return triples.contains(new Triple(node, Rdf.TYPE, Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/mmdb/Relationship")));
    }

    private static String offset(int depth) {
        StringBuffer sBuffer = new StringBuffer();
        if (depth > 5) {
            sBuffer.append("*");
            depth -= 5;
        }
        for (int i = 0; i < depth; i++) {
            sBuffer.append("  ");
        }
        return sBuffer.toString();
    }

    /**
     * Modified from
     * edu.illinois.ncsa.medici.extractor.geoserver.GeoserverExtractor by Jong
     * Lee Delete geoserver datastore via geoserver rest api
     * 
     * @param geoserver
     * @param username
     * @param password
     * @param worksapce
     * @param storeName
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public static boolean deleteGeoserverDataStore(UriRef dataset) {
        CloseableHttpResponse response = null;
        try {
            // create geoserver rest publisher
            String geoserver = props.getProperty("proxiedgeoserver");
            if (geoserver == null) {
                geoserver = "http://localhost:8080/geoserver";
            }
            println("GEOSERVER: " + geoserver);

            String username = props.getProperty("geouser");
            if (username == null) {
                username = "admin";
            }
            println("USER: " + username);
            String password = props.getProperty("geopassword");
            if (password == null) {
                println("password was null");
                password = "admin";
            }

            String storeName = URLEncoder.encode(dataset.toString(), "UTF-8");
            println("Store: " + storeName);
            URL urlp = new URL(geoserver);
            println("URLP: " + urlp.toExternalForm());
            String url = geoserver + "/rest/workspaces/" + GEOWORKSPACE + "/datastores/" + storeName + ".html?recurse=true";
            System.out.println("Calling: " + url);

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(urlp.getHost(), urlp.getPort()), new UsernamePasswordCredentials(username, password));
            CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();

            HttpDelete delete = new HttpDelete(url);

            response = httpClient.execute(delete);
        } catch (Exception e) {
            if (verbose) {
                println(e.getMessage());
            }
        }
        if ((response != null) && (response.getStatusLine().getStatusCode() == 200))
            return true;
        else
            return false;
    }
}
