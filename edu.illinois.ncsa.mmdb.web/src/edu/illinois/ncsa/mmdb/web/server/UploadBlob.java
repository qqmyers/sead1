/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.tupeloproject.kernel.BlobWriter;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.rdf.Resource;
import static org.tupeloproject.rdf.terms.Rdfs.LABEL;

import edu.illinois.ncsa.mmdb.web.rest.RestService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author plutchak
 *
 */
public class UploadBlob extends HttpServlet {

    private static final long  serialVersionUID = 6448203283400080791L;
    public static final String LISTENER_NAME    = "uploadListener";

    Log log = LogFactory.getLog(UploadBlob.class);

    class UploadInfo {
        URI     uri;
        String  filename;
        boolean isUploaded;

        public UploadInfo ( URI uri, String filename ) {
            super();
            this.uri = uri;
            this.filename = filename;
            isUploaded = false;
        }

        public void setUploaded ( boolean b ) {
            isUploaded = b;
        }
    }

    /**
     * A listener for file upload progress
     */
    class FileUploadListener implements ProgressListener {
        private volatile long bytesRead     = 0L;
        private volatile long contentLength = 0L;
        private volatile long item          = 0L;
        private volatile Vector<UploadInfo> uploadInfo    = new Vector<UploadInfo>();

        public FileUploadListener ( ) {
            super();
        }

        public void update ( long aBytesRead, long aContentLength, int anItem ) {
            bytesRead = aBytesRead;
            contentLength = aContentLength;
            item = anItem;
        }

        public long getBytesRead ( ) {
            return bytesRead;
        }

        public long getContentLength ( ) {
            return contentLength;
        }

        public long getItem ( ) {
            return item;
        }

        public void addUploadInfo ( UploadInfo info ) {
            uploadInfo.add(info);
        }

        public UploadInfo addUploadInfo ( URI uri, String filename ) {
            UploadInfo u = new UploadInfo(uri, filename);
            uploadInfo.add(u);
            return u;
        }

        public Vector<UploadInfo> getUploadInfo ( ) {
            return uploadInfo;
        }

        public Vector<URI> getBlobUris ( ) {
            Vector<URI> v = new Vector<URI>(uploadInfo.size());
            for (UploadInfo u : uploadInfo) {
                v.add(u.uri);
            }
            return v;
        }

        public Vector<String> getFilenames ( ) {
            Vector<String> v = new Vector<String>(uploadInfo.size());
            for (UploadInfo u : uploadInfo) {
                v.add(u.filename);
            }
            return v;
        }

        public Vector<Boolean> getIsUploaded ( ) {
            Vector<Boolean> v = new Vector<Boolean>(uploadInfo.size());
            for (UploadInfo u : uploadInfo) {
                v.add(u.isUploaded);
            }
            return v;
        }

