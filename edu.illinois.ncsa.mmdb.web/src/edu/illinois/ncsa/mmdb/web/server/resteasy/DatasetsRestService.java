package edu.illinois.ncsa.mmdb.web.server.resteasy;

/**
 * @author Rattanachai Ramathitima
 * @author myersjd@umich.edu
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.tupeloproject.kernel.BlobFetcher;
import org.tupeloproject.kernel.BlobWriter;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;
import org.tupeloproject.util.UnicodeTranscoder;

import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AddToCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.util.BeanFiller;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

/**
 * SEAD Dataset Service Endpoints
 * These services require user authentication. The simplest mechanism to do this
 * is to
 * call /api/authenticate with an Oauth2 token (e.g. from Google) which will set
 * a session
 * cookie that can be returned with all calls here. (/api/logout will invalidate
 * session).
 * Credentials can also be sent with each call.
 *
 */
@Path("/datasets")
@NoCache
public class DatasetsRestService extends ItemServicesImpl {

    private static String[] CopyIgnore  = new String[] {
                                        "http://cet.ncsa.uiuc.edu/2007/mmdb/isViewedBy",
                                        "http://cet.ncsa.uiuc.edu/2007/mmdb/isLikedDislikedBy",
                                        "http://cet.ncsa.uiuc.edu/2007/role/hasRole",
                                        "http://cet.ncsa.uiuc.edu/2007/foaf/context/password",
                                        };
    private static String[] CopyTriples = new String[] {
                                        "http://cet.ncsa.uiuc.edu/2007/hasPreview",
                                        "http://www.holygoat.co.uk/owl/redwood/0.1/tags/tag",
                                        "http://www.holygoat.co.uk/owl/redwood/0.1/tags/associatedTag",
                                        "http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag",
                                        "tag:tupeloproject.org,2006:/2.0/gis/hasGeoPoint",
                                        "http://purl.org/dc/elements/1.1/creator",
                                        "http://cet.ncsa.uiuc.edu/2007/pyramid/tiles",
                                        "http://cet.ncsa.uiuc.edu/2007/hasExtent",
                                        };

    private static String[] CopyBytes   = new String[] {
                                        "http://cet.ncsa.uiuc.edu/2007/hasPreview",
                                        "http://cet.ncsa.uiuc.edu/2007/pyramid/tiles",
                                        };

    private static String[] CopyNoBytes = new String[] {
                                        "http://cet.ncsa.uiuc.edu/2007/ImagePyramid",
                                        "http://cet.ncsa.uiuc.edu/2007/PreviewGeoserver",
                                        };

    /** Commons logging **/
    private static Log      log         = LogFactory.getLog(DatasetsRestService.class);

    /**
     * Copy Dataset from another SEAD repository
     *
     * @param url
     *            - the URL to copy from (the SEAD URL for the dataset page)
     * @returns - success (200) or failure (500)
     */

    @POST
    @Path("/copy")
    public Response copyDataset(@FormParam("url") String surl, @javax.ws.rs.core.Context HttpServletRequest request) {
        try {
            Map<String, Object> result = new LinkedHashMap<String, Object>();

            final URL url = new URL(surl);

            if (!url.getRef().startsWith("dataset?id=")) {
                throw (new Exception("URL does not appear to be medici URL"));
            }
            final UriRef creator = Resource.uriRef((String) request.getAttribute("userid"));

            PermissionCheck p = new PermissionCheck(creator, Permission.UPLOAD_DATA);
            if (!p.userHasPermission()) {
                return p.getErrorResponse();
            }
            new Thread(new Runnable() {
                public void run() {
                    try {
                        String id = url.getRef().substring(11);
                        URL endpoint = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + "resteasy/sparql");

                        TripleWriter tw = new TripleWriter();

                        tw.add(Resource.uriRef(id), Resource.uriRef("http://www.w3c.org/ns/prov#hadPrimarySource"), Resource.uriRef(url.toURI()));

                        Set<String> copyURI = new HashSet<String>();
                        Set<String> ignoreURI = new HashSet<String>();
                        copyURI.add(id);
                        copyTriples(endpoint, id, tw, copyURI, ignoreURI);
                        copyURI.removeAll(ignoreURI);

                        TupeloStore.getInstance().getContext().perform(tw);

                        for (String s : copyURI ) {
                            endpoint = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + "resteasy/datasets/" + URLEncoder.encode(s, "UTF8") + "/file?" + ConfigurationKey.RemoteAPIKey.getPropertyKey() + "=" + URLEncoder.encode(TupeloStore.getInstance().getConfiguration(ConfigurationKey.RemoteAPIKey), "UTF8"));
                            copyData(endpoint, s);
                        }
                        log.info("Copied dataset [" + url.toExternalForm() + "], " + tw.getToAdd().size() + " triples and " + copyURI.size() + " blobs");
                    } catch (Exception e) {
                        log.error("Error copying dataset from " + url.toExternalForm(), e);
                    }
                }
            }).start();

