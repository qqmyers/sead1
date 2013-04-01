package edu.illinois.ncsa.mmdb.web.server.resteasy;

/**
 * @author Rattanachai Ramathitima
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.BlobFetcher;
import org.tupeloproject.kernel.BlobWriter;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.Tuple;
import org.tupeloproject.util.UnicodeTranscoder;

import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Metadata;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

@Path("/datasets")
public class DatasetsRestService {

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
                                        };

    private static String[] CopyBytes   = new String[] {
                                        "http://cet.ncsa.uiuc.edu/2007/hasPreview",
                                        "http://cet.ncsa.uiuc.edu/2007/pyramid/tiles",
                                        };

    /** Commons logging **/
    private static Log      log         = LogFactory.getLog(DatasetsRestService.class);

    @POST
    @Path("/copy")
    public Response copyDataset(@FormParam("url") String surl) {
        try {
            URL url = new URL(surl);

            if (!url.getRef().startsWith("dataset?id=")) {
                throw (new Exception("URL does not appear to be medici URL"));
            }

            String id = url.getRef().substring(11);
            URL endpoint = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + "resteasy/sparql");

            TripleWriter tw = new TripleWriter();
            Set<String> uris = copyTriples(endpoint, id, tw);
            uris.add(id);

            for (String s : uris ) {
                endpoint = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + "resteasy/datasets/" + URLEncoder.encode(s, "UTF8") + "/file?" + ConfigurationKey.RemoteAPIKey.getPropertyKey() + "=" + URLEncoder.encode(TupeloStore.getInstance().getConfiguration(ConfigurationKey.RemoteAPIKey), "UTF8"));
                copyData(endpoint, s);
            }
            TupeloStore.getInstance().getContext().perform(tw);

            return Response.status(200).entity("Copied " + tw.getToAdd().size() + " triples and " + uris.size() + " blobs").build();
        } catch (Exception e) {
            log.error("Error copying dataset from " + surl, e);
            return Response.status(500).entity("Error copying dataset [" + e.getMessage() + "]").build();
        }
    }

    private Set<String> copyTriples(URL endpoint, String uri, TripleWriter tw) throws IOException, ParseException {
        Set<String> datas = new HashSet<String>();

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
                    if (obj.getString().startsWith("tag:cet.ncsa.uiuc.edu,2008:/bean/PreviewPyramid/")) {
                        log.info("Ignoring blob for " + obj.getString());
                    } else {
                        datas.add(obj.getString());
                    }
                }

                tw.add(sub, pre, obj);
            }
        }
        br.close();

        for (String s : uris ) {
            datas.addAll(copyTriples(endpoint, s, tw));
        }

        return datas;
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
    public Response getDatasetById(@PathParam("id") String id) {
        String result = "";

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
                result += "Title: " + dataset.getTitle() + "<br>";
                PersonBean creator = dataset.getCreator();
                if (creator != null) {
                    result += "Contributor: " + creator.getName() + "<br>";
                }
                result += "Filename: " + dataset.getFilename() + "<br>";
                result += "Size: " + TextFormatter.humanBytes(dataset.getSize()) + "<br>";
                result += "MIME Type: " + dataset.getMimeType() + "<br>";
                if (dataset.getDate() != null) {
                    result += "Uploaded: " + DateFormat.getInstance().format(dataset.getDate()) + "<br>";
                }
            }
            else {
                return Response.status(404).entity("Dataset " + id + " Not Found.").build();
            }
        } catch (Exception e1) {
            log.error("Error getting DatasetBean for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting DatasetBean for " + id).build();
        }

        return Response.status(200).entity(result).build();
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
    public Response getDatasetTagsById(@PathParam("id") String id) {
        String result = "";

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
                result += tag + "<br>";
            }
        } catch (Exception e1) {
            log.error("Error getting tags for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting tags for " + id).build();
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
    public Response getDatasetCollectionsById(@PathParam("id") String id) {
        String result = "";

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
                    result += URLEncoder.encode(row.get(0).getString(), "UTF-8") + "<br>";
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
    public Response getDatasetMetadataById(@PathParam("id") String id) {
        String result = "";

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
        Resource uri = Resource.uriRef(id);
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
                    result += row.get(2).getString() + ": " + row.get(0).getString() + ": " +
                            row.get(1).getString() + "<br>";
                }
            }
        } catch (OperatorException e1) {
            log.error("Error getting metadata for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting metadata for " + id).build();
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

    private Set<String> getTagSet(String cdl) {
        Set<String> tagSet = new HashSet<String>();
        for (String s : cdl.split(",") ) {
            tagSet.add(s.trim());
        }
        return tagSet;
    }
}
