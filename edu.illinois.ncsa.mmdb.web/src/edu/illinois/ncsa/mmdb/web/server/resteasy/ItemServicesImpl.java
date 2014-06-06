package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Encoded;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Namespaces;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListNamedThingsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Metadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataField;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.Memoized;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListRelationshipTypesHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListUserMetadataFieldsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.SetRelationshipHandlerNew;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

public class ItemServicesImpl
{
    /** Commons logging **/
    private static Log                         log               = LogFactory.getLog(ItemServicesImpl.class);

    public static UriRef                       SHA1_DIGEST       = Resource.uriRef("http://sead-data.net/terms/hasSHA1Digest");

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
                                                                         Namespaces.rdfs("label")
                                                                         ));

    //FixME - the labels used here should come from the database, but only user and extracted metadata have such labels right now (not all basic/biblio) - 
    // should switch to just listing predicates here and pulling labels once all exist.

    @SuppressWarnings("serial")
    protected static final Map<String, Object> datasetBasics     = new LinkedHashMap<String, Object>() {
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
    protected static final Map<String, Object> collectionBasics  = new LinkedHashMap<String, Object>() {
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
                                                                     {
                                                                         put("Identifier", Dc.IDENTIFIER.toString());
                                                                         put("License", "http://purl.org/dc/terms/license");
                                                                         put("Rights Holder", "http://purl.org/dc/terms/rightsHolder");
                                                                         put("Rights", "http://purl.org/dc/terms/rights");
                                                                         put("Data Creation Date", Dc.DATE.toString());
                                                                         put("Creation Date", Namespaces.dcTerms("created"));
                                                                         put("Size", Files.LENGTH.toString());
                                                                         put("Label", Namespaces.rdfs("label"));
                                                                         put("Mimetype", Dc.FORMAT.toString());
                                                                         put("Description", org.tupeloproject.rdf.terms.Dc.DESCRIPTION.toString());
                                                                         put("Title", Dc.TITLE.toString());
                                                                         put("Uploaded By", Dc.CREATOR.toString());
                                                                         put("Abstract", Namespaces.dcTerms("abstract"));
                                                                         put("Contact", "http://sead-data.net/terms/contact");
                                                                         put("Creator", Namespaces.dcTerms("creator"));
                                                                         put("Publication Date", Namespaces.dcTerms("issued"));

                                                                     }
                                                                 };

    static Context                             c                 = TupeloStore.getInstance().getContext();
    static SEADRbac                            rbac              = new SEADRbac(TupeloStore.getInstance().getContext());

    protected Response uploadMetadata(String id, MultipartFormDataInput input, HttpServletRequest request) {
        UriRef creator = Resource.uriRef((String) request.getAttribute("userid"));
        try {
            id = URLDecoder.decode(id, "UTF-8");
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

    public static List<Metadata> listExtractedMetadataFields() {

        List<Metadata> metadata = new ArrayList<Metadata>();
        Unifier uf = new Unifier();
        uf.addPattern("predicate", Rdf.TYPE, MMDB.METADATA_TYPE); //$NON-NLS-1$
        uf.addPattern("predicate", MMDB.METADATA_CATEGORY, "category"); //$NON-NLS-1$ //$NON-NLS-2$
        uf.addPattern("predicate", Rdfs.LABEL, "label"); //$NON-NLS-1$ //$NON-NLS-2$
        uf.setColumnNames("label", "predicate", "category"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        try {
            TupeloStore.getInstance().getContext().perform(uf);

            for (Tuple<Resource> row : uf.getResult() ) {
                if (row.get(0) != null) {
                    log.debug("extracted fields found: " + row.get(0).getString());
                    log.debug(row.get(1).getString());
                    log.debug(row.get(2).getString());
                    metadata.add(new Metadata(row.get(2).getString(), row.get(0).getString(), row.get(1).getString()));
                }
            }
        } catch (OperatorException e1) {
            log.error("Error getting Extracted Metadata list", e1);
        }
        return metadata;
    }

    public Response getItemMetadataAsJSON(String id, UriRef userId) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error decoding id: " + id).build();
        }
        UriRef itemUri = Resource.uriRef(id);

        if (!isAccessible(userId, itemUri)) {

            result.put("Error", "Item not accessible");
            return Response.status(403).entity(result).build();
        }

        TripleMatcher tm = new TripleMatcher();
        tm.setSubject(itemUri);
        try {
            TupeloStore.getInstance().getContext().perform(tm);
        } catch (OperatorException e) {
            log.error("Error getting metadata for id: " + id + " : " + e.getMessage());
            return Response.status(500).entity("Error getting information about " + id).build();
        }
        Map<UriRef, String> metadataMap = getPredToLabelMap();
        for (Triple t : tm.getResult() ) {
            UriRef pred = (UriRef) t.getPredicate();
            if (metadataMap.containsKey(pred)) {
                result.put(metadataMap.get(pred), t.getObject().toString());
            }
        }
        if (result.isEmpty()) {
            return Response.status(404).entity("Item " + id + " Not Found.").build();
        }
        return Response.status(200).entity(result).build();
    }

    private static Memoized<Map<UriRef, String>> predToLabelMap = null;

    public static Map<UriRef, String> getPredToLabelMap() {

        if (predToLabelMap == null) {
            predToLabelMap = new Memoized<Map<UriRef, String>>() {
                @SuppressWarnings("unchecked")
                public Map<UriRef, String> computeValue() {

                    Map<UriRef, String> metadataMap = new LinkedHashMap<UriRef, String>();

                    //1) Biblio triples
                    for (Entry<String, Object> e : itemBiblio.entrySet() ) {
                        String pred;
                        Object temp = e.getValue();
                        if (temp instanceof String) {
                            pred = (String) temp;
                        } else {
                            pred = ((Map<String, String>) temp).get("@id");
                        }
                        metadataMap.put(Resource.uriRef(pred), e.getKey());
                    }
                    //2) User metadata fields
                    ListUserMetadataFieldsResult lmuf = ListUserMetadataFieldsHandler.listUserMetadataFields(false);
                    for (UserMetadataField umf : lmuf.getFields() ) {
                        metadataMap.put(Resource.uriRef(umf.getUri()), umf.getLabel());
                    }
                    //3) relationships
                    try {
                        ListNamedThingsResult result = relationships.execute(null, null);

                        for (Entry<String, String> entry : result.getThingNames().entrySet() ) {
                            metadataMap.put(Resource.uriRef(entry.getKey()), entry.getValue());
                        }
                    } catch (ActionException e1) {
                        log.error("Error retrieving relationship predicates" + e1);
                    }
                    //4) Extracted metadata
                    List<Metadata> extracted = listExtractedMetadataFields();
                    for (Metadata md : extracted ) {
                        metadataMap.put(Resource.uriRef(md.getValue()), md.getLabel());
                    }
                    return metadataMap;
                }
            };
        }

        predToLabelMap.setTtl(60 * 60 * 1000); // 1 hour
        return predToLabelMap.getValue();
    }

    protected boolean isPermitted(String userId, Permission permission, String objectUri) {
        Resource userUri = userId == null ? PersonBeanUtil.getAnonymousURI() : Resource.uriRef(userId);
        Resource permissionUri = Resource.uriRef(permission.getUri());
        Resource oUri = objectUri != null ? Resource.uriRef(objectUri) : null;
        try {
            if (!rbac.checkPermission(userUri, oUri, permissionUri)) {
                return false;
            }
        } catch (RBACException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        try {
            id = URLDecoder.decode(id, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            log.error("Error decoding url for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error decoding id: " + id).build();
        }
        UriRef itemUri = Resource.uriRef(id);

        if (!isAccessible(userId, itemUri)) {

            result.put("Error", "Item not accessible");
            return Response.status(403).entity(result).build();
        }

        //Get fields
        Unifier uf = new Unifier();
        uf = populateUnifier(itemUri, uf, context);
        //Get columns to put in result (all in this case)
        List<String> names = uf.getColumnNames();
        log.debug("names: " + names);
        try {
            c.perform(uf);
            log.debug("Performed");
        } catch (Throwable e1) {
            log.error("Error getting requested metadata for " + id, e1);
            e1.printStackTrace();
            return Response.status(500).entity("Error getting information about " + id).build();
        }
        try {

            result = buildResultMap(uf.getResult(), context, names, false, null);
            return Response.status(200).entity(result).build();
        } catch (Exception e) {
            log.debug(e.getMessage());

            return Response.status(404).entity("Item " + id + " Not Found.").build();
        }

    }

    /* Adds patterns corresponding to each entry in the context to the Unifier prior to evaluation
     * @thing  - can be a UriRef, or a String (making it a variable in the query).
     * 
     * 
     */
    @SuppressWarnings("unchecked")
    protected static Unifier populateUnifier(Object thing, Unifier uf, Map<String, Object> context) {
        /*FixME: Note: if the first pattern of an all optional unifier doesn't match, tupelo will return no results
         * even if the other terms would match. So - all contexts should be LinkedHashMaps / other order preserving map
         * and the first entry should be a predicate that is known to be assigned (e.g. dc:identifier). 
         */

        for (String key : context.keySet() ) {
            String pred = null;
            Object temp = context.get(key);
            if (temp instanceof String) {
                pred = (String) temp;
            } else {
                pred = ((Map<String, String>) temp).get("@id");
            }
            UriRef predRef = Resource.uriRef(pred);
            if (thing instanceof UriRef) {
                log.debug("Adding pattern for id: " + ((UriRef) thing).toString() + " " + predRef.toString() + " " + key);
                uf.addPattern(thing, predRef, key, true);

            } else {
                log.debug("Adding pattern: " + thing + " " + predRef.toString() + " " + key);
                uf.addPattern(thing, predRef, key, true);

            }
            uf.addColumnName(key);
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
    protected static Map<String, Object> buildResultMap(Table<Resource> table, Map<String, Object> context, List<String> names, boolean useHierarchy, FilterCallback filter) throws Exception {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        for (Tuple<Resource> tu : table ) {

            if ((filter == null) || filter.include(tu)) {

                for (int i = 0; i < names.size(); i++ ) {
                    String key = names.get(i);
                    Resource o = tu.get(i);
                    if (o != null) {
                        String obj = tu.get(i).toString();
                        if (useHierarchy) {
                            //add submap for this id
                            if (i == 0) {
                                if (!result.containsKey(obj)) {
                                    result.put(obj, new LinkedHashMap<String, Object>());

                                }
                                //For i=0, add value as  metadata if the key is "Identifier"  
                                if (key.equals("Identifier")) {
                                    addTuple((Map<String, Object>) result.get(tu.get(0).toString()), key, obj);
                                }
                            } else {

                                addTuple((Map<String, Object>) result.get(tu.get(0).toString()), key, obj);
                            }
                        } else {
                            addTuple(result, key, obj);
                        }
                    }
                }
            }
        }
        if (result.isEmpty()) {
            throw new Exception("Empty result");
        }
        result.put("@context", context);
        return result;
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

    /* Get metadata corresponding to all the predicates in context for datasets or collections related to the 
     * given id by the given relationship working in the given direction (isSubject means the given id is the subject of the relationship triple)
     * , respecting access control and ignoring deleted items.
     */

    protected static Response getMetadataByForwardRelationship(UriRef base, Resource relationship, Map<String, Object> context, UriRef userId, UriRef requestedType) {
        return getMetadataByRelationship(base, true, relationship, context, userId, requestedType);

    }

    protected static Response getMetadataByReverseRelationship(String base, Resource relationship, Map<String, Object> context, UriRef userId, UriRef requestedType) {
        Resource baseLiteral = Resource.literal(base);
        return getMetadataByRelationship(baseLiteral, false, relationship, context, userId, requestedType);
    }

    protected static Response getMetadataByReverseRelationship(UriRef baseId, Resource relationship, Map<String, Object> context, UriRef userId, UriRef requestedType) {
        return getMetadataByRelationship(baseId, false, relationship, context, userId, requestedType);
    }

    protected static Response getMetadataByRelationship(Resource baseId, boolean isSubject, Resource relationship, Map<String, Object> context, UriRef userId, UriRef requestedType) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        if (baseId instanceof UriRef) {
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
        uf = populateUnifier("thing", uf, context);

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

        try {
            result = buildResultMap(results, context, names, true, null);
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

    protected Response getItemsByMetadata(String pred, String type, String value, Map<String, Object> context, HttpServletRequest request) {
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

                r = getMetadataByReverseRelationship((UriRef) base, relationship, context, userId, null);
            } else {
                return Response.status(403).entity("Related Item not accessible").build();
            }
        } else if (type.equals("literal")) {
            r = getMetadataByReverseRelationship(value, relationship, context, userId, null);
        }
        if (r == null) {
            r = Response.status(500).entity("Error getting information about " + base.toString()).build();

        }
        return r;

    }

    protected Response getTopLevelItems(Resource itemType, Map<String, Object> context, final UriRef userId) {

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Unifier uf = new Unifier();
        uf.addPattern("item", Rdf.TYPE, itemType);

        //column 0 must be identifier
        populateUnifier("item", uf, context);
        //Must come after to avoid having no result of row 0, column 0 would be null
        uf.addColumnName("parent");
        uf.addPattern("parent", DcTerms.HAS_PART, "item", true);
        List<String> names = uf.getColumnNames();
        final int parentIndex = names.indexOf("parent");
        log.debug("Parent index: " + parentIndex);
        org.tupeloproject.util.Table<Resource> table = null;
        try {
            table = TupeloStore.getInstance().unifyExcludeDeleted(uf, "item");
        } catch (OperatorException e) {
            log.error("Error listing top-level collections: " + e.getMessage());
            return Response.status(500).entity("Error listing top-level items").build();
        }
        names.remove("parent");

        try {
            result = buildResultMap(table, context, names, true, new FilterCallback() {
                @Override
                public boolean include(Tuple<Resource> t) {
                    if (t.get(parentIndex) == null) {
                        UriRef id = null;
                        if (t.get(0).isLiteral()) {
                            id = Resource.uriRef(t.get(0).toString());
                            log.warn("Literal identifier:" + t.get(0).toString());
                        } else {
                            id = (UriRef) t.get(0);
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
            log.debug("Exception during processing: " + e.getMessage());
            e.printStackTrace();

            //Nothing found
            return Response.status(404).entity("Items Not Found").build();
        }
        return Response.status(200).entity(result).build();
    }

    protected Response getItemTagsByIdAsJSON(String id, Resource type, HttpServletRequest request) {
        Response r;
        UriRef userId = Resource.uriRef((String) request.getAttribute("userid"));
        List<String> tags = new ArrayList<String>();
        try {
            UriRef itemId = Resource.uriRef(URLDecoder.decode(id, "UTF-8"));
            ValidItem item = new ValidItem(itemId, type, userId);
            if (!item.isValid()) {
                r = item.getErrorResponse();
            } else {
                Unifier uf = new Unifier();
                uf.addPattern(id, Tags.TAGGED_WITH_TAG, "tag");
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
        List<String> tags = new ArrayList<String>();
        try {
            UriRef itemId = Resource.uriRef(URLDecoder.decode(encoded_id, "UTF-8"));
            ValidItem item = new ValidItem(itemId, type, userId);
            if (!item.isValid()) {
                r = item.getErrorResponse();
            } else {
                TripleWriter tw = new TripleWriter();
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

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static String asHex(byte[] buf)
    {
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i )
        {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }

    protected static Set<String> getNormalizedTagSet(String cdl) {
        Set<String> tagSet = new HashSet<String>();
        for (String s : cdl.split(",") ) {
            tagSet.add(s.trim().replaceAll("  +", " ").toLowerCase());
        }
        return tagSet;
    }
}
