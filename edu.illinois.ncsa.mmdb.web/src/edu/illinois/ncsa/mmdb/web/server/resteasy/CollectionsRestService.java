package edu.illinois.ncsa.mmdb.web.server.resteasy;

/**
 * @author Rattanachai Ramathitima
 */

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

@Path("/collections")
public class CollectionsRestService {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(TagsRestService.class);

    @GET
    @Path("")
    public Response getCollection() {

        String result = "";
        // use getIDs to get String collection of Collections' URL
        try {
            Collection<String> ids = new CollectionBeanUtil(TupeloStore.getInstance().getBeanSession()).getIDs();
            for (String id : ids ) {
                // check whether this collection still exists
                if (new CollectionBeanUtil(TupeloStore.getInstance().getBeanSession()).get(id) != null) {
                    result += URLEncoder.encode(id, "UTF-8") + "<br>";
                }
            }

        } catch (Exception e1) {
            log.error("Error getting All CollectionBeans", e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting All CollectionBeans").build();
        }

        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("")
    @Produces("application/json")
    public Response getCollectionAsJSON() {

        HashSet<String> result = new HashSet<String>();

        // use getIDs to get String collection of Collections' URL
        try {
            Collection<String> ids = new CollectionBeanUtil(TupeloStore.getInstance().getBeanSession()).getIDs();
            for (String id : ids ) {
                // check whether this collection still exists
                if (new CollectionBeanUtil(TupeloStore.getInstance().getBeanSession()).get(id) != null) {
                    result.add(URLEncoder.encode(id, "UTF-8"));
                }
            }

        } catch (Exception e1) {
            log.error("Error getting All CollectionBeans", e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting All CollectionBeans").build();
        }

        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("/{id}")
    public Response getCollection(@PathParam("id") String id) {
        String result = "";

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
        }

        // use CollectionBeanUtil to get CollectionBean of corresponding id
        try {
            result = "Collection Id: " + URLEncoder.encode(id, "UTF-8") + "<br>";

            CollectionBean col = new CollectionBeanUtil(TupeloStore.getInstance().getBeanSession()).get(id);
            if (col == null || col.getTitle() == null) {
                return Response.status(404).entity("Collection: " + id + "Not Found").build();
            }
            result += "Title: " + col.getTitle() + "<br>";
            PersonBean creator = col.getCreator();
            if (creator != null) {
                result += "Creator: " + creator.getName() + "<br>";
            }

        } catch (Exception e1) {
            log.error("Error getting collectionBean for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting collectionBean for " + id).build();
        }

        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getCollectionAsJSON(@PathParam("id") String id) {

        Map<String, String> result = new HashMap<String, String>();

        try {
            result.put("Id", URLEncoder.encode(id, "UTF-8"));
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
        }

        // use CollectionBeanUtil to get CollectionBean of corresponding id
        try {
            CollectionBean col = new CollectionBeanUtil(TupeloStore.getInstance().getBeanSession()).get(id);
            if (col == null || col.getTitle() == null) {
                return Response.status(404).entity("Collection: " + id + "Not Found").build();
            }
            result.put("Title", col.getTitle());
            PersonBean creator = col.getCreator();
            if (creator != null) {
                result.put("Creator", creator.getName());
            }
        } catch (Exception e1) {
            log.error("Error getting collectionBean for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting collectionBean for " + id).build();
        }

        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("/{id}/datasets")
    public Response getDatasetsByCollection(@PathParam("id") String id) {

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
        }

        String result = "";

        Unifier u = new Unifier();

        // use CollectionBeanUtil to get CollectionBean of corresponding id
        // then use Unifier to search for datasets belonging to this collection
        try {
            CollectionBean col = new CollectionBeanUtil(TupeloStore.getInstance().getBeanSession()).get(id);
            if (col == null || col.getTitle() == null) {
                return Response.status(404).entity("Collection: " + id + "Not Found").build();
            }
            Resource collectionURI = Resource.uriRef(col.getUri());
            u.setColumnNames("datasets");
            u.addPattern(collectionURI, DcTerms.HAS_PART, "datasets");
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "datasets") ) {
                result += URLEncoder.encode(row.get(0).getString(), "UTF-8") + "<br>";
            }
        } catch (Exception e) {
            log.info("Retrieving datasets in collection failed.");
            e.printStackTrace();
            return Response.status(500).entity("Retrieving datasets in collection failed.").build();
        }

        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("/{id}/datasets")
    @Produces("application/json")
    public Response getDatasetsByCollectionAsJSON(@PathParam("id") String id) {

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
        }

        HashSet<String> result = new HashSet<String>();
        Unifier u = new Unifier();

        // use CollectionBeanUtil to get CollectionBean of corresponding id
        // then use Unifier to search for datasets belonging to this collection
        try {
            CollectionBean col = new CollectionBeanUtil(TupeloStore.getInstance().getBeanSession()).get(id);
            if (col == null || col.getTitle() == null) {
                return Response.status(404).entity("Collection: " + id + "Not Found").build();
            }
            Resource collectionURI = Resource.uriRef(col.getUri());
            u.setColumnNames("datasets");
            u.addPattern(collectionURI, DcTerms.HAS_PART, "datasets");
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "datasets") ) {
                result.add(URLEncoder.encode(row.get(0).getString(), "UTF-8"));
            }
        } catch (Exception e) {
            log.info("Retrieving datasets in collection failed.");
            e.printStackTrace();
            return Response.status(500).entity("Retrieving datasets in collection failed.").build();
        }

        return Response.status(200).entity(result).build();
    }

    @POST
    @Path("/{id}/datasets")
    public Response addDataseToCollection(@PathParam("id") String id, @FormParam("data_id") String data_id) {
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
        // check collection id and dataset id, then add dataset to collection using CollectionBeanUtil
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
                    cbu.addToCollection(col, input);
                    result += "adding " + data_id + " to " + id + "<br>";
                }
            }
        } catch (Exception e) {
            log.info("Adding dataset " + data_id + " to collection " + id + " failed.");
            e.printStackTrace();
            return Response.status(500).entity("Adding dataset " + data_id + " to collection " + id + " failed.").build();
        }
        return Response.status(200).entity(result).build();
    }

    @DELETE
    @Path("/{id}/{data_id}")
    public Response removeDatasetFromCollection(@PathParam("id") String id, @PathParam("data_id") String data_id) {
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
}
