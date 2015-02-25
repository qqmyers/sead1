package edu.illinois.ncsa.mmdb.web.server.resteasy;

/**
 * @author Rattanachai Ramathitima
 * @author myersjd@umich.edu
 */

/*
 *  NB: For these services to work, -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true must be set on the server. This
 * potentially opens the door for attacks related to CVE-2007-0450 if any code on the server follows paths sent
 * in URLs.
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.jboss.resteasy.annotations.cache.NoCache;
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
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;

import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.rest.RestService;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.util.BeanFiller;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

/**
 * SEAD Data Service endpoints for collections. NB: For collection IDs
 * containing '/' characters, the ids must be urlencoded for
 * transmission.
 */
@Path("/collections")
@NoCache
public class CollectionsRestService extends ItemServicesImpl {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(CollectionsRestService.class);

    /**
     * Get top-level collections (those that are not sub-collections of any
     * other collection)
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
     * Get Basic metadata for {id} : ( Identifier, Title, Date, Uploaded By,
     * Abstract, Contact(s), Creator(s) )
     *
     * @param id
     *            - the URL-encoded ID of the collection
     *
     * @return - Basic Metadata as JSON-LD
     */
    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getCollectionAsJSON(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        return getMetadataById(id, collectionBasics, userId);
    }

    /**
     * Delete collection (Collection will be marked as deleted)
     *
     * @param id
     *            - the URL-encoded SEAD ID for the collection
     *
     * @return - success or failure message
     */
    @DELETE
    @Path("/{id}")
    @Produces("application/json")
    public Response markDatasetAsDeleted(@PathParam("id") String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        final UriRef creator = Resource.uriRef((String) request.getAttribute("userid"));

        PermissionCheck p = new PermissionCheck(creator, Permission.DELETE_COLLECTION);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }
        return markItemAsDeleted(id, (UriRef) CollectionBeanUtil.COLLECTION_TYPE, request);
    }

    /**
     * Get Datasets that are children of this collection
     *
     * @param id
     *            = the URL-encoded ID of the collection
     *
     * @return - ID and basic metadata for datasets in this collection
     */
    @SuppressWarnings("deprecation")
    @GET
    @Path("/{id}/datasets")
    @Produces("application/json")
    public Response getDatasetsByCollectionAsJSON(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        UriRef baseId = null;

        PermissionCheck p = new PermissionCheck(userId, Permission.VIEW_MEMBER_PAGES);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }
        try {
            id = URLDecoder.decode(id, "UTF-8");
            baseId = Resource.uriRef(id);
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + baseId.toString(), e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error decoding id: " + baseId.toString()).build();
        }

        return getMetadataByForwardRelationship(baseId, DcTerms.HAS_PART, datasetBasics, userId, Cet.DATASET);
    }

    /**
     * Add a dataset to this collection
     *
     * @param id
     *            - the URL-encoded ID of the collection
     * @param item_id
     *            - the URL-encoded ID of the dataset
     *
     * @return - success or failure message
     */
    @POST
    @Path("/{id}/datasets")
    public Response addDatasetToCollection(@PathParam("id") @Encoded String id, @FormParam("dataset_id") String item_id, @javax.ws.rs.core.Context HttpServletRequest request) {
        return addItemToCollection(id, item_id, Cet.DATASET, request);
    }

    /**
     * Get Collections that are children (sub-collections) of this collection
     *
     * @param id
     *            = the URL-encoded ID of the collection
     *
     * @return - ID and basic metadata for subcollections.
     */
    @GET
    @Path("/{id}/collections")
    @Produces("application/json")
    public Response getCollectionsByCollectionAsJSON(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        UriRef baseId = null;
        try {

            PermissionCheck p = new PermissionCheck(userId, Permission.VIEW_MEMBER_PAGES);
            if (!p.userHasPermission()) {
                return p.getErrorResponse();
            }
            id = URLDecoder.decode(id, "UTF-8");
            baseId = Resource.uriRef(id);
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + baseId.toString(), e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error decoding id: " + baseId.toString()).build();
        }
        return getMetadataByForwardRelationship(baseId, DcTerms.HAS_PART, collectionBasics, userId, (UriRef) CollectionBeanUtil.COLLECTION_TYPE);
    }

    /**
     * Add a collection to this collection (as a sub-collection)
     *
     * @param id
     *            - the URL-encoded ID of the parent collection
     * @param item_id
     *            - the URL-encoded ID of the sub-collection
     *
     * @return - success or failure message
     */
    @POST
    @Path("/{id}/collections")
    public Response addCollectionToCollection(@PathParam("id") @Encoded String id, @FormParam("collection_id") String item_id, @javax.ws.rs.core.Context HttpServletRequest request) {
        return addItemToCollection(id, item_id, CollectionBeanUtil.COLLECTION_TYPE, request);
    }

    private Response addItemToCollection(String id, String item_id, Resource type, @javax.ws.rs.core.Context HttpServletRequest request) {

        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        PermissionCheck p = new PermissionCheck(userId, Permission.ADD_RELATIONSHIP);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }

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

    /**
     * Remove a dataset or collection from a collection
     *
     * @param id
     *            - the URL-encoded ID of the parent collection
     * @param item_id
     *            - the URL-encoded ID of the child dataset/collection to be
     *            removed
     *
     * @return - success or failure message
     */
    @DELETE
    @Path("/{id}/{item_id}")
    public Response removeItemFromCollection(@PathParam("id") @Encoded String id, @PathParam("item_id") String item_id, @javax.ws.rs.core.Context HttpServletRequest request) {

        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        Response r = null;

        PermissionCheck p = new PermissionCheck(userId, Permission.ADD_RELATIONSHIP);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }
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

    /**
     * Get tags associated with this collection
     *
     * @param id
     *            - the URL-encoded ID of the collection
     *
     * @return - JSON array of tags
     */
    @GET
    @Path("/{id}/tags")
    @Produces("application/json")
    public Response getCollectionTagsByIdAsJSON(@PathParam("id") String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        return getItemTagsByIdAsJSON(id, CollectionBeanUtil.COLLECTION_TYPE, request);
    }

    /**
     * Add tags to this collection
     *
     * @param id
     *            - the URL-encoded ID of the collection
     * @param tags
     *            - comma separated list of tags
     * @return - JSON array of tags
     */
    @POST
    @Path("/{id}/tags")
    public Response addTagsToCollection(@PathParam("id") String id, @FormParam("tags") String tags, @javax.ws.rs.core.Context HttpServletRequest request) {
        return addTagsToItem(id, tags, (UriRef) CollectionBeanUtil.COLLECTION_TYPE, request);
    }

    /**
     * Remove some tags associated with this collection
     *
     * @param id
     *            - the URL-encoded ID of the collection
     * @param tags
     *            - comma separated list of tags
     * @return - success or failure message
     */
    @DELETE
    @Path("/{id}/tags/{tags}")
    public Response removeTagFromDataset(@PathParam("id") String id, @PathParam("tags") String tags, @javax.ws.rs.core.Context HttpServletRequest request) {
        return deleteTagsFromItem(id, tags, (UriRef) CollectionBeanUtil.COLLECTION_TYPE, request);
    }

    /**
     * Create a new collection including metadata and list of children (datasets
     * and/or subcollections). Basic metadata
     * are generated from the dir stats and session username
     * (dc:creator/uploader).
     *
     * Note: Adding metadata via this method should be done with awareness that
     * it does not perform all 'side effects', e.g. while new predicates are
     * automatically added to the list of extracted or user
     * metadata, there is no way through this endpoint to give them
     * human-friendly labels.
     *
     * @param input
     *            - multipart form data including "collection" part specifying
     *            the
     *            collection name and additional String predicate/value pairs as
     *            other parts for other
     *            metadata. The value strings will be interpreted based on a
     *            submitted media/mime type:
     *            text/plain (default) : a literal value
     *            text/uri-list : a URI
     *
     * @return - success/failure message
     */
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

        PermissionCheck p = new PermissionCheck(creator, Permission.ADD_COLLECTION);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }

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

                        BeanFiller.fillCollectionBean(t, dirName, creator, new Date());

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

    /**
     * Get Bibliographic metadata for {id} : (Identifier, License, Rights
     * Holder, Rights,
     * Creation Date, Size, Label, Mimetype, Description(s), Title, Uploaded By,
     * Abstract,
     * Contact(s),Creator(s), Publication Date )
     *
     * @param id
     *            - the URL-encoded ID of the collection
     * @return - Biblio Metadata as JSON-LD
     */
    @GET
    @Path("/{id}/biblio")
    @Produces("application/json")
    public Response getCollectionBiblioAsJSON(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        return getBiblioByIdAsJSON(id, userId);

    }

    /**
     * Get unique metadata (excluding extracted metadata) for {id} :
     * (Basic/biblio + user-added metadata )
     *
     * @param id
     *            - the URL-encoded ID of the collection
     * @return - Metadata as JSON-LD
     */
    @GET
    @Path("/{id}/unique")
    @Produces("application/json")
    public Response getCollectionUniqueMetadataAsJSON(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        //Note - don't currently have extractors that work on collections so this currently just returns
        //the same results as /metadata minus the triples about extractor run start/end times
        return getItemMetadataAsJSON(id, userId, false);

    }

    /**
     * Get all metadata for {id} : (Basic/biblio + user-added metadata and
     * extracted metadata)
     *
     * @param id
     *            - the URL-encoded ID of the collection
     * @return - Metadata as JSON-LD
     */
    @GET
    @Path("/{id}/metadata")
    @Produces("application/json")
    public Response getCollectionMetadataAsJSON(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        return getItemMetadataAsJSON(id, userId, true);

    }

    /**
     * Get collections(s) that have the specified metadata
     *
     * @param pred
     *            - URL-encoded predicate
     * @param type
     *            - the type of the value (must be "uri" or "literal")
     * @param value
     *            - the URL-encoded value
     *
     * @return - the list of matching collections, with their basic metadata, as
     *         JSON-LD
     */
    @GET
    @Path("/metadata/{pred}/{type}/{value}")
    @Produces("application/json")
    public Response getCollectionsWithMetadataAsJSON(@PathParam("pred") @Encoded String pred, @PathParam("type") @Encoded String type, @PathParam("value") @Encoded String value, @javax.ws.rs.core.Context HttpServletRequest request) {
        return getItemsByMetadata(pred, type, value, (UriRef) CollectionBeanUtil.COLLECTION_TYPE, request);
    }

    /**
     * Add metadata to collection.
     *
     * Note: New predicates will be added as viewable user metadata. Some
     * predicates (related to
     * license, rightsHolder, rights, title, uploaded by, identifier, dates,
     * size, label) cannot be changed through this method.
     *
     * @param id
     *            - the URL-encoded ID of the collection
     * @param input
     *            - multipart form data specifying the String predicate/value
     *            pairs for the
     *            metadata. The value strings will be interpreted based on a
     *            submitted media/mime type:
     *            text/plain (default) : a literal value
     *            text/uri-list : a URI
     *
     * @result - success/failure message
     */
    @POST
    @Path("/{id}/metadata")
    @Consumes("multipart/form-data")
    public Response uploadMetadata(@PathParam("id") @Encoded String id, MultipartFormDataInput input, @javax.ws.rs.core.Context HttpServletRequest request) {

        return super.uploadMetadata(id, input, request);
    }

}