        public boolean allDone ( ) {
            if ((bytesRead >= contentLength)) {
                for (UploadInfo u : uploadInfo) {
                    if (!u.isUploaded) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }

    void log(Object o) {
        log.info(o);
    }

    /**
     * Trim path information from a filename
     */
    private String trimFilename ( String fullname ) {
        int beginIndex = fullname.lastIndexOf('/');
        if (beginIndex < 0) {
            beginIndex = fullname.lastIndexOf('\\');
        }
        if (beginIndex >= 0) {
            fullname = fullname.substring(beginIndex + 1);
        }
        return fullname.trim();
    }

    /**
     * Handle POST request.<br>
     * A post should only be be the initial upload request, i.e., a form with multipart content
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    public void doPost ( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException, IOException {
        Context c = TupeloStore.getInstance().getContext();

        if (c == null) {
            throw new ServletException("Tupelo server has no context");
        }

        log("Content type: " + request.getContentType());
        if (!ServletFileUpload.isMultipartContent(request)) {
            log("Content is not multipart/form-data");
            throw new ServletException("Content is not multipart/form-data");
        }

        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler & listener
        HttpSession session = request.getSession();
        FileUploadListener listener = new FileUploadListener();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setProgressListener(listener);

        // set the listener attribute
        session.setAttribute(LISTENER_NAME, listener);
        log("Session (post): " + session.getId());

        URI uri = null;
        // Parse the request
        try {
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                // process a file
                String fieldName = item.getFieldName();
                String fileName = item.getName();
                String contentType = item.getContentType();
                boolean isInMemory = item.isInMemory();
                long sizeInBytes = item.getSize();
                log("Post: " + item.isFormField() + "|" + fieldName + "|" + fileName + "|" + isInMemory + "|"
                        + contentType + "|" + sizeInBytes);
                // if it's a field from a form and the size is non-zero...
                if (!item.isFormField() && (sizeInBytes > 0)) {
                    BlobWriter bw = new BlobWriter();

                    // generate a new pseudorandom URI
                    uri = Resource.uriRef().getUri();
                    bw.setUri(uri);
                    String url = TupeloStore.getInstance().getUriCanonicalizer(request).canonicalize("dataset",uri.toString());
                    UploadInfo u = listener.addUploadInfo(URI.create(url), trimFilename(fileName));
                    bw.setInputStream(item.getInputStream());

                    try {
                        // write the blob
                        c.perform(bw);
                        log("Wrote: " + bw.getSize());

                        // add metadata
                        ThingSession ts = c.getThingSession();
                        Thing t = ts.newThing(Resource.uriRef(uri));
                        t.addType(RestService.IMAGE_TYPE);
                        t.setValue(LABEL, fileName);
                        t.setValue(RestService.LABEL_PROPERTY, fileName);
                        t.setValue(RestService.DATE_PROPERTY, new Date());
                        t.setValue(RestService.FORMAT_PROPERTY, contentType);
                        t.save();
                        ts.close();

                        u.setUploaded(true);
                    }
                    catch (OperatorException e) {
                        log("Error writing blob/label: " + e.getMessage());
                        throw new ServletException(e);
                    }
                }
            }
        }
        catch (FileUploadException e1) {
            log(e1.getLocalizedMessage());
            e1.printStackTrace();
        }
    }

    /**
     * Convert a vector of values into a Jason array of strings
     * @param name the name of the array
     * @param v the vector
     * @return
     */
    public String toJsonArray ( String name, Vector v ) {
        StringBuffer sb = new StringBuffer();
        if (name != null) {
            sb.append("\"" + name + "\":");
        }
        sb.append("[");

        if (v != null) {
            for (int i = 0; i < v.size(); i++) {
                Object obj = v.get(i);
                if ((obj instanceof Boolean) || (obj instanceof Number)) {
                    sb.append(obj.toString() + ",");
                }
                else { // string or something non-atomic so trwt as string
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
     * @param req
     * @return a JSON representation of state variables
     */
    public String stateToJSON ( boolean hasStarted, FileUploadListener listener, HttpServletRequest req ) {
        StringBuffer buffer = new StringBuffer("{");
        long bytesRead = 0L;
        long contentLength = 0L;
        boolean isFinished = false;

        buffer.append("\"serverUrl\":\"" + getBaseUrl(req) + "/tupelo" + "\",");
        buffer.append("\"hasStarted\":" + hasStarted + ",");
        if (listener == null) {
            buffer.append( "\"uris\":[],");
            buffer.append("\"filenames\":[],");
            buffer.append("\"isUploaded\":[],");
        } else {
            bytesRead = listener.getBytesRead();
            contentLength = listener.getContentLength();
            isFinished = listener.allDone();
            buffer.append(toJsonArray("uris", listener.getBlobUris()) + ",");
            buffer.append(toJsonArray("filenames", listener.getFilenames()) + ",");
            buffer.append(toJsonArray("isUploaded", listener.getIsUploaded()) + ",");
        }
        buffer.append("\"bytesRead\":" + bytesRead + ",");
        buffer.append("\"contentLength\":" + contentLength + ",");
        buffer.append("\"isFinished\":" + isFinished + ",");

        // Calculate the percent complete
        long percentComplete = 0;
        if (contentLength != 0) {
            percentComplete = ((100 * bytesRead) / contentLength);
        }
        buffer.append("\"percentComplete\":" + percentComplete);
        buffer.append("}");

        return buffer.toString();
    }

    /**
     * Set response to a JSON string with all zero or null values.
     * @param response
     * @throws IOException
     */
    public void returnZeroedJSON ( HttpServletResponse response ) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(stateToJSON(false, null, null));
        out.flush();
        out.close();
    }

    /**
     * The GET request is used to check the listener object for file upload stats
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *    javax.servlet.http.HttpServletResponse)
     */
    public void doGet ( HttpServletRequest request,
            HttpServletResponse response ) throws ServletException, IOException {
        HttpSession session = request.getSession();

        response.setContentType("application/json");
        // Make sure the session has started
        if (session == null) {
            log("(get): session null");
            returnZeroedJSON(response);
            return;
        }
        log("Session (get): " + session.getId());

        long bytesRead = 0;
        long contentLength = 0;
        //Vector<URI> blobUris = null;
        FileUploadListener listener = null;
        // Check to see if we've created the listener object yet
        listener = (FileUploadListener) session.getAttribute(LISTENER_NAME);
        if (listener == null) {
            log("(get): listener null");
            returnZeroedJSON(response);
            return;
        }

        // Get the meta information
        bytesRead = listener.getBytesRead();
        contentLength = listener.getContentLength();
        //Vector<UploadInfo> u = listener.getUploadInfo();
        //blobUris = listener.getUris();

        if (listener.allDone()) {
            // No reason to keep listener in session since we're done
            session.removeAttribute(LISTENER_NAME);
        }
        log("Progress: " + bytesRead + "/" + contentLength);
        PrintWriter out = response.getWriter();
        out.print(stateToJSON(true, listener, request));
        log("(get): " + stateToJSON(true, listener, request));
        out.flush();
        out.close();
    }

    /**
     * return the base URL string for this servlet
     * @param req the HttpServletRequest
     * @return a string form of the base servlet URL
     */
    public static String getBaseUrl ( HttpServletRequest req ) {
        String baseUrl = null;
        if (req != null) {
            baseUrl = req.getScheme().trim() + "://" + req.getServerName().trim() + getPort(req).trim()
            + req.getContextPath().trim();
        }
        else {
            baseUrl = "";
        }
        return baseUrl;
    }

    /**
     * Return the request port for this servlet
     * @param req the HttpServletRequest
     * @return the request port with prepended separator; blank for standard ports
     */
    private static String getPort ( HttpServletRequest req ) {
        if (("http".equalsIgnoreCase(req.getScheme()) && (req.getServerPort() == 80))
                || ("https".equalsIgnoreCase(req.getScheme()) && (req.getServerPort() == 443))) {
            return "";
        }
        else {
            return (":" + req.getServerPort());
        }
    }
}