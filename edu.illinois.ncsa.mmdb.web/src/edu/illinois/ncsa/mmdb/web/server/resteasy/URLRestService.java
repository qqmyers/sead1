package edu.illinois.ncsa.mmdb.web.server.resteasy;

/**
 * @author myersjd@umich.edu
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.nds.util.IDMediator;
import org.tupeloproject.rdf.Namespaces;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Dc;

import edu.illinois.ncsa.mmdb.web.common.Permission;

/**
 * SEAD URL Service Endpoints
 * These services require user authentication. The simplest mechanism to do this
 * is to
 * call /api/authenticate with an Oauth2 token (e.g. from Google) which will set
 * a session
 * cookie that can be returned with all calls here. (/api/logout will invalidate
 * session).
 * Credentials can also be sent with each call.
 * 
 */
@Path("/urls")
public class URLRestService extends ItemServicesImpl {
    /** Commons logging **/
    //  private static Log                         log       = LogFactory.getLog(DatasetsRestService.class);

    protected static final Map<String, Object> doiBasics = new LinkedHashMap<String, Object>() {
                                                             {
                                                                 /* Example of how to do a typed term (description can actually have string or URIs currently, so this is a hypothetical example)
                                                                  Map<String, String> desc = new HashMap<String, String>();
                                                                  desc.put("@id", Dc.DESCRIPTION.toString());
                                                                  desc.put("@type", "@id");
                                                                  put("Description", desc);
                                                                 */
                                                                 put("Identifier", Namespaces.dcTerms("identifier"));
                                                                 put("Title", Namespaces.dcTerms("title"));
                                                                 put("Date", Namespaces.dcTerms("date"));
                                                                 put("Publisher", Namespaces.dcTerms("publisher"));
                                                                 put("SameAs", "http://www.w3.org/2002/07/owl#sameAs");
                                                                 put("Mimetype", Dc.FORMAT.toString());
                                                                 put("Creator", Namespaces.dcTerms("creator"));
                                                             }
                                                         };

    /**
     * Get metadata for URI
     * 
     * @return basic metadata as JSON-LD
     */
    @GET
    @Path("{uri}/metadata")
    @Produces("application/json")
    public Response getURLMetadataAsJSON(@PathParam("uri") @Encoded String uri, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef id = Resource.uriRef((String) request.getAttribute("userid"));
        Map<String, Object> tempResult = new LinkedHashMap<String, Object>();
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            //        log.warn(e1);
            result.put("Error", "Bad URL");
            return Response.status(500).entity(result).build();
        }
        if (uri.startsWith("doi:")) {
            String url = "http://dx.doi.org/" + uri.substring(4);
            //      log.error("Getting  " + url);
            IDMediator.getMetadata(url, tempResult);
        } else if (uri.startsWith("tag:")) {
            //return sead info        
            return getItemMetadataAsJSON(uri, id, true);
        }
        Map<UriRef, String> ptlMap = getPredToLabelMap(doiBasics);
        Map<String, String> context = new HashMap<String, String>(doiBasics.size());
        //Map to LD
        for (Entry<String, Object> e : tempResult.entrySet() ) {
            UriRef pred = new UriRef(e.getKey());
            if (ptlMap.containsKey(pred)) {
                //Label - so use it and add to LD context
                result.put(ptlMap.get(pred), e.getValue());
                context.put(ptlMap.get(pred), e.getKey());
            } else {
                //No label
                result.put(e.getKey(), e.getValue());
            }

        }

        if (result.isEmpty()) {
            return Response.status(404).entity("Item " + id + " Not Found.").build();
        }
        // Note - since this context comes form simple pred/label info, none of the entries can have the 
        // value-type info possible with set contexts (see example at top of file)
        result.put("@context", context);
        return Response.status(200).entity(result).build();
    }

    /**
     * Get dataset content
     * 
     * @param id
     *            - the URL encoded SEAD ID for the desired dataset
     * @return the bytes associated with this ID (i.e. the 'file' contents)
     */
    @GET
    @Path("/{uri}/file")
    public Response getDataset(@PathParam("uri") @Encoded String uri, @javax.ws.rs.core.Context HttpServletRequest request) {
        String origUri = uri;
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Response r = null;

        final UriRef creator = Resource.uriRef((String) request.getAttribute("userid"));

        PermissionCheck p = new PermissionCheck(creator, Permission.DOWNLOAD);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            //    log.warn(e1);
            result.put("Error", "BadURL");
            return Response.status(500).entity(result).build();
        }
        if (uri.startsWith("doi:")) {
            final InputStream is;
            try {
                is = IDMediator.getDataStream(new URL("http://dx.doi.org/" + uri.substring(4)));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                //      log.warn(e);
                result.put("Error", "Bad URL");
                return Response.status(500).entity(result).build();

            }
            //Build output
            StreamingOutput streamingResult = new StreamingOutput() {

                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    IOUtils.copyLarge(is, output);
                }
            };

            return Response.ok(streamingResult, "application/octet-stream").header("content-disposition", "attachement; filename = " + uri).build();

        } else if (uri.startsWith("tag:")) {
            //return sead info        
            return DatasetsRestService.getDataFile(origUri, request);
        }
        result.put("Not Found", uri);
        return Response.status(404).entity(result).build();

    }

    private static Map<UriRef, String> predToLabelMap = null;

    private static Map<UriRef, String> getPredToLabelMap(Map<String, Object> valMap) {

        if (predToLabelMap == null) {
            predToLabelMap = new LinkedHashMap<UriRef, String>();

        }
        for (Entry<String, Object> e : valMap.entrySet() ) {
            String pred;
            Object temp = e.getValue();
            if (temp instanceof String) {
                pred = (String) temp;
            } else {
                pred = ((Map<String, String>) temp).get("@id");
            }
            predToLabelMap.put(Resource.uriRef(pred), e.getKey());
        }
        return predToLabelMap;
    }
}
