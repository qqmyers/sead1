package edu.illinois.ncsa.mmdb.web.server.resteasy;

/**
 * @author Rattanachai Ramathitima
 * @author myersjd@umich.edu
 */

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Beans;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.mmdb.web.rest.RestService;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

/*Service endpoints for collections. NB: For collection IDs containing '/' characters, the ids must be urlencoded for 
 * transmission AND -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true must be set on the server. This 
 * potentially opens the door for attacks related to CVE-2007-0450 if any code on the server follows paths sent 
 * in URLs.
 */

@Path("/collections")
public class CollectionsRestService extends ItemServicesImpl {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(CollectionsRestService.class);

    /*Get top-level collections*/
    @GET
    @Path("")
    @Produces("application/json")
    public Response getCollectionAsJSON(@javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        return getTopLevelItems(CollectionBeanUtil.COLLECTION_TYPE, collectionBasics, userId);
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getCollectionAsJSON(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        return getMetadataById(id, collectionBasics, userId);
    }

    @GET
    @Path("/{id}/datasets")
    @Produces("application/json")
    public Response getDatasetsByCollectionAsJSON(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        UriRef baseId = null;
        try {
            id = URLDecoder.decode(id, "UTF-8");
            baseId = Resource.uriRef(id);
        } catch (Exception e1) {
            log.error("Error decoding url for " + baseId.toString(), e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error decoding id: " + baseId.toString()).build();
        }

        return getMetadataByForwardRelationship(baseId, DcTerms.HAS_PART, datasetBasics, userId);
    }

    @POST
    @Path("/{id}/datasets")
    public Response addDatasetToCollection(@PathParam("id") @Encoded String id, @FormParam("dataset_id") String item_id, @javax.ws.rs.core.Context HttpServletRequest request) {
        return addItemToCollection(id, item_id, Cet.DATASET, request);
    }

    @POST
    @Path("/{id}/collections")
    public Response addCollectionToCollection(@PathParam("id") @Encoded String id, @FormParam("collection_id") String item_id, @javax.ws.rs.core.Context HttpServletRequest request) {
        return addItemToCollection(id, item_id, CollectionBeanUtil.COLLECTION_TYPE, request);
    }

    private Response addItemToCollection(String id, String item_id, Resource type, @javax.ws.rs.core.Context HttpServletRequest request) {

        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        Response r = null;
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        UriRef colId = null;
        UriRef childId = null;
        try {
            colId = Resource.uriRef(URLDecoder.decode(id, "UTF-8"));
            childId = Resource.uriRef(URLDecoder.decode(item_id, "UTF-8"));
            ValidItem parent = new ValidItem(colId, CollectionBeanUtil.COLLECTION_TYPE, userId);

            if (!parent.isValid()) {
                r = parent.getErrorResponse();
            } else {
                ValidItem child = new ValidItem(childId, type, userId);
                if (!child.isValid()) {
                    r = child.getErrorResponse();
                } else {
                    TripleWriter tw = new TripleWriter();
                    tw.add(colId, DcTerms.HAS_PART, childId);

                    c.perform(tw);

                    result.put("Success", childId.toString() + " added to " + colId.toString());
                    r = Response.status(200).entity(result).build();

                }
            }
        } catch (Exception e) {
            log.info("Adding item " + childId + " to collection " + colId + " failed.");
            e.printStackTrace();
            result.put("Error", "Adding dataset " + item_id + " to collection " + id + " failed.");
            r = Response.status(500).entity(result).build();
        }

        return r;
    }

    @DELETE
    @Path("/{id}/{item_id}")
    public Response removeItemFromCollection(@PathParam("id") @Encoded String id, @PathParam("item_id") String item_id, @javax.ws.rs.core.Context HttpServletRequest request) {

        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        Response r = null;

        try {
            UriRef parentId = Resource.uriRef(URLDecoder.decode(id, "UTF-8"));
            UriRef childId = Resource.uriRef(URLDecoder.decode(item_id, "UTF-8"));

            ValidItem parent = new ValidItem(parentId, CollectionBeanUtil.COLLECTION_TYPE, userId);

            if (!parent.isValid()) {
                r = parent.getErrorResponse();
            } else {
                TripleMatcher tm = new TripleMatcher();
                tm.match(parentId, DcTerms.HAS_PART, childId);
                c.perform(tm);
                if (!tm.getResult().isEmpty()) {
                    if (isAccessible(userId, childId)) {
                        TripleWriter tw = new TripleWriter();
                        tw.remove(parentId, DcTerms.HAS_PART, childId);
                        c.perform(tw);
                        Map<String, Object> result = new LinkedHashMap<String, Object>();
                        result.put("Success", childId.toString() + " removed from " + parentId);
                        r = Response.status(200).entity(result).build();
                    } else {
                        Map<String, Object> result = new LinkedHashMap<String, Object>();
                        result.put("Error", childId.toString() + " not accessible.");
                        r = Response.status(403).entity(result).build();
                    }
                } else {
                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                    result.put("Error", childId.toString() + " not in " + parentId);
                    r = Response.status(404).entity(result).build();
                }

            }
        } catch (Exception e) {
            log.info("Error: Removing " + item_id + " from collection " + id + " failed.");
            e.printStackTrace();
            r = Response.status(500).entity("Failure when removing " + item_id + " from collection " + id + " .").build();
        }

        return r;
    }

    @POST
    @Path("")
    @Consumes("multipart/form-data")
    public Response uploadDir(MultipartFormDataInput input, @javax.ws.rs.core.Context HttpServletRequest request) {
        Context c = TupeloStore.getInstance().getContext();
        String dirName = "unknown";
        Map<Resource, Object> md = new LinkedHashMap<Resource, Object>();
        md.put(Rdf.TYPE, RestService.COLLECTION_TYPE);
        String uri = RestUriMinter.getInstance().mintUri(md); // Create collection uri
        ThingSession ts = c.getThingSession();
        Thing t = ts.newThing(Resource.uriRef(uri));
        UriRef creator = Resource.uriRef((String) request.getAttribute("userid"));

        try {
            //Get API input data
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

            for (Entry<String, List<InputPart>> parts : uploadForm.entrySet() ) {
                for (InputPart part : parts.getValue() ) {
                    if (parts.getKey().equals("collection")) {

                        if (log.isDebugEnabled()) {
                            listHeaders(part);
                        }

                        //get name...
                        dirName = part.getBodyAsString();

                        t.addType(CollectionBeanUtil.COLLECTION_TYPE);
                        t.addValue(Rdfs.LABEL, dirName);

                        //Next 4 are Bean specific
                        t.addType(Beans.STORAGE_TYPE_BEAN_ENTRY);
                        t.setValue(Beans.PROPERTY_VALUE_IMPLEMENTATION_CLASSNAME,
                                Resource.literal("edu.uiuc.ncsa.cet.bean.CollectionBean"));
                        t.setValue(Dc.IDENTIFIER, Resource.uriRef(uri));
                        t.setValue(Beans.PROPERTY_IMPLEMENTATION_MAPPING_SUBJECT,
                                Resource.uriRef("tag:cet.ncsa.uiuc.edu,2009:/mapping/" + CollectionBeanUtil.COLLECTION_TYPE));

                        t.setValue(Dc.TITLE, dirName);
                        t.addValue(DcTerms.DATE_CREATED, new Date());
                        t.setValue(Dc.CREATOR, creator);
                    } else {
                        //Should only be one val per Key...
                        addMetadataItem(t, parts.getKey(), part.getBodyAsString(), part.getMediaType(), creator);//Should only be one val per Key...
                    }

                }

            }
            t.save();
            ts.close();
        }

        catch (OperatorException oe) {
            log.error("Error uploading collection: ", oe);
            return Response.status(500)
                    .entity(uri).build();
        } catch (IOException ie) {
            log.error("Error uploading collection: ", ie);
            return Response.status(500)
                    .entity(uri).build();
        }

        // submit to extraction service
        try {
            TupeloStore.getInstance().extractPreviews(uri);
        } catch (Exception e) {
            log.info("Could not submit uri to extraction service, is it down?", e);
        }

        return Response.status(200)
                .entity(uri).build();
    }

    @GET
    @Path("/{id}/biblio")
    @Produces("application/json")
    public Response getCollectionBiblioAsJSON(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        return getBiblioByIdAsJSON(id, userId);

    }

    @GET
    @Path("/{id}/metadata")
    @Produces("application/json")
    public Response getCollectionMetadataAsJSON(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        return getItemMetadataAsJSON(id, userId);

    }

    /* Get collection(s) that have the specified metadata
     * type must be "uri" or "literal"
     */
    @GET
    @Path("/metadata/{pred}/{type}/{value}")
    @Produces("application/json")
    public Response getCollectionsWithMetadataAsJSON(@PathParam("pred") @Encoded String pred, @PathParam("type") @Encoded String type, @PathParam("value") @Encoded String value, @javax.ws.rs.core.Context HttpServletRequest request) {
        return getItemsByMetadata(pred, type, value, collectionBasics, request);
    }

    @POST
    @Path("/{id}/metadata")
    @Consumes("multipart/form-data")
    public Response uploadMetadata(@PathParam("id") @Encoded String id, MultipartFormDataInput input, @javax.ws.rs.core.Context HttpServletRequest request) {

        return super.uploadMetadata(id, input, request);
    }

}
