package edu.illinois.ncsa.mmdb.web.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;

import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.CopyFile;
import org.tupeloproject.util.Xml;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.UriCanonicalizer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * RestServlet
 */
public class RestServlet extends HttpServlet {
    Log log = LogFactory.getLog(RestServlet.class);

	public static final String COLLECTION_REMOVE_INFIX = "/collection/remove/";

	public static final String COLLECTION_ADD_INFIX = "/collection/add/";

	public static final String COLLECTION_CREATE_ANON_INFIX = "/collection";

	public static final String COLLECTION_INFIX = "/collection/";

	public static final String ANY_COLLECTION_INFIX = "/collection";

	public static final String IMAGE_DOWNLOAD_INFIX = "/image/download/";

	public static final String IMAGE_CREATE_ANON_INFIX = "/image";

	public static final String IMAGE_INFIX = "/image/";

	public static final String ANY_IMAGE_INFIX = "/image";

    static RestService restService; // TODO manage this lifecycle better

    public void init() throws ServletException {
        super.init();
        restService = new RestServiceImpl();
        log.info("REST servlet initialized");
    }

    UriCanonicalizer getUriCanonicalizer(HttpServletRequest request) throws ServletException {
    	return TupeloStore.getInstance().getUriCanonicalizer(request);
    }

    /**
     * the canonical URL of a resource is
     * {servlet path}{infix}{uri}
     * for instance if the servlet path is http://mymmdb.org/api
     * and the infix is /image/
     * and the URI is urn:foo, the canonical URL is
     * http://mymmdb.org/api/image/urn:foo
     *
     * @param uri the RDF subject of the resource
     * @param infix the infix to use
     * @param request the request (so we can extract the server prefix)
     * @return the canonical URL of the resource
     */
    String canonicalizeUri(String uri, String infix, HttpServletRequest request) throws ServletException {
        return getUriCanonicalizer(request).canonicalize(infix,uri);
    }

    boolean hasPrefix(String uri, String infix, HttpServletRequest request) throws ServletException {
        return getUriCanonicalizer(request).hasPrefix(infix,uri);
    }

    boolean hasPrefix(String infix, HttpServletRequest request) throws ServletException {
        return getUriCanonicalizer(request).hasPrefix(infix,request.getRequestURL().toString());
    }

    String decanonicalizeUrl(String url, HttpServletRequest request) throws ServletException {
        return getUriCanonicalizer(request).decanonicalize(url);
    }
    String decanonicalizeUrl(HttpServletRequest request) throws ServletException {
        return getUriCanonicalizer(request).decanonicalize(request.getRequestURL().toString());
    }

    void dumpCrap(HttpServletRequest request) {
        System.out.println("requestURI = "+request.getRequestURI());
        System.out.println("servletPath = "+request.getServletPath());
        System.out.println("contextPath = "+request.getContextPath());
        System.out.println("requestURL = "+request.getRequestURL());
        System.out.println("localAddr = "+request.getLocalAddr());
    }

