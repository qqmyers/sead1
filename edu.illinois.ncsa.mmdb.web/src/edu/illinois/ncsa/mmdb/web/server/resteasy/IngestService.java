package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.Base64;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.rdf.Namespaces;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListNamedThingsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListRelationshipTypesHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListUserMetadataFieldsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetRelationshipHandlerNew;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

@Path("/item")
public class IngestService
{
    /** Commons logging **/
    private static Log       log               = LogFactory.getLog(IngestService.class);

    public static UriRef     SHA1_DIGEST       = Resource.uriRef("http://sead-data.net/terms/hasSHA1Digest");

    static final Set<String> managedPredicates = new HashSet<String>(Arrays.asList(
                                                       "http://purl.org/dc/terms/license",
                                                       "http://purl.org/dc/terms/rightsHolder",
                                                       "http://purl.org/dc/terms/rights",
                                                       Namespaces.dc("title"),
                                                       Namespaces.dc("creator"),
                                                       Namespaces.dc("identifier"),
                                                       Namespaces.dc("contributor"),
                                                       Namespaces.rdfs("label"),
                                                       "datablob",
                                                       "collection"));

    @POST
    @Path("/{id}/metadata")
    @Consumes("multipart/form-data")
    public Response uploadMetadata(@PathParam("id") String id, MultipartFormDataInput input, @HeaderParam("Authorization") String auth) {
        Context c = TupeloStore.getInstance().getContext();
        ThingSession ts = c.getThingSession();
        Thing t = ts.fetchThing(Resource.uriRef(id));
        UriRef creator = getUserName(auth);
        try {
            //Get API input data
            Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

            for (Entry<String, List<InputPart>> parts : uploadForm.entrySet() ) {
                for (InputPart part : parts.getValue() ) {
                    //Should only be one val per Key...
                    addMetadataItem(t, parts.getKey(), part.getBodyAsString(), part.getMediaType(), creator);//Should only be one val per Key...
                }
            }
            t.save();
            ts.close();
        }

        catch (OperatorException oe) {
            log.error("Error adding metadata to: " + id, oe);
        } catch (IOException ie) {
            log.error("Error adding metadata to: " + id, ie);

        }
        return Response.status(200)
                .entity(id).build();
    }

    static void addMetadataItem(Thing t, String pred, String obj, MediaType mediaType, UriRef user) {
        try {
            if (!isManaged(pred)) {
                if (isRelationship(pred)) {
                    //FixMe - verify that object is a valid bean (and not just a URI/external URI)
                    SetRelationshipHandlerNew.setRelationship((UriRef) t.getSubject(), Resource.uriRef(new URI(pred)), Resource.uriRef(new URI(pred)), user);
                } else {
                    log.debug(mediaType.getType() + " : " + mediaType.getSubtype());
                    if (mediaType.getType().equals("text")) {
                        if (mediaType.getSubtype().equals("uri-list")) {
                            t.addValue(Resource.uriRef(pred), Resource.uriRef(obj));
                            log.debug("Write: <" + pred + "> <" + obj + ">");
                        }
                        else if (mediaType.getSubtype().equals("plain")) {
                            t.addValue(Resource.uriRef(pred), obj);
                            log.debug("Write: <" + pred + "> \"" + obj + "\"");
                        }
                        //FixMe? - would be nice to get a human readable label, but perhaps that should be managed in the GUI
                        // (I.e. a general capability to change user metadata labels)
                        ListUserMetadataFieldsHandler.addViewablePredicate(pred);
                    }
                }
            } else {
                log.debug("Managed predicate not set: " + pred);
            }
        } catch (OperatorException oe) {
            log.error("Error adding metadata (pred = " + pred + ") : ", oe);
        } catch (URISyntaxException e) {
            log.error("Predicate or object nor a Uri when setting relationship: " + pred + " : " + obj);
        }

    }

    static ListRelationshipTypesHandler relationships = new ListRelationshipTypesHandler();

    private static boolean isRelationship(String pred) {
        try {
            //Hack: memoized result managed by ListRelationshipTypesHandler
            ListNamedThingsResult result = relationships.execute(null, null);
            return (result.getThingNames().containsKey(pred));
        } catch (ActionException e) {
            log.error("Could not retrieve relationships list:" + e.getMessage());
        }
        return false;
    }

    static UriRef getUserName(String auth) {
        UriRef uploader = null;
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

    static boolean isManaged(String r) {
        return managedPredicates.contains(r);
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
