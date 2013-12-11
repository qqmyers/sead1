/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.TagBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

/**
 * @author Luigi Marini <lmarini@ncsa.illinois.edu>
 * 
 */
@Path("/tags")
public class TagsRestService {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(TagsRestService.class);

    @POST
    @Path("/object/{id}")
    public Response tagDataset(@PathParam("id") String id, @Context HttpServletRequest request, @FormParam("tags") String tags) {
        TagEventBeanUtil teb = new TagEventBeanUtil(TupeloStore.getInstance().getBeanSession());

        String userid = request.getAttribute("userid").toString();
        try {
            teb.addTags(id, userid, tags);
            return Response.status(200).entity("OK").build();
        } catch (OperatorException e) {
            log.error("Error adding tags to " + id, e);
            return Response.status(500).entity("Error adding tags : " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/object/{id}")
    public Response getTags(@PathParam("id") String id) {
        TagEventBeanUtil teb = new TagEventBeanUtil(TupeloStore.getInstance().getBeanSession());
        try {
            Set<String> tags = teb.getTags(id);
            return Response.status(200).entity(StringUtils.join(tags, ",")).build();
        } catch (OperatorException e) {
            log.error("Error getting tags for " + id, e);
            return Response.status(500).entity("Error getting tags for : " + e.getMessage()).build();
        }
    }

    @GET
    @Path("")
    public Response getTag() {

        String result = "";

        try {
            Collection<TagBean> tags = new TagBeanUtil(TupeloStore.getInstance().getBeanSession()).getAll();
            for (TagBean tag : tags ) {
                result += tag.getTagString() + "<br>";
            }
        } catch (Exception e1) {
            log.error("Error getting All TagBeans");
            e1.printStackTrace();
            return Response.status(500).entity("Error getting All TagBeans").build();
        }
        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("")
    @Produces("application/json")
    public Response getTagAsJSON() {
        List<String> result = new ArrayList<String>();
        try {
            Collection<TagBean> tags = new TagBeanUtil(TupeloStore.getInstance().getBeanSession()).getAll();
            for (TagBean tag : tags ) {
                result.add(tag.getTagString());
            }

        } catch (Exception e1) {
            log.error("Error getting All TagBeans");
            e1.printStackTrace();
            return Response.status(500).entity("Error getting All TagBeans").build();
        }
        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("/{tag}")
    public Response getTag(@PathParam("tag") String tag) {

        String result = "Getting tag: " + tag;

        return Response.status(200).entity(result).build();

    }

    @GET
    @Path("/{tag}/datasets")
    public Response getDatasetsByTag(@PathParam("tag") String tag) {

        String result = "";

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

        HashSet<DatasetBean> datasets = new HashSet<DatasetBean>();

        Unifier uf = new Unifier();
        uf.addPattern("dataset", Tags.TAGGED_WITH_TAG, TagEventBeanUtil.createTagUri(tag));
        uf.addPattern("dataset", Rdf.TYPE, Cet.DATASET);
        uf.setColumnNames("dataset");

        try {
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(uf, "dataset") ) {
                if (row.get(0) != null) {
                    Resource id = row.get(0);
                    datasets.add(dbu.get(id));
                    result += URLEncoder.encode(id.toString(), "UTF-8") + "<br>";
                }
            }
        } catch (OperatorException e1) {
            log.error("Error retrieving datasets with tag " + tag);
            e1.printStackTrace();
            return Response.status(500).entity("Error retrieving datasets with tag " + tag).build();
        } catch (UnsupportedEncodingException e1) {
            log.error("Error encoding datasets' url for " + tag, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error encoding datasets' url for " + tag).build();
        }

        log.debug("Found " + datasets.size() + " datasets with tag '"
                + tag + "'");

        return Response.status(200).entity(result).build();

    }

    @GET
    @Path("/{tag}/datasets")
    @Produces("application/json")
    public Response getDatasetsByTagAsJSON(@PathParam("tag") String tag) {

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

        HashSet<DatasetBean> datasets = new HashSet<DatasetBean>();

        Unifier uf = new Unifier();
        uf.addPattern("dataset", Tags.TAGGED_WITH_TAG, TagEventBeanUtil.createTagUri(tag));
        uf.addPattern("dataset", Rdf.TYPE, Cet.DATASET);
        uf.setColumnNames("dataset");

        try {
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(uf, "dataset") ) {
                if (row.get(0) != null) {
                    Resource id = row.get(0);
                    datasets.add(dbu.get(id));
                }
            }
        } catch (OperatorException e1) {
            log.error("Error retrieving datasets with tag " + tag);
            e1.printStackTrace();
            return Response.status(500).entity("Error retrieving datasets with tag " + tag).build();
        }

        log.debug("Found " + datasets.size() + " datasets with tag '"
                + tag + "'");

        TestClass test = new TestClass();
        test.setName("test");

        return Response.status(200).entity(datasets).build();

    }

    class TestClass {

        private String name;

        public TestClass() {
            // TODO Auto-generated constructor stub
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
