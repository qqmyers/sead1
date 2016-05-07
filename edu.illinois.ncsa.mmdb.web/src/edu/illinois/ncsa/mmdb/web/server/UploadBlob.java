/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
/**
 *
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BlobWriter;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Literal;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Beans;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Rdfs;
import org.tupeloproject.util.SecureHashMinter;

import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.rest.AuthenticatedServlet;
import edu.illinois.ncsa.mmdb.web.rest.RestService;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;
import edu.illinois.ncsa.mmdb.web.server.dispatch.AddToCollectionHandler;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

/**
 * @author plutchak
 *
 */
public class UploadBlob extends AuthenticatedServlet {

    private static final long  serialVersionUID     = 6448203283400080791L;
    public static final String UPLOAD_LISTENER_NAME = "uploadListener";

    static Log                 log                  = LogFactory.getLog(UploadBlob.class);

    class UploadInfo {
        URI     uri;
        String  filename;
        boolean isUploaded;

        public UploadInfo(URI uri, String filename) {
            super();
            this.uri = uri;
            this.filename = filename;
            isUploaded = false;
        }

        public void setUploaded(boolean b) {
            isUploaded = b;
        }
    }

    /**
     * Handle POST request.<br>
     * A post should only be be the initial upload request, i.e., a form with
     * multipart content
     *
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    public void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String userId = getUserUri(request);
        String storedUser = null;
        //In either case, check to see if there's  a valid upload Key and use the userId stored there to assess whether to accept the upload
        String uploadKey = request.getParameter("uploadkey");
        if (uploadKey != null) {
            log.trace("Found uploadKey: " + uploadKey);
            storedUser = useUploadKey(uploadKey);
        }
        if (userId == null) {
            //Either an invalid upload attempt or a case where the user's auth token has timed out.
            userId = storedUser;
        }
        if (userId == null) {
            //Don't think this should be possible, but this will make sure anonymous is listed as the uploader
            // rather than the field being missing and causing the data not to display.
            log.warn("Null user!");
            userId = PersonBeanUtil.getPersonID("anonymous");
        }

        if (!isAllowed(userId, Permission.UPLOAD_DATA)) {
            log.warn("Upload attempt by: " + userId);
            return;

        }

        Context c = TupeloStore.getInstance().getContext();

        if (c == null) {
            throw new ServletException("Tupelo server has no context");
        }

        log.trace("Content type: " + request.getContentType());
        if (!ServletFileUpload.isMultipartContent(request)) {
            log.error("Content is not multipart/form-data");
            throw new ServletException("Content is not multipart/form-data");
        }

        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        List<String> uris = new LinkedList<String>();
        String uri = null;
        // Parse the request
        try {
            @SuppressWarnings("unchecked")
            List<FileItem> items = upload.parseRequest(request);
            String collectionName = null;
            String collectionUri = null;

            for (FileItem item : items ) {
                // process a file
                String fieldName = item.getFieldName();

                // check to see if this a form field, if so remember for later
                if (item.isFormField()) {
                    if (fieldName.equals("collection")) {
                        collectionName = item.getString();
                        log.trace("POST: upload collection name = " + collectionName);
                    } else if (fieldName.equals("collectionUri")) {
                        collectionUri = item.getString();
                        log.trace("POST: upload collection uri = " + collectionUri);
                    }

                } else if (item.getSize() > 0) {
                    // item is the actual data, process it

                    // orginal filename
                    String fileName = item.getName();
                    if (fileName != null) {
                        fileName = normalizeFilename(fileName);
                    }

                    // Medici Android App uses fieldname to send the caption (BAD App)
                    String caption = fileName;
                    if (!fieldName.equals("f1")) {
                        caption = fieldName;
                    }
                    if (caption == null) {
                        caption = "Upload";
                    }

                    // mimetype
                    String contentType = item.getContentType();
                    if (MimeMap.UNKNOWN_TYPE.equals(contentType)) {
                        contentType = TupeloStore.getInstance().getMimeMap().getContentTypeFor(fileName);
                    }

                    // generate a URI based on the request using the REST service's minter
                    Map<Resource, Object> md = new HashMap<Resource, Object>();
                    md.put(RestService.LABEL_PROPERTY, caption);
                    uri = RestUriMinter.getInstance().mintUri(md);

                    // create the blobwriter that will write the actual data
                    BlobWriter bw = new BlobWriter();
                    bw.setUri(URI.create(uri));
                    MessageDigest sha1 = null;
                    InputStream is = item.getInputStream();
                    try {
                        sha1 = MessageDigest.getInstance("SHA1");
                        is = new DigestInputStream(item.getInputStream(), sha1);
                    } catch (NoSuchAlgorithmException e1) {
                        log.error("No SHA1 algorithm!");
                    }

                    bw.setInputStream(is);
                    try {
                        // write the blob
                        log.trace("writing " + fileName + " to " + uri);
                        c.perform(bw);

                        log.trace("writing metadata for " + uri);
                        // add metadata
                        ThingSession ts = c.getThingSession();
                        Literal id = Resource.literal(uri);

                        Thing t = ts.newThing(Resource.uriRef(uri));
                        //DatasetBean-related metadata
                        t.addType(Cet.DATASET);
                        t.setValue(Dc.IDENTIFIER, id);
                        t.addType(Beans.STORAGE_TYPE_BEAN_ENTRY);
                        t.setValue(Beans.PROPERTY_VALUE_IMPLEMENTATION_CLASSNAME,
                                Resource.literal("edu.uiuc.ncsa.cet.bean.DatasetBean"));

                        t.setValue(Rdfs.LABEL, caption);
                        t.setValue(RestService.LABEL_PROPERTY, caption);
                        t.setValue(RestService.FILENAME_PROPERTY, fileName);
                        t.setValue(RestService.DATE_PROPERTY, new Date());
                        t.setValue(Dc.CREATOR, Resource.uriRef(userId));
                        t.setValue(Dc.CREATOR, Resource.uriRef(userId));
                        t.setValue(Files.LENGTH, bw.getSize());
                        byte[] digest = sha1.digest();
                        t.addValue(edu.illinois.ncsa.mmdb.web.server.util.BeanFiller.SHA1_DIGEST, edu.illinois.ncsa.mmdb.web.server.util.BeanFiller.asHex(digest));

                        if (contentType != null) {
                            // httpclient also gives the content type a "charset"; ignore that.
                            contentType = contentType.replaceFirst("; charset=.*", "");
                            // FIXME parse this properly and set the charset accordingly!
                            t.setValue(RestService.FORMAT_PROPERTY, contentType);

                            // update context with new mime-type potentially
                            TupeloStore.getInstance().getMimeMap().checkMimeType(contentType);
                        }
                        t.save();
                        ts.close();
                        log.debug("user uploaded " + fileName + " (" + bw.getSize() + " bytes), uri=" + uri);

                        uris.add(uri);

                    } catch (OperatorException e) {
                        log.error("Error writing blob/label: " + e.getMessage());
                        throw new ServletException(e);
                    }

                    // submit to extraction service
                    try {
                        if (!TupeloStore.getInstance().getConfiguration(ConfigurationKey.BigData).equals("true")) {
                            TupeloStore.getInstance().extractPreviews(uri);
                        }
                    } catch (Exception e) {
                        log.info("Could not submit uri to extraction service, is it down?", e);
                    }
                }
            }

            // add a collection, if necessary
            if (collectionUri != null) {
                try {
                    ThingSession ts = TupeloStore.getInstance().getContext().getThingSession();
                    Thing t = ts.newThing(Resource.uriRef(collectionUri));
                    t.setValue(DcTerms.DATE_MODIFIED, new Date());
                    t.setValue(Dc.CREATOR, Resource.uriRef(userId));
                    for (String itemUri : uris ) {
                        log.debug("added " + itemUri + " to collection " + collectionUri);
                        t.addValue(RestService.HAS_MEMBER, Resource.uriRef(itemUri));
                    }
                    ts.save();
                } catch (OperatorException x) {
                    x.printStackTrace();
                }
            } else if (collectionName != null) {
                try {
                    Map<Resource, Object> md = new HashMap<Resource, Object>();
                    md.put(RestService.LABEL_PROPERTY, collectionName);
                    md.put(Rdf.TYPE, RestService.COLLECTION_TYPE);
                    collectionUri = RestUriMinter.getInstance().mintUri(md);
                    ThingSession ts = TupeloStore.getInstance().getContext().getThingSession();
                    Thing t = ts.newThing(Resource.uriRef(collectionUri));
                    t.addType(RestService.COLLECTION_TYPE);
                    t.setLabel(collectionName);
                    t.setValue(Dc.TITLE, collectionName);
                    t.setValue(DcTerms.DATE_CREATED, new Date());
                    t.setValue(DcTerms.DATE_MODIFIED, new Date());
                    t.setValue(Dc.CREATOR, Resource.uriRef(userId));
                    log.debug("created collection '" + collectionName + "' @ " + collectionUri);
                    for (String itemUri : uris ) {
                        log.debug("added " + itemUri + " to collection " + collectionUri);
                        t.addValue(RestService.HAS_MEMBER, Resource.uriRef(itemUri));
                    }
                    ts.save();
                } catch (OperatorException x) {
                    //
                    x.printStackTrace();
                }
            } else {
                TripleWriter tw = new TripleWriter();
                for (String itemUri : uris ) {
                    tw.add(AddToCollectionHandler.TOP_LEVEL, AddToCollectionHandler.INCLUDES, Resource.uriRef(itemUri));
                }
                try {
                    TupeloStore.getInstance().getContext().perform(tw);
                } catch (OperatorException e) {
                    log.error("Error indexing data as TOP_LEVEL: " + e.getMessage());
                    throw new ServletException(e);
                }
            }
            // return list of URI's
            returnList(response, collectionUri, uris);
        } catch (FileUploadException e1) {
            log.error("file upload error: " + e1.getLocalizedMessage());
            e1.printStackTrace();
        }
    }

    /**
     * Return, as a response, an HTML ordered list, each item of which contains
     * a URI. The class attribute for each of these items will be "dataset" or
     * "collection", depending on whether the URI identifies a dataset, or a
     * collection. Datasets come first, any collection last. e.g.,:
     * <p>
     *
     * <pre>
     * <ol>
     *   <li class="dataset">http://foo.bar#myKewlDataset</li>
     *   <li class="dataset">http://foo.bar#someOtherDataset</li>
     *   <li class="collection">http://foo.bar#collection3</li>
     * </ol>
     * </pre>
     *
     * @param response
     * @param collectionUri
     * @param uris
     */
    private void returnList(HttpServletResponse response, String collectionUri, List<String> uris) throws IOException {
        response.setContentType("text/html"); // do not allow browsers to parse and rewrite this response!
        // produce HTML the good old-fashioned way; println.
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
        pw.println("<ol>");
        for (String datasetUri : uris ) {
            pw.println("  <li class=\"dataset\">" + datasetUri + "</li>");
        }
        if (collectionUri != null) {
            pw.println("  <li class=\"collection\">" + collectionUri + "</li>");
        }
        pw.println("</ol>");
        pw.flush();
    }

