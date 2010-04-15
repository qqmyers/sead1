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

import static org.tupeloproject.rdf.terms.Rdfs.LABEL;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
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
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BlobWriter;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.SecureHashMinter;

import edu.illinois.ncsa.mmdb.web.rest.AuthenticatedServlet;
import edu.illinois.ncsa.mmdb.web.rest.RestService;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

/**
 * @author plutchak
 * 
 */
public class UploadBlob extends AuthenticatedServlet {

    private static final long  serialVersionUID     = 6448203283400080791L;
    public static final String UPLOAD_LISTENER_NAME = "uploadListener";

    Log                        log                  = LogFactory.getLog(UploadBlob.class);

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
     * A listener for file upload progress
     */
    class FileUploadListener implements ProgressListener {
        private volatile long               bytesRead     = 0L;
        private volatile long               bytesWritten  = 0L;
        private volatile long               contentLength = 0L;
        private volatile long               itemsLength   = 0L;
        private volatile long               item          = 0L;
        private volatile Vector<UploadInfo> uploadInfo    = new Vector<UploadInfo>();

        public FileUploadListener() {
            super();
        }

        int debugPrune = 0;

        public void update(long aBytesRead, long aContentLength, int anItem) {
            bytesRead = aBytesRead;
            contentLength = aContentLength;
            item = anItem;
        }

        public void wrote(long aBytesWritten) {
            bytesWritten += aBytesWritten;
        }

        public long getBytesRead() {
            return bytesRead;
        }

        public long getBytesWritten() {
            return bytesWritten;
        }

        public long getContentLength() {
            return contentLength;
        }

        public long getItem() {
            return item;
        }

        public void addUploadInfo(UploadInfo info) {
            uploadInfo.add(info);
        }

        public UploadInfo addUploadInfo(URI uri, String filename, long length) {
            UploadInfo u = new UploadInfo(uri, filename);
            uploadInfo.add(u);
            itemsLength += length;
            return u;
        }

        public Vector<UploadInfo> getUploadInfo() {
            return uploadInfo;
        }

        public Vector<URI> getBlobUris() {
            Vector<URI> v = new Vector<URI>(uploadInfo.size());
            for (UploadInfo u : uploadInfo ) {
                v.add(u.uri);
            }
            return v;
        }

        public Vector<String> getFilenames() {
            Vector<String> v = new Vector<String>(uploadInfo.size());
            for (UploadInfo u : uploadInfo ) {
                v.add(u.filename);
            }
            return v;
        }

        public Vector<Boolean> getIsUploaded() {
            Vector<Boolean> v = new Vector<Boolean>(uploadInfo.size());
            for (UploadInfo u : uploadInfo ) {
                v.add(u.isUploaded);
            }
            return v;
        }

