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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Encoded;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.json.JSONException;
import org.sead.acr.common.utilities.Memoized;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.BlobFetcher;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Literal;
import org.tupeloproject.rdf.Namespaces;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;

import com.hp.hpl.jena.vocabulary.DCTerms;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListNamedThingsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataField;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TokenStore;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AddToCollectionHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.IsReadyForPublicationHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListRelationshipTypesHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListUserMetadataFieldsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.RequestPublicationHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetRelationshipHandlerNew;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SystemInfoHandler;
import edu.illinois.ncsa.mmdb.web.server.util.CollectionInfo;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

public class ItemServicesImpl
{
    /** Commons logging **/
    private static Log                         log               = LogFactory.getLog(ItemServicesImpl.class);

    static final Set<String>                   managedPredicates = new HashSet<String>(Arrays.asList(
                                                                         "http://purl.org/dc/terms/license",
                                                                         "http://purl.org/dc/terms/rightsHolder",
                                                                         "http://purl.org/dc/terms/rights",
                                                                         Namespaces.dc("title"),
                                                                         Namespaces.dc("creator"),
                                                                         Namespaces.dc("identifier"),
                                                                         Dc.DATE.toString(),
                                                                         Namespaces.dcTerms("created"),
                                                                         Files.LENGTH.toString(),
                                                                         Namespaces.rdfs("label"),
                                                                         "http://sead-data.net/terms/hasSHA1Digest"
                                                                         ));

    //FixME - the labels used here should come from the database, but only user and extracted metadata have such labels right now (not all basic/biblio) -
    // should switch to just listing predicates here and pulling labels once all exist.

    @SuppressWarnings("serial")
    protected static final Map<String, Object> datasetBasics     = new LinkedHashMap<String, Object>() {
                                                                     /**
         *
         */
                                                                     private static final long serialVersionUID = -8944149602891422182L;

                                                                     {
                                                                         /* Example of how to do a typed term (description can actually have string or URIs currently, so this is a hypothetical example)
                                                                          Map<String, String> desc = new HashMap<String, String>();
                                                                          desc.put("@id", Dc.DESCRIPTION.toString());
                                                                          desc.put("@type", "@id");
                                                                          put("Description", desc);
                                                                         */
                                                                         put("Identifier", Dc.IDENTIFIER.toString());
                                                                         put("Title", Dc.TITLE.toString());
                                                                         put("Date", Dc.DATE.toString());
                                                                         put("Uploaded By", Dc.CREATOR.toString());
                                                                         put("Size(Bytes)", Files.LENGTH.toString());
                                                                         put("Mimetype", Dc.FORMAT.toString());
                                                                         put("Creator", Namespaces.dcTerms("creator"));

                                                                     }
                                                                 };

    @SuppressWarnings("serial")
    public static final Map<String, Object>    collectionBasics  = new LinkedHashMap<String, Object>() {
                                                                     /**
         *
         */
                                                                     private static final long serialVersionUID = -6902027597159004342L;

                                                                     {
                                                                         put("Identifier", Dc.IDENTIFIER.toString());
                                                                         put("Title", Dc.TITLE.toString());
                                                                         put("Date", Namespaces.dcTerms("created"));
                                                                         put("Uploaded By", Dc.CREATOR.toString());
                                                                         put("Abstract", Namespaces.dcTerms("abstract"));
                                                                         put("Contact", "http://sead-data.net/terms/contact");
                                                                         put("Creator", Namespaces.dcTerms("creator"));
                                                                     }
                                                                 };

    @SuppressWarnings("serial")
    protected static final Map<String, Object> itemBiblio        = new LinkedHashMap<String, Object>() {
                                                                     /**
         *
         */
                                                                     private static final long serialVersionUID = -8032155107058510486L;

                                                                     {
                                                                         put("Identifier", Dc.IDENTIFIER.toString());
                                                                         put("License", "http://purl.org/dc/terms/license");
                                                                         put("Rights Holder", "http://purl.org/dc/terms/rightsHolder");
                                                                         put("Rights", "http://purl.org/dc/terms/rights");
                                                                         put("Date", Dc.DATE.toString());
                                                                         put("Creation Date", Namespaces.dcTerms("created"));
                                                                         put("Size", Files.LENGTH.toString());
                                                                         put("Label", Namespaces.rdfs("label"));
                                                                         put("Location", "http://sead-data.net/terms/generatedAt");
                                                                         put("Mimetype", Dc.FORMAT.toString());
                                                                         put("Description", org.tupeloproject.rdf.terms.Dc.DESCRIPTION.toString()); //user metadata
                                                                         put("Descriptor", DCTerms.description.toString()); //relationship
                                                                         put("Keyword", Tags.TAGGED_WITH_TAG.toString());
                                                                         put("SHA1 Hash", "http://sead-data.net/terms/hasSHA1Digest");
                                                                         put("Title", Dc.TITLE.toString());
                                                                         put("Uploaded By", Dc.CREATOR.toString());
                                                                         put("Abstract", Namespaces.dcTerms("abstract"));
                                                                         put("Contact", "http://sead-data.net/terms/contact");
                                                                         put("Creator", Namespaces.dcTerms("creator"));
                                                                         put("Publication Date", Namespaces.dcTerms("issued"));

                                                                     }
                                                                 };

    @SuppressWarnings("serial")
    protected static final Map<String, Object> layerBasics       = new LinkedHashMap<String, Object>() {
                                                                     /**
         *
         */
                                                                     private static final long serialVersionUID = 6873224207331969152L;

                                                                     {
                                                                         put("Identifier", Dc.IDENTIFIER.toString());
                                                                         put("Date", Dc.DATE.toString());
                                                                         put("Creation Date", Namespaces.dcTerms("created"));
                                                                         put("Size", Files.LENGTH.toString());
                                                                         put("Label", Namespaces.rdfs("label"));
                                                                         put("Mimetype", Dc.FORMAT.toString());
                                                                         put("Title", Dc.TITLE.toString());
                                                                         put("WMSLayerName", Cet.cet("metadata/Extractor/WmsLayerName").toString());
                                                                         put("WMSLayerUrl", Cet.cet("metadata/Extractor/WmsLayerUrl").toString());
                                                                     }
                                                                 };

    protected static final Map<String, Object> featureBasics     = new LinkedHashMap<String, Object>() {
                                                                     /**
         *
         */
                                                                     private static final long serialVersionUID = -8047764613827878445L;

                                                                     {
                                                                         put("Identifier", Dc.IDENTIFIER.toString());
                                                                         put("Date", Dc.DATE.toString());
                                                                         put("Creation Date", Namespaces.dcTerms("created"));
                                                                         put("Size", Files.LENGTH.toString());
                                                                         put("Label", Namespaces.rdfs("label"));
                                                                         put("Mimetype", Dc.FORMAT.toString());
                                                                         put("Title", Dc.TITLE.toString());

                                                                         Map<String, String> geopoint = new HashMap<String, String>();
                                                                         geopoint.put("@id", "tag:tupeloproject.org,2006:/2.0/gis/hasGeoPoint");
                                                                         geopoint.put("lat", "http://www.w3.org/2003/01/geo/wgs84_pos#lat");
                                                                         geopoint.put("long", "http://www.w3.org/2003/01/geo/wgs84_pos#long");
                                                                         put("GeoPoint", geopoint);
                                                                     }
                                                                 };

    protected static final Map<String, Object> commentBasics     = new LinkedHashMap<String, Object>() {
                                                                     /**
     *
     */
                                                                     private static final long serialVersionUID = 6200945234224918100L;

                                                                     {
                                                                         Map<String, String> comment = new LinkedHashMap<String, String>();
                                                                         comment.put("@id", "http://cet.ncsa.uiuc.edu/2007/annotation/hasAnnotation");
                                                                         comment.put("comment_body", "http://purl.org/dc/elements/1.1/description");
                                                                         comment.put("comment_author", "http://purl.org/dc/elements/1.1/creator");
                                                                         comment.put("comment_date", "http://purl.org/dc/elements/1.1/date");
                                                                         put("Comment", comment);
                                                                     }
                                                                 };

    @SuppressWarnings("serial")
    public static final Map<String, Object>    collectionStats   = new LinkedHashMap<String, Object>() {
                                                                     /**
                                                                      *
                                                                      */
                                                                     private static final long serialVersionUID = -6902027597159004342L;

                                                                     {
                                                                         put("Identifier", Dc.IDENTIFIER.toString());
                                                                         put("Total Size", Files.LENGTH.toString());
                                                                         put("Number of Collections", "http://sead-data.net/terms/collectioncount");
                                                                         put("Number of Datasets", "http://sead-data.net/terms/datasetcount");
                                                                         put("Max Dataset Size", "http://sead-data.net/terms/maxdatasetsize");
                                                                         put("Max Collection Depth", "http://sead-data.net/terms/maxcollectiondepth");
                                                                         put("Data Mimetypes", Dc.FORMAT.toString());
                                                                     }
                                                                 };

    protected static final Map<String, Object> publishedVersions = new LinkedHashMap<String, Object>() {
                                                                     /**
     *
     */
                                                                     private static final long serialVersionUID = 6200945234224918100L;

                                                                     {
                                                                         Map<String, String> version = new LinkedHashMap<String, String>();
                                                                         version.put("@id", DcTerms.HAS_VERSION.toString());
                                                                         version.put("version number", "http://sead-data.net/vocab/hasVersionNumber");
                                                                         version.put("External Identifier", DCTerms.identifier.getURI());
                                                                         version.put("publication_date", DCTerms.issued.getURI());
                                                                         put("Published Version", version);
                                                                     }
                                                                 };

    /* Only used for blacklisting predicates and reserving labels...
     *
     */
    public static final Map<String, String>    oreTerms          = new LinkedHashMap<String, String>() {
                                                                     {

                                                                         put("describes", "http://www.openarchives.org/ore/terms/describes");
                                                                         put("aggregates", "http://www.openarchives.org/ore/terms/aggregates");
                                                                         put("Aggregation", "http://www.openarchives.org/ore/terms/Aggregation");
                                                                         put("similarTo", "http://www.openarchives.org/ore/terms/similarTo");
                                                                         put("AggregatedResource", "http://www.openarchives.org/ore/terms/AggregatedResource");
                                                                         put("ResourceMap", "http://www.openarchives.org/ore/terms/ResourceMap");

                                                                     }

                                                                 };
    static Context                             c                 = TupeloStore.getInstance().getContext();
    static protected SEADRbac                  rbac              = new SEADRbac(c);

