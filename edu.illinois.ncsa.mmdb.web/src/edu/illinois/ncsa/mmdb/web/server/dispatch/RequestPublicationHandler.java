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

/*
 *  NB: For these services to work, -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true must be set on the server. This
 * potentially opens the door for attacks related to CVE-2007-0450 if any code on the server follows paths sent
 * in URLs.
 */

package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Namespaces;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;

import com.hp.hpl.jena.vocabulary.DCTerms;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserPID;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RequestPublication;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TokenStore;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.resteasy.ItemServicesImpl;
import edu.illinois.ncsa.mmdb.web.server.util.CollectionInfo;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

public class RequestPublicationHandler implements ActionHandler<RequestPublication, EmptyResult> {

    /** Commons logging **/
    private static Log   log           = LogFactory.getLog(RequestPublicationHandler.class);

    public static UriRef Aggregation   = Resource.uriRef("http://www.openarchives.org/ore/terms/Aggregation");
    public static UriRef hasSalt       = Resource.uriRef("http://sead-data.net/vocab/hasSalt");
    public static UriRef hasVersionNum = Resource.uriRef("http://sead-data.net/vocab/hasVersionNumber");

    @Override
    public EmptyResult execute(final RequestPublication action, ExecutionContext context) throws ActionException {

        log.debug("Requesting pub of " + action.getUri().toString());
        // only allow if user can edit user metadata fields
        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        try {
            if (!rbac.checkPermission(action.getUser(), action.getUri(), Permission.EDIT_USER_METADATA)) {
                throw new ActionException("Unauthorized");
            }
        } catch (RBACException e) {
            throw new ActionException("access control failure", e);
        }
        try {
            TripleWriter tw = new TripleWriter();

            //1.5
            final Resource subject = Resource.uriRef(action.getUri());
            Resource predicate = IsReadyForPublicationHandler.proposedForPublicationRef;
            Resource value = Resource.literal("true"); //Value is not used - just checking the existence of the triple for 1.5 publication
            log.debug("Writing 1.5 triple");
            tw.add(subject, predicate, value);

            //2.0 publication

            final String versionNumber = getVersionNumber(subject);

            //Generate ID for published version
            Map<Resource, Object> md = new HashMap<Resource, Object>();
            md.put(Rdf.TYPE, Aggregation);
            final UriRef aggId = Resource.uriRef(RestUriMinter.getInstance().mintUri(md));

            //Write required triples
            tw.add(aggId, Rdf.TYPE, Aggregation);
            tw.add(subject, DcTerms.HAS_VERSION, aggId);
            tw.add(aggId, hasVersionNum, Resource.literal(versionNumber));
            //Used to generate unique keys for file retrieval
            final String salt = UUID.randomUUID().toString();
            tw.add(aggId, hasSalt, Resource.literal(salt));

            log.debug("Agg: " + aggId.toString() + " is version: " + versionNumber);

            TupeloStore.getInstance().getContext().perform(tw);

            //Start pub request and oremap generation as a background process

            Thread oreThread = new Thread()
            {
                public void run() {
                    String this_space = PropertiesLoader.getProperties().getProperty("domain");
                    final String idUri = this_space + "/resteasy/researchobjects/" + aggId.toString();

                    ItemServicesImpl.generateOREById(aggId.toString(), idUri);

                    //Send notice to service
                    String server = TupeloStore.getInstance().getConfiguration(ConfigurationKey.CPURL);
                    try {
                        JSONObject requestJsonObject = new JSONObject();
                        //Kludge - change label for dc terms created to be consistent with 2.0 and with the item biblio map
                        //Will make the change here rather than changing in the basic collection map to avoid side affects other places that's used.
                        //FWIW - root cause is that datasets and collections use dc elements date and dc terms created respectively
                        //and we've tried to manage that with different labels, except wieh lists may mix both datasets and collections...
                        //rather than go back adn fix all existing datasets and every place the different labels are expected....
                        //NB. The metadata has been correctly reported, it's just an issue of consistent labels
                        Map<String, Object> newColBasicsMap = ItemServicesImpl.collectionBasics;
                        newColBasicsMap.remove("Date");
                        newColBasicsMap.put("Creation Date", Namespaces.dcTerms("created"));
                        JSONObject aggJsonObject = new JSONObject(ItemServicesImpl.getMetadataMapById(subject.toString(), newColBasicsMap, Resource.uriRef(action.getUser())));
                        aggJsonObject.put("Identifier", aggId.toString());
                        aggJsonObject.put("@id", idUri + "?pubtoken=" + TokenStore.generateToken("/researchobjects/" + aggId.toString(), salt) + "#aggregation");
                        aggJsonObject.put("@type", "Aggregation");

                        //Collection ids must be encoded
                        aggJsonObject.put("similarTo", this_space + "/resteasy/collections/" + URLEncoder.encode(subject.toString(), "UTF-8"));

                        JSONObject contextObject = aggJsonObject.getJSONObject("@context");
                        requestJsonObject.put("Aggregation", aggJsonObject);
                        requestJsonObject.put("Repository", TupeloStore.getInstance().getConfiguration(ConfigurationKey.DefaultRepository));
                        String method = "/researchobjects/" + aggId.toString() + "/pid";
                        requestJsonObject.put("Publication Callback", this_space + "/resteasy" + method + "?pubtoken=" + TokenStore.generateToken(method, salt));
                        JSONObject preferencesJsonObject = new JSONObject(TupeloStore.getInstance().getConfiguration(ConfigurationKey.DefaultCPPreferences));
                        Object prefContextObject = null;
                        if (preferencesJsonObject.has("@context")) {
                            prefContextObject = preferencesJsonObject.get("@context");
                        }
                        requestJsonObject.put("Preferences", preferencesJsonObject);

                        String rHolderString = action.getUser();

                        TripleMatcher idMatcher = new TripleMatcher();
                        idMatcher.setSubject(Resource.uriRef(rHolderString));
                        idMatcher.setPredicate(Resource.uriRef(GetUserPID.userPIDPredicate));
                        TupeloStore.getInstance().getContext().perform(idMatcher);
                        Set<Triple> idSet = idMatcher.getResult();
                        if (idSet.size() > 0) {
                            rHolderString = idSet.iterator().next().getObject().toString();
                        }
                        requestJsonObject.put("Rights Holder", rHolderString);

                        //Now add stats
                        CollectionInfo ci = ItemServicesImpl.getCollectionInfo((UriRef) subject, 0);
                        JSONObject stats = new JSONObject();

                        stats.put("Total Size", "" + ci.getSize());
                        stats.put("Number of Collections", "" + ci.getNumCollections());
                        stats.put("Number of Datasets", "" + ci.getNumDatasets());
                        stats.put("Max Dataset Size", "" + ci.getMaxDatasetSize());
                        stats.put("Max Collection Depth", "" + ci.getMaxDepth());
                        stats.put("Data Mimetypes", ci.getMimetypeSet());
                        requestJsonObject.put("Aggregation Statistics", stats);

                        aggJsonObject.put("Publishing Project", getProjectID());

                        aggJsonObject.put("Publishing Project Name", TupeloStore.getInstance().getConfiguration(ConfigurationKey.ProjectName));

                        for (String key : ItemServicesImpl.collectionStats.keySet() ) {
                            contextObject.put(key, ItemServicesImpl.collectionStats.get(key));
                        }
                        contextObject.put("Preferences", "http://sead-data.net/terms/publicationpreferences");
                        contextObject.put("Repository", "http://sead-data.net/terms/requestedrepository");
                        contextObject.put("Aggregation Statistics", "http://sead-data.net/terms/publicationstatistics");
                        contextObject.put("Publication Callback", "http://sead-data.net/terms/publicationcallback");
                        contextObject.put("Rights Holder", DCTerms.rightsHolder.toString());
                        contextObject.put("Affiliations", "http://sead-data.net/terms/affiliations");
                        contextObject.put("Publishing Project", "http://sead-data.net/terms/publishingProject");
                        contextObject.put("Publishing Project Name", "http://sead-data.net/terms/publishingProjectName");

                        aggJsonObject.remove("@context");

                        requestJsonObject.accumulate("@context", contextObject);
                        requestJsonObject.accumulate("@context", "https://w3id.org/ore/context");
                        if (prefContextObject != null) {
                            requestJsonObject.accumulate("@context", prefContextObject);
                        }

                        log.debug("JSON: " + requestJsonObject.toString());

                        URL url = new URL(server + "/researchobjects");
                        log.debug("URL = " + url.toString());
                        try {
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                            // send post
                            conn.setDoOutput(true);
                            conn.setRequestProperty("Accept", "application/json");
                            conn.setRequestProperty("Content-type", "application/json");
                            conn.setRequestMethod("POST");
                            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                            wr.write(requestJsonObject.toString());
                            wr.flush();
                            wr.close();

                            // Get the response
                            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String line;
                            StringBuilder sb = new StringBuilder();
                            while ((line = rd.readLine()) != null) {
                                log.debug(line);
                                sb.append(line);
                                sb.append("\n"); //$NON-NLS-1$
                            }
                            rd.close();
                        } catch (IOException io) {
                            log.warn("2.0 Pub request call failed for " + aggId.toString(), io);
                            ItemServicesImpl.removeMap(aggId.toString());
                        }
                    }
                    catch (Exception e) {
                        log.debug("Pub Request Failed", e);
                        //Remove triples about this request
                        TripleWriter tw = new TripleWriter();
                        tw.remove(aggId, Rdf.TYPE, RequestPublicationHandler.Aggregation);
                        tw.remove(subject, DcTerms.HAS_VERSION, aggId);
                        tw.remove(aggId, Resource.uriRef("http://sead-data.net/vocab/hasVersionNumber"), Resource.literal(versionNumber));
                        tw.remove(aggId, hasSalt, Resource.literal(salt));
                        //Remove map if it was created
                        ItemServicesImpl.removeMap(aggId.toString());

                        try {
                            TupeloStore.getInstance().getContext().perform(tw);
                            log.debug("Removed RO: " + aggId);
                        } catch (Exception pe) {
                            log.debug("Failed to remove RO with ID: " + aggId, pe);
                        }
                    }
                }

            };
            oreThread.start();

            return new EmptyResult();
        } catch (Exception x) {
            log.error("Error publishing on " + action.getUri(), x);
            throw new ActionException("failed", x);
        }
    }

