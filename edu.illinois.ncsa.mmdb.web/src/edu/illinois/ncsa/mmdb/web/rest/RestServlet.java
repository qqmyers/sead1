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

import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.CopyFile;
import org.tupeloproject.util.Xml;

import edu.uiuc.ncsa.cet.bean.tupelo.UriCanonicalizer;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.xml.sax.SAXException;

/**
 * RestServlet
 */
public class RestServlet extends HttpServlet {
    static RestService restService; // TODO manage this lifecycle better

    public void init() throws ServletException {
        super.init();
        restService = new RestServiceImpl();
        // TODO make that service aware of the Context
    }

    final String IMAGE_INFIX = "/image/";
    final String IMAGE_CREATE_ANON_INFIX = "/image";
    final String IMAGE_DOWNLOAD_INFIX = "/image/download/";

    final String COLLECTION_INFIX = "/collection/";
    final String COLLECTION_CREATE_ANON_INFIX = "/collection";
    final String COLLECTION_ADD_INFIX = "/collection/add/";
    final String COLLECTION_REMOVE_INFIX = "/collection/remove/";

    final String INFIXES[] = new String[] {
            IMAGE_INFIX,
            IMAGE_CREATE_ANON_INFIX,
            IMAGE_DOWNLOAD_INFIX,
            COLLECTION_INFIX,
            COLLECTION_CREATE_ANON_INFIX,
            COLLECTION_ADD_INFIX,
            COLLECTION_REMOVE_INFIX
    };

    UriCanonicalizer canon = null;
    UriCanonicalizer getCanon(HttpServletRequest request) throws ServletException {
        if(canon == null) {
            canon = new UriCanonicalizer();
            try {
                URL requestUrl = new URL(request.getRequestURL().toString());
                String prefix = requestUrl.getProtocol()+"://"+requestUrl.getHost();
                if(requestUrl.getPort() != -1) {
                    prefix = prefix + ":" + requestUrl.getPort();
                }
                for(String infix : INFIXES) {
                    canon.setCanonicalUrlPrefix(infix, prefix + request.getContextPath() + request.getServletPath() + infix);
                }
            } catch(MalformedURLException x) {
                throw new ServletException("unexpected error: servlet URL is not a URL");
            }
        }
        return canon;
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
        return getCanon(request).canonicalize(infix,uri);
    }

    boolean hasPrefix(String uri, String infix, HttpServletRequest request) throws ServletException {
        return uri.startsWith(canonicalizeUri("",infix,request));
    }

    boolean hasPrefix(String infix, HttpServletRequest request) throws ServletException {
        return hasPrefix(request.getRequestURL().toString(),infix,request);
    }

    String decanonicalizeUrl(String uri, String infix, HttpServletRequest request) throws ServletException {
        String prefix = canonicalizeUri("",infix,request);
        while(uri.startsWith(prefix)) {
            uri = uri.substring(prefix.length());
        }
        return uri;
    }
    String decanonicalizeUrl(String infix, HttpServletRequest request) throws ServletException {
        return decanonicalizeUrl(request.getRequestURL().toString(), infix, request);
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //dumpCrap(request); // FIXME debug
        dumpHeaders(request); // FIXME debug
        if(hasPrefix("/image/",request)) {
            try {
                CopyFile.copy(restService.retrieveImage(decanonicalizeUrl("/image/",request)), response.getOutputStream());
            } catch(RestServiceException e) {
                throw new ServletException("failed to retrieve "+request.getRequestURI());
            }
        } if(hasPrefix("/image/download/",request)) {
            response.setHeader("content-disposition","attachment; filename=foo.bar");
            try {
                CopyFile.copy(restService.retrieveImage(decanonicalizeUrl("/image/download/",request)), response.getOutputStream());
            } catch(RestServiceException e) {
                throw new ServletException("failed to retrieve "+request.getRequestURI());
            }
        } else if(hasPrefix("/collection/",request)) {
            try {
                // TODO currently assumes that everything in a collection is an image
                // TODO if that assumption is not correct, will need a way to track canonical URL's per-resource
                // TODO if the collection is huge, this will bloat memory, may need different API to stage
                List<String> canonicalMembers = new LinkedList<String>();
                String collectionUri = decanonicalizeUrl("/collection/",request);
                for(String member : restService.retrieveCollection(collectionUri)) {
                    canonicalMembers.add(canonicalizeUri(member, "/image/", request));
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
        if(hasPrefix("/image/",request)) {
            try {
                restService.updateImage(decanonicalizeUrl("/image/",request),md,imageData);
            } catch(RestServiceException e) {
                throw new ServletException("failed to write "+request.getRequestURI());
            }
        } else if(hasPrefix("/image",request)) {
            String uri = null;
            try {
                uri = restService.createImage(md,imageData);
            } catch(RestServiceException e) {
                throw new ServletException("failed to create image",e);
            }
            response.getWriter().write(canonicalizeUri(uri,"/image/",request)+"\n");
        } else {
            // not sure how we would hit this case
            throw new ServletException("server error: impossible case in image upload");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //dumpCrap(request); // FIXME debug
        dumpHeaders(request); // FIXME debug
        if(hasPrefix("/image",request)) {
            doPostImage(request,response);
        } else if(hasPrefix("/collection",request)) {
            // TODO accept multipart uploads
            // TODO currently assumes that everything in a collection is an image
            // TODO if that assumption is not correct, will need a way to track canonical URL's per-resource
            List<String> members = new LinkedList<String>();
            try {
                for(String member : parseList(request.getInputStream())) {
                    if(hasPrefix(member,"/image/",request)) {
                        members.add(decanonicalizeUrl(member,"/image/",request));
                    } else {
                        members.add(member);
                    }
                }
            } catch(XPathExpressionException e) {
                throw new ServletException("could not parse collection parameter",e);
            } catch(SAXException e) {
                throw new ServletException("could not parse collection parameter",e);
            }
            try {
                if(hasPrefix("/collection/add/",request)) {
                    restService.addToCollection(decanonicalizeUrl("/collection/add/",request),members);
                } else if(hasPrefix("/collection/remove/",request)) {
                    restService.removeFromCollection(decanonicalizeUrl("/collection/add/",request),members);
                } else if(hasPrefix("/collection/",request)) {
                    restService.updateCollection(decanonicalizeUrl("/collection/",request),members);
                } else { // mint a new URI
                    String uri = restService.createCollection(members);
                    response.getWriter().write(canonicalizeUri(uri,"/collection/",request)+"\n");
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