    // certain browsers return full pathnames. we just want the last component
    private String normalizeFilename(String name) {
        name = name.replaceFirst(".*\\\\", "");
        return name;
    }

    /**
     * Convert a vector of values into a Jason array of strings
     *
     * @param name
     *            the name of the array
     * @param v
     *            the vector
     * @return
     */
    public String toJsonArray(String name, Vector<? extends Object> v) {
        StringBuffer sb = new StringBuffer();
        if (name != null) {
            sb.append("\"" + name + "\":");
        }
        sb.append("[");

        if (v != null) {
            for (int i = 0; i < v.size(); i++ ) {
                Object obj = v.get(i);
                if ((obj instanceof Boolean) || (obj instanceof Number)) {
                    sb.append(obj.toString() + ",");
                } else { // string or something non-atomic so trwt as string
                    sb.append("\"" + obj.toString() + "\"" + ",");
                }
            }

            if (v.size() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * The GET request is used to check the listener object for file upload
     * stats
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // allow clients to request a key that will allow upload to complete without valid authentication
        //(e.g. to support upload times that are longer than the lifetime of auth tokens)

        // Authenticate to get a key
        String userId = getUserUri(request);
        if (userId == null) {
            return;
        }
        if (!isAllowed(userId, Permission.UPLOAD_DATA)) {
            return;
        }
        String sessionKey = SecureHashMinter.getMinter().mint(); // mint a session key
        setKeyForUpload(sessionKey, userId);
        // OK, we REALLY don't want IE to cache this. For reals
        response.addHeader("cache-control", "no-store, no-cache"); // don't cache
        response.addHeader("cache-control", "post-check=0, pre-check=0, false"); // really don't cache
        response.addHeader("Pragma", "no-cache"); // no, we mean it, really don't cache
        // report
        PrintWriter out = response.getWriter();
        out.println("{\"uploadkey\":\"" + sessionKey + "\"}");
        log.trace("GET: minted session key = " + sessionKey);
    }

    /**
     * return the base URL string for this servlet
     *
     * @param req
     *            the HttpServletRequest
     * @return a string form of the base servlet URL
     */
    public static String getBaseUrl(HttpServletRequest req) {
        String baseUrl = null;
        if (req != null) {
            baseUrl = req.getScheme().trim() + "://" + req.getServerName().trim() + getPort(req).trim()
                    + req.getContextPath().trim();
        } else {
            baseUrl = "";
        }
        return baseUrl;
    }

    /**
     * Return the request port for this servlet
     *
     * @param req
     *            the HttpServletRequest
     * @return the request port with prepended separator; blank for standard
     *         ports
     */
    private static String getPort(HttpServletRequest req) {
        if (("http".equalsIgnoreCase(req.getScheme()) && (req.getServerPort() == 80))
                || ("https".equalsIgnoreCase(req.getScheme()) && (req.getServerPort() == 443))) {
            return "";
        } else {
            return (":" + req.getServerPort());
        }
    }

    static Map<String, String> uploadKeys = new HashMap<String, String>();

    public static void setKeyForUpload(String sessionKey, String userId) {
        uploadKeys.put(sessionKey, userId);
        log.trace("Registering Upload Key: " + sessionKey + " for " + userId);
        if (uploadKeys.size() > 200) {
            log.warn("Active Upload Keys >200 (" + uploadKeys.size() + ")");
        }
    }

    // only call this once per key
    public static String useUploadKey(String sessionKey) {
        log.trace("Using Upload Key: " + sessionKey + " for " + uploadKeys.get(sessionKey));
        return uploadKeys.remove(sessionKey);
    }

}