        public boolean allDone() {
            if (percentComplete() >= 100) {
                for (UploadInfo u : uploadInfo ) {
                    if (!u.isUploaded) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        public int percentComplete() {
            long pct = (int) (contentLength == 0L ? 0 : ((bytesRead * 100) / contentLength));
            //if(pct < 99) {
            //        		log.trace(bytesRead +" read / "+bytesWritten+" written = "+pct+"%");
            //}
            return (int) pct;
        }
    }

    Map<String, FileUploadListener> listeners = new HashMap<String, FileUploadListener>();

    void debug(String s) {
        log.trace(s);
    }

    void log(Object o) {
        log.info(o);
    }

    /**
     * Trim path information from a filename
     */
    private String trimFilename(String fullname) {
        int beginIndex = fullname.lastIndexOf('/');
        if (beginIndex < 0) {
            beginIndex = fullname.lastIndexOf('\\');
        }
        if (beginIndex >= 0) {
            fullname = fullname.substring(beginIndex + 1);
        }
        return fullname.trim();
    }

    FileUploadListener trackProgress(ServletFileUpload upload, String sessionKey) {
        if (sessionKey != null && listeners.containsKey(sessionKey)) {
            return listeners.get(sessionKey); // shouldn't happen, but this is safe
        }
        FileUploadListener listener = new FileUploadListener();
        upload.setProgressListener(listener);
        if (sessionKey != null) {
            listeners.put(sessionKey, listener);
        }
        return listener;
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
        if (!authenticate(request, response)) {
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
        String sessionKey = request.getParameter("session");
        FileUploadListener listener = trackProgress(upload, sessionKey);

        List<String> uris = new LinkedList<String>();
        String uri = null;
        // Parse the request
        try {
            List<FileItem> items = upload.parseRequest(request);
            int nFiles = 0;
            String collectionName = null;
            String collectionUri = null;
            for (FileItem item : items ) {
                // process a file
                String fieldName = item.getFieldName();
                String fileName = item.getName();
                if (fileName != null) {
                    fileName = normalizeFilename(fileName);
                }
                String contentType = item.getContentType();
                if (MimeMap.UNKNOWN_TYPE.equals(contentType)) {
                    contentType = TupeloStore.getInstance().getFileNameMap().getContentTypeFor(fileName);
                }
                boolean isInMemory = item.isInMemory();
                long sizeInBytes = item.getSize();
                log.info("Post: " + item.isFormField() + "|" + fieldName + "|" + fileName + "|" + isInMemory + "|" + contentType + "|" + sizeInBytes);
                if (item.isFormField() && fieldName.equals("session") && listener == null) {
                    sessionKey = item.getString();
                    log.trace("POST: upload session key (part) = " + sessionKey);
                    listener = trackProgress(upload, sessionKey);
                }
                if (item.isFormField() && fieldName.equals("collection")) {
                    collectionName = item.getString();
                    log.debug("POST: upload collection name = " + collectionName);
                }
                if (item.isFormField() && fieldName.equals("collectionUri")) {
                    collectionUri = item.getString();
                    log.debug("POST: upload collection uri = " + collectionUri);
                }
                // if it's a field from a form and the size is non-zero...
                if (!item.isFormField() && (sizeInBytes > 0)) {
                    nFiles++;
                    BlobWriter bw = new BlobWriter();
                    // generate a URI based on the request using the REST service's minter
                    Map<Resource, Object> md = new HashMap<Resource, Object>();
                    md.put(RestService.LABEL_PROPERTY, fileName);
                    uri = RestUriMinter.getInstance().mintUri(md);
                    //
                    bw.setUri(URI.create(uri));
                    UploadInfo u = null;
                    if (listener != null) {
                        u = listener.addUploadInfo(URI.create(uri), trimFilename(fileName), sizeInBytes);
                    }
                    final FileUploadListener _listener = listener;
                    bw.setInputStream(new FilterInputStream(item.getInputStream()) {
                        int wrote(int written) {
                            if (written > 0 && _listener != null) {
                                _listener.wrote(written);
                            }
                            return written;
                        }

                        @Override
                        public int read() throws IOException {
                            return wrote(super.read());
                        }

                        @Override
                        public int read(byte[] arg0, int arg1, int arg2)
                                throws IOException {
                            return wrote(super.read(arg0, arg1, arg2));
                        }

                        @Override
                        public int read(byte[] arg0) throws IOException {
                            return wrote(super.read(arg0));
                        }
                    });

                    try {
                        // write the blob
                        log.trace("writing " + fileName + " to " + uri);
                        c.perform(bw);

                        log.trace("writing metadata for " + uri);
                        // add metadata
                        ThingSession ts = c.getThingSession();
                        Thing t = ts.newThing(Resource.uriRef(uri));
                        t.addType(RestService.IMAGE_TYPE);
                        t.setValue(LABEL, fileName);
                        t.setValue(RestService.LABEL_PROPERTY, fileName);
                        t.setValue(RestService.FILENAME_PROPERTY, fileName);
                        t.setValue(RestService.DATE_PROPERTY, new Date());
                        String username = getHttpSessionUser(request);
                        if (username != null) {
                            t.setValue(Dc.CREATOR, Resource.uriRef(PersonBeanUtil.getPersonID(username)));
                        }
                        t.setValue(Files.LENGTH, bw.getSize());
                        if (contentType != null) {
                            // httpclient also gives the content type a "charset"; ignore that.
                            contentType = contentType.replaceFirst("; charset=.*", "");
                            // FIXME parse this properly and set the charset accordingly!
                            t.setValue(RestService.FORMAT_PROPERTY, contentType);
                        }
                        t.save();
                        ts.close();
                        log.info("user uploaded " + fileName + " (" + sizeInBytes + " bytes), uri=" + uri);

                        uris.add(uri);

                        if (u != null) {
                            u.setUploaded(true);
                        }

                        // submit to extraction service
                        TupeloStore.getInstance().extractPreviews(uri);
                    } catch (OperatorException e) {
                        log.error("Error writing blob/label: " + e.getMessage());
                        throw new ServletException(e);
                    }
                }
            }
            // add a collection, if necessary
            if (collectionUri != null) {
                try {
                    ThingSession ts = TupeloStore.getInstance().getContext().getThingSession();
                    Thing t = ts.newThing(Resource.uriRef(collectionUri));
                    t.setValue(DcTerms.DATE_MODIFIED, new Date());
                    for (String itemUri : uris ) {
                        log.debug("added " + itemUri + " to collection " + collectionUri);
                        t.addValue(RestService.HAS_MEMBER, Resource.uriRef(itemUri));
                    }
                    ts.save();
                    TupeloStore.getInstance().setHistoryForUpload(sessionKey, "collection?uri=" + collectionUri);
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
                    log.debug("created collection '" + collectionName + "' @ " + collectionUri);
                    for (String itemUri : uris ) {
                        log.debug("added " + itemUri + " to collection " + collectionUri);
                        t.addValue(RestService.HAS_MEMBER, Resource.uriRef(itemUri));
                    }
                    ts.save();
                    TupeloStore.getInstance().setHistoryForUpload(sessionKey, "collection?uri=" + collectionUri);
                    // FIXME need whole state, not just collection URI (see lines 410-411 commented out below)
                    response.getOutputStream().print(collectionUri);
                    response.getOutputStream().flush();
                } catch (OperatorException x) {
                    //
                    x.printStackTrace();
                }
            } else if (nFiles == 1) {
                TupeloStore.getInstance().setHistoryForUpload(sessionKey, "dataset?id=" + uri);
            } else {
                TupeloStore.getInstance().setHistoryForUpload(sessionKey, "listDatasets?sort=date-desc");
            }
            // output state to output stream
            /*
            response.getOutputStream().println(stateToJSON(true,listener,request));
            response.getOutputStream().flush();
            */
        } catch (FileUploadException e1) {
            log.error("file upload error: " + e1.getLocalizedMessage());
            e1.printStackTrace();
        }
    }

    // certain browsers made in Redmond, WA return full pathnames. we just want the last component
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
     * Create a JSON string from the state variables.
     * 
     * @param req
     * @return a JSON representation of state variables
     */
    public String stateToJSON(boolean hasStarted, FileUploadListener listener, HttpServletRequest req) {
        StringBuffer buffer = new StringBuffer("{");
        long bytesRead = 0L;
        long contentLength = 0L;
        boolean isFinished = false;

        buffer.append("\"serverUrl\":\"" + getBaseUrl(req) + "/tupelo" + "\",");
        buffer.append("\"hasStarted\":" + hasStarted + ",");
        int percentComplete = 0;
        if (listener == null) {
            buffer.append("\"uris\":[],");
            buffer.append("\"filenames\":[],");
            buffer.append("\"isUploaded\":[],");
        } else {
            bytesRead = listener.getBytesRead();
            contentLength = listener.getContentLength();
            isFinished = listener.allDone();
            buffer.append(toJsonArray("uris", listener.getBlobUris()) + ",");
            buffer.append(toJsonArray("filenames", listener.getFilenames()) + ",");
            buffer.append(toJsonArray("isUploaded", listener.getIsUploaded()) + ",");
            percentComplete = listener.percentComplete();
        }
        buffer.append("\"bytesRead\":" + bytesRead + ",");
        buffer.append("\"contentLength\":" + contentLength + ",");
        buffer.append("\"isFinished\":" + isFinished + ",");

        buffer.append("\"percentComplete\":" + percentComplete);
        buffer.append("}");

        return buffer.toString();
    }

    /**
     * Set response to a JSON string with all zero or null values.
     * 
     * @param response
     * @throws IOException
     */
    public void returnZeroedJSON(HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(stateToJSON(false, null, null));
        out.flush();
        out.close();
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
        // allow clients with a session key in hand to continue unauthenticated
        if (request.getParameterMap().containsKey("uploadComplete")) { // need to redirect after completion
            TupeloStore t = TupeloStore.getInstance();
            String sessionKey = request.getParameter("uploadComplete");
            String historyToken = t.getHistoryForUpload(sessionKey);
            if (historyToken != null) {
                String redirectUrl = request.getContextPath() + TupeloStore.MMDB_WEBAPP_PATH + "#" + historyToken;
                response.getOutputStream().print(redirectUrl);
                response.getOutputStream().flush();
            }
            return;
        }
        if (!request.getParameterMap().containsKey("session")) { // no session?
            // then you'd better authenticate to get one
            if (!authenticate(request, response)) {
                return;
            }
            String sessionKey = SecureHashMinter.getMinter().mint(); // mint a session key
            // OK, we REALLY don't want IE to cache this. For reals
            response.addHeader("cache-control", "no-store, no-cache"); // don't cache
            response.addHeader("cache-control", "post-check=0, pre-check=0, false"); // really don't cache
            response.addHeader("Pragma", "no-cache"); // no, we mean it, really don't cache
            // report
            PrintWriter out = response.getWriter();
            out.println("{\"session\":\"" + sessionKey + "\"}");
            log.info("GET: minted session key = " + sessionKey); // FIXME make log.trace
        } else {
            String sessionKey = request.getParameter("session");
            log.trace("GET: session key = " + sessionKey);
            // return if there's no progress yet
            if (listeners.get(sessionKey) == null) {
                log("GET: no upload for session key " + sessionKey);
                returnZeroedJSON(response);
                return;
            }
            // get the listener
            FileUploadListener listener = listeners.get(sessionKey);
            // are we done?
            if (listener.allDone()) {
                // FIXME need to clean up, but only when client has the info it needs
                //listeners.put(sessionKey, null);
            }
            // report progress
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(stateToJSON(true, listener, request));
            //log.trace("GET: reported " + stateToJSON(true, listener, request));
        }
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
}