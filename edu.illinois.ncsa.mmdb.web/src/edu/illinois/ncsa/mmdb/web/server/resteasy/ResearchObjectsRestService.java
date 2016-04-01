/*
*
* Copyright 2015, 2016 University of Michigan
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

package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;

import edu.illinois.ncsa.mmdb.web.server.TokenStore;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

/**
 * SEAD Data Service endpoints for collections. NB: For collection IDs
 * containing '/' characters, the ids must be urlencoded for
 * transmission.
 */
@Path("/researchobjects")
@NoCache
public class ResearchObjectsRestService extends ItemServicesImpl {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PIDBean {
        private String uri;

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

    }

    /** Commons logging **/
    private static Log log = LogFactory.getLog(ResearchObjectsRestService.class);

    /**
     * Get published ROs
     *
     * @return IDs and basic metadata for collections as JSON-LD
     */
    @GET
    @Path("")
    @Produces("application/json")
    public Response getCollectionAsJSON(@javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        return getTopLevelItems(CollectionBeanUtil.COLLECTION_TYPE, collectionBasics, userId);
    }

    /**
     * Get OREMap for Aggregation
     *
     * @param id
     *            - the ID of RO
     *
     * @return - The ORE map in json-ld
     */
    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getROAsORE_JSON_LD(@PathParam("id") String id, @QueryParam("pubtoken") String token, @javax.ws.rs.core.Context HttpServletRequest request) {

        //Hash key based access check:
        try {
            id = URLDecoder.decode(id, "UTF-8");
            TripleMatcher tMatcher = new TripleMatcher();
            tMatcher.setSubject(Resource.uriRef(id));
            tMatcher.setPredicate(Resource.uriRef("http://sead-data.net/vocab/hasSalt"));
            c.perform(tMatcher);
            String salt = tMatcher.getResult().iterator().next().getObject().toString();

            if ((token == null) || (!TokenStore.isValidToken(token, "/researchobjects/" + id, salt))) {
                log.debug("Invalid pubtoken: " + token);
                Map<String, String> result = new HashMap<String, String>(1);
                result.put("Failure", "Invalid pubtoken");
                return Response.status(Status.FORBIDDEN).entity(result).build();
            }

        } catch (UnsupportedEncodingException e) {
            Map<String, String> result = new HashMap<String, String>(1);
            result.put("Failure", "Could not decode resource id");
            return Response.status(Status.BAD_REQUEST).entity(result).build();
        } catch (OperatorException e) {
            Map<String, String> result = new HashMap<String, String>(1);
            log.error("Error Processing " + id, e);
            result.put("Failure", e.getMessage());
            return Response.status(Status.BAD_REQUEST).entity(result).build();
        }

        return getOREById(id, request);
    }

    /**
     * Get a file for Aggregation
     *
     * @param id
     *            - the ID of the file
     *
     * @return - The ORE map in json-ld
     */
    @GET
    @Path("/{aggId}/files/{id}")
    @Produces("application/json")
    public Response getDataFile(@PathParam("aggId") String aggId, @Encoded @PathParam("id") String id, @QueryParam("pubtoken") String token, @javax.ws.rs.core.Context HttpServletRequest request) {
        try {
            TripleMatcher tMatcher = new TripleMatcher();
            tMatcher.setSubject(Resource.uriRef(aggId));
            tMatcher.setPredicate(Resource.uriRef("http://sead-data.net/vocab/hasSalt"));
            c.perform(tMatcher);
            String salt = null;
            Triple t = tMatcher.getResult().iterator().next();
            if (t != null) {
                salt = tMatcher.getResult().iterator().next().getObject().toString();
            }

            if ((token == null) || (!TokenStore.isValidToken(token, "/researchobjects/" + aggId + "/files/" + id, salt))) {
                log.debug("Invalid pubtoken: " + token);
                Map<String, String> result = new HashMap<String, String>(1);
                result.put("Failure", "Invalid pubtoken");
                return Response.status(Status.FORBIDDEN).entity(result).build();
            }
            id = URLDecoder.decode(id, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            Map<String, String> result = new HashMap<String, String>(1);
            result.put("Failure", "Could not decode resource id");
            return Response.status(Status.BAD_REQUEST).entity(result).build();
        } catch (OperatorException e) {
            Map<String, String> result = new HashMap<String, String>(1);
            log.error("Error Processing " + id, e);
            result.put("Failure", e.getMessage());
            return Response.status(Status.BAD_REQUEST).entity(result).build();
        }
        return DatasetsRestService.getDataFileBytes(Resource.uriRef(id));

    }

    @POST
    @Path("/{id}/pid")
    @Consumes("application/json")
    @Produces("application/json")
    public Response setPid(@PathParam("id") String agg_id, @QueryParam("pubtoken") String token, PIDBean pid, @javax.ws.rs.core.Context HttpServletRequest request) {
        String identifierUriString = null;
        try {

            identifierUriString = pid.getUri();
            if (identifierUriString == null) {
                Map<String, String> result = new HashMap<String, String>(1);
                result.put("Failure", "Could not find \"uri\" in json object");
                return Response.status(Status.BAD_REQUEST).entity(result).build();
            }
            TripleMatcher tMatcher = new TripleMatcher();
            tMatcher.setSubject(Resource.uriRef(agg_id));
            tMatcher.setPredicate(Resource.uriRef("http://sead-data.net/vocab/hasSalt"));
            c.perform(tMatcher);
            String salt = tMatcher.getResult().iterator().next().getObject().toString();

            if ((token == null) || (!TokenStore.isValidToken(token, "/researchobjects/" + agg_id + "/pid", salt))) {
                log.debug("Invalid pubtoken: " + token);
                Map<String, String> result = new HashMap<String, String>(1);
                result.put("Failure", "Invalid pubtoken");
                return Response.status(Status.FORBIDDEN).entity(result).build();
            }
        } catch (Exception e) {
            Map<String, String> result = new HashMap<String, String>(1);
            log.error("Error Processing " + agg_id, e);
            result.put("Failure", e.getMessage());
            return Response.status(Status.BAD_REQUEST).entity(result).build();

        }
        return publishVersion(agg_id, System.currentTimeMillis(), identifierUriString, request);

    }

    /**
     * Publish version.
     *
     * This removes the proposed for publication metadata and sets version
     * publication information
     * *
     *
     * @param id
     *            - the URL-encoded ID of the aggregation
     * @query date
     *        - the publication date to be set, as a long (milliseconds
     *        since January 1, 1970, 00:00:00 GMT) - now by default,
     *
     * @query pid
     *        a persistent identifier for the published item
     *        required, must be in URI form
     *
     * @result - success/failure message: 200 - item published,, 403 -
     *         permission issue, 409 - item has not been
     *         "proposed for publication"
     */
    @POST
    @Path("/{id}/publication")
    public Response uploadMetadata(@PathParam("id") String agg_id, @QueryParam("date") Long date, @QueryParam("pid") String pid, @javax.ws.rs.core.Context HttpServletRequest request) {
        long millis = System.currentTimeMillis();
        if (date != null) {
            millis = date.longValue();
        }
        return super.publishVersion(agg_id, millis, pid, request);
    }

    /**
     * Get stats for this collection and its contents (collections and datasets)
     * including
     * total size, {@link #CollectionsRestService()}, #files, max collection
     * depth, max file size, complete list of mime types included
     *
     * @param id
     *            - URL-encoded identifier for the collection
     *
     * @return - a map of stats, as
     *         JSON-LD
     */
    @GET
    @Path("/{id}/stats")
    @Produces("application/json")
    public Response getCollectionStats(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        return getStats(id, request);
    }

}
