package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.Base64;
import org.tupeloproject.kernel.BlobWriter;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.rdf.Namespaces;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Beans;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;

import edu.illinois.ncsa.mmdb.web.rest.RestService;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

@Path("/ingest")
public class IngestDataService
{
    /** Commons logging **/
    private static Log       log                   = LogFactory.getLog(IngestDataService.class);

    private static Resource  SHA1_DIGEST           = Resource.uriRef("http://sead-data.net/terms/hasSHA1Digest");

    static final Set<String> blacklistedPredicates = new HashSet<String>(Arrays.asList(
                                                           "http://purl.org/dc/terms/license",
                                                           "http://purl.org/dc/terms/rightsHolder",
                                                           "http://purl.org/dc/terms/rights",
                                                           Namespaces.dc("title"),
                                                           Namespaces.dc("creator"),
                                                           Namespaces.dc("identifier"),
                                                           Namespaces.dc("contributor"),
                                                           Namespaces.rdfs("label"),
                                                           "datablob"));

    @POST
    @Path("/collection-upload")
    @Consumes("multipart/form-data")
    public Response uploadDir(MultipartFormDataInput input, @HeaderParam("Authorization") String auth) {
        Context c = TupeloStore.getInstance().getContext();
        String dirName = "unknown";
        Map<Resource, Object> md = new HashMap<Resource, Object>();
        md.put(Rdf.TYPE, RestService.COLLECTION_TYPE);
        String uri = RestUriMinter.getInstance().mintUri(md); // Create dataset uri
        ThingSession ts = c.getThingSession();
        Thing t = ts.newThing(Resource.uriRef(uri));
        try {
            //Get API input data
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

            for (Entry<String, List<InputPart>> parts : uploadForm.entrySet() ) {
                for (InputPart part : parts.getValue() ) {
                    //Should only be one val per Key...
                    if (!isBlacklisted(parts.getKey())) {
                        MediaType mt = part.getMediaType();
                        log.debug(mt.getType() + " : " + mt.getSubtype());
                        if (mt.getType().equals("text")) {
                            if (mt.getSubtype().equals("uri-list")) {
                                t.addValue(Resource.uriRef(parts.getKey()), Resource.uriRef(part.getBodyAsString()));
                                log.debug("Write: <" + parts.getKey() + "> <" + part.getBodyAsString() + ">");
                            }
                            else if (mt.getSubtype().equals("plain")) {
                                t.addValue(Resource.uriRef(parts.getKey()), part.getBodyAsString());
                                log.debug("Write: <" + parts.getKey() + "> \"" + part.getBodyAsString() + "\"");
                            }
                        }
                    } else if (parts.getKey().equals("collection")) {

                        //get name...

                        dirName = part.getBodyAsString();
                        ;

                        t.addType(CollectionBeanUtil.COLLECTION_TYPE);
                        t.addValue(Rdfs.LABEL, dirName);

                        //Next 3 are Bean related
                        t.addType(Beans.STORAGE_TYPE_BEAN_ENTRY);
                        t.setValue(Beans.PROPERTY_VALUE_IMPLEMENTATION_CLASSNAME,
                                Resource.uriRef("edu.uiuc.ncsa.cet.bean.CollectionBean"));
                        t.setValue(Dc.IDENTIFIER, uri);
                        t.setValue(Beans.PROPERTY_IMPLEMENTATION_MAPPING_SUBJECT,
                                Resource.uriRef("tag:cet.ncsa.uiuc.edu,2009:/mapping/" + CollectionBeanUtil.COLLECTION_TYPE));

                        t.setValue(Dc.TITLE, dirName);
                        t.addValue(Dc.DATE, new Date());
                        t.setValue(Dc.CREATOR, getUserName(auth));
                    } else {
                        log.debug("Blacklisted predicate not set: " + parts.getKey());
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

    @POST
    @Path("/data-upload")
    @Consumes("multipart/form-data")
    public Response uploadFile(MultipartFormDataInput input, @HeaderParam("Authorization") String auth) {
        Context c = TupeloStore.getInstance().getContext();
        String fileName = "unknown";
        String uri = RestUriMinter.getInstance().mintUri(null); // Create dataset uri
        ThingSession ts = c.getThingSession();
        Thing t = ts.newThing(Resource.uriRef(uri));
        try {
            //Get API input data
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

            for (Entry<String, List<InputPart>> parts : uploadForm.entrySet() ) {
                for (InputPart part : parts.getValue() ) {
                    //Should only be one val per Key...
                    if (!isBlacklisted(parts.getKey())) {
                        MediaType mt = part.getMediaType();
                        log.debug(mt.getType() + " : " + mt.getSubtype());
                        if (mt.getType().equals("text")) {
                            if (mt.getSubtype().equals("uri-list")) {
                                t.addValue(Resource.uriRef(parts.getKey()), Resource.uriRef(part.getBodyAsString()));
                                log.debug("Write: <" + parts.getKey() + "> <" + part.getBodyAsString() + ">");
                            }
                            else if (mt.getSubtype().equals("plain")) {
                                t.addValue(Resource.uriRef(parts.getKey()), part.getBodyAsString());
                                log.debug("Write: <" + parts.getKey() + "> \"" + part.getBodyAsString() + "\"");
                            }
                        }
                    } else if (parts.getKey().equals("datablob")) {

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
                            t.setValue(Dc.CREATOR, getUserName(auth));
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
                        log.debug("Blacklisted predicate not set: " + parts.getKey());
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

    private static Resource getUserName(String auth) {
        Resource uploader = null;
        try {
            String authString = new String(Base64.decode(auth.substring(auth.indexOf("Basic ") + 6)));
            authString = authString.substring(0, authString.indexOf(":"));
            log.debug("Uploader : " + authString);
            uploader = Resource.uriRef(PersonBeanUtil.getPersonID(authString));

        } catch (IOException e) {
            log.warn("Couldn't decode auth string to find uploader", e);
        }
        return uploader;
    }

    static boolean isBlacklisted(String r) {
        return blacklistedPredicates.contains(r);
    }

    static void listHeaders(InputPart inputPart) {
        try
        {

            //File name and type in headers
            MultivaluedMap<String, String> header = inputPart.getHeaders();
            for (Entry<String, List<String>> pair : header.entrySet() )
            {
                for (String val : pair.getValue() ) {
                    log.debug("Headers: " + pair.getKey() + " : " + val);
                }

            }
            /*
            // convert the uploaded file to inputstream
            InputStream inputStream = inputPart.getBody(InputStream.class, null);
            
            byte[] bytes = IOUtils.toByteArray(inputStream);
            // constructs upload file path
            fileName = UPLOADED_FILE_PATH + fileName;
            writeFile(bytes, fileName);
            System.out.println("Success !!!!!");
            */
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