    void dumpHeaders(HttpServletRequest request) {
        Enumeration headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            System.out.println(headerName+": "+request.getHeader(headerName));
        }
    }

    Context getContext() {
    	return TupeloStore.getInstance().getContext();
    }
    
    Thing getImageThing(String uri) {
    	return getContext().getThingSession().fetchThing(Resource.uriRef(uri));
    }
    
    @Override  
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //dumpCrap(request); // FIXME debug
        //dumpHeaders(request); // FIXME debug
        String uri = decanonicalizeUrl(request);
        if(hasPrefix(IMAGE_DOWNLOAD_INFIX,request)) {
            log("DOWNLOAD IMAGE "+uri);
            try {
                String filename = getImageThing(uri).getString(RestService.LABEL_PROPERTY);
                response.setHeader("content-disposition","attachment; filename="+filename);
                CopyFile.copy(restService.retrieveImage(uri), response.getOutputStream());
            } catch(RestServiceException e) {
            	throw new ServletException("failed to retrieve "+request.getRequestURI());
            } catch (OperatorException e) {
            	throw new ServletException("failed to retrieve metadata for "+request.getRequestURI());
			}
        } else if(hasPrefix(IMAGE_INFIX,request)) {
            log("GET IMAGE "+uri);
            try {
                CopyFile.copy(restService.retrieveImage(uri), response.getOutputStream());
            } catch(RestServiceException e) {
                throw new ServletException("failed to retrieve "+request.getRequestURI());
            }
        } else if(hasPrefix(COLLECTION_INFIX,request)) {
            log("LIST COLLECTION"+uri);
            try {
                // TODO currently assumes that everything in a collection is an image
                // TODO if that assumption is not correct, will need a way to track canonical URL's per-resource
                // TODO if the collection is huge, this will bloat memory, may need different API to stage
                List<String> canonicalMembers = new LinkedList<String>();
                for(String member : restService.retrieveCollection(uri)) {
                    canonicalMembers.add(canonicalizeUri(member, IMAGE_INFIX, request));
                }
                response.getWriter().write(formatList(canonicalMembers));
            } catch(RestServiceException e) {
                throw new ServletException("failed to retrieve "+request.getRequestURI());
            }
        } else {
            throw new ServletException("unrecognized API call "+request.getRequestURI());
        }
    }

    void doPostImage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        InputStream imageData = null;
        //
        Map<Resource,Object> md = new HashMap<Resource,Object>();
        md.put(RestService.DATE_PROPERTY, new Date());
        //
        if(ServletFileUpload.isMultipartContent(request)) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            try {
                List<FileItem> items = upload.parseRequest(request);
                if(items.size() > 1) {
                    log("warning: ignoring all but first content item in multi-part POST");
                } else if(items.size() == 0) {
                    throw new ServletException("no file data in multi-part POST");
                }
                FileItem item = items.get(0);
                md.put(RestService.FORMAT_PROPERTY, item.getContentType());
                md.put(RestService.LABEL_PROPERTY, item.getName());
                imageData = item.getInputStream();
            } catch(FileUploadException e) {
                throw new ServletException("cannot parse POST",e);
            }
        } else {
            md.put(RestService.FORMAT_PROPERTY, "image/*");
            imageData = request.getInputStream();
        }
        if(hasPrefix(IMAGE_INFIX,request)) {
            try {
                String uri = this.decanonicalizeUrl(request);
                log("UPLOAD IMAGE "+uri);
                restService.updateImage(uri,md,imageData);
            } catch(RestServiceException e) {
                throw new ServletException("failed to write "+request.getRequestURI());
            }
        } else if(hasPrefix(IMAGE_CREATE_ANON_INFIX,request)) {
            String uri = null;
            try {
                log("UPLOAD IMAGE (anonymous)");
                uri = restService.createImage(md,imageData);
            } catch(RestServiceException e) {
                throw new ServletException("failed to create image",e);
            }
            response.getWriter().write(canonicalizeUri(uri,IMAGE_INFIX,request)+"\n");
        } else {
            // not sure how we would hit this case
            throw new ServletException("server error: impossible case in image upload");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //dumpCrap(request); // FIXME debug
        //dumpHeaders(request); // FIXME debug
        if(hasPrefix(ANY_IMAGE_INFIX,request)) {
            doPostImage(request,response);
        } else if(hasPrefix(ANY_COLLECTION_INFIX,request)) {
            // TODO accept multipart uploads
            // TODO currently assumes that everything in a collection is an image
            // TODO if that assumption is not correct, will need a way to track canonical URL's per-resource
            List<String> members = new LinkedList<String>();
            try {
                for(String member : parseList(request.getInputStream())) {
                    String uri = decanonicalizeUrl(member,request);
                    members.add(uri);
                }
            } catch(XPathExpressionException e) {
                throw new ServletException("could not parse collection parameter",e);
            } catch(SAXException e) {
                throw new ServletException("could not parse collection parameter",e);
            }
            try {
                if(hasPrefix(COLLECTION_ADD_INFIX,request)) {
                    String uri = decanonicalizeUrl(request);
                    log("COLLECTION ADD "+uri);
                    restService.addToCollection(uri,members);
                } else if(hasPrefix(COLLECTION_REMOVE_INFIX,request)) {
                    String uri = decanonicalizeUrl(request);
                    log("COLLECTION REMOVE "+uri);
                    restService.removeFromCollection(uri,members);
                } else if(hasPrefix(COLLECTION_INFIX,request)) { // Update
                    String uri = decanonicalizeUrl(request);
                    log("COLLECTION UPDATE "+uri);
                    restService.updateCollection(uri,members);
                } else { // mint a new URI
                    log("COLLECTION CREATE (anonymous)");
                    String uri = restService.createCollection(members);
                    response.getWriter().write(canonicalizeUri(uri,COLLECTION_INFIX,request)+"\n");
                }
            } catch(RestServiceException e) {
                throw new ServletException("could not modify collection",e);
            }
        }
    }

    // deal with lists
    String formatList(Iterable<String> members) {
        StringWriter sw = new StringWriter();
        sw.append("<ul>");
        for(String member : members) {
            sw.append("<li>").append(Xml.escape(member)).append("</li>");
        }
        sw.append("</ul>");
        return sw.toString();
    }

    List<String> parseList(Document list) throws XPathExpressionException, IOException, SAXException {
        return Xml.getNodeValues(Xml.evaluateXPath("/ul/li/text()",list));
    }

    List<String> parseList(InputStream list) throws XPathExpressionException, IOException, SAXException {
        return parseList(Xml.parse(list));
    }

    List<String> parseList(Reader list) throws XPathExpressionException, IOException, SAXException {
        return parseList(Xml.parse(list));
    }


}
