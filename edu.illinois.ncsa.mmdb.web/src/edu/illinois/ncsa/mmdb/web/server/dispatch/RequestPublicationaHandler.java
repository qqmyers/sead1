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

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;

import com.hp.hpl.jena.vocabulary.DCTerms;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RequestPublication;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.resteasy.ItemServicesImpl;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

public class RequestPublicationaHandler implements ActionHandler<RequestPublication, EmptyResult> {

    /** Commons logging **/
    private static Log     log         = LogFactory.getLog(RequestPublicationaHandler.class);

    public static Resource Aggregation = Resource.uriRef("http://www.openarchives.org/ore/terms/Aggregation");

    @Override
    public EmptyResult execute(RequestPublication action, ExecutionContext context) throws ActionException {

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
            Resource subject = Resource.uriRef(action.getUri());
            Resource predicate = IsReadyForPublicationHandler.proposedForPublicationRef;
            Resource value = Resource.literal("true"); //Value is not used - just checking the existence of the triple for 1.5 publication
            log.debug("Writing 1.5 triple");
            tw.add(subject, predicate, value);

            //2.0 publication

            //Find out how many times published
            //via 2.0
            TripleMatcher tMatcher = new TripleMatcher();
            tMatcher.match(subject, DcTerms.HAS_VERSION, null);
            TupeloStore.getInstance().getContext().perform(tMatcher);
            //via 1.5
            Unifier uf = new Unifier();
            TripleMatcher tm2 = new TripleMatcher();
            tm2.match(subject, Resource.uriRef(DCTerms.identifier.toString()), null);
            TupeloStore.getInstance().getContext().perform(tm2);
            String versionNumber = Integer.toString(tMatcher.getResult().size() + tm2.getResult().size() + 1);

            //Generate ID for published version
            Map<Resource, Object> md = new HashMap<Resource, Object>();
            md.put(Rdf.TYPE, Aggregation);
            UriRef aggId = Resource.uriRef(RestUriMinter.getInstance().mintUri(md));

            //Write required triples
            tw.add(aggId, Rdf.TYPE, Aggregation);
            tw.add(subject, DcTerms.HAS_VERSION, aggId);
            tw.add(aggId, Resource.uriRef("http://sead-data.net/vocab/hasVersionNumber"), Resource.literal(versionNumber));

            log.debug("Agg: " + aggId.toString() + " is version: " + versionNumber);

            TupeloStore.getInstance().getContext().perform(tw);

            //Send notice to service
            String server = TupeloStore.getInstance().getConfiguration(ConfigurationKey.CPURL);

            String this_space = PropertiesLoader.getProperties().getProperty("domain");

            JSONObject requestJsonObject = new JSONObject();
            JSONObject aggJsonObject = new JSONObject(ItemServicesImpl.getMetadataMapById(subject.toString(), ItemServicesImpl.collectionBasics, Resource.uriRef(action.getUser())));
            aggJsonObject.put("Identifier", aggId.toString());
            aggJsonObject.put("@id", this_space + "/resteasy/researchobjects/" + aggId.toString() + "#aggregation");
            aggJsonObject.put("@type", "Aggregation");
            //Collection ids must be encoded
            aggJsonObject.put("similarTo", this_space + "/resteasy/collections/" + URLEncoder.encode(subject.toString(), "UTF-8"));

            JSONObject contextObject = aggJsonObject.getJSONObject("@context");
            aggJsonObject.remove("@context");
            requestJsonObject.accumulate("@context", contextObject);
            requestJsonObject.accumulate("@context", "https://w3id.org/ore/context");
            requestJsonObject.put("Aggregation", aggJsonObject);
            requestJsonObject.put("Repository", TupeloStore.getInstance().getConfiguration(ConfigurationKey.DefaultRepository));

            JSONObject preferencesJsonObject = new JSONObject(TupeloStore.getInstance().getConfiguration(ConfigurationKey.DefaultCPPreferences));
            requestJsonObject.put("Preferences", preferencesJsonObject);

            log.debug("JSON" + requestJsonObject.toString());

            URL url = new URL(server + "/cp/researchobjects");
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
            }
            return new EmptyResult();
        } catch (Exception x) {
            log.error("Error publishing on " + action.getUri(), x);
            throw new ActionException("failed", x);
        }
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