    static public List<String> getReservedLabels() {
        ArrayList<String> labelsArrayList = new ArrayList<String>();
        labelsArrayList.addAll(datasetBasics.keySet());
        labelsArrayList.addAll(collectionBasics.keySet());
        labelsArrayList.addAll(itemBiblio.keySet());
        labelsArrayList.addAll(layerBasics.keySet());
        labelsArrayList.addAll(featureBasics.keySet());
        labelsArrayList.addAll(commentBasics.keySet());
        labelsArrayList.addAll(URLRestService.doiBasics.keySet());
        labelsArrayList.addAll(URLRestService.collectionStats.keySet());
        labelsArrayList.addAll(URLRestService.publishedVersions.keySet());
        labelsArrayList.addAll(oreTerms.keySet());
        return labelsArrayList;
    }

    protected Response uploadMetadata(String id, MultipartFormDataInput input, HttpServletRequest request) {
        UriRef creator = Resource.uriRef((String) request.getAttribute("userid"));
        try {
            id = URLDecoder.decode(id, "UTF-8");
            //FixMe - don't have a general permission for add/edit any metadata

            PermissionCheck p = new PermissionCheck(creator, Permission.EDIT_USER_METADATA);
            if (!p.userHasPermission()) {
                return p.getErrorResponse();
            }
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
        }

        UriRef objId = Resource.uriRef(id);
        if (isAccessible(creator, objId)) {
            ThingSession ts = c.getThingSession();
            Thing t = ts.fetchThing(Resource.uriRef(id));

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
        } else {
            return Response.status(403).build(); //Forbidden
        }
    }

    protected Response getBiblioByIdAsJSON(@PathParam("id") @Encoded String id, UriRef userId) {

        return getMetadataById(id, itemBiblio, userId);

    }