    public static String getProjectID() {
        String projID;
        try {
            projID = new URL(PropertiesLoader.getProperties().getProperty("domain")).getHost();
            projID = projID.substring(0, projID.indexOf("."));
            projID = "http://sead-data.net/projects/" + projID;
        } catch (Exception e) {
            projID = "http://sead-data.net/projects/unknown";
            log.error("Unable to determine project name: " + e.getLocalizedMessage());
        }
        return projID;
    }

    public static String getVersionNumber(Resource subject) throws OperatorException {
        //Find out how many times published
        //via 2.0
        TripleMatcher tMatcher = new TripleMatcher();
        tMatcher.match(subject, DcTerms.HAS_VERSION, null);
        TupeloStore.getInstance().getContext().perform(tMatcher);
        //via 1.5

        TripleMatcher tm2 = new TripleMatcher();
        tm2.match(subject, Resource.uriRef(DCTerms.identifier.toString()), null);
        TupeloStore.getInstance().getContext().perform(tm2);
        return Integer.toString(tMatcher.getResult().size() + tm2.getResult().size() + 1);
    }

    @Override
    public Class<RequestPublication> getActionType() {
        return RequestPublication.class;
    }

    @Override
    public void rollback(RequestPublication action, EmptyResult result, ExecutionContext context) throws ActionException {
        // TODO Auto-generated method stub

    }

}
