package edu.illinois.ncsa.mmdb.web.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;

import org.tupeloproject.util.CopyFile;
import org.tupeloproject.util.Xml;

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
        try {
            URL requestUrl = new URL(request.getRequestURL().toString());
            String prefix = requestUrl.getProtocol()+"://"+requestUrl.getHost();
            if(requestUrl.getPort() != -1) {
                prefix = prefix + ":" + requestUrl.getPort();
            }
            String canonical = prefix + request.getContextPath() + request.getServletPath() + infix + uri;
            //System.out.println("canonical URI for "+uri+" with infix "+infix+" = "+canonical);//FIXME debug
            return canonical;
        } catch(MalformedURLException x) {
            throw new ServletException("unexpected error: servlet URL is not a URL");
        }
    }

    boolean hasPrefix(String uri, String infix, HttpServletRequest request) throws ServletException {
        return uri.startsWith(canonicalizeUri("",infix,request));
    }

    boolean hasPrefix(String infix, HttpServletRequest request) throws ServletException {
        return hasPrefix(request.getRequestURL().toString(),infix,request);
    }

    String getSuffix(String uri, String infix, HttpServletRequest request) throws ServletException {
        return uri.substring(canonicalizeUri("",infix,request).length());
    }
    String getSuffix(String infix, HttpServletRequest request) throws ServletException {
        return getSuffix(request.getRequestURL().toString(), infix, request);
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
                CopyFile.copy(restService.retrieveImage(getSuffix("/image/",request)), response.getOutputStream());
            } catch(RestServiceException e) {
                throw new ServletException("failed to retrieve "+request.getRequestURI());
            }
        } else if(hasPrefix("/collection/",request)) {
            try {
                // TODO currently assumes that everything in a collection is an image
                // TODO if that assumption is not correct, will need a way to track canonical URL's per-resource
                // TODO if the collection is huge, this will bloat memory, may need different API to stage
                List<String> canonicalMembers = new LinkedList<String>();
                String collectionUri = getSuffix("/collection/",request);
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //dumpCrap(request); // FIXME debug
        dumpHeaders(request); // FIXME debug
        if(hasPrefix("/image/",request)) {
            try {
                restService.updateImage(getSuffix("/image/",request),request.getInputStream());
            } catch(RestServiceException e) {
                throw new ServletException("failed to write "+request.getRequestURI());
            }
        } else if(hasPrefix("/image",request)) {
            String uri = null;
            try {
                uri = restService.createImage(request.getInputStream());
            } catch(RestServiceException e) {
                throw new ServletException("failed to create image",e);
            }
            response.getWriter().write(canonicalizeUri(uri,"/image/",request)+"\n");
        } else if(hasPrefix("/collection",request)) {
            // TODO currently assumes that everything in a collection is an image
            // TODO if that assumption is not correct, will need a way to track canonical URL's per-resource
            List<String> members = new LinkedList<String>();
            try {
                for(String member : parseList(request.getInputStream())) {
                    if(hasPrefix(member,"/image/",request)) {
                        members.add(getSuffix(member,"/image/",request));
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
                    restService.addToCollection(getSuffix("/collection/add/",request),members);
                } else if(hasPrefix("/collection/remove/",request)) {
                    restService.removeFromCollection(getSuffix("/collection/add/",request),members);
                } else if(hasPrefix("/collection/",request)) {
                    restService.updateCollection(getSuffix("/collection/",request),members);
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