    static protected void addMetadataItem(Thing t, String pred, String obj, MediaType mediaType, UriRef user) {
        try {
            if (!isManaged(pred)) {
                if (isRelationship(pred)) {
                    //FixMe - verify that object is a valid bean (and not just a URI/external URI)
                    SetRelationshipHandlerNew.setRelationship((UriRef) t.getSubject(), Resource.uriRef(new URI(pred)), Resource.uriRef(new URI(obj)), user);
                } else {
                    if (mediaType == null) {
                        mediaType = new MediaType("text", "plain");
                    }
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

    protected static ListRelationshipTypesHandler relationships = new ListRelationshipTypesHandler();

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

    static boolean isManaged(String r) {
        if (r.equals("datablob") || r.equals("collection") || managedPredicates.contains(r)) {
            return true;
        }
        return false;
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

    private static Memoized<Map<String, Object>> extractedMap = null;

    public static Map<String, Object> listExtractedMetadataFields2() {
        if (extractedMap == null) {
            extractedMap = new Memoized<Map<String, Object>>() {
                @SuppressWarnings("unchecked")
                public Map<String, Object> computeValue() {
                    Map<String, Object> metadata = new LinkedHashMap<String, Object>();
                    ;
                    Unifier uf = new Unifier();
                    uf.addPattern("predicate", Rdf.TYPE, MMDB.METADATA_EXTRACTOR); //$NON-NLS-1$
                    uf.addPattern("predicate", Rdfs.LABEL, "label"); //$NON-NLS-1$ //$NON-NLS-2$
                    uf.setColumnNames("label", "predicate"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                    try {
                        c.perform(uf);

                        for (Tuple<Resource> row : uf.getResult() ) {
                            if (row.get(0) != null) {
                                log.debug("extracted fields found: " + row.get(0).getString());
                                log.debug(row.get(1).getString());
                                log.debug(row.get(2).getString());
                                metadata.put(row.get(0).getString(), row.get(1).getString());
                            }
                        }
                    } catch (OperatorException e1) {
                        log.error("Error getting Extracted Metadata list", e1);
                    }
                    return metadata;
                }
            };

            extractedMap.setTtl(60 * 60 * 1000); // 1 hour
        }
        return extractedMap.getValue();

    }

    public Response getItemMetadataAsJSON(String id, UriRef userId, boolean includeExtracted) {

        Map<String, Object> combinedContext = getCombinedContext(includeExtracted);

        return getMetadataById(id, combinedContext, userId);
    }

    private static Memoized<Map<String, Object>> combinedContext              = null;

    private static Memoized<Map<String, Object>> inverseUniqueCombinedContext = null;

    public static Map<String, Object> getCombinedContext(boolean includeExtracted) {

        if (combinedContext == null) {
            combinedContext = new Memoized<Map<String, Object>>() {
                @SuppressWarnings("unchecked")
                public Map<String, Object> computeValue() {

                    Map<String, Object> combinedMap = new LinkedHashMap<String, Object>();
                    //FixME - use labels that are custom to this space (changed by an admin in the GUI)?

                    //1) Biblio triples
                    combinedMap.putAll(itemBiblio);

                    //2) Geofeature triples - mostly duplicates with biblio but includes geopoint
                    combinedMap.putAll(featureBasics);
                    //3) Comments
                    combinedMap.putAll(commentBasics);

                    //4) User metadata fields
                    ListUserMetadataFieldsResult lmuf = ListUserMetadataFieldsHandler.listUserMetadataFields(false);
                    for (UserMetadataField umf : lmuf.getFields() ) {
                        combinedMap.put(umf.getLabel(), umf.getUri());
                    }
                    //5) relationships
                    try {
                        ListNamedThingsResult result = relationships.execute(null, null);

                        for (Entry<String, String> entry : result.getThingNames().entrySet() ) {
                            combinedMap.put(entry.getValue(), entry.getKey());
                        }
                    } catch (ActionException e1) {
                        log.error("Error retrieving relationship predicates" + e1);
                    }

                    //6 - Don't show hasPart relationships since there are other endpoints for those and we would not be handling
                    //    access control here

                    combinedMap.remove("Has Subcollection");
                    //Force inverse to refresh if/when called
                    inverseUniqueCombinedContext = null;
                    return combinedMap;
                }
            };
            combinedContext.setTtl(60 * 60 * 1000); // 1 hour
        }
        // extracted fields are memoized/cached separately, just assemble here if needed
        Map<String, Object> fullMap = combinedContext.getValue();
        if (includeExtracted) {
            fullMap.putAll(listExtractedMetadataFields2());
        }
        return fullMap;
    }

    public static Map<String, Object> getCombinedInverseContext(boolean includeExtracted) {

        if (inverseUniqueCombinedContext == null) {
            inverseUniqueCombinedContext = new Memoized<Map<String, Object>>() {
                @SuppressWarnings("unchecked")
                public Map<String, Object> computeValue() {

                    Map<String, Object> combinedMap = getCombinedContext(false);
                    return getInverseContext(combinedMap);
                }
            };
            inverseUniqueCombinedContext.setTtl(60 * 60 * 1000); // 1 hour
        }
        // extracted fields are memoized/cached separately, just assemble here if needed
        // the inverse of the combo is the combo of the inverses (true if lists don't overlap)
        Map<String, Object> fullMap = inverseUniqueCombinedContext.getValue();
        if (includeExtracted) {
            fullMap.putAll(getInverseContext(listExtractedMetadataFields2()));
        }
        return fullMap;
    }

    protected static boolean isAccessible(UriRef userId, UriRef objId) {

        if (!rbac.checkAccessLevel(userId, objId)) {
            return false;
        }
        return true;

    }

    /* Get metadata corresponding to all the predicates in context for the id given, respecting access control
     * and ignoring deleted items.
     */
    protected static Response getMetadataById(String id, Map<String, Object> context, UriRef userId) {
        Map<String, Object> result = getMetadataMapById(id, context, userId);
        if (result.containsKey("Error Response")) {
            return (Response) result.get("Error Response");
        } else {
            return Response.status(200).entity(result).build();
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMetadataMapById(String id, Map<String, Object> context, UriRef userId) {

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
            result.put("Error Response", Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error decoding id: " + id).build());
            return result;

        }

        //Permission to see pages means you can see metadata as well...
        PermissionCheck p = new PermissionCheck(userId, Permission.VIEW_MEMBER_PAGES);
        if (!p.userHasPermission()) {
            //See if this is a published item and user has view published
            String version = "1";
            try {
                //Will be > 1 if any prior versions exist
                version = RequestPublicationHandler.getVersionNumber(Resource.uriRef(id));
            } catch (OperatorException e) {
                log.warn(e);
            }
            PermissionCheck p2 = new PermissionCheck(userId, Permission.VIEW_PUBLISHED);
            if (!((!version.equals("1")) && p2.userHasPermission())) {

                log.debug("Insufficient permission: " + userId.toString());
                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("Error Response", p.getErrorResponse());
                return result;
            }
        }

        UriRef itemUri = Resource.uriRef(id);
        if (!isAccessible(userId, itemUri)) {
            log.debug("Not accessible: " + userId.toString());

            Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
            Map<String, Object> result = new LinkedHashMap<String, Object>();

            responseMap.put("Error", "Item not accessible");
            result.put("Error Response", Response.status(403).entity(responseMap).build());
            return result;
        }

        return getMetadataMapById(itemUri, context);
    }

    public static Map<String, Object> getMetadataMapById(UriRef itemUri, Map<String, Object> context) {

        Map<String, Object> inverseContext = getInverseContext(context);
        return getMetadataMapById(itemUri, context, inverseContext);
    }

    public static Map<String, Object> getMetadataMapById(UriRef itemUri, Map<String, Object> context, Map<String, Object> inverseContext) {

        Map<String, Object> result = new LinkedHashMap<String, Object>();

        Map<String, Object> finalContext = new LinkedHashMap<String, Object>();
        Map<String, Object> finalResult = new LinkedHashMap<String, Object>();

        try { //Get fields
            TripleMatcher tm = new TripleMatcher();
            tm.setSubject(itemUri);
            c.perform(tm);
            for (Triple t : tm.getResult() ) {

                if (context.containsValue(t.getPredicate().toString())) {
                    //We have a string value (not an object with structure)
                    log.trace("Adding: " + t.getPredicate().toString() + " : " + t.getObject().toString());
                    addTuple(result, t.getPredicate().toString(), t.getObject().toString());
                }
                //Check for subobjects
                //Fixme - make recursive
                for (Object object : context.values() ) {
                    if (object instanceof Map) {
                        if (((Map<String, Object>) object).get("@id").equals((t.getPredicate().toString()))) {
                            Map<String, Object> subresult = new LinkedHashMap<String, Object>();
                            subresult.put(Dc.IDENTIFIER.toString(), t.getObject().toString());
                            TripleMatcher tm2 = new TripleMatcher();
                            tm2.setSubject(t.getObject());
                            c.perform(tm2);
                            for (Triple t2 : tm2.getResult() ) {
                                if (((Map<String, Object>) object).containsValue(t2.getPredicate().toString())) {
                                    log.trace("Adding object field: " + t2.getPredicate().toString() + " : " + t2.getObject().toString());
                                    addTuple(subresult, t2.getPredicate().toString(), t2.getObject().toString());
                                }
                            }
                            log.trace("Adding object for: " + t.getPredicate().toString());
                            addObjectTuple(result, t.getPredicate().toString(), subresult, Dc.IDENTIFIER.toString());
                        }
                    }
                }
            }
            //The code above builds a result that uses the predicates as keys
            //The section below replaces the predicates with the labels and adds the label-predicate mapping into the context
            log.trace("Swapping labels for predicates");
            for (Entry<String, Object> e : result.entrySet() ) {

                if (e.getValue() instanceof String) {
                    //Strings get mapped directly, using the new key (the label from the context)
                    log.trace("Adding string for key: " + e.getKey() + " " + inverseContext.get(e.getKey()));
                    finalResult.put((String) inverseContext.get(e.getKey()), e.getValue());

                } else if (e.getValue() instanceof Map) {
                    //Maps get rebuilt using labels internally and then mapped with the label into the final result
                    Map<String, Object> newSubResultMap = new LinkedHashMap<String, Object>();
                    for (Entry<String, Object> se : ((Map<String, Object>) e.getValue()).entrySet() ) {
                        if (!(se.getKey().equals("@id"))) {
                            log.trace("Adding string in sub-object for key: " + se.getKey() + " " + inverseContext.get(se.getKey()));
                            //Find the label who's key is the object's predicate, and then get the context object who's key is that label
                            Map<String, Object> subContext = (Map<String, Object>) inverseContext.get(inverseContext.get(e.getKey()));
                            newSubResultMap.put((String) (subContext.get(se.getKey())), se.getValue());
                            //Each predicate is mapped into the top level of the context
                            finalContext.put((String) (subContext.get(se.getKey())), se.getKey());
                        }
                    }
                    log.trace("Adding object for key: " + e.getKey() + " " + inverseContext.get(e.getKey()));
                    finalResult.put((String) inverseContext.get(e.getKey()), newSubResultMap);
                } else if (e.getValue() instanceof List) {
                    //Handle each entry in the list and then map the new list into the final result
                    List<?> l = (List<?>) e.getValue();
                    ListIterator<?> i = l.listIterator();
                    ArrayList<Object> subArrayList = new ArrayList<Object>();
                    while (i.hasNext()) {
                        Object o = i.next();
                        if (o instanceof String) {
                            //Just copy a string into the new list
                            subArrayList.add(o);

                        } else if (o instanceof Map) { //An object
                            //Internally map to use labels and then add to new list
                            Map<String, Object> newSubResultMap = new LinkedHashMap<String, Object>();
                            for (Entry<String, Object> se : ((Map<String, Object>) o).entrySet() ) {
                                if (!(se.getKey().equals("@id"))) {
                                    log.trace("Adding string for sub-object in list for key: " + se.getKey() + " " + inverseContext.get(se.getKey()));
                                    //Find the label who's key is the object's predicate, and then get the context object who's key is that label
                                    Map<String, Object> subContext = (Map<String, Object>) inverseContext.get(inverseContext.get(e.getKey()));
                                    newSubResultMap.put((String) (subContext.get(se.getKey())), se.getValue());
                                    finalContext.put((String) (subContext.get(se.getKey())), se.getKey());
                                }
                            }
                            subArrayList.add(newSubResultMap);

                        }
                    }
                    //Now add new list into result
                    log.trace("Adding list for key: " + inverseContext.get(e.getKey()));
                    finalResult.put((String) inverseContext.get(e.getKey()), subArrayList);

                }
                //Regardless of value type, put label/predicate into final context
                finalContext.put((String) inverseContext.get(e.getKey()), e.getKey());
            }
            /*
             Unifier uf = new Unifier();
             uf = populateUnifier(itemUri, uf, context, null);
             //Get columns to put in result (all in this case)
             List<String> names = uf.getColumnNames();
             log.debug("names: " + names);

             c.perform(uf);
             log.debug("Performed");

             result = buildResultMap(uf.getResult(), context, names, false, null);
            */
            if (finalResult.isEmpty()) {
                log.debug("Empty result");
            } else {
                finalResult.put("@context", finalContext);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            finalResult.put("Error Response", Response.status(500).entity("Error processing id: " + itemUri.toString()).build());
        }

        return finalResult;

    }

    //TBD - consider apache BidiMaps...
    private static Map<String, Object> getInverseContext(Map<String, Object> context) {
        Map<String, Object> inverseMap = new HashMap<String, Object>();
        for (Entry<String, Object> e : context.entrySet() ) {
            if (e.getValue() instanceof String) {
                log.trace("Map: " + e.getValue() + e.getKey());
                inverseMap.put((String) e.getValue(), e.getKey());
            } else {
                //For objects put an entry mapping the predicate used for the "@id" to the label for the object
                //and create an object who's key is the label that contains the predicate to label maps used within the objects
                Map<String, Object> subMap = new HashMap<String, Object>();
                for (Entry<String, Object> se : ((Map<String, Object>) e.getValue()).entrySet() ) {
                    if (se.getKey().equals("@id")) {
                        log.trace("Map: " + se.getValue() + e.getKey());
                        inverseMap.put((String) se.getValue(), e.getKey());
                        //FixMe - use '@id'?
                        subMap.put(Dc.IDENTIFIER.toString(), "Identifier");
                    } else {
                        log.trace("Map: " + se.getValue() + se.getKey());
                        subMap.put((String) se.getValue(), se.getKey());
                    }
                }
                inverseMap.put(e.getKey(), subMap);

            }
        }

        return inverseMap;
    }

    /* Adds patterns corresponding to each entry in the context to the Unifier prior to evaluation
     * @thing  - can be a UriRef, or a String (making it a variable in the query).
     *
     *
     */
    @SuppressWarnings("unchecked")
    protected static Unifier populateUnifier(Object thing, Unifier uf, Map<String, Object> context, Set<String> requiredKeys) {
        /*FixME: Note: if the first pattern of an all optional unifier doesn't match, tupelo will return no results
         * even if the other terms would match. So - all contexts should be LinkedHashMaps / other order preserving map
         * and the first entry should be a predicate that is known to be assigned (e.g. dc:identifier).
         */

        for (String key : context.keySet() ) {
            String pred = null;
            boolean optional = true;
            if (requiredKeys != null) {
                if (requiredKeys.contains(key)) {
                    optional = false;
                }
            }
            Object temp = context.get(key);
            if (temp instanceof String) {
                pred = (String) temp;
            } else {
                pred = ((Map<String, String>) temp).get("@id");
            }
            UriRef predRef = Resource.uriRef(pred);
            if (thing instanceof UriRef) {
                log.debug("Adding pattern for id: " + ((UriRef) thing).toString() + " " + predRef.toString() + " " + key);
                uf.addPattern(thing, predRef, key, optional);

            } else {
                log.debug("Adding pattern: " + thing + " " + predRef.toString() + " " + key);
                uf.addPattern(thing, predRef, key, optional);

            }
            uf.addColumnName(key);
            if (!(temp instanceof String)) {
                //Deal with other terms needed for key

                for (String subkey : ((Map<String, String>) temp).keySet() ) {
                    if (!subkey.equals("@id")) {
                        uf.addPattern(key, Resource.uriRef(((Map<String, String>) temp).get(subkey)), subkey, true);
                        uf.addColumnName(subkey);
                    }
                }
            }
        }
        return uf;
    }

    /*
     * Read Unifier result and add an entry for each unique 'triple' by: reading through returned table and
     * for useHierarchy = false, using the column name as predicate and corresponding row value as the object to
     * create tuples associated with the queried id.
     * for useHierarchy=true, treat column 1 as an id and make collections of triples associated with each id using all other columns
     * Due to the way tupelo returns results, there can be duplicate information in different rows, so logic is required to
     * check for existing values and to capture multiple values for the same id/predicate.
     * The use of Maps provides a nice json structure as output. Adding the context in is consistent with the draft JSON-LD spec
     * that links the readable terms with formal rdf predicates for linked data
     *
     */
    @SuppressWarnings("unchecked")
    protected static Map<String, Object> buildResultMap(Table<Resource> table, Map<String, Object> context, List<String> names, boolean useHierarchy, FilterCallback filter) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Map<String, String> partsMap = buildPartsMap(context);

        for (Tuple<Resource> tu : table ) {

            if ((filter == null) || filter.include(tu)) {
                Map<String, Map<String, Object>> currentRowObjects = new LinkedHashMap<String, Map<String, Object>>();
                for (int i = 0; i < names.size(); i++ ) {
                    String key = names.get(i);
                    Resource o = tu.get(i);
                    if (o != null) {
                        String obj = tu.get(i).toString();
                        log.debug(key + " : " + obj);
                        if (useHierarchy) {
                            //add submap for this id
                            if (i == 0) {
                                if (!result.containsKey(obj)) {
                                    result.put(obj, new LinkedHashMap<String, Object>());

                                }
                                //For i=0, add value as  metadata if the key is "Identifier"
                                if (key.equals("Identifier")) {
                                    ((Map<String, Object>) result.get(tu.get(0).toString())).put(key, obj);
                                }
                            } else {
                                addToResult((Map<String, Object>) result.get(tu.get(0).toString()), key, obj, context, currentRowObjects, partsMap);
                            }
                        } else {
                            addToResult(result, key, obj, context, currentRowObjects, partsMap);
                        }
                    }
                }
            }
        }
        if (result.isEmpty()) {
            log.debug("Empty result");
        } else {
            result.put("@context", context);
        }
        return result;
    }

    private static void addToResult(Map<String, Object> resultItem, String key, String obj, Map<String, Object> context, Map<String, Map<String, Object>> currentRowObjects, Map<String, String> partsMap) {
        if (context.get(key) != null) {
            //top level item
            if (context.get(key) instanceof String) {
                //flat item
                addTuple(resultItem, key, obj);
            } else {
                //an object with structure - add an id field and be ready to find other parts
                Map<String, Object> newObj = new LinkedHashMap<String, Object>();
                newObj.put("Identifier", obj);
                currentRowObjects.put(key, newObj);
                //Fixme - change to "@id"?
                addObjectTuple(resultItem, key, newObj, "Identifier");

            }
        } else {
            //entry is from a sub object - find where it goes and add it there
            String objectName = partsMap.get(key);
            Map<String, Object> objectMap = currentRowObjects.get(objectName);
            objectMap.put(key, obj);
        }

    }

    private static Map<String, String> buildPartsMap(Map<String, Object> context) {
        Map<String, String> partsMap = new LinkedHashMap<String, String>();
        for (Entry<String, Object> e : context.entrySet() ) {
            Object o = e.getValue();
            if (o instanceof Map<?, ?>) {
                for (String s : ((Map<String, String>) o).keySet() ) {
                    if (!s.equals("@id")) {
                        partsMap.put(s, e.getKey());
                    }
                }
            }
        }
        return partsMap;
    }

    /* Logic to only add unique values to the map and to switch from string to List<String> for multiple values
     *
     */
    @SuppressWarnings("unchecked")
    private static void addTuple(Map<String, Object> map, String key, String object) {
        if (!map.containsKey(key)) {
            map.put(key, object);
        } else {
            if (map.get(key) instanceof String) {
                if (!object.equals(map.get(key))) {
                    //switch to array
                    List<String> list = new ArrayList<String>();
                    list.add((String) map.get(key));
                    list.add(object);
                    map.put(key, list);
                }
            } else {
                if (!((List<String>) map.get(key)).contains(object)) {
                    ((List<String>) map.get(key)).add(object);
                }
            }

        }

    }

    private static void addObjectTuple(Map<String, Object> map, String key, Map<String, Object> newObj, String idKey) {
        if (!map.containsKey(key)) {
            map.put(key, newObj);
        } else {
            if (map.get(key) instanceof Map<?, ?>) {
                if (!newObj.get(idKey).equals(((Map<String, String>) map.get(key)).get(idKey))) {
                    //switch to array
                    List<Object> list = new ArrayList<Object>();
                    list.add(map.get(key));
                    list.add(newObj);
                    map.put(key, list);
                }
            } else {
                List<Object> list = (List<Object>) map.get(key);
                boolean isNew = true;
                for (Object o : list ) {
                    if (((Map<String, String>) o).get(idKey).equals(newObj.get(idKey))) {
                        isNew = false;
                        break;
                    }
                }
                if (isNew) {
                    ((List<Object>) map.get(key)).add(newObj);
                }
            }

        }

    }

    /* Get metadata corresponding to all the predicates in context for datasets or collections related to the
     * given id by the given relationship working in the given direction (isSubject means the given id is the subject of the relationship triple)
     * , respecting access control and ignoring deleted items.
     */

    protected static Response getMetadataByForwardRelationship(UriRef base, Resource relationship, Map<String, Object> context, UriRef userId, UriRef requestedType) {
        return getMetadataByRelationship(base, true, relationship, context, userId, requestedType);

    }

    private static Response getMetadataByReverseRelationship(String base, Resource relationship, Map<String, Object> context, UriRef userId, UriRef requestedType) {
        Resource baseLiteral = Resource.literal(base);
        return getMetadataByRelationship(baseLiteral, false, relationship, context, userId, requestedType);
    }

    protected static Response getMetadataByReverseRelationship(UriRef baseId, Resource relationship, Map<String, Object> context, UriRef userId, UriRef requestedType) {
        return getMetadataByRelationship(baseId, false, relationship, context, userId, requestedType);
    }

    private static Response getMetadataByRelationship(Resource baseId, boolean isSubject, Resource relationship, Map<String, Object> context, final UriRef userId, UriRef requestedType) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        if (isSubject && (baseId instanceof UriRef)) {
            if (!isAccessible(userId, (UriRef) baseId)) {

                result.put("Error", "Parent Item not accessible");
                return Response.status(403).entity(result).build();
            }
        }

        //Get fields
        Unifier uf = new Unifier();
        if (isSubject) {
            uf.addPattern(baseId, relationship, "thing");
        } else {
            uf.addPattern("thing", relationship, baseId);
        }
        uf.addColumnName("thing");
        if (requestedType != null) {
            uf.addPattern("thing", Rdf.TYPE, requestedType);
        }
        uf = populateUnifier("thing", uf, context, null);

        //Get column name list before the is-deleted column is added
        List<String> names = uf.getColumnNames();
        Table<Resource> results = null;
        try {
            results = TupeloStore.getInstance().unifyExcludeDeleted(uf, "thing");
        } catch (Throwable e1) {
            log.error("Error getting requested metadata for " + baseId.toString(), e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting information about " + baseId.toString()).build();
        }
        final int idIndex = names.indexOf("Identifier");
        log.debug(names);
        log.debug("idIndex: " + idIndex);
        try {
            result = buildResultMap(results, context, names, true, new FilterCallback() {
                @Override
                public boolean include(Tuple<Resource> t) {
                    UriRef id = null;
                    if (t.get(0) == null) {
                        log.error("Missing identifier - skipping a dataset");
                    } else {
                        if (t.get(idIndex).isLiteral()) {

                            id = Resource.uriRef(t.get(0).toString());
                        } else {
                            id = (UriRef) t.get(idIndex);
                            log.warn("UriRef identifier:" + t.get(0).toString());
                        }
                        log.debug("by rel U: " + userId.toString() + " o: " + id.toString());
                        if (isAccessible(userId, id)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    return false;
                }
            });
            return Response.status(200).entity(result).build();
        } catch (Exception e) {
            //No results just means an empty list, which is OK
            result.put("@context", context);
            return Response.status(200).entity(result).build();
        }
    }

    protected boolean isDeleted(String objectId) throws OperatorException {
        TripleMatcher tm = new TripleMatcher();
        tm.setSubject(Resource.uriRef(objectId));
        tm.setPredicate(DcTerms.IS_REPLACED_BY);
        c.perform(tm);
        for (Triple t : tm.getResult() ) {
            if (t.getObject().equals(Rdf.NIL)) {
                return true;
            }
        }
        return false;

    }

    protected Response getItemsThatAreGeoLayers(UriRef itemType, UriRef tag, HttpServletRequest request) {
        final UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        PermissionCheck p = new PermissionCheck(userId, Permission.VIEW_LOCATION);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }

        Map<String, Object> context = layerBasics;
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Unifier uf = new Unifier();
        uf.addPattern("item", Rdf.TYPE, itemType);
        if (tag != null) {
            uf.addPattern("item", Tags.TAGGED_WITH_TAG, tag);
        }
        Set<String> requiredKeys = new HashSet<String>();
        //requiredKeys are the labels, not the preds in the @context
        requiredKeys.add("WMSLayerUrl");
        //column 0 must be identifier
        populateUnifier("item", uf, context, requiredKeys);

        //Get column name list before the is-deleted column is added
        List<String> names = uf.getColumnNames();
        log.debug(names);
        Table<Resource> results = null;
        try {
            results = TupeloStore.getInstance().unifyExcludeDeleted(uf, "item");
        } catch (Throwable e1) {
            log.error("Error getting Geo Layers", e1);
            return Response.status(500).entity("Error getting Items that are Geo Layers").build();
        }
        try {
            result = buildResultMap(results, context, names, true, new FilterCallback() {
                @Override
                public boolean include(Tuple<Resource> t) {
                    UriRef id = null;
                    if (t.get(0) == null) {
                        log.error("Missing identifier - skipping an item");
                    } else {
                        if (t.get(0).isLiteral()) {
                            id = Resource.uriRef(t.get(0).toString());
                        } else {
                            id = (UriRef) t.get(0);
                            log.warn("UriRef identifier:" + t.get(0).toString());
                        }
                        log.debug("U: " + userId.toString() + " O: " + id.toString());
                        if (isAccessible(userId, id)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    return false;
                }
            });
            return Response.status(200).entity(result).build();
        } catch (Exception e) {
            //No results just means an empty list, which is OK
            result.put("@context", context);
            return Response.status(200).entity(result).build();
        }
    }

    protected Response getItemsThatAreGeoFeatures(UriRef itemType, UriRef tag, HttpServletRequest request) {
        final UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        PermissionCheck p = new PermissionCheck(userId, Permission.VIEW_LOCATION);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }

        Map<String, Object> context = featureBasics;
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Unifier uf = new Unifier();
        uf.addPattern("item", Rdf.TYPE, itemType);
        if (tag != null) {
            uf.addPattern("item", Tags.TAGGED_WITH_TAG, tag);
        }
        Set<String> requiredKeys = new HashSet<String>();
        //requiredKeys are the labels, not the preds in the @context
        requiredKeys.add("GeoPoint");
        //column 0 must be identifier
        populateUnifier("item", uf, context, requiredKeys);

        //Get column name list before the is-deleted column is added
        List<String> names = uf.getColumnNames();
        log.debug(names);
        Table<Resource> results = null;
        try {
            results = TupeloStore.getInstance().unifyExcludeDeleted(uf, "item");
        } catch (Throwable e1) {
            log.error("Error getting Geo Features", e1);
            return Response.status(500).entity("Error getting Items that are Geo Features").build();
        }
        try {
            result = buildResultMap(results, context, names, true, new FilterCallback() {
                @Override
                public boolean include(Tuple<Resource> t) {
                    UriRef id = null;
                    if (t.get(0) == null) {
                        log.error("Missing identifier - skipping an item");
                    } else {
                        if (t.get(0).isLiteral()) {
                            id = Resource.uriRef(t.get(0).toString());
                        } else {
                            id = (UriRef) t.get(0);
                            log.warn("UriRef identifier:" + t.get(0).toString());
                        }
                        log.debug("U: " + userId.toString() + " O: " + id.toString());
                        if (isAccessible(userId, id)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    return false;
                }
            });
            return Response.status(200).entity(result).build();
        } catch (Exception e) {
            //No results just means an empty list, which is OK
            result.put("@context", context);
            return Response.status(200).entity(result).build();
        }
    }

    protected Response getItemsByMetadata(String pred, String type, String value, UriRef itemType, HttpServletRequest request) {
        Response r = null;
        Resource base = null;
        Resource relationship = null;
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        PermissionCheck p = new PermissionCheck(userId, Permission.VIEW_MEMBER_PAGES);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }

        Map<String, Object> context = layerBasics;
        if (itemType.equals(Cet.DATASET)) {
            context = datasetBasics;
        } else if (itemType.equals(CollectionBeanUtil.COLLECTION_TYPE)) {
            context = collectionBasics;
        }
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

                r = getMetadataByReverseRelationship((UriRef) base, relationship, context, userId, itemType);
            } else {
                return Response.status(403).entity("Related Item not accessible").build();
            }
        } else if (type.equals("literal")) {
            r = getMetadataByReverseRelationship(value, relationship, context, userId, itemType);
        }
        if (r == null) {
            r = Response.status(500).entity("Error getting information about " + base.toString()).build();

        }
        return r;

    }

    protected Response getTopLevelItems(Resource itemType, Map<String, Object> context, final UriRef userId) {

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        try {

            PermissionCheck p = new PermissionCheck(userId, Permission.VIEW_MEMBER_PAGES);
            if (!p.userHasPermission()) {
                return p.getErrorResponse();
            }

            Unifier uf = new Unifier();
            uf.addPattern("item", Rdf.TYPE, itemType);

            //column 0 must be identifier
            populateUnifier("item", uf, context, null);
            uf.addPattern(AddToCollectionHandler.TOP_LEVEL, AddToCollectionHandler.INCLUDES, "item");
            List<String> names = uf.getColumnNames();
            org.tupeloproject.util.Table<Resource> table = null;
            table = TupeloStore.getInstance().unifyExcludeDeleted(uf, "item");

            result = buildResultMap(table, context, names, true, new FilterCallback() {
                @Override
                public boolean include(Tuple<Resource> t) {

                    UriRef id = null;
                    if (t.get(0) == null) {
                        log.error("Missing identifier - skipping a dataset");
                    } else {
                        if (t.get(0).isLiteral()) {
                            id = Resource.uriRef(t.get(0).toString());
                        } else {
                            id = (UriRef) t.get(0);
                            log.warn("UriRef identifier:" + t.get(0).toString());
                        }
                        if (isAccessible(userId, id)) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    return false;
                }
            });
        } catch (Exception e) {
            log.debug("Exception during processing: ", e);
            return Response.status(500).entity("Error retrieving items").build();
        }
        return Response.status(200).entity(result).build();
    }

    protected Response getLatestItems(Resource itemType, Map<String, Object> context, final UriRef userId, final String dateColumn) {

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        try {

            PermissionCheck p = new PermissionCheck(userId, Permission.VIEW_MEMBER_PAGES);
            if (!p.userHasPermission()) {
                return p.getErrorResponse();
            }

            Unifier uf = new Unifier();
            uf.addPattern("item", Rdf.TYPE, itemType);

            //column 0 must be identifier
            populateUnifier("item", uf, context, null);
            uf.addOrderByDesc(dateColumn);
            uf.setLimit(100);
            List<String> names = uf.getColumnNames();
            org.tupeloproject.util.Table<Resource> table = null;
            table = TupeloStore.getInstance().unifyExcludeDeleted(uf, "item");

            result = buildResultMap(table, context, names, true, new FilterCallback() {
                @Override
                public boolean include(Tuple<Resource> t) {
                    UriRef id = null;
                    if (t.get(0) == null) {
                        log.error("Missing identifier - skipping a dataset");
                    } else {
                        if (t.get(0).isLiteral()) {
                            id = Resource.uriRef(t.get(0).toString());
                        } else {
                            id = (UriRef) t.get(0);
                            log.warn("UriRef identifier:" + t.get(0).toString());
                        }
                        if (isAccessible(userId, id)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            log.debug("Exception during processing: ", e);
            return Response.status(500).entity("Error retrieving items").build();
        }
        return Response.status(200).entity(result).build();
    }

    protected Response getItemTagsByIdAsJSON(String id, Resource type, HttpServletRequest request) {
        Response r;
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        List<String> tags = new ArrayList<String>();
        try {

            PermissionCheck p = new PermissionCheck(userId, Permission.VIEW_MEMBER_PAGES);
            if (!p.userHasPermission()) {
                return p.getErrorResponse();
            }

            UriRef itemId = Resource.uriRef(URLDecoder.decode(id, "UTF-8"));
            ValidItem item = new ValidItem(itemId, type, userId);
            if (!item.isValid()) {
                r = item.getErrorResponse();
            } else {
                Unifier uf = new Unifier();
                uf.addPattern(itemId, Tags.TAGGED_WITH_TAG, "tag");
                uf.addPattern("tag", Tags.HAS_TAG_TITLE, "title");
                uf.addColumnName("title");
                c.perform(uf);
                for (Tuple<Resource> row : uf.getResult() ) {
                    if (row.get(0) != null) {
                        tags.add(row.get(0).getString());
                    }
                }
                r = Response.status(200).entity(tags).build();
            }
        } catch (Exception e) {
            log.error("Error for " + id, e);
            e.printStackTrace();
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("Error", "Server error while retrieving tags for " + id);
            r = Response.status(500).entity(result).build();
        }
        return r;
    }

    protected Response addTagsToItem(String id, String tags, UriRef type, HttpServletRequest request) {
        Response r;
        try {
            UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

            PermissionCheck p = new PermissionCheck(userId, Permission.ADD_TAG);
            if (!p.userHasPermission()) {
                return p.getErrorResponse();
            }

            UriRef itemId = Resource.uriRef(URLDecoder.decode(id, "UTF-8"));
            ValidItem item = new ValidItem(itemId, type, userId);
            if (!item.isValid()) {
                r = item.getErrorResponse();
            } else {
                BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
                TagEventBeanUtil tebu = new TagEventBeanUtil(beanSession);
                Set<String> tagSet = getNormalizedTagSet(tags);
                log.debug("normalized tags = " + tagSet);
                tebu.addTags(Resource.uriRef(id), userId, tagSet);

                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("Tags Added", tagSet);
                r = Response.status(200).entity(result).build();
            }
        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("Error", "Server error while adding tags for " + id);
            r = Response.status(500).entity(result).build();
        }
        return r;
    }

    protected Response deleteTagsFromItem(String id, String tags, UriRef type, HttpServletRequest request) {
        Response r;
        try {
            UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

            PermissionCheck p = new PermissionCheck(userId, Permission.DELETE_TAG);
            if (!p.userHasPermission()) {
                return p.getErrorResponse();
            }
            UriRef itemId = Resource.uriRef(URLDecoder.decode(id, "UTF-8"));
            ValidItem item = new ValidItem(itemId, type, userId);
            if (!item.isValid()) {
                r = item.getErrorResponse();
            } else {
                BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
                TagEventBeanUtil tebu = new TagEventBeanUtil(beanSession);
                Set<String> tagSet = getNormalizedTagSet(tags);
                log.debug("normalized tags = " + tagSet);
                // use TagEventBeanUtil to remove tags from dataset
                tebu.removeTags(Resource.uriRef(id), tagSet);
                log.debug("Removed tags " + tagSet + " from " + id);

                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("Tags Deleted", tagSet);
                r = Response.status(200).entity(result).build();
            }
        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("Error", "Server error while deleting tags for " + id);
            r = Response.status(500).entity(result).build();
        }
        return r;
    }

    protected Response markItemAsDeleted(String encoded_id, UriRef type, HttpServletRequest request) {
        Response r;
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        Permission perm = Permission.DELETE_DATA;
        if (type.equals(CollectionBeanUtil.COLLECTION_TYPE)) {
            perm = Permission.DELETE_COLLECTION;
        }
        PermissionCheck p = new PermissionCheck(userId, perm);
        if (!p.userHasPermission()) {
            return p.getErrorResponse();
        }
        try {
            UriRef itemId = Resource.uriRef(URLDecoder.decode(encoded_id, "UTF-8"));

            ValidItem item = new ValidItem(itemId, type, userId);
            if (!item.isValid()) {
                r = item.getErrorResponse();
            } else {
                TripleWriter tw = new TripleWriter();

                //Remove any children
                TripleMatcher tm = new TripleMatcher();
                tm.match(itemId, DcTerms.HAS_PART, null);
                c.perform(tm);

                for (Triple t : tm.getResult() ) {
                    Resource childId = t.getObject();
                    tw.remove(itemId, DcTerms.HAS_PART, childId);
                    //Index as top level if no other parents

                    if (!AddToCollectionHandler.hasMoreParents((UriRef) childId, itemId, c)) {
                        tw.add(AddToCollectionHandler.TOP_LEVEL, AddToCollectionHandler.INCLUDES, childId);
                    }
                }

                //Mark as deleted
                tw.add(itemId, DcTerms.IS_REPLACED_BY, Rdf.NIL);
                c.perform(tw);

                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("Item Deleted", itemId.toString());
                r = Response.status(200).entity(result).build();
            }
        } catch (Exception e) {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("Error", "Server error while deleting " + encoded_id);
            r = Response.status(500).entity(result).build();
        }
        return r;
    }

    protected Response publishItem(String encoded_id, Resource type, long date, String pid, HttpServletRequest request) {
        Response r;
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        if (pid == null) {
            result.put("Missing", "pid");
            r = Response.status(400).entity(result).build();

        } else {

            UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
            PermissionCheck p = new PermissionCheck(userId, Permission.EDIT_METADATA);
            if (!p.userHasPermission()) {
                return p.getErrorResponse();
            }

            try {
                UriRef itemId = Resource.uriRef(URLDecoder.decode(encoded_id, "UTF-8"));
                UriRef itemPid = Resource.uriRef(URLDecoder.decode(pid, "UTF-8"));

                ValidItem item = new ValidItem(itemId, type, userId);
                if (!item.isValid()) {
                    r = item.getErrorResponse();
                } else {
                    TripleMatcher tMatcher = new TripleMatcher();

                    tMatcher.match(itemId, IsReadyForPublicationHandler.proposedForPublicationRef, null);
                    c.perform(tMatcher);
                    Set<Triple> triples = tMatcher.getResult();
                    TripleWriter tw = new TripleWriter();
                    if (triples.size() >= 1) {
                        tw.remove((Triple) triples.toArray()[0]);

                        Date theDate = new Date(date);
                        UriRef issued = Resource.uriRef(DCTerms.issued.getURI());
                        UriRef identifier = Resource.uriRef(DCTerms.identifier.getURI());
                        //Fixme - we can't delete non-string literals from the GUI since we lose type info along the way...
                        //So - using a string here
                        tw.add(itemId, issued, Resource.literal(DateFormat.getDateTimeInstance().format(theDate)));
                        tw.add(itemId, identifier, itemPid);

                        //2.0 support
                        Unifier uf = new Unifier();
                        uf.addPattern(itemId, DcTerms.HAS_VERSION, "agg");

                        uf.addPattern("agg", identifier, "ext", true);
                        uf.setColumnNames("agg", "ext");
                        c.perform(uf);

                        //Should only be the latest version that has yet to get a pid
                        for (Tuple<Resource> row : uf.getResult() ) {
                            if (row.get(1) == null) {
                                //Write required triples
                                tw.add(row.get(0), identifier, itemPid);
                                //Fixme - legacy use of date as string
                                tw.add(row.get(0), issued, Resource.literal(DateFormat.getDateTimeInstance().format(theDate)));
                            }
                        }
                        c.perform(tw);

                        ListUserMetadataFieldsHandler.addViewablePredicate(issued.toString());

                        result.put("Item Published", itemId.toString());
                        r = Response.status(200).entity(result).build();

                    } else {
                        result.put("Item Not Proposed For Publication", itemId.toString());
                        r = Response.status(409).entity(result).build();
                    }

                }
            } catch (Exception e) {
                result = new LinkedHashMap<String, Object>();
                result.put("Error", "Server error while publishing " + encoded_id);
                r = Response.status(500).entity(result).build();
            }
        }
        return r;
    }

    /* Publish a version (SEAD 2.0) - used to return the final success message and persistent ID assigned to the
     * published version. The agg_id is for the version - it is used to find the corresponding live collection.
     *
     * For 2.0, the persistent ID and publication date are assigned to the version rather than the collection itself.
     *
     * If the PID has already been used for a prior version, this one is considered as a replacement for the prior one
     *
     * Access is controlled by token, and publication should succeed even if the live collection has been deleted.
     */
    protected Response publishVersion(String agg_id, long date, String pid, HttpServletRequest request) {
        Response r;
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        pendingOREMaps.remove(agg_id);

        if (pid == null) {
            result.put("Missing", "pid");
            r = Response.status(400).entity(result).build();

        } else {

            UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

            try {

                //Find underlying collection - make sure it can be edited by user
                TripleMatcher tm1 = new TripleMatcher();
                UriRef newVersion = Resource.uriRef(agg_id);
                tm1.match(null, DcTerms.HAS_VERSION, newVersion);
                c.perform(tm1);
                Set<Triple> colTripleSet = tm1.getResult();
                UriRef itemId = (UriRef) colTripleSet.iterator().next().getSubject();

                TripleMatcher tMatcher = new TripleMatcher();

                tMatcher.match(itemId, IsReadyForPublicationHandler.proposedForPublicationRef, null);
                c.perform(tMatcher);
                Set<Triple> triples = tMatcher.getResult();
                TripleMatcher saltMatcher = new TripleMatcher();

                saltMatcher.match(newVersion, RequestPublicationHandler.hasSalt, null);
                c.perform(saltMatcher);
                Set<Triple> saltTriple = saltMatcher.getResult();

                TripleWriter tw = new TripleWriter();
                if (triples.size() >= 1) {
                    //Remove proposedForPub flag - we've completed publishing
                    tw.remove((Triple) triples.toArray()[0]);

                    //2.0 support

                    //Remove the salt to block further access to the keyed endpoints for getting data files and setting the pid
                    if (saltTriple.size() == 1) {
                        tw.remove((Triple) saltTriple.toArray()[0]);
                    }

                    UriRef identifier = Resource.uriRef(DCTerms.identifier.getURI());
                    UriRef issued = Resource.uriRef(DCTerms.issued.getURI());

                    Date theDate = new Date(date);
                    //Fixme - we can't delete non-string literals from the GUI since we lose type info along the way...
                    //So - using a string here
                    Literal now = Resource.literal(DateFormat.getDateTimeInstance().format(theDate));

                    //Determine if this is a new version or replaces an existing one
                    Unifier uf = new Unifier();
                    uf.addPattern(itemId, DcTerms.HAS_VERSION, "ver");
                    uf.addPattern("ver", identifier, "id");
                    uf.addPattern("ver", issued, "issued");
                    uf.addPattern("ver", RequestPublicationHandler.hasVersionNum, "num");
                    uf.setColumnNames("ver", "id", "issued", "num");
                    c.perform(uf);
                    Table<Resource> ROTable = uf.getResult();
                    Iterator<Tuple<Resource>> ROsIterator = ROTable.iterator();
                    boolean replacesExisting = false;
                    while (ROsIterator.hasNext() && !replacesExisting) {
                        Tuple<Resource> tuple = ROsIterator.next();
                        //if the pid MatchesPattern and we're not looking at the new RO
                        Resource oldVer = tuple.get(0);

                        if (tuple.get(1).toString().equals(pid) && (!(oldVer.toString().equals(agg_id)))) {
                            replacesExisting = true;
                            log.info(agg_id + " is replacing " + oldVer.toString());
                            //Then replace the old RO with this one

                            //1) current date becomes the modified date
                            tw.add(newVersion, DcTerms.DATE_MODIFIED, now);

                            //2) the original RO's issued date becomes the issued date for the new one
                            tw.add(newVersion, issued, tuple.get(2));

                            //3)existing version # is removed
                            TripleMatcher numMatcher = new TripleMatcher();
                            numMatcher.match(newVersion, RequestPublicationHandler.hasVersionNum, null);
                            c.perform(numMatcher);
                            Set<Triple> numTriple = numMatcher.getResult();
                            if (numTriple.size() == 1) {
                                tw.remove((Triple) numTriple.toArray()[0]);
                            }

                            //4) the old versions number becomes this versions #
                            tw.add(newVersion, RequestPublicationHandler.hasVersionNum, tuple.get(3));

                            //5) the old version is removed from the Collection and re-attaches as this RO replaces the old one
                            tw.remove(itemId, DcTerms.HAS_VERSION, oldVer);
                            tw.add(newVersion, Resource.uriRef(DCTerms.replaces.getURI()), oldVer);

                        }
                    }
                    //For new versions with a new ID, set the issued date
                    if (!replacesExisting) {
                        tw.add(newVersion, issued, now);
                    }
                    //For all cases, set the PID
                    tw.add(newVersion, identifier, Resource.uriRef(pid));

                    //To DO? Add published_as_part_of links
                    //FixMe - should not assume that collection has the same contents as when published and should
                    // retrieve final list from authoritative OREMap (wherever that ends up), or call a service dynamically
                    //rather than caching this info as a triple.

                    //Now perform all add/removes...
                    c.perform(tw);

                    ListUserMetadataFieldsHandler.addViewablePredicate(issued.toString());
                    if (!replacesExisting) {

                        result.put("Version of Collection Published", itemId.toString());
                    } else {
                        result.put("Version of Collection Updated", itemId.toString());
                    }
                    r = Response.status(200).entity(result).build();

                } else {
                    result.put("Collection Not Proposed For Publication", itemId.toString());
                    r = Response.status(409).entity(result).build();
                }

            } catch (Exception e) {

                log.error("Error assigning pid: " + pid + " to " + agg_id);
                log.error(e.getMessage());
                e.printStackTrace();
                result = new LinkedHashMap<String, Object>();
                result.put("Error", "Server error while publishing " + agg_id);
                r = Response.status(500).entity(result).build();
            }
        }
        return r;
    }

    protected void export(UriRef uri, UriRef userId, OutputStream os) {

        ZipOutputStream zos = new ZipOutputStream(os);

        BlobFetcher bf = new BlobFetcher(uri);
        try {
            c.perform(bf);
        } catch (OperatorException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        InputStream is = bf.getInputStream();
        ZipEntry ze = new ZipEntry("exportedfile).dat");
        try {
            zos.putNextEntry(ze);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            IOUtils.copy(is, zos);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            is.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            zos.closeEntry();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //metadata...
        ZipEntry ze2 = new ZipEntry("exportedfile.json-ld");
        try {
            zos.putNextEntry(ze2);
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        Response metaResponse = getItemMetadataAsJSON(uri.toString(), userId, false);
        String metaString = metaResponse.getEntity().toString();

        StringReader metaReader = new StringReader(metaString);
        try {
            IOUtils.copy(metaReader, zos);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        metaReader.close();
        try {
            zos.closeEntry();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        //remember close it
        try {
            zos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    protected static Response getStats(String id, HttpServletRequest request) {
        Response r;
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        try {
            if (!rbac.checkPermission(userId, Resource.uriRef(Permission.VIEW_SYSTEM.getUri()))) {
                return Response.status(401).entity("Access to this endpoint is access controlled.").build();
            }
        } catch (RBACException e1) {
            log.error("Error running sys info: ", e1);
            return Response.status(500).entity("Error running sys info [" + e1.getMessage() + "]").build();
        }

        UriRef itemId;
        try {
            itemId = Resource.uriRef(URLDecoder.decode(id, "UTF-8"));

            //Get Dataset info
            CollectionInfo ci = getCollectionInfo(itemId, 0);

            result.put("Identifier", itemId.toString());
            result.put("Total Size", "" + ci.getSize());
            result.put("Number of Collections", "" + ci.getNumCollections());
            result.put("Number of Datasets", "" + ci.getNumDatasets());
            result.put("Max Dataset Size", "" + ci.getMaxDatasetSize());
            result.put("Max Collection Depth", "" + ci.getMaxDepth());
            result.put("Data Mimetypes", ci.getMimetypeSet());

            result.put("@context", collectionStats);

        } catch (UnsupportedEncodingException e) {
            log.error("Error decoding: " + id, e);
            return Response.status(Status.BAD_REQUEST).entity("Error decoding id: " + id + " [" + e.getMessage() + "]").build();
        } catch (ActionException ae) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error retrieiving stats for: " + id + " [" + ae.getMessage() + "]").build();
        }

        return Response.status(Status.OK).entity(result).build();

    }

    public static CollectionInfo getCollectionInfo(UriRef itemId, int currentDepth) throws ActionException {
        CollectionInfo info = new CollectionInfo();
        info.setMaxDepth(currentDepth++);
        info.incrementNumCollections(1); //For root collection
        //Get Datasets

        Unifier uf = new Unifier();
        uf.addPattern("ds", Rdf.TYPE, Cet.DATASET);
        uf.addPattern("ds", Files.LENGTH, "size");
        uf.addPattern("ds", Dc.FORMAT, "mimetype");
        uf.addPattern(itemId, DcTerms.HAS_PART, "ds");
        uf.setColumnNames("ds", "mimetype", "size");
        try {

            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(uf, "ds") ) {

                if (row.get(2) != null) {
                    long size = Long.parseLong(row.get(2).getString());
                    if (size < -3) {
                        size += SystemInfoHandler.UNSIGNED_INT;
                    }
                    info.increaseSize(size);
                    info.setMaxDatasetSize(info.getMaxDatasetSize() >= size ? info.getMaxDatasetSize() : size);
                } else {
                    log.error("Dataset without size: " + row.get(0).toString());
                }
                info.addMimetype(row.get(1).toString());
                info.incrementNumDatasets(1);
            }
        } catch (OperatorException e) {
            throw (new ActionException("Could not count datasets."));
        }

        //Get subcollections

        Unifier uf2 = new Unifier();
        log.debug("Counting Collections");
        uf2.addPattern("cl", Rdf.TYPE, Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/Collection"));
        uf2.addPattern(itemId, DcTerms.HAS_PART, "cl");
        uf2.setColumnNames("cl");
        try {

            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(uf2, "cl") ) {
                CollectionInfo ci = getCollectionInfo((UriRef) row.get(0), currentDepth);
                info.addMimetypes(ci.getMimetypeSet());
                info.increaseSize(ci.getSize());
                info.incrementNumDatasets(ci.getNumDatasets());
                info.incrementNumCollections(ci.getNumCollections());
                info.setMaxDatasetSize(info.getMaxDatasetSize() >= ci.getMaxDatasetSize() ? info.getMaxDatasetSize() : ci.getMaxDatasetSize());
                info.setMaxDepth(info.getMaxDepth() >= ci.getMaxDepth() ? info.getMaxDepth() : ci.getMaxDepth());
            }
        } catch (OperatorException e) {
            throw (new ActionException("Error counting subcollections of: " + itemId));
        }

        return info;
    }

    protected static Set<String> getNormalizedTagSet(String cdl) {
        Set<String> tagSet = new HashSet<String>();
        for (String s : cdl.split(",") ) {
            tagSet.add(s.trim().replaceAll("  +", " ").toLowerCase());
        }
        return tagSet;
    }

    static private ConcurrentHashMap<String, Object> pendingOREMaps = new ConcurrentHashMap<String, Object>();

    public static void removeMap(String id, boolean force) {
        if (log.isDebugEnabled() && !force) {
            log.debug("Debug: Retaining map for ID (a potential memory leak): " + id);
        } else {
            log.info("Remove map for ID: " + id);
            pendingOREMaps.remove(id);
        }
    }

    @SuppressWarnings("unchecked")
    Response getOREById(String id, HttpServletRequest request) {
        log.debug("OREMap request for ID: " + id);
        Object o = pendingOREMaps.get(id);

        if (o == null) {
            log.debug("Null for ID: " + id);
            return Response.status(Status.NOT_FOUND).build();

        } else {
            // log.debug("OREMap requested, clearing ID: " + id);
            // pendingOREMaps.remove(id);
            return (Response) o;
        }
    }

    public static void generateOREById(String id, String idUri) {
        log.debug("Generating map for ID: " + id);
        Map<String, Object> oremap = new LinkedHashMap<String, Object>();
        try {
            log.debug("Generating OREMap for : " + id);
            oremap.put("@id", idUri);
            oremap.put("@type", "ResourceMap");
            //Add info about map creation

            //Find connect to live objects
            Unifier uf = new Unifier();
            uf.addPattern("coll", DcTerms.HAS_VERSION, Resource.uriRef(id));
            uf.addColumnName("coll");

            c.perform(uf);
            UriRef topCollRef = null;
            for (Tuple<Resource> row : uf.getResult() ) {
                topCollRef = (UriRef) row.get(0);
                break;
            }

            if (topCollRef == null) {
                pendingOREMaps.put(id, Response.status(Status.NOT_FOUND).build());
                return;
            }

            //Find out how many times published
            TripleMatcher tMatcher = new TripleMatcher();
            tMatcher.match(topCollRef, DcTerms.HAS_VERSION, null);
            c.perform(tMatcher);
            String versionNumber = Integer.toString(tMatcher.getResult().size());

            //Find salt for acccess pubtokens
            tMatcher = new TripleMatcher();
            tMatcher.match(Resource.uriRef(id), RequestPublicationHandler.hasSalt, null);
            c.perform(tMatcher);
            String salt = tMatcher.getResult().iterator().next().getObject().toString();

            log.debug("Found Collection: " + topCollRef.toString());
            Map<String, Object> combined = getCombinedContext(false);
            combined.put("Has Part", DcTerms.HAS_PART.toString());
            Map<String, Object> inverseMap = getCombinedInverseContext(false);
            inverseMap.put(DcTerms.HAS_PART.toString(), "Has Part");
            Map<String, Object> agg = getMetadataMapById(topCollRef, combined, inverseMap);
            agg.remove("@context");
            //The aggregation has an ID in the space, don't need to create a <collectionid>/v<x> style identifier as we do for aggregated things
            agg.put("Identifier", id);

            agg.put("@id", idUri + "#aggregation");

            /*Manage versions - when the live object (DCterms.HAS_VERSION) has versions,
             * this Aggregation should (http://www.w3.org/ns/prov#wasRevisionOf) have prior versions,
             * and this list should not include itself, or test pubs if we can catch them
             */
            Object ver = agg.get("Version");
            List<String> priorVersions = new ArrayList<String>();
            if (ver != null) {
                if (ver instanceof String) {
                    //Should be the ID of this version - just needs to be dropped
                    if (!((String) ver).equals(id)) {
                        log.warn("Unexpected Version: " + ver);
                    }
                } else if (ver instanceof Map) {
                    for (int i = 0; i < ((List<String>) ver).size(); i++ ) {
                        String aVersion = ((List<String>) ver).get(i);
                        if (!aVersion.equals(id)) {
                            //Look for pid and use it if it exists and is not a test DOI:
                            TripleMatcher pidMatcher = new TripleMatcher();
                            pidMatcher.match(Resource.uriRef(id), Resource.uriRef(DCTerms.identifier.getURI()), null);
                            c.perform(pidMatcher);
                            Set<Triple> triples = pidMatcher.getResult();
                            if (!triples.isEmpty()) {
                                String pid = triples.iterator().next().getObject().toString();
                                //Check for old and new styles
                                if (!(pid.startsWith("http://dx.doi.org/10.5072/FK") || pid.startsWith("http://doi.org/10.5072/FK"))) {
                                    priorVersions.add(pid);
                                }
                            }
                        }
                    }
                }
            }
            agg.remove("Version");
            if (priorVersions.size() > 0) {
                agg.put("has prior version", priorVersions);
                //Also need to add this term to the context below: seadContext.put("has prior version", "http://www.w3.org/ns/prov#wasRevisionOf");
            }

            //Remove Proposed For Publication metadata, which also applies only to the live version

            agg.remove("Proposed for Publication");

            List<String> types = new ArrayList<String>(2);
            types.add("Aggregation");
            types.add("http://cet.ncsa.uiuc.edu/2007/Collection");
            agg.put("@type", types);

            agg.put("Is Version Of", topCollRef.toString());

            agg.put("Publishing Project", RequestPublicationHandler.getProjectID());
            agg.put("Publishing Project Name", TupeloStore.getInstance().getConfiguration(ConfigurationKey.ProjectName));

            //Further Munge - return tags as strings, not uris
            recreateTagStrings(agg);

            agg.put("similarTo", idUri.substring(0, idUri.indexOf("/researchobjects")) + "/collections/" + URLEncoder.encode(topCollRef.toString(), "UTF-8"));
            oremap.put("describes", agg);
            log.debug("OREMAP started: " + oremap.toString());
            //Now add all the children and their info
            //Collections
            agg.put("aggregates", new ArrayList<Object>());
            addSubCollectionsToAggregation(topCollRef, agg, agg, versionNumber, salt);

            if (((List<Object>) agg.get("aggregates")).isEmpty()) {
                agg.remove("aggregates");
            }
            //Add metadata about map
            oremap.put("Rights", "This Resource Map is available under the Creative Commons Attribution-Noncommercial Generic license.");
            oremap.put("Creation Date", DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())));
            List<String> creatorsList = new ArrayList<String>();
            creatorsList.add("SEAD Version 1.5, " + PropertiesLoader.getProperties().getProperty("domain"));
            oremap.put("Creator", creatorsList);

            //add contexts;
            List<Object> contextList = new ArrayList<Object>();
            contextList.add("https://w3id.org/ore/context");

            Map<String, Object> seadContext = getCombinedContext(false);
            seadContext.put("Is Version Of", DcTerms.IS_VERSION_OF.toString());
            seadContext.put("Has Part", DcTerms.HAS_PART.toString());
            seadContext.put("Publishing Project", "http://sead-data.net/terms/publishingProject");
            seadContext.put("Publishing Project Name", "http://sead-data.net/terms/publishingProjectName");

            //Version is only for live object
            seadContext.remove("Version");
            seadContext.put("has prior version", "http://www.w3.org/ns/prov#wasRevisionOf");
            //Proposed for Publication is only for the live object
            seadContext.remove("Proposed for Publication");

            contextList.add(seadContext);
            oremap.put("@context", contextList);

        } catch (InterruptedException ie) {
            pendingOREMaps.put(id, Response.status(Status.NOT_FOUND).build());
            return;

        } catch (Exception e) {

            e.printStackTrace();

            log.error(e.getLocalizedMessage(), e);
            Map<String, String> failmsgMap = new HashMap<String, String>();
            failmsgMap.put("Failure", "Unable to generate ORE Map - see log for details");
            pendingOREMaps.put(id, Response.status(Status.INTERNAL_SERVER_ERROR).entity(failmsgMap).build());
            return;
        }
        pendingOREMaps.put(id, Response.ok().entity(oremap).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_TYPE + "; charset=utf-8").build());
    }

    @SuppressWarnings("unchecked")
    private static void addSubCollectionsToAggregation(UriRef collId, Map<String, Object> agg, Map<String, Object> parent, String version, String salt) throws JSONException, OperatorException, InterruptedException, UnsupportedEncodingException {
        Set<UriRef> subcollections = getSubCollections(collId);
        log.debug("Adding collection: " + collId.toString());
        //Should not exist but some space may have this term - so remove it to be safe
        parent.remove("Has Part");
        //Handle sub collections
        for (UriRef collection : subcollections ) {
            Map<String, Object> combined = getCombinedContext(false); //may refresh inverse
            Map<String, Object> aggRes = getMetadataMapById(collection, combined, getCombinedInverseContext(false));
            aggRes.remove("@context");
            //Munge to separate static and live versions
            aggRes.put("Identifier", collection.toString() + "/v" + version);
            //Duplicate as @id which is used by jsonld to rdf converters as the node ID
            aggRes.put("@id", collection.toString() + "/v" + version);

            // Further Munge - return tags as strings, not uris
            recreateTagStrings(aggRes);

            List<String> types = new ArrayList<String>(2);
            types.add("AggregatedResource");
            types.add("http://cet.ncsa.uiuc.edu/2007/Collection");
            aggRes.put("@type", types);

            aggRes.put("Is Version Of", collection.toString());
            ((List<Object>) agg.get("aggregates")).add(aggRes);

            if (parent.get("Has Part") == null) {
                parent.put("Has Part", new ArrayList<String>());
            }
            ((List<String>) parent.get("Has Part")).add(collection.toString() + "/v" + version);

            log.debug("Adding subcol: " + collection.toString());
            addSubCollectionsToAggregation(collection, agg, aggRes, version, salt);
        }
        //Handle Datasets
        Set<UriRef> datasets = getDatasets(collId);
        for (UriRef dataset : datasets ) {
            log.debug("Adding dataset: " + dataset.toString());
            Map<String, Object> combined = getCombinedContext(false); //may refresh inverse
            Map<String, Object> aggRes = getMetadataMapById(dataset, combined, getCombinedInverseContext(false));
            aggRes.remove("@context");
            //Munge to separate static and live versions
            aggRes.put("Identifier", dataset.toString() + "/v" + version);
            //Duplicate as @id which is used by jsonld to rdf converters as the node ID
            aggRes.put("@id", dataset.toString() + "/v" + version);

            //Further Munge - return tags as strings, not uris
            recreateTagStrings(aggRes);

            aggRes.put("Is Version Of", dataset.toString());

            String urlString = PropertiesLoader.getProperties().getProperty("domain");
            String path = "/researchobjects/" + agg.get("Identifier") + "/files/" + URLEncoder.encode(dataset.toString(), "UTF-8");

            aggRes.put("similarTo", urlString + "/resteasy" + path + "?pubtoken=" + TokenStore.generateToken(path, salt));

            List<String> types = new ArrayList<String>(2);
            types.add("AggregatedResource");
            types.add(Cet.DATASET.toString());
            aggRes.put("@type", types);

            ((List<Object>) agg.get("aggregates")).add(aggRes);
            if (parent.get("Has Part") == null) {
                parent.put("Has Part", new ArrayList<String>());
            }
            ((List<String>) parent.get("Has Part")).add(dataset.toString() + "/v" + version);

        }
    }

    // - should go find the
    // http://www.holygoat.co.uk/owl/redwood/0.1/tags/name, but we know that tag uris just prepend
    // tag:cet.ncsa.uiuc.edu,2008:/tag# and swap ' ' with '+', so we can reverse that here
    //without another call
    private static void recreateTagStrings(Map<String, Object> aggRes) {
        Object o = aggRes.get("Keyword");
        if (o instanceof String) {
            aggRes.put("Keyword", tagString((String) o));
        } else if (o instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> l = ((List<String>) o);
            for (int i = 0; i < l.size(); i++ ) {
                l.set(i, tagString((l.get(i))));
            }
        }
    }

    private static String tagString(String tagUri) {
        String tagString = tagUri;
        if (tagUri.startsWith("tag:cet.ncsa.uiuc.edu,2008:/tag#")) {
            tagString = tagUri.substring("tag:cet.ncsa.uiuc.edu,2008:/tag#".length());
            tagString = tagString.replaceAll("\\+", " ");
        }
        return tagString;
    }

    private static Set<UriRef> getSubCollections(UriRef parent) throws OperatorException {
        Set<UriRef> colls = new HashSet<UriRef>();
        Unifier uf2 = new Unifier();
        uf2.addPattern("coll", Rdf.TYPE, Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/Collection"));
        uf2.addPattern(parent, DcTerms.HAS_PART, "coll");
        uf2.setColumnNames("coll");

        for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(uf2, "coll") ) {
            colls.add((UriRef) row.get(0));
        }
        return colls;
    }

    private static Set<UriRef> getDatasets(UriRef parent) throws OperatorException {
        Set<UriRef> datasets = new HashSet<UriRef>();
        Unifier uf2 = new Unifier();
        uf2.addPattern("ds", Rdf.TYPE, Cet.DATASET);
        uf2.addPattern(parent, DcTerms.HAS_PART, "ds");
        uf2.setColumnNames("ds");

        for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(uf2, "ds") ) {
            datasets.add((UriRef) row.get(0));
        }
        return datasets;
    }

    private Map<String, Object> getPubContext() {
        Map<String, Object> contextMap = new LinkedHashMap<String, Object>();
        //Needed to preserve insertion order (id first)
        for (String key : itemBiblio.keySet() ) {
            contextMap.put(key, itemBiblio.get(key));
        }
        contextMap.putAll(publishedVersions);
        return contextMap;
    }

    //Return JSON-LD with basic biblio for a single collection that has been published via 1.5 or 2.0
    public Response getPublishedROsByCollection(String id, HttpServletRequest request) {

        //Check Perms:
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        try {
            if (!rbac.checkPermission(userId, Resource.uriRef(Permission.VIEW_PUBLISHED.getUri()))) {
                return Response.status(401).entity("Access to this endpoint is access controlled.").build();
            }
        } catch (RBACException e1) {
            log.error("Error running sys info: ", e1);
            return Response.status(500).entity("Error running sys info [" + e1.getMessage() + "]").build();
        }

        //Verify that this is a published collection
        try {
            TripleMatcher tm = new TripleMatcher();
            tm.setPredicate(DcTerms.HAS_VERSION);
            tm.setSubject(Resource.uriRef(id));
            c.perform(tm);
            if (tm.getResult().isEmpty()) {
                if (!haveParentsBeenPublished(id)) {
                    return Response.status(Status.BAD_REQUEST).entity(id + "has not been published").build();
                }
            }
        } catch (Throwable e) {
            log.error("Error getting published collection: " + id, e);
            e.printStackTrace();
            return Response.status(500).entity("Error getting published collection" + id).build();
        }

        Map<String, Object> collMap = getMetadataMapById(id, getPubContext(), userId);

        return Response.ok().entity(collMap).build();
    }

    private boolean haveParentsBeenPublished(String id) {
        //Recurse up through parents, check for version, add new ancestors to list and repeat - ignore top level
        //Return true at first published ancestor
        HashSet<UriRef> ancestorsHashSet = new HashSet<UriRef>();
        ancestorsHashSet.add(Resource.uriRef(id));
        while (!ancestorsHashSet.isEmpty()) {
            for (UriRef resource : ancestorsHashSet ) {
                Unifier uf = new Unifier();
                uf.addPattern("parent", DcTerms.HAS_PART, resource);
                uf.addPattern("parent", DcTerms.HAS_VERSION, "ver", true);
                uf.setColumnNames("parent", "ver");
                try {
                    Table<Resource> results = TupeloStore.getInstance().unifyExcludeDeleted(uf, "parent");
                    for (Tuple<Resource> tuple : results ) {
                        if (tuple.get(1) != null) {
                            return true;
                        } else {
                            UriRef rent = (UriRef) tuple.get(0);
                            if (!rent.equals(AddToCollectionHandler.TOP_LEVEL)) {
                                ancestorsHashSet.add(rent);
                            }
                        }
                    }
                    ancestorsHashSet.remove(resource);

                } catch (OperatorException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    //Return JSON-LD with basic biblio for any collection that has been published via 1.5 or 2.0
    public Response getPublishedROsByCollection(HttpServletRequest request) {

        //Check Perms:
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));

        try {
            if (!rbac.checkPermission(userId, Resource.uriRef(Permission.VIEW_PUBLISHED.getUri()))) {
                return Response.status(401).entity("Access to this endpoint is access controlled.").build();
            }
        } catch (RBACException e1) {
            log.error("Error running sys info: ", e1);
            return Response.status(500).entity("Error running sys info [" + e1.getMessage() + "]").build();
        }

        Map<String, Map<String, Object>> collMap = new LinkedHashMap<String, Map<String, Object>>();
        /*


                //Find 1.5 pubs
                //Any collection with a DOI(s) as dcterms identifier
                Unifier uf = new Unifier();
                uf.addPattern("coll", Resource.uriRef(DCTerms.identifier.toString()), "doi");
                uf.addPattern("coll", Resource.uriRef(DCTerms.issued.toString()), "date");
                uf.setColumnNames("coll", "doi", "date");

                try {
                    c.perform(uf);
                } catch (Throwable e1) {
                    log.error("Error getting published collections", e1);
                    e1.printStackTrace();
                    return Response.status(500).entity("Error getting published collections").build();
                }
                //FixMe - trat deleted ones differently (i.e. don't try to show live versions, so need to send isdeleted flag...)
                for(Tuple tu: uf.getResult()) {
                    String coll =  tu.get(0).toString();
                    String doi = tu.get(1).toString();
                    String date = tu.get(2).toString();
                    if(collMap.containsKey(coll)) {
                        //Add version
                        addVersionToCollection(collMap.get(coll), doi, -1, "<=" + date);
                    } else {
                        //First time - add collection and its metadata
                        addCollectionAndVersion(collMap.get("coll"), doi, 1, "<=" + date);
                    }
                }
          */

        //Find collections with published versions
        //collections with a version
        TripleMatcher tm = new TripleMatcher();
        tm.setPredicate(DcTerms.HAS_VERSION);

        Map<String, Object> contextMap = getPubContext();
        contextMap.put("IsReplacedBy", "http://purl.org/dc/terms/isReplacedBy");

        Set<UriRef> collections = new HashSet<UriRef>();
        try {
            c.perform(tm);
            for (Triple triple : tm.getResult() ) {
                collections.add((UriRef) triple.getSubject());

            }

            for (UriRef coll : collections ) {
                Map<String, Object> nextResultMap = getMetadataMapById(coll.toString(), contextMap, userId);
                //Only need one at the top
                nextResultMap.remove("@context");
                collMap.put(coll.toString(), nextResultMap);
            }
        } catch (Throwable e1) {
            log.error("Error getting published collections", e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting published collections").build();
        }

        //Any collection with an Aggregation that has dcterms identifier entry (an external persistent identifier)

        //Add version stuff to context
        collMap.put("@context", contextMap);
        return Response.ok().entity(collMap).build();
    }

}
