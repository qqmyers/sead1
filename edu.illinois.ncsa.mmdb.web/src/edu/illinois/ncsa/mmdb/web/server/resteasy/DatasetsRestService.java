package edu.illinois.ncsa.mmdb.web.server.resteasy;

/**
 * @author Rattanachai Ramathitima
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.BlobFetcher;
import org.tupeloproject.kernel.BlobWriter;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Beans;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;
import org.tupeloproject.util.UnicodeTranscoder;

import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Metadata;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.rest.RestService;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

@Path("/datasets")
public class DatasetsRestService extends IngestService {

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

    @POST
    @Path("/copy")
    public Response copyDataset(@FormParam("url") String surl) {
        try {
            final URL url = new URL(surl);

            if (!url.getRef().startsWith("dataset?id=")) {
                throw (new Exception("URL does not appear to be medici URL"));
            }

            new Thread(new Runnable() {
                public void run() {
                    try {
                        String id = url.getRef().substring(11);
                        URL endpoint = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + "resteasy/sparql");

                        TripleWriter tw = new TripleWriter();

                        Resource p = createPredicate("Medici", "original", "Original Source", tw);
                        tw.add(Resource.uriRef(id), p, Resource.uriRef(url.toURI()));

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
     * Simple function to create a predicate and store information about the
     * predicate.
     * 
     * @param category
     * @param id
     * @param label
     * @param tw
     * @return
     */
    static public Resource createPredicate(String category, String id, String label, TripleWriter tw) {
        String uType;
        if (category.length() > 1) {
            uType = category.substring(0, 1).toUpperCase() + category.substring(1);
        } else {
            uType = category.toUpperCase();
        }
        try {
            uType = URLEncoder.encode(uType, "UTF8"); //$NON-NLS-1$ 
        } catch (UnsupportedEncodingException e) {
            log.warn("Could not encode type.", e); //$NON-NLS-1$
            uType = uType.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$ 
        }

        Resource p;
        try {
            p = Cet.cet(String.format("metadata/%s/%s", URLEncoder.encode(category, "UTF8"), id)); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (UnsupportedEncodingException e) {
            log.warn("Could not encode category.", e); //$NON-NLS-1$
            p = Cet.cet(String.format("metadata/%s/%s", category.replace(" ", "%20"), id)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        tw.add(p, Rdf.TYPE, Cet.cet("metadata/" + uType)); //$NON-NLS-1$
        tw.add(p, Rdf.TYPE, MMDB.METADATA_TYPE);
        tw.add(p, MMDB.METADATA_CATEGORY, category);
        tw.add(p, Rdfs.LABEL, label);
        return p;
    }

    private void copyTriples(URL endpoint, String uri, TripleWriter tw, Set<String> copyURI, Set<String> ignoreURI) throws IOException, ParseException {
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

    private Resource parseNTriples(String s) throws ParseException {
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

    private void copyData(URL endpoint, String uri) throws IOException, OperatorException {
        InputStream is = endpoint.openStream();
        BlobWriter bw = new BlobWriter();
        bw.setSubject(Resource.uriRef(uri));
        bw.setInputStream(is);
        TupeloStore.getInstance().getContext().perform(bw);
        is.close();
    }

    @GET
    @Path("/{id}/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDatasetBlob(@PathParam("id") String id) {

        try {
            id = URLDecoder.decode(id, "UTF-8");
            BlobFetcher bf = new BlobFetcher();
            bf.setSubject(Resource.uriRef(id));
            TupeloStore.getInstance().getContext().perform(bf);

            ResponseBuilder response = Response.ok(bf.getInputStream());
            return response.build();
        } catch (Exception e) {
            log.error("Error copying blob from " + id, e);
            return Response.status(500).entity("Error copying blob [" + e.getMessage() + "]").build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getDatasetByIdAsJSON(@PathParam("id") String id) {
        Map<String, String> result = new HashMap<String, String>();

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
        }

        // use DatasetBeanUtil to get DatasetBean of corresponding id
        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

        try {
            DatasetBean dataset = dbu.get(id);
            if (dataset != null && dbu.get(id).getTitle() != null) {
                result.put("Title", dataset.getTitle());
                PersonBean creator = dataset.getCreator();
                if (creator != null) {
                    result.put("Contributor", creator.getName());
                }
                result.put("Filename", dataset.getFilename());
                result.put("Size", TextFormatter.humanBytes(dataset.getSize()));
                //                result.put("Category: " + dataset.get;
                result.put("MIME Type", dataset.getMimeType());
                if (dataset.getDate() != null) {
                    result.put("Uploaded", DateFormat.getInstance().format(dataset.getDate()));
                }
            }
            else {
                return Response.status(404).entity("Dataset " + id + " Not Found.").build();
            }
        } catch (Throwable e1) {
            log.error("Error getting DatasetBean for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting DatasetBean for " + id).build();
        }
        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("/{id}/tags")
    @Produces("application/json")
    public Response getDatasetTagsByIdAsJSON(@PathParam("id") String id) {
        List<String> tags = new ArrayList<String>();

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
        }

        // use TagEventBeanUtil to find all tags of a corresponding dataset
        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        TagEventBeanUtil tebu = new TagEventBeanUtil(beanSession);

        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

        try {

            if (dbu.get(id) == null || dbu.get(id).getTitle() == null) {
                return Response.status(404).entity("Dataset " + id + " Not Found").build();
            }

            Set<String> tagsSet = tebu.getTags(Resource.uriRef(id));
            for (String tag : tagsSet ) {
                tags.add(tag);
            }
        } catch (Exception e1) {
            log.error("Error getting tags for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting tags for " + id).build();
        }

        return Response.status(200).entity(tags).build();
    }

    @POST
    @Path("/{id}/tags")
    public Response addTagsToDataset(@PathParam("id") String id, @FormParam("tags") String tags) {

        String result = "";

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
        }

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        TagEventBeanUtil tebu = new TagEventBeanUtil(beanSession);

        Set<String> tagSet = getTagSet(tags);

        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

        // use TagEventBeanUtil to add new tags to dataset
        try {

            if (dbu.get(id) == null || dbu.get(id).getTitle() == null) {
                return Response.status(404).entity("Dataset " + id + " Not Found").build();
            }

            Set<String> normalizedTags = new HashSet<String>();
            for (String tag : tagSet ) {
                // collapse multiple spaces and lowercase
                normalizedTags.add(tag.replaceAll("  +", " ").toLowerCase());
            }

            log.debug("normalized tags = " + normalizedTags);

            tebu.addTags(Resource.uriRef(id), null, normalizedTags);

            Set<String> allTags = tebu.getTags(id);
            for (String tag : normalizedTags ) {
                if (!allTags.contains(tag)) {
                    log.error("failed to add tag " + tag);
                }
            }

            log.debug("Tagged " + id + " with tags " + normalizedTags);
            result = "Tagged " + id + " with tags " + normalizedTags;
        } catch (Exception e1) {
            log.error("Error tagging " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error tagging " + id).build();
        }
        return Response.status(200).entity(result).build();
    }

    @DELETE
    @Path("/{id}/tags/{tags}")
    public Response removeTagFromDataset(@PathParam("id") String id, @PathParam("tags") String tags) {
        String result = "";

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
        }

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        TagEventBeanUtil tebu = new TagEventBeanUtil(beanSession);

        Set<String> tagSet = getTagSet(tags);

        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

        // use TagEventBeanUtil to remove tags from dataset
        try {

            if (dbu.get(id) == null || dbu.get(id).getTitle() == null) {
                return Response.status(404).entity("Dataset " + id + " Not Found").build();
            }

            Set<String> normalizedTags = new HashSet<String>();
            for (String tag : tagSet ) {
                // collapse multiple spaces and lowercase
                normalizedTags.add(tag.replaceAll("  +", " ").toLowerCase());
            }

            log.debug("normalized tags = " + normalizedTags);

            tebu.removeTags(Resource.uriRef(id), normalizedTags);

            Set<String> allTags = tebu.getTags(id);
            for (String tag : normalizedTags ) {
                if (allTags.contains(tag)) {
                    log.error("failed to remove tag " + tag);
                }
            }

            log.debug("Removed tags " + normalizedTags + " from " + id);
            result = "Removed tags " + normalizedTags + " from " + id;
        } catch (Exception e) {
            log.error("Error removing tags on " + id, e);
            e.printStackTrace();
            return Response.status(500).entity("Error removing tags on " + id).build();
        }
        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("/{id}/collections")
    @Produces("application/json")
    public Response getDatasetCollectionsByIdAsJSON(@PathParam("id") String id) {
        List<String> result = new ArrayList<String>();

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
        }

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

        try {
            if (dbu.get(id) == null || dbu.get(id).getTitle() == null) {
                return Response.status(404).entity("Dataset " + id + " Not Found").build();
            }
        } catch (Exception e1) {
            log.error("Error getting " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting " + id).build();
        }

        // use Unifier to search for all collections of a corresponding dataset
        Unifier uf = new Unifier();
        uf.addPattern("collection", Rdf.TYPE,
                CollectionBeanUtil.COLLECTION_TYPE);
        uf.addPattern("collection", CollectionBeanUtil.DCTERMS_HAS_PART,
                Resource.uriRef(id));
        uf.setColumnNames("collection");
        try {
            TupeloStore.getInstance().getContext().perform(uf);

            for (Tuple<Resource> row : uf.getResult() ) {
                if (row.get(0) != null) {
                    result.add(URLEncoder.encode(row.get(0).getString(), "UTF-8"));
                }
            }
        } catch (OperatorException e1) {
            log.error("Error getting Collections for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting Collections for " + id).build();
        } catch (UnsupportedEncodingException e1) {
            log.error("Error encoding Collection's url for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error encoding Collection's url for " + id).build();
        }

        return Response.status(200).entity(result).build();
    }

    @GET
    @Path("/{id}/metadata")
    @Produces({ "application/json", "application/xml" })
    public Response getDatasetMetadataByIdAsJSON(@PathParam("id") String id) {
        List<Metadata> metadata = new ArrayList<Metadata>();

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
        }

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

        try {
            if (dbu.get(id) == null || dbu.get(id).getTitle() == null) {
                return Response.status(404).entity("Dataset " + id + " Not Found").build();
            }
        } catch (Exception e1) {
            log.error("Error getting " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting " + id).build();
        }

        // use Unifier to search for extracted metadata of a corresponding dataset
        Resource uri = Resource.resource(id);
        Unifier uf = new Unifier();
        uf.addPattern(uri, "predicate", "value"); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addPattern("predicate", Rdf.TYPE, MMDB.METADATA_TYPE); //$NON-NLS-1$
        uf.addPattern("predicate", MMDB.METADATA_CATEGORY, "category"); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addPattern("predicate", Rdfs.LABEL, "label"); //$NON-NLS-1$ //$NON-NLS-2$
        uf.setColumnNames("label", "value", "category"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        try {
            TupeloStore.getInstance().getContext().perform(uf);

            for (Tuple<Resource> row : uf.getResult() ) {
                if (row.get(0) != null) {
                    metadata.add(new Metadata(row.get(2).getString(), row.get(0).getString(), row.get(1).getString()));
                }
            }
        } catch (OperatorException e1) {
            log.error("Error getting metadata for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting metadata for" + id).build();
        }
        return Response.status(200).entity(metadata).build();
    }

    @POST
    @Path("")
    @Consumes("multipart/form-data")
    public Response uploadFile(MultipartFormDataInput input, @HeaderParam("Authorization") String auth) {
        Context c = TupeloStore.getInstance().getContext();
        String fileName = "unknown";
        String uri = RestUriMinter.getInstance().mintUri(null); // Create dataset uri
        ThingSession ts = c.getThingSession();
        Thing t = ts.newThing(Resource.uriRef(uri));
        UriRef creator = getUserName(auth);
        try {
            //Get API input data
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

            for (Entry<String, List<InputPart>> parts : uploadForm.entrySet() ) {
                for (InputPart part : parts.getValue() ) {

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
                        byte[] digest = null;
                        try {
                            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
                            DigestInputStream dis = new DigestInputStream(is, sha1);

                            BlobWriter bw = new BlobWriter();
                            bw.setUri(URI.create(uri));
                            bw.setInputStream(dis);
                            c.perform(bw);
                            dis.close();
                            is.close();
                            log.debug("user uploaded " + fileName + " (" + bw.getSize() + " bytes), uri=" + uri);

                            digest = sha1.digest();
                            t.addType(Cet.DATASET);
                            //Next 3 are Bean related
                            t.addType(Beans.STORAGE_TYPE_BEAN_ENTRY);
                            t.setValue(Beans.PROPERTY_VALUE_IMPLEMENTATION_CLASSNAME,
                                    Resource.uriRef("edu.uiuc.ncsa.cet.bean.DatasetBean"));
                            t.setValue(Dc.IDENTIFIER, uri);
                            t.setValue(Beans.PROPERTY_IMPLEMENTATION_MAPPING_SUBJECT,
                                    Resource.uriRef("tag:cet.ncsa.uiuc.edu,2009:/mapping/http://cet.ncsa.uiuc.edu/2007/Dataset"));

                            t.addValue(Rdfs.LABEL, fileName);
                            t.addValue(SHA1_DIGEST, new String(digest));

                            t.setValue(RestService.FILENAME_PROPERTY, fileName);
                            t.setValue(Dc.TITLE, fileName);
                            t.addValue(Dc.DATE, new Date());
                            t.setValue(Dc.CREATOR, creator);
                            t.addValue(Files.LENGTH, bw.getSize());

                            // mimetype
                            String contentType = part.getHeaders().get("Content-Type").get(0);
                            if (contentType != null) {
                                // httpclient also gives the content type a "charset"; ignore that.
                                contentType = contentType.replaceFirst("; charset=.*", "");
                                if (MimeMap.UNKNOWN_TYPE.equals(contentType)) {
                                    contentType = TupeloStore.getInstance().getMimeMap().getContentTypeFor(fileName);
                                }
                                t.setValue(RestService.FORMAT_PROPERTY, contentType);
                                // update context with new mime-type potentially
                                TupeloStore.getInstance().getMimeMap().checkMimeType(contentType);
                            }

                        } catch (NoSuchAlgorithmException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

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
            log.error("Error uploading dataset: ", oe);
        } catch (IOException ie) {
            log.error("Error uploading dataset: ", ie);

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

    private Set<String> getTagSet(String cdl) {
        Set<String> tagSet = new HashSet<String>();
        for (String s : cdl.split(",") ) {
            tagSet.add(s.trim());
        }
        return tagSet;
    }
}
