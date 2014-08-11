/*******************************************************************************
 * Copyright 2014 University of Michigan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Tags;

import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.TagBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

/**
 * @author Jim Myers <myersjd@umich.edu>
 * 
 */
@Path("/tags")
public class TagsRestService extends ItemServicesImpl {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(TagsRestService.class);

    /**
     * List all tags used
     * 
     * @param request
     *            - used to get user Id
     * @return JSON list of tag strings
     */

    @GET
    @Path("")
    @Produces("application/json")
    public Response getTagAsJSON(@javax.ws.rs.core.Context HttpServletRequest request) {
        List<String> result = new ArrayList<String>();
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        //Permission to see pages means you can see metadata as well...
        //Should we even require any permission to know what tags exist?
        PermissionCheck p = new PermissionCheck(userId, Permission.VIEW_SYSTEM);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }
        try {
            Collection<TagBean> tags = new TagBeanUtil(TupeloStore.getInstance().getBeanSession()).getAll();
            for (TagBean tag : tags ) {
                result.add(tag.getTagString());
            }

        } catch (Exception e1) {
            log.error("Error getting all Tags");
            e1.printStackTrace();
            return Response.status(500).entity("Error getting all Tags").build();
        }
        return Response.status(200).entity(result).build();
    }

    /**
     * Get all datasets (non-deleted, that user can see) that are tagged with
     * the given tag
     * 
     * @param tag
     * @return basic metadata for each dataset in json-ld
     */

    @GET
    @Path("/{tag}/datasets")
    @Produces("application/json")
    public Response getDatasetsByTagAsJSON(@PathParam("tag") String tag, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        PermissionCheck p = new PermissionCheck(userId, Permission.VIEW_MEMBER_PAGES);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }

        return getMetadataByReverseRelationship((UriRef) TagEventBeanUtil.createTagUri(tag), Tags.TAGGED_WITH_TAG, datasetBasics, userId, Cet.DATASET);

    }

    /**
     * Get all collections (non-deleted, that user can see) that are tagged with
     * the given tag
     * 
     * @param tag
     * @return basic metadata for each collection in json-ld
     */

    @GET
    @Path("/{tag}/collections")
    @Produces("application/json")
    public Response getCollectionsByTagAsJSON(@PathParam("tag") String tag, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        PermissionCheck p = new PermissionCheck(userId, Permission.VIEW_MEMBER_PAGES);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }

        return getMetadataByReverseRelationship((UriRef) TagEventBeanUtil.createTagUri(tag), Tags.TAGGED_WITH_TAG, collectionBasics, userId, (UriRef) CollectionBeanUtil.COLLECTION_TYPE);

    }

    /**
     * Get all (non-deleted, that user can see) geo layers (collections or
     * datasets that have a WMSLayer annotation) that are tagged with
     * the given tag
     * 
     * @param tag
     * @return geo metadata for each item in json-ld
     */

    @GET
    @Path("/{tag}/layers")
    @Produces("application/json")
    public Response getGeoDatasetsByTagAsJSON(@PathParam("tag") String tagString, @javax.ws.rs.core.Context HttpServletRequest request) {
        return getItemsThatAreGeoLayers(Cet.DATASET, (UriRef) TagEventBeanUtil.createTagUri(tagString), request);

    }

}