            return Response.status(200).entity("Object is being copied.").build();
        } catch (Exception e) {
            log.error("Error copying dataset from " + surl, e);
            return Response.status(500).entity("Error copying dataset [" + e.getMessage() + "]").build();
        }
    }

    /**
     * Import Data from any URL. Unless something is known about a given source,
     * this simply copies the bytes as a new
     * dataset and adds metadata linking back to the source URL.
     *
     * @param surl
     *            - the URL to import from
     * @returns - success (200) or failure (500)
     */

    @POST
    @Path("/import/{surl}")
    public Response importDataset(@PathParam("surl") String surl, @javax.ws.rs.core.Context HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        try {
            String decodedUrl = URLDecoder.decode(surl, "UTF-8");
            log.debug("Importing: " + decodedUrl);
            final URL url = new URL(decodedUrl);
            final UriRef creator = Resource.uriRef((String) request.getAttribute("userid"));

            PermissionCheck p = new PermissionCheck(creator, Permission.UPLOAD_DATA);
            if (!p.userHasPermission()) {
                return p.getErrorResponse();
            }

            final String id = RestUriMinter.getInstance().mintUri(null); // Create dataset uri
            final ThingSession ts = c.getThingSession();

            new Thread(new Runnable() {
                public void run() {
                    try {

                        UriRef s = Resource.uriRef(id);
                        Thing t = ts.newThing(s);

                        InputStream is = getDataStream(url);
                        String path = url.getPath();
                        if (path.endsWith("/")) {
                            path = path.substring(0, path.length() - 1);
                        }
                        String fileName = path.substring(path.lastIndexOf('/') + 1);
                        // mimetype
                        String contentType = TupeloStore.getInstance().getMimeMap().getContentTypeFor(fileName);
                        // update context with new mime-type potentially
                        TupeloStore.getInstance().getMimeMap().checkMimeType(contentType);

                        try {
                            BeanFiller.fillDataBean(c, t, fileName, contentType, creator, new Date(), is);

                            t.addValue(Resource.uriRef("http://www.w3.org/ns/prov#hadPrimarySource"), Resource.uriRef(url.toExternalForm()));
                        } catch (NoSuchAlgorithmException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        //Add as top-level item
                        ts.addValue(AddToCollectionHandler.TOP_LEVEL, AddToCollectionHandler.INCLUDES, s);
                        t.save();
                        ts.close();

                        log.debug("Created dataset from " + url.getPath());
                    } catch (IOException io) {
                        log.warn("IO error retrieving " + url.toExternalForm() + " for dataset " + id);
                        if (log.isDebugEnabled()) {
                            io.printStackTrace();
                        }
                    } catch (OperatorException e) {
                        log.warn("OperatorError retrieving " + url.toExternalForm() + " for dataset " + id);
                        if (log.isDebugEnabled()) {
                            e.printStackTrace();
                        }
                    }

                }

                private InputStream getDataStream(URL url) throws IOException {

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");

                    boolean redirect = false;

                    // normally, 3xx is redirect
                    int status = conn.getResponseCode();
                    if (status != HttpURLConnection.HTTP_OK) {
                        if (status == HttpURLConnection.HTTP_MOVED_TEMP
                                || status == HttpURLConnection.HTTP_MOVED_PERM
                                || status == HttpURLConnection.HTTP_SEE_OTHER) {
                            redirect = true;
                        }
                    }

                    if (redirect) {

                        // get redirect url from "location" header field
                        String newUrl = conn.getHeaderField("Location");

                        // If the first site has set a cookie, pass it on in the redirect
                        String cookies = conn.getHeaderField("Set-Cookie");

                        // open the new connection
                        conn = (HttpURLConnection) new URL(newUrl).openConnection();
                        conn.setRequestProperty("Cookie", cookies);
                        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                        status = conn.getResponseCode();

                    }
                    if (status == HttpURLConnection.HTTP_OK) {
                        return conn.getInputStream();
                    } else {
                        throw new IOException("Remote server response for " + conn.getURL().toExternalForm() + " : " + status);
                    }
                }
            }).start();
            //TO be clear - success means we've done the job of making the call to the remote URL to get the data, not that that call(s) have succeeded.
            //FixMe - could add the feedback service developed for the internal upload (showing state of the upload as it progresses.
            result.put("Success", "Object is imported as: " + id);
            return Response.status(200).entity(result).build();
        } catch (Exception e) {
            log.error("Error importing dataset from " + surl, e);
            result.put("Failure", "Error importing dataset [" + e.getMessage() + "]");
            return Response.status(500).entity(result).build();
        }
    }

    /**
     * Get top-level datasets
     *
     * @return List of top-level datasets by ID with basic metadata as JSON-LD
     */
    @GET
    @Path("")
    @Produces("application/json")
    public Response getDatasetsAsJSON(@javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        return getTopLevelItems(Cet.DATASET, datasetBasics, userId);
    }

    /**
     * Get recent datasets (latest 100 datasets are searched)
     *
     * @return List of recent datasets that have not been deleted by ID with
     *         basic metadata as JSON-LD
     */
    @GET
    @Path("/recent")
    @Produces("application/json")
    public Response getRecentDatasetsAsJSON(@javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        return getLatestItems(Cet.DATASET, datasetBasics, userId, "Date");
    }

    /**
     * Get all datasets
     *
     * @return List of all datasets by ID with basic metadata as JSON-LD
     */
    /*
    @GET
    @Path("/all")
    @Produces("application/json")
    public Response getAllDatasetsAsJSON(@javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

    //Build output
    StreamingOutput streamingResult = new StreamingOutput() {

        @Override
        public void write(OutputStream output) throws IOException, WebApplicationException {
            IOUtils.copyLarge(bf.getInputStream(), output);
        }
    };

    r = Response.ok(streamingResult, mimetype).header("content-disposition", "attachement; filename = " + title).build();
    }

    */

    /**
     * Get dataset content
     *
     * @param id
     *            - the URL encoded SEAD ID for the desired dataset
     * @return the bytes associated with this ID (i.e. the 'file' contents)
     */
    @GET
    @Path("/{id}/file")
    public Response getDatasetBlob(@PathParam("id") String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        return getDataFile(id, request);
    }

    public static Response getDataFile(String id, HttpServletRequest request) {
        Response r = null;
        try {
            final UriRef creator = Resource.uriRef((String) request.getAttribute("userid"));

            PermissionCheck p = new PermissionCheck(creator, Permission.DOWNLOAD);
            log.debug("User can getCombinedContext(logo/banner: " + p.userHasPermission());
            boolean dataInHeader = dataUsedInHeader(id);
            log.debug("File is logo/banner: " + dataInHeader);
            if (!p.userHasPermission() && !dataInHeader) {

                return p.getErrorResponse();
            }

            UriRef itemId = Resource.uriRef(URLDecoder.decode(id, "UTF-8"));
            boolean valid = false;
            if (dataInHeader || (request.getAttribute("token") != null)) {
                valid = true;
            } else {
                //only need user to check permissions - may be null for token exists case (above)
                UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
                ValidItem item = new ValidItem(itemId, Cet.DATASET, userId);
                if (!item.isValid()) {
                    r = item.getErrorResponse();
                } else {
                    valid = true;
                }
            }
            if (valid) {

                r = getDataFileBytes(itemId);

            }

        } catch (Exception e) {
            e.printStackTrace();
            log.debug(e.getMessage());
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("Error", "Server error: " + e.getMessage() + " while retrieving content for " + id);
            r = Response.status(500).entity(result).build();
        }
        return r;
    }

    protected static Response getDataFileBytes(UriRef itemId) {
        Response r = null;
        //Get file metadata
        Unifier uf = new Unifier();
        uf.addPattern(itemId, Dc.FORMAT, "mimetype");
        uf.addPattern(itemId, Dc.TITLE, "title");
        uf.setColumnNames("mimetype", "title");
        try {
            TupeloStore.getInstance().getContext().perform(uf);
            Tuple<Resource> metadata = uf.getResult().iterator().next();
            String mimetype = metadata.get(0).toString();
            String title = metadata.get(1).toString();

            //Get Bytes
            final BlobFetcher bf = new BlobFetcher();
            bf.setSubject(itemId);
            TupeloStore.getInstance().getContext().perform(bf);

            //Build output
            StreamingOutput streamingResult = new StreamingOutput() {

                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    IOUtils.copyLarge(bf.getInputStream(), output);
                }
            };

            r = Response.ok(streamingResult, mimetype).header("content-disposition", "attachement; filename = " + title).build();
        } catch (Exception e) {
            e.printStackTrace();
            log.debug(e.getMessage());
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("Error", "Server error: " + e.getMessage() + " while retrieving content for " + itemId.toString());
            r = Response.status(500).entity(result).build();
        }
        return r;

    }

    //This allows the raw bytes of any dataset selected as the header logo or background to be downloaded without additional permissions
    // (i.e. so anonymous can see the logo/background in spaces that don't let anonymous see dataset pages/download data).
    private static boolean dataUsedInHeader(String id) {
        Unifier uf = new Unifier();
        uf.addPattern("background", MMDB.CONFIGURATION_KEY, TupeloStore.getConfigurationKeyURI(ConfigurationKey.ProjectHeaderBackground));
        uf.addPattern("background", MMDB.CONFIGURATION_VALUE, "bimage");
        uf.setColumnNames("background", "bimage");
        try {
            TupeloStore.getInstance().getContext().perform(uf);
            Tuple<Resource> tuple = uf.getFirstRow();
            if (tuple != null) {
                if ((tuple.get(1).toString()).contains(id)) {
                    return true;
                }
            }
        } catch (OperatorException e) {
            log.error("Unable to retrieve HeaderBackground configuration", e);
        }
        uf = new Unifier();
        uf.addPattern("logo", MMDB.CONFIGURATION_KEY, TupeloStore.getConfigurationKeyURI(ConfigurationKey.ProjectHeaderLogo));
        uf.addPattern("logo", MMDB.CONFIGURATION_VALUE, "limage");
        uf.setColumnNames("logo", "limage");
        try {
            TupeloStore.getInstance().getContext().perform(uf);
            Tuple<Resource> tuple = uf.getFirstRow();
            if (tuple != null) {
                if ((tuple.get(1).toString()).contains(id)) {
                    return true;
                }
            }
        } catch (OperatorException e) {
            log.error("Unable to retrieve HeaderLogo configuration", e);
        }
        return false;
    }

    /**
     * Get basic metadata for the given dataset ( Identifier, Title, Date,
     * Uploaded By, Size(Bytes), Mimetype, Creator(s) )
     *
     * @param id
     *            - the URL encoded ID of the dataset
     *
     * @return - the basic metadata for this dataset as JSON-LD
     */
    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getDatasetByIdAsJSON(@PathParam("id") String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        return getMetadataById(id, datasetBasics, userId);
    }

    /**
     * Delete dataset (Dataset will be marked as deleted)
     *
     * @param id
     *            - the URL-encoded SEAD ID for the dataset
     *
     * @return - success or failure message
     */
    @DELETE
    @Path("/{id}")
    @Produces("application/json")
    public Response markDatasetAsDeleted(@PathParam("id") String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        final UriRef creator = Resource.uriRef((String) request.getAttribute("userid"));

        PermissionCheck p = new PermissionCheck(creator, Permission.DELETE_DATA);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }
        return markItemAsDeleted(id, Cet.DATASET, request);
    }

    /**
     * Get tags associated with this dataset
     *
     * @param id
     *            - the URL-encoded ID of the dataset
     *
     * @return - JSON array of tags
     */
    @GET
    @Path("/{id}/tags")
    @Produces("application/json")
    public Response getDatasetTagsByIdAsJSON(@PathParam("id") String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        return getItemTagsByIdAsJSON(id, Cet.DATASET, request);
    }

    /**
     * Add tags to this dataset
     *
     * @param id
     *            - the URL-encoded ID of the dataset
     * @param tags
     *            - comma separated list of tags
     * @return - JSON array of tags
     */
    @POST
    @Path("/{id}/tags")
    public Response addTagsToDataset(@PathParam("id") String id, @FormParam("tags") String tags, @javax.ws.rs.core.Context HttpServletRequest request) {
        return addTagsToItem(id, tags, Cet.DATASET, request);
    }

    /**
     * Remove some tags associated with this dataset
     *
     * @param id
     *            - the URL-encoded ID of the dataset
     * @param tags
     *            - comma separated list of tags
     * @return - success or failure message
     */
    @DELETE
    @Path("/{id}/tags/{tags}")
    public Response removeTagFromDataset(@PathParam("id") String id, @PathParam("tags") String tags, @javax.ws.rs.core.Context HttpServletRequest request) {
        return deleteTagsFromItem(id, tags, Cet.DATASET, request);
    }

    /**
     * Get collection(s) that this dataset is in
     *
     * @param id
     *            - the URL-encoded ID of the dataset
     *
     * @return - list of collections by ID with basic metadata as JSON-LD
     */
    @GET
    @Path("/{id}/collections")
    @Produces("application/json")
    public Response getDatasetCollectionsByIdAsJSON(@PathParam("id") String id, @javax.ws.rs.core.Context HttpServletRequest request) {
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
        return getMetadataByReverseRelationship(baseId, DcTerms.HAS_PART, collectionBasics, userId, (UriRef) CollectionBeanUtil.COLLECTION_TYPE);
    }

    /**
     * Get bibliographic metadata for the given dataset (Identifier, License,
     * Rights Holder, Rights,
     * Data Creation Date, Size, Label, Mimetype, Description(s), Title,
     * Uploaded By, Abstract,
     * Contact(s),Creator(s), Publication Date )
     *
     * @param id
     *            - the URL encoded ID of the dataset
     *
     * @return - the bibliographic metadata for this dataset as JSON-LD
     */
    @GET
    @Path("/{id}/biblio")
    @Produces("application/json")
    public Response getDatasetBiblioAsJSON(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        return getBiblioByIdAsJSON(id, userId);

    }

    /**
     * Get dataset(s) that have the specified metadata
     *
     * @param pred
     *            - URL-encoded predicate
     * @param type
     *            - the type of the value (must be "uri" or "literal")
     * @param value
     *            - the URL-encoded value
     *
     * @return - the list of matching datasets with basic metadata as JSON-LD
     */
    @GET
    @Path("/metadata/{pred}/{type}/{value}")
    @Produces("application/json")
    public Response getDatasetsWithMetadataAsJSON(@PathParam("pred") @Encoded String pred, @PathParam("type") @Encoded String type, @PathParam("value") @Encoded String value, @javax.ws.rs.core.Context HttpServletRequest request) {
        return getItemsByMetadata(pred, type, value, Cet.DATASET, request);
    }

    /**
     * Get unique metadata (i.e. not from an extractor) for the given dataset
     * (Basic/biblio + user-added
     * metadata)
     *
     * @param id
     *            - the URL encoded ID of the dataset
     *
     * @return - the full list of metadata for this dataset as JSON-LD
     */
    @GET
    @Path("/{id}/unique")
    @Produces("application/json")
    public Response getDatasetUniqueMetadataAsJSON2(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        return getItemMetadataAsJSON(id, userId, false);

    }

    /**
     * Get all metadata for the given dataset (Basic/biblio + user-added
     * metadata and extracted metadata)
     *
     * @param id
     *            - the URL encoded ID of the dataset
     *
     * @return - the full list of metadata for this dataset as JSON-LD
     */
    @GET
    @Path("/{id}/metadata")
    @Produces("application/json")
    public Response getDatasetMetadataAsJSON(@PathParam("id") @Encoded String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        return getItemMetadataAsJSON(id, userId, true);

    }

    /**
     * Export the given dataset (data + unique metadata)
     *
     * @param id
     *            - the URL encoded ID of the dataset
     *
     * @return - a zip file with the data and a json-ld metadata file
     */
    @GET
    @Path("/{id}/export")
    @Produces("application/zip")
    @Formatted
    public Response exportDataset(@PathParam("id") @Encoded final String id, @javax.ws.rs.core.Context HttpServletRequest request) {
        final UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        StreamingOutput stream = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                export(Resource.uriRef(URLDecoder.decode(id, "UTF-8")), userId, output);

            }

        };
        return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM).header("content-disposition", "attachment; filename=\"temp.zip\"").build();
    }

    /**
     * Get all (non-deleted, that user can see) geo layers (collections or
     * datasets that have a WMSLayer annotation) that are tagged with
     * the given tag
     *
     * @return geo metadata for each item in json-ld
     */

    @GET
    @Path("/layers")
    @Produces("application/json")
    public Response getGeoLayerDatasetsByTagAsJSON(@javax.ws.rs.core.Context HttpServletRequest request) {
        return getItemsThatAreGeoLayers(Cet.DATASET, null, request);

    }

    /**
     * Get all (non-deleted, that user can see) geo fatures (collections or
     * datasets that have a GeoPoint annotation) that are tagged with
     * the given tag
     *
     * @return geo metadata for each item in json-ld
     */

    @GET
    @Path("/features")
    @Produces("application/json")
    public Response getGeoFeatureDatasetsByTagAsJSON(@javax.ws.rs.core.Context HttpServletRequest request) {
        return getItemsThatAreGeoFeatures(Cet.DATASET, null, request);

    }

    /**
     * Upload dataset including blob and metadata. Basic metadata and SHA1
     * digest are generated from the file stats and session username
     * (dc:creator/uploader)
     *
     * Note: Adding metadata via this method should be done with awareness that
     * it does not perform all 'side effects', e.g. while new predicates are
     * automatically added to the list of extracted or user
     * metadata, there is no way through this endpoint to give them
     * human-friendly labels.
     *
     * @param input
     *            - multipart form data including "datablob" part specifying a
     *            file name and additional predicate/value pairs for other
     *            metadata
     *
     * @return - success/failure message
     */
    @POST
    @Path("")
    @Consumes("multipart/form-data")
    public Response uploadFile(MultipartFormDataInput input, @javax.ws.rs.core.Context HttpServletRequest request) {

        String fileName = "unknown";
        String uri = RestUriMinter.getInstance().mintUri(null); // Create dataset uri
        ThingSession ts = c.getThingSession();
        Thing t = ts.newThing(Resource.uriRef(uri));
        UriRef creator = Resource.uriRef((String) request.getAttribute("userid"));

        try {

            PermissionCheck p = new PermissionCheck(creator, Permission.UPLOAD_DATA);
            if (!p.userHasPermission()) {
                return p.getErrorResponse();
            }
            //Get API input data
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

            for (Entry<String, List<InputPart>> parts : uploadForm.entrySet() ) {
                for (InputPart part : parts.getValue() ) {
                    log.debug("Processing: " + parts.getKey());
                    if (parts.getKey().equals("datablob")) {
                        //get filename...
                        if (log.isDebugEnabled()) {
                            listHeaders(part);
                        }
                        String disp = part.getHeaders().get("Content-Disposition").get(0);
                        log.debug(disp);
                        int start = disp.indexOf("filename=\"") + "filename\"".length() + 1;
                        fileName = disp.substring(start);
                        log.debug("Start : " + start + fileName);
                        int end = fileName.indexOf("\"");
                        fileName = fileName.substring(0, end);
                        log.debug("Filename: " + end + fileName);

                        InputStream is = part.getBody(InputStream.class, null);
                        try {
                            // mimetype
                            String contentType = part.getHeaders().get("Content-Type").get(0);
                            if (contentType != null) {
                                // httpclient also gives the content type a "charset"; ignore that.
                                contentType = contentType.replaceFirst("; charset=.*", "");
                                if (MimeMap.UNKNOWN_TYPE.equals(contentType)) {
                                    contentType = TupeloStore.getInstance().getMimeMap().getContentTypeFor(fileName);
                                }
                                // update context with new mime-type potentially
                                TupeloStore.getInstance().getMimeMap().checkMimeType(contentType);
                            }
                            //Add blob and file-related metadata to Thing t (not saved here)
                            BeanFiller.fillDataBean(c, t, fileName, contentType, creator, new Date(), is);

                        } catch (NoSuchAlgorithmException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    } else {
                        //Should only be one val per Key...
                        log.debug("Adding: " + parts.getKey() + " : " + part.getBodyAsString());
                        addMetadataItem(t, parts.getKey(), part.getBodyAsString(), part.getMediaType(), creator);//Should only be one val per Key...
                    }

                }

            }
            //Add as top-level item
            ts.addValue(AddToCollectionHandler.TOP_LEVEL, AddToCollectionHandler.INCLUDES, t.getSubject());
            t.save();
            ts.close();

        }

        catch (OperatorException oe) {
            log.error("Error uploading dataset: ", oe);
            return Response.status(500).entity(uri).build();
        } catch (IOException ie) {
            log.error("Error uploading dataset: ", ie);
            return Response.status(500).entity(uri).build();

        }

        // submit to extraction service unless we're big
        try {
            if (!TupeloStore.getInstance().getConfiguration(ConfigurationKey.BigData).equals("true")) {
                TupeloStore.getInstance().extractPreviews(uri);
            }
        } catch (Exception e) {
            log.info("Could not submit uri to extraction service, is it down?", e);
        }

        return Response.status(200)
                .entity(uri).build();
    }

    /**
     * Add metadata to dataset.
     * Note: Managed metadata cannot be changed by this method.
     *
     * @param id
     *            - URL-encoded id of dataset
     * @param input
     *            - multipart form data describing predicate/value pairs to add.
     */
    @POST
    @Path("/{id}/metadata")
    @Consumes("multipart/form-data")
    public Response uploadMetadata(@PathParam("id") String id, MultipartFormDataInput input, @javax.ws.rs.core.Context HttpServletRequest request) {
        return super.uploadMetadata(id, input, request);
    }

    //FixME - delete metadata??

    /**
     * Simple function to create a predicate and store information about the
     * predicate.
     *
     * @param category
     * @param id
     * @param label
     * @param tw
     * @return
     */
    static private Resource createPredicate(String category, String id, String label, TripleWriter tw) {
        assert (Character.isUpperCase(category.charAt(0)));
        try {
            category = URLEncoder.encode(category, "UTF8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            log.warn("Could not encode type.", e); //$NON-NLS-1$
            category = category.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        Resource p;
        try {
            p = Cet.cet(String.format("metadata/%s/%s", URLEncoder.encode(category, "UTF8"), id)); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (UnsupportedEncodingException e) {
            log.warn("Could not encode category.", e); //$NON-NLS-1$
            p = Cet.cet(String.format("metadata/%s/%s", category.replace(" ", "%20"), id)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        tw.add(p, Rdf.TYPE, Cet.cet("metadata/" + category)); //$NON-NLS-1$
        tw.add(p, Rdf.TYPE, MMDB.METADATA_TYPE);
        tw.add(p, MMDB.METADATA_CATEGORY, category);
        tw.add(p, Rdfs.LABEL, label);
        return p;
    }

    private static void copyTriples(URL endpoint, String uri, TripleWriter tw, Set<String> copyURI, Set<String> ignoreURI) throws IOException, ParseException {
        StringBuilder sb = new StringBuilder();
        sb.append(URLEncoder.encode(ConfigurationKey.RemoteAPIKey.getPropertyKey(), "UTF8"));
        sb.append("=");
        sb.append(URLEncoder.encode(TupeloStore.getInstance().getConfiguration(ConfigurationKey.RemoteAPIKey), "UTF8"));
        sb.append("&");
        sb.append(URLEncoder.encode("query", "UTF8"));
        sb.append("=");
        sb.append(URLEncoder.encode("SELECT ?p ?o WHERE { <" + uri + "> ?p ?o . }", "UTF8"));

        URLConnection conn = endpoint.openConnection();
        conn.setRequestProperty("Accept", "text/csv");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.getOutputStream().write(sb.toString().getBytes("UTF8"));
        conn.getOutputStream().close();

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        Set<String> uris = new HashSet<String>();
        if (br.readLine().equals("p\to")) {
            String line;
            Resource sub = Resource.uriRef(uri);
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\t");
                if (data.length != 2) {
                    log.warn("ignoring : " + line);
                    continue;
                    //throw (new IOException("Invalid result : " + line));
                }

                Resource pre = parseNTriples(data[0]);
                Resource obj = parseNTriples(data[1]);

                if (Arrays.asList(CopyIgnore).contains(pre.getString())) {
                    continue;
                }
                if (Arrays.asList(CopyTriples).contains(pre.getString())) {
                    uris.add(obj.getString());
                }
                if (pre.getString().startsWith(Cet.cet("metadata/").getString())) {
                    uris.add(pre.getString());
                }
                if (Arrays.asList(CopyBytes).contains(pre.getString())) {
                    copyURI.add(obj.getString());
                }
                if (Arrays.asList(CopyNoBytes).contains(obj.getString())) {
                    log.info("Ignoring blob for " + obj.getString());
                    ignoreURI.add(uri);
                }

                tw.add(sub, pre, obj);
            }
        }
        br.close();

        for (String s : uris ) {
            copyTriples(endpoint, s, tw, copyURI, ignoreURI);
        }
    }

    private static Resource parseNTriples(String s) throws ParseException {
        if (s.startsWith("<") && s.endsWith(">")) {
            return Resource.uriRef(s.substring(1, s.length() - 1));
        }

        if (s.startsWith("_:")) {
            return Resource.blankNode(s.substring(2));
        }

        if (s.startsWith("\"")) {
            Matcher m = Pattern.compile("^\"(.*)\"(.*)$").matcher(s);
            if (!m.matches()) {
                throw (new ParseException("invalid literal " + s, -1));
            }
            String str = UnicodeTranscoder.decode(m.group(1));
            if (m.groupCount() != 2) {
                return Resource.literal(str);
            }
            if (m.group(2).startsWith("^^<")) {
                return Resource.literal(str, m.group(2).substring(3, m.group(2).length() - 1));
            } else if (m.group(2).startsWith("@")) {
                return Resource.plainLiteral(str, m.group(2).substring(2));
            } else {
                return Resource.literal(str);
            }
        }

        return Resource.literal(s);
    }

    private static void copyData(URL endpoint, String uri) throws IOException, OperatorException {
        InputStream is = endpoint.openStream();
        BlobWriter bw = new BlobWriter();
        bw.setSubject(Resource.uriRef(uri));
        bw.setInputStream(is);
        TupeloStore.getInstance().getContext().perform(bw);
        is.close();
    }

}
