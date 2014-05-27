package edu.illinois.ncsa.mmdb.web.server.resteasy;

/**
 * @author Rattanachai Ramathitima
 * @author myersjd@umich.edu
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Beans;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.rest.RestService;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/*Service endpoints for collections. NB: For collection IDs containing '/' characters, the ids must be urlencoded for 
 * transmission AND -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true must be set on the server. This 
 * potentially opens the door for attacks related to CVE-2007-0450 if any code on the server follows paths sent 
 * in URLs.
 */

@Path("/collections")
public class CollectionsRestService extends IngestService {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(CollectionsRestService.class);

    /*Get top-level collections*/
    @GET
    @Path("")
    @Produces("application/json")
    public Response getCollectionAsJSON() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Unifier uf = new Unifier();
        uf.addColumnName("parent");

        populateUnifier("collection", uf, collectionBasics);
        //Must come after to avoid having no result of row 0, column 0 would be null
        uf.addPattern("parent", DcTerms.HAS_PART, "collection", true);
        List<String> names = uf.getColumnNames();
        names.remove("parent");
        try {
            TupeloStore.getInstance().unifyExcludeDeleted(uf, "collection");
        } catch (OperatorException e) {
            log.error("Error listing top-level collections: " + e.getMessage());
            return Response.status(500).entity("Error listing top-level collections").build();
        }
        org.tupeloproject.util.Table<Resource> table = uf.getResult();

        try {
            buildResultMap(uf, collectionBasics, names, true, new FilterCallback() {
                @Override
                public boolean include(Tuple<Resource> t) {
                    if (t.get(0) == null) {
                        return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            //Nothing found
            return Response.status(404).entity("Collections Not Found").build();
        }
        return Response.status(200).entity(result).build();
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
    public Response addDatasetToCollection(@PathParam("id") @Encoded String id, @FormParam("data_id") String data_id) {
        String result = "";
        String colId = null;
        String childId = null;
        try {
            colId = URLDecoder.decode(id, "UTF-8");
            childId = URLDecoder.decode(data_id, "UTF-8");

            BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
            DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
            CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);
            // check collection id and dataset id, then add dataset to collection using CollectionBeanUtil

            CollectionBean col = cbu.get(colId);
            if (col == null || col.getTitle() == null) {
                return Response.status(404).entity("Collection " + colId + "Not Found.").build();
            }
            else {
                DatasetBean dataset = dbu.get(childId);
                if (dataset == null || dataset.getTitle() == null) {
                    return Response.status(404).entity("Dataset " + childId + "Not Found.").build();
                } else {
                    Collection<Resource> input = new HashSet<Resource>();
                    input.add(Resource.uriRef(childId));
                    cbu.addToCollection(col, input);
                    result += "adding " + childId + " to " + colId + "<br>";
                }
            }
        } catch (Exception e) {
            log.info("Adding dataset " + childId + " to collection " + colId + " failed.");
            e.printStackTrace();
            return Response.status(500).entity("Adding dataset " + data_id + " to collection " + id + " failed.").build();
        }
        return Response.status(200).entity(result).build();
    }

    @DELETE
    @Path("/{id}/{data_id}")
    public Response removeDatasetFromCollection(@PathParam("id") @Encoded String id, @PathParam("data_id") String data_id) {
        String result = "";
        try {
            id = URLDecoder.decode(id, "UTF-8");
            data_id = URLDecoder.decode(data_id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
        }

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
        CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);

        // check collection id and dataset id, then remove dataset from collection using CollectionBeanUtil
        try {
            CollectionBean col = cbu.get(id);
            if (col == null || col.getTitle() == null) {
                return Response.status(404).entity("Collection " + id + "Not Found.").build();
            }
            else {
                DatasetBean dataset = dbu.get(data_id);
                if (dataset == null || dataset.getTitle() == null) {
                    return Response.status(404).entity("Dataset " + data_id + "Not Found.").build();
                } else {
                    Collection<Resource> input = new HashSet<Resource>();
                    input.add(Resource.resource(data_id));
                    cbu.removeFromCollection(col, input);
                    result += "removing " + data_id + " from " + id + "<br>";
                }
            }
        } catch (Exception e) {
            log.info("Removing dataset " + data_id + " from collection " + id + " failed.");
            e.printStackTrace();
            return Response.status(500).entity("Removing dataset " + data_id + " from collection " + id + " failed.").build();
        }

        return Response.status(200).entity(result).build();
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
                        t.setValue(Dc.IDENTIFIER, uri);
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
        Response r = null;
        Resource base = null;
        Resource relationship = null;
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        try {
            pred = URLDecoder.decode(pred, "UTF-8");
            value = URLDecoder.decode(value, "UTF-8");
            relationship = Resource.uriRef(pred);
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + pred + " or " + value, e1);
        }
        if (type.equals("uri")) {
            base = Resource.uriRef(value);
            if ((base != null) && isAccessible(userId, (UriRef) base)) {

                r = getMetadataByReverseRelationship((UriRef) base, relationship, collectionBasics, userId);
            } else {
                return Response.status(403).entity("Related Item not accessible").build();
            }
        } else if (type.equals("literal")) {
            r = getMetadataByReverseRelationship(value, relationship, collectionBasics, userId);
        }
        if (r == null) {
            r = Response.status(500).entity("Error getting information about " + base.toString()).build();

        }
        return r;

    }

    @POST
    @Path("/{id}/metadata")
    @Consumes("multipart/form-data")
    public Response uploadMetadata(@PathParam("id") @Encoded String id, MultipartFormDataInput input, @HeaderParam("Authorization") String auth) {
        return super.uploadMetadata(id, input, auth);
    }

}
