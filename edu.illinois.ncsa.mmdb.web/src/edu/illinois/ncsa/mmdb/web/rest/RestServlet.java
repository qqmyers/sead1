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
package edu.illinois.ncsa.mmdb.web.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.http.HTTPException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Xsd;
import org.tupeloproject.util.CopyFile;
import org.tupeloproject.util.Tuple;
import org.tupeloproject.util.Xml;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.illinois.ncsa.cet.search.Hit;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.JiraIssue.JiraIssueType;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.dispatch.JiraIssueHandler;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;
import edu.uiuc.ncsa.cet.bean.PreviewVideoBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewImageBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewVideoBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.UriCanonicalizer;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

/**
 * RestServlet
 */
public class RestServlet extends AuthenticatedServlet {
    static Log                 log                          = LogFactory.getLog(RestServlet.class);

    public static final String COLLECTION_REMOVE_INFIX      = "/collection/remove/";

    public static final String COLLECTION_ADD_INFIX         = "/collection/add/";

    public static final String COLLECTION_CREATE_ANON_INFIX = "/collection";

    public static final String COLLECTION_INFIX             = "/collection/";

    public static final String ANY_COLLECTION_INFIX         = "/collection";

    public static final String VIDEO_INFIX                  = "/video/";

    public static final String IMAGE_DOWNLOAD_INFIX         = "/image/download/";

    public static final String IMAGE_CREATE_ANON_INFIX      = "/image";

    public static final String IMAGE_INFIX                  = "/image/";

    public static final String ANY_IMAGE_INFIX              = "/image";

    public static final String PREVIEW_ANY                  = "/image/preview/";

    public static final String PREVIEW_SMALL                = "/image/preview/small/";

    public static final String PREVIEW_LARGE                = "/image/preview/large/";

    public static final String PREVIEW_SMALL_NEW            = "/image/preview/small/new/";
    public static final String PREVIEW_LARGE_NEW            = "/image/preview/large/new/";

    public static final String SEARCH_INFIX                 = "/search";

    public static final String COLLECTION_PREVIEW           = "/collection/preview/";
    public static final String COLLECTION_PREVIEW_NEW       = "/collection/preview/new/";

    public static final String JIRA_ISSUE                   = "/jira";

    public static final String DATASET                      = "/dataset/";

    static RestService         restService;                                                        // TODO manage this lifecycle better

    public static final String SMALL_404                    = "/nopreview-100.gif";
    public static final String LARGE_404                    = "/nopreview-100.gif";

    public void init() throws ServletException {
        super.init();
        restService = new RestServiceImpl();
        log.trace("REST servlet initialized");
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
     * @param uri
     *            the RDF subject of the resource
     * @param infix
     *            the infix to use
     * @param request
     *            the request (so we can extract the server prefix)
     * @return the canonical URL of the resource
     */
    String canonicalizeUri(String uri, String infix, HttpServletRequest request) throws ServletException {
        return getUriCanonicalizer(request).canonicalize(infix, uri);
    }

    boolean hasPrefix(String uri, String infix, HttpServletRequest request) throws ServletException {
        return getUriCanonicalizer(request).hasPrefix(infix, uri);
    }

    boolean hasPrefix(String infix, HttpServletRequest request) throws ServletException {
        return getUriCanonicalizer(request).hasPrefix(infix, request.getRequestURL().toString());
    }

    String decanonicalizeUrl(String url, HttpServletRequest request) throws ServletException {
        return getUriCanonicalizer(request).decanonicalize(url);
    }

    String decanonicalizeUrl(HttpServletRequest request) throws ServletException {
        String canonical = request.getRequestURL().toString();
        String decanonicalized = getUriCanonicalizer(request).decanonicalize(canonical);
        try {
            decanonicalized = URLDecoder.decode(decanonicalized, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("Could not urldecode uri", e);
        }
        if (!canonical.contains(VIDEO_INFIX) && !decanonicalized.isEmpty() && !decanonicalized.matches("^[a-z]+:.*")) { // if it's empty, it means there is no URI suffix (e.g., search)
            log.warn("canonical url " + canonical + " decanonicalized (incorrectly?) as " + decanonicalized);
        }
        return decanonicalized;
    }

    void dumpHeaders(HttpServletRequest request) {
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            log.trace(headerName + ": " + request.getHeader(headerName));
        }
    }

    Thing getImageThing(String uri) {
        return getContext().getThingSession().fetchThing(Resource.uriRef(uri));
    }

    public static String getSmallPreviewUri(String datasetUri) {
        return getPreviewUri(datasetUri, GetPreviews.SMALL);
    }

    public static String getLargePreviewUri(String datasetUri) {
        return getPreviewUri(datasetUri, GetPreviews.LARGE);
    }

    public static String getCollectionPreviewUri(String collectionUri) {
        return TupeloStore.getInstance().getPreviewUri(collectionUri, GetPreviews.BADGE);
    }

    public static String getPreviewUri(String uri, String size) {
        return getPreviewUri(getPreview(uri, size));
    }

    public static String getPreviewUri(PreviewImageBean pib) {
        return pib == null ? null : pib.getUri();
    }

    public static PreviewImageBean getPreview(String uri, String size) {
        final String realuri;
        if (uri == null) {
            return null;
        }
        log.debug("RestServlet.getPreview(" + uri + ", " + size);
        String badgeUri = TupeloStore.getInstance().getBadge(uri);
        if (badgeUri != null) {
            realuri = badgeUri;
        } else {
            realuri = uri;
        }

        log.debug("Real uri is now: " + realuri);

        try {
            Collection<PreviewImageBean> previews = new LinkedList<PreviewImageBean>();
            Unifier u = new Unifier();
            u.setColumnNames("preview", "height", "width", "mimeType");
            u.addPattern(Resource.uriRef(realuri), PreviewImageBeanUtil.HAS_PREVIEW, "preview");
            u.addPattern("preview", Rdf.TYPE, PreviewImageBeanUtil.PREVIEW_TYPE);
            u.addPattern("preview", PreviewImageBeanUtil.IMAGE_HEIGHT, "height");
            u.addPattern("preview", PreviewImageBeanUtil.IMAGE_WIDTH, "width");
            u.addPattern("preview", Dc.FORMAT, "mimeType");
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "preview") ) {
                Resource previewSubject = row.get(0);
                if (Xsd.LONG.equals(row.get(1).getDatatype()) && Xsd.LONG.equals(row.get(2).getDatatype())) {
                    long height = (Long) row.get(1).asObject();
                    long width = (Long) row.get(2).asObject();
                    PreviewImageBean preview = new PreviewImageBean();
                    preview.setUri(previewSubject.getString());
                    preview.setHeight(height);
                    preview.setWidth(width);
                    String mimeType = row.get(3).getString();
                    preview.setMimeType(mimeType);
                    previews.add(preview);
                }
            }
            if (previews.size() == 0) {
                // do not block on this operation
                (new Thread() {
                    public void run() {
                        TupeloStore.getInstance().extractPreviews(realuri);
                    }
                }).start();
                return null;
            } else {
                long maxArea = 0L;
                long minArea = 0L;
                PreviewImageBean max = null;
                PreviewImageBean min = null;
                for (PreviewImageBean preview : previews ) {
                    long area = preview.getHeight() * preview.getWidth();
                    if (area > maxArea) {
                        maxArea = area;
                        max = preview;
                    }
                    if (minArea == 0 || area < minArea) {
                        minArea = area;
                        min = preview;
                    }
                }
                if (GetPreviews.LARGE.equals(size)) {
                    //log.debug("large preview = "+maxArea+"px "+maxUri);
                    if (maxArea > 100 * 100) {
                        return max;
                    } else {
                        return null; // not big enough to count as "large"
                    }
                } else {
                    //if(minUri != null) { log.debug("small preview = "+minArea+"px "+minUri); }
                    return min;
                }
            }
        } catch (OperatorException x) {
            x.printStackTrace();
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long then = System.currentTimeMillis();
        try {
            doDoGet(request, response);
        } catch (ServletException x) {
            throw x;
        } catch (IOException x) {
            throw x;
        } finally {
            try {
                response.flushBuffer();
            } catch (IllegalStateException x) {
                // may not have been opened; ignore
            }
        }
    }

    protected void doDoGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //dumpCrap(request); // FIXME debug
        dumpHeaders(request); // FIXME debug
        log.debug("Calling: " + request.getRequestURL().toString());
        HttpSession sess = request.getSession(false);
        if (sess != null) {
            log.debug("SessionID: " + request.getSession(false).getId());
        }

        /*********
         * APP SESSION MANAGEMENT ************
         * 
         * /authenticate is handles as a POST
         * /logout invalidates the current session
         * /checklogin returns the user associated with the session
         */

        if (request.getRequestURL().toString().endsWith("logout")) {
            log.debug("REST /logout");
            dontCache(response);
            logout(request, response);
            return;
        }

        //Get ID from session - could be null at this point
        String userId = getUserUri(request);

        //Called when the app has a session cookie but has lost info about who the user is (i.e. at browser refresh)
        if (request.getRequestURL().toString().endsWith("checkLogin")) {
            log.debug("Check Login: " + userId);
            dontCache(response);
            if (userId != null) {
                response.getWriter().print(userId);
                response.flushBuffer();
                log.debug("REST /checkLogin: HTTP session " + request.getSession(false).getId() + " is authenticated as " + userId);
                return;
            } else {
                log.debug("REST /checkLogin: unauthenticated");
                //Only the app should call this, so return forbidden if there's no session/userId
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return; // do not proceed with authentication!
            }
        }

        /**** END SESSION MANAGEMENT ****/

        //If user is not identified by session, check for BasicAuth credentials and authenticate or assign as anonymous
        if ((userId == null) || (userId.equals(PersonBeanUtil.getAnonymous().getUri()))) {
            try {
                //doBasic will set the id based on basic auth credentials if set, otherwise will return anonymous 
                userId = doBasicAuthenticate(request, response);
            } catch (HTTPException he) {
                //If the client sent an auth header and did not succeed in establishing a valid ID, 
                //send an SC_UNAUTHORIZED response with a WWW_Authenticate Header
                // and stop processing
                unauthorized(request, response);
                return;
            }
        }

        /* At this point validUser is a valid user Id or anonymous if no creds available
         * Following sections need to decide, when access is not allowed,
         * whether to return SC_FORBIDDEN (the truth) or SC_UNAUTHORIZED
         * SC_UNAUTHORIZED makes sense for URLs that will be accessed outside the app/standalone
         * and having the browser request credentials directly makes sense
         * SC_FORBIDDEN lets the app know that 'anonymous' /the current user
         * doesn't have enough permissions, and lets our login page provide a relevant
         * message related to permissions (versus bad user/pass)
         * 
         */

        /*Begin large switch - for each class of uri, 
         * 1) check appropriate permission for userId and the resource
         * 2) handle processing
         * 3) decide how to handle permission failure (401 0r 403)
         */

        String uri = decanonicalizeUrl(request);
        // JIRA Issue creator
        if (hasPrefix(JIRA_ISSUE, request)) {
            //We don't have a separate permission for this - using COMMENT as something conceptually close
            if (isAllowed(userId, Permission.ADD_COMMENT)) {

                dontCache(response);
                createJiraIssue(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        } else if (hasPrefix(IMAGE_DOWNLOAD_INFIX, request)) {
            if (isAllowed(userId, uri, Permission.DOWNLOAD, true)) {
                log.trace("DOWNLOAD IMAGE " + uri);
                // keep track who downloads the file
                TripleWriter tw = new TripleWriter();
                Resource personuri = Resource.uriRef(PersonBeanUtil.getPersonID(getHttpSessionUser(request)));
                Resource downloaduri = Resource.uriRef();
                tw.add(Resource.uriRef(uri), MMDB.DOWNLOADED_BY, downloaduri);
                tw.add(downloaduri, Dc.CREATOR, personuri);
                tw.add(downloaduri, Dc.DATE, new Date());
                try {
                    getContext().perform(tw);
                } catch (OperatorException e1) {
                    throw new ServletException("failed to count download for " + request.getRequestURI(), e1);
                }
                Unifier uf = new Unifier();
                uf.addPattern(Resource.uriRef(uri), MMDB.DOWNLOADED_BY, "download");
                uf.addPattern("download", Dc.CREATOR, "downloader");
                uf.setColumnNames("download", "downloader");
                try {
                    getContext().perform(uf);
                } catch (OperatorException e) {
                    log.warn("Could not get download count for dataset.", e);
                }
                for (Tuple<Resource> row : uf.getResult() ) {
                    log.debug(row.get(0) + " " + row.get(1));
                }

                try {
                    Thing imageThing = getImageThing(uri);
                    String contentType = imageThing.getString(Dc.FORMAT);
                    String label = imageThing.getString(RestService.LABEL_PROPERTY);
                    String filename = getImageThing(uri).getString(RestService.FILENAME_PROPERTY);
                    String name = filename;
                    if (name == null) {
                        name = label;
                    }
                    if (contentType != null) {
                        response.setContentType(contentType);
                    }
                    response.setHeader("content-disposition", "attachment; filename=\"" + name + "\"");
                    response.flushBuffer();
                    CopyFile.copy(restService.retrieveImage(uri), response.getOutputStream());
                } catch (RestServiceException e) {
                    throw new ServletException("failed to retrieve " + request.getRequestURI());
                } catch (OperatorException e) {
                    throw new ServletException("failed to retrieve metadata for " + request.getRequestURI());
                }
            } else {
                if (userId.equals(_anonymousId)) {
                    //Download URLs may be used external to the app...
                    unauthorized(request, response);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
                return;
            }

        } else if (hasPrefix(DATASET, request)) {
            //FixME: Same as IMAGE_DOWNLOAD_INFIX in terms of processing?
            //FixMe: Is this still used anywhere? (stripping the extension seems obsolete)
            if (isAllowed(userId, uri, Permission.DOWNLOAD, true)) {
                log.debug("Downloading Dataset " + uri);
                // TODO check that file ends in extension instead of assuming it does
                uri = uri.substring(0, uri.length() - 4);
                // keep track who downloads the file
                TripleWriter tw = new TripleWriter();
                Resource personuri = Resource.uriRef(PersonBeanUtil.getPersonID(getHttpSessionUser(request)));
                Resource downloaduri = Resource.uriRef();
                tw.add(Resource.uriRef(uri), MMDB.DOWNLOADED_BY, downloaduri);
                tw.add(downloaduri, Dc.CREATOR, personuri);
                tw.add(downloaduri, Dc.DATE, new Date());
                try {
                    getContext().perform(tw);
                } catch (OperatorException e1) {
                    throw new ServletException("failed to count download for " + request.getRequestURI(), e1);
                }
                try {
                    Thing imageThing = getImageThing(uri);
                    String contentType = imageThing.getString(Dc.FORMAT);
                    String label = imageThing.getString(RestService.LABEL_PROPERTY);
                    String filename = getImageThing(uri).getString(RestService.FILENAME_PROPERTY);
                    String name = filename;
                    if (name == null) {
                        name = label;
                    }
                    if (contentType != null) {
                        response.setContentType(contentType);
                    }
                    response.setHeader("content-disposition", "attachment; filename=\"" + name + "\"");
                    response.flushBuffer();
                    CopyFile.copy(restService.retrieveImage(uri), response.getOutputStream());
                } catch (RestServiceException e) {
                    throw new ServletException("failed to retrieve " + request.getRequestURI());
                } catch (OperatorException e) {
                    throw new ServletException("failed to retrieve metadata for " + request.getRequestURI());
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
            return;

        } else if (hasPrefix(PREVIEW_ANY, request)) {
            if (isAllowed(userId, uri, Permission.VIEW_MEMBER_PAGES, false)) {

                log.debug("Getting preview: " + request.getRequestURL().toString());
                response.flushBuffer(); // MMDB-620
                long then = System.currentTimeMillis(); // FIXME debug
                PreviewImageBean preview = null;
                String image404 = null;
                if (hasPrefix(PREVIEW_SMALL, request)) {
                    log.debug("GET PREVIEW (small) " + uri);
                    preview = TupeloStore.getInstance().getPreview(uri, GetPreviews.SMALL);
                    image404 = SMALL_404;
                } else if (hasPrefix(PREVIEW_LARGE, request)) {
                    log.debug("GET PREVIEW (large) " + uri);
                    preview = TupeloStore.getInstance().getPreview(uri, GetPreviews.LARGE);
                    image404 = LARGE_404;
                } else {
                    log.debug("GET PREVIEW (any) " + uri);
                    preview = getPreview(uri, PREVIEW_ANY);
                    image404 = SMALL_404;
                }
                if (preview == null) {
                    log.info(uri + " has NO PREVIEW");
                } else {
                    long now = System.currentTimeMillis();
                    if (now - then > 100) {
                        log.warn("preview lookup took " + (now - then) + "ms");
                    }
                    String contentType = preview.getMimeType();
                    if (contentType != null) {
                        response.setContentType(contentType);
                        response.flushBuffer(); // MMDB-620
                    }
                }
                String previewUri = preview != null ? preview.getUri() : null;
                //FixMe: is 404 w/o any image the right thing to send when a preview is not available?
                //(e.g. if, for example, extractor is down, should we send the 404 image and no-cache headers?)
                //Does this break logic anywhere?
                returnImage(request, response, previewUri, image404, shouldCache404(uri));
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        } else if (hasPrefix(COLLECTION_PREVIEW, request)) {
            if (isAllowed(userId, uri, Permission.VIEW_MEMBER_PAGES, false)) {

                log.debug("GET PREVIEW (collection) " + uri);
                String badge = TupeloStore.getInstance().getBadge(uri);
                String previewUri = TupeloStore.getInstance().getPreviewUri(badge, GetPreviews.SMALL); // should accept and propogate null
                returnImage(request, response, previewUri, SMALL_404, shouldCache404(badge)); // should accept and propagate null
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } else if (hasPrefix(VIDEO_INFIX, request)) {
            if (isAllowed(userId, "tag:cet.ncsa.uiuc.edu,2008:/bean/PreviewVideo/" + uri, Permission.DOWNLOAD, false)) {
                int idx = uri.lastIndexOf(".");
                String ext = null;
                if (idx > 0) {
                    ext = uri.substring(idx + 1);
                    uri = uri.substring(0, idx);
                }
                uri = "tag:cet.ncsa.uiuc.edu,2008:/bean/PreviewVideo/" + uri;
                try {
                    PreviewVideoBean pvb = new PreviewVideoBeanUtil(TupeloStore.getInstance().getBeanSession()).get(uri);

                    if (request.getHeader("If-Modified-Since") != null) {
                        Date d = new Date(request.getHeader("If-Modified-Since"));
                        if ((pvb.getDate().getTime() - d.getTime()) < 1000) {
                            response.setStatus(304);
                            return;
                        }
                    }
                    long start = 0;
                    long len = pvb.getSize();
                    if (request.getHeader("Range") != null) {
                        Pattern p = Pattern.compile("bytes=(\\d+)-(\\d+)?");
                        Matcher m = p.matcher(request.getHeader("Range"));
                        if (m.find()) {
                            start = Long.parseLong(m.group(1));
                            long end = pvb.getSize() - 1;
                            if (m.group(2) != null) {
                                end = Long.parseLong(m.group(2));
                            }
                            len = (end - start) + 1;
                            response.setStatus(206);
                            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + pvb.getSize());
                        }
                    }
                    response.setHeader("Accept-Ranges", "bytes");
                    response.setContentType(pvb.getMimeType());
                    response.setContentLength((int) len);
                    response.setDateHeader("Last-Modified", pvb.getDate().getTime());
                    response.setHeader("Expires", null);
                    response.flushBuffer();
                    InputStream is = restService.retrieveImage(uri);
                    if (start > 0) {
                        is.skip(start);
                    }
                    OutputStream os = response.getOutputStream();
                    try {
                        byte[] buf = new byte[10240];
                        int x = 0;
                        while ((len > 0) && (x = is.read(buf, 0, (int) Math.min(buf.length, len))) > 0) {
                            len -= x;
                            os.write(buf, 0, x);
                        }
                    } finally {
                        is.close();
                        os.close();
                    }
                } catch (Exception e) {
                    throw new ServletException("failed to retrieve " + request.getRequestURI(), e);
                }
            } else {
                if (userId.equals(_anonymousId)) {
                    //Video URLs may be used external to the app...
                    unauthorized(request, response);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }

            }
            return;
        } else if (hasPrefix(IMAGE_INFIX, request)) {
            if (isAllowed(userId, uri, Permission.DOWNLOAD, false)) {
                if (hasPrefix(PREVIEW_ANY, request)) {
                    //Preview images should not fall through to this block
                    log.warn("Preview request being handled as image request!");
                }
                log.debug("GET IMAGE " + uri);
                try {
                    Thing imageThing = getImageThing(uri);
                    String contentType = imageThing.getString(Dc.FORMAT);
                    if (contentType != null) {
                        response.setContentType(contentType);
                    }
                    response.flushBuffer();
                    CopyFile.copy(restService.retrieveImage(uri), response.getOutputStream());
                } catch (RestServiceException e) {
                    throw new ServletException("failed to retrieve " + request.getRequestURI(), e);
                } catch (OperatorException e) {
                    throw new ServletException("failed to retrieve metadata for " + request.getRequestURI(), e);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        } else if (hasPrefix(COLLECTION_INFIX, request)) {
            if (isAllowed(userId, uri, Permission.VIEW_MEMBER_PAGES, false)) {

                log.trace("LIST COLLECTION" + uri);
                try {
                    // TODO currently assumes that everything in a collection is an image
                    // TODO if that assumption is not correct, will need a way to track canonical URL's per-resource
                    // TODO if the collection is huge, this will bloat memory, may need different API to stage
                    List<String> canonicalMembers = new LinkedList<String>();
                    for (String member : restService.retrieveCollection(uri) ) {
                        canonicalMembers.add(canonicalizeUri(member, IMAGE_INFIX, request));
                    }
                    dontCache(response);
                    response.setContentType("text/html");
                    response.getWriter().write(formatList(canonicalMembers));
                } catch (RestServiceException e) {
                    if (e.isNotFound()) {
                        dontCache(response);
                        response.setStatus(404);
                    } else {
                        throw new ServletException("failed to retrieve " + request.getRequestURI());
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        } else if (hasPrefix(SEARCH_INFIX, request)) {
            if (isAllowed(userId, Permission.VIEW_MEMBER_PAGES)) {

                doSearch(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            throw new ServletException("unrecognized API call " + request.getRequestURI());
        }
    }

    // ----------------------------------------------------------------------
    // JIRA ISSUE CREAION
    // ----------------------------------------------------------------------

    /**
     * Submit the issue to the Jira Issue Handler
     * 
     * @param request
     *            the form with all information about the issue to be created.
     * @param response
     *            any information to be returned to the user.
     */
    private void createJiraIssue(HttpServletRequest request, HttpServletResponse response) {
        JiraIssueType issueType = JiraIssueType.valueOf(request.getParameter("issueType"));
        String summary = request.getParameter("subject");
        String description = request.getParameter("body");
        String email = request.getParameter("email");

        try {
            JiraIssueHandler.createJiraIssue(issueType, email, summary, description);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (MessagingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // ----------------------------------------------------------------------

    void returnImage(HttpServletRequest request, HttpServletResponse response, String imageUri) throws IOException, ServletException {
        returnImage(request, response, imageUri, null);
    }

    void returnImage(HttpServletRequest request, HttpServletResponse response, String imageUri, String image404) throws IOException, ServletException {
        returnImage(request, response, imageUri, image404, true);
    }

    void returnImage(HttpServletRequest request, HttpServletResponse response, String imageUri, String image404, boolean shouldCache) throws IOException, ServletException {
        response.flushBuffer();
        if (!shouldCache) {
            dontCache(response);
        }
        if (imageUri != null) {
            try {
                log.debug("Retrieving Image: " + imageUri);
                long then = System.currentTimeMillis(); // FIXME debug
                InputStream imageData = restService.retrieveImage(imageUri);
                long now = System.currentTimeMillis(); // FIXME debug
                if (now - then > 100) {
                    log.warn("BlobFetcher took " + (now - then) + "ms");
                }
                then = System.currentTimeMillis();
                response.flushBuffer();
                CopyFile.copy(imageData, response.getOutputStream());
                now = System.currentTimeMillis(); // FIXME debug
                if (now - then > 100) {
                    log.warn("image write to client took " + (now - then) + "ms");
                }
                return;
            } catch (RestServiceException e) {
                if (!e.isNotFound()) {
                    throw new ServletException("failed to retrieve " + request.getRequestURI());
                }
            }
        }
        if (image404 != null && shouldCache) {
            response.setContentType("image/gif");
            // return the 404 image
            URL fileLocation = this.getClass().getResource(image404);
            try {
                log.debug("404 image location = " + fileLocation);
                File file = new File(fileLocation.toURI());
                FileInputStream fis = new FileInputStream(file);
                CopyFile.copy(fis, response.getOutputStream());
                response.getOutputStream().flush();
                return;
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        response.setStatus(404);
    }

    public static boolean shouldCache404(String datasetUri) {
        if (datasetUri == null) {
            return false;
        }
        try {
            ThingSession ts = new ThingSession(TupeloStore.getInstance().getContext());
            // FIXME "endTime1" is a kludgy way to represent execution stage information
            Date endTime = ts.getDate(Resource.uriRef(datasetUri), Cet.cet("metadata/Extractor/endTime1")); // FIXME magic number antipattern
            if (endTime == null) {
                endTime = ts.getDate(Resource.uriRef(datasetUri), Cet.cet("metadata/extractor/endTime1"));
            }
            ts.close();
            // if there's an end time, then preview extraction has completed, so we should cache this response
            return endTime != null;
        } catch (OperatorException x) {
            return false; // to be safe, don't cache when we don't know
        }
    }

    private void doPostImage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        InputStream imageData = null;
        //
        //Get ID from session - could be null at this point
        String userId = getUserUri(request);
        if (userId != null) {
            //            duplicates what's in doPost()            
            //            if (isAllowed(userId, Permission.UPLOAD_DATA)) {
            Map<Resource, Object> md = new HashMap<Resource, Object>();
            md.put(RestService.DATE_PROPERTY, new Date());
            //
            if (ServletFileUpload.isMultipartContent(request)) {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                try {
                    List<FileItem> items = upload.parseRequest(request);
                    if (items.size() > 1) {
                        log.trace("warning: ignoring all but first content item in multi-part POST");
                    } else if (items.size() == 0) {
                        throw new ServletException("no file data in multi-part POST");
                    }
                    FileItem item = items.get(0);
                    md.put(RestService.FORMAT_PROPERTY, item.getContentType());
                    md.put(RestService.LABEL_PROPERTY, item.getName());
                    md.put(RestService.FILENAME_PROPERTY, item.getName());
                    md.put(Files.LENGTH, item.getSize());
                    md.put(Dc.DATE, new Date()); // uploaded at current date
                    imageData = item.getInputStream();
                } catch (FileUploadException e) {
                    throw new ServletException("cannot parse POST", e);
                }
            } else {
                md.put(RestService.FORMAT_PROPERTY, "image/*");
                imageData = request.getInputStream();
            }
            if (hasPrefix(IMAGE_INFIX, request)) {
                try {
                    String uri = this.decanonicalizeUrl(request);
                    log.trace("UPLOAD IMAGE " + uri);
                    restService.updateImage(uri, md, imageData);
                } catch (RestServiceException e) {
                    throw new ServletException("failed to write " + request.getRequestURI());
                }
            } else if (hasPrefix(IMAGE_CREATE_ANON_INFIX, request)) {
                String uri = null;
                try {
                    log.trace("UPLOAD IMAGE");
                    /* Don't want anonymous uploads, so add the username */
                    String username = getHttpSessionUser(request);
                    if (username != null) {
                        md.put(Dc.CREATOR, Resource.uriRef(PersonBeanUtil.getPersonID(username)));
                    }
                    uri = restService.createImage(md, imageData);
                } catch (RestServiceException e) {
                    throw new ServletException("failed to create image", e);
                }
                response.getWriter().write(canonicalizeUri(uri, IMAGE_INFIX, request) + "\n");
            } else {
                // not sure how we would hit this case
                throw new ServletException("server error: impossible case in image upload");
            }
        }
        //        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //dumpCrap(request); // FIXME debug
        //dumpHeaders(request); // FIXME debug

        //Handle "/authenticate" post
        super.doPost(request, response);

        //Get ID from session - could be null at this point
        String userId = getUserUri(request);
        if (userId != null) {
            if (isAllowed(userId, Permission.UPLOAD_DATA)) {

                if (hasPrefix(ANY_IMAGE_INFIX, request)) {
                    doPostImage(request, response);
                } else if (hasPrefix(ANY_COLLECTION_INFIX, request)) {
                    // TODO accept multipart uploads
                    // TODO currently assumes that everything in a collection is an image
                    // TODO if that assumption is not correct, will need a way to track canonical URL's per-resource
                    List<String> members = new LinkedList<String>();
                    try {
                        for (String member : parseList(request.getInputStream()) ) {
                            String uri = decanonicalizeUrl(member, request);
                            members.add(uri);
                        }
                    } catch (XPathExpressionException e) {
                        throw new ServletException("could not parse collection parameter", e);
                    } catch (SAXException e) {
                        throw new ServletException("could not parse collection parameter", e);
                    }
                    try {
                        if (hasPrefix(COLLECTION_ADD_INFIX, request)) {
                            String uri = decanonicalizeUrl(request);
                            log.trace("COLLECTION ADD " + uri);
                            restService.addToCollection(uri, members);
                        } else if (hasPrefix(COLLECTION_REMOVE_INFIX, request)) {
                            String uri = decanonicalizeUrl(request);
                            log.trace("COLLECTION REMOVE " + uri);
                            restService.removeFromCollection(uri, members);
                        } else if (hasPrefix(COLLECTION_INFIX, request)) { // Update
                            String uri = decanonicalizeUrl(request);
                            log.trace("COLLECTION UPDATE " + uri);
                            restService.updateCollection(uri, members);
                        } else { // mint a new URI
                            log.trace("COLLECTION CREATE (anonymous)");
                            String uri = restService.createCollection(members);
                            response.getWriter().write(canonicalizeUri(uri, COLLECTION_INFIX, request) + "\n");
                        }
                    } catch (RestServiceException e) {
                        throw new ServletException("could not modify collection", e);
                    }
                }
            } else if (hasPrefix(SEARCH_INFIX, request) && isAllowed(userId, Permission.VIEW_MEMBER_PAGES)) { // FIXME check permissions
                doSearch(request, response);
            }
        }
    }

    void doSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchString = request.getParameter("query");
        String offsetString = request.getParameter("offset");
        String limitString = request.getParameter("limit");
        int offset = 0;
        int limit = 1000;
        if (offsetString != null) {
            offset = Integer.parseInt(offsetString);
        }
        if (limitString != null) {
            limit = Integer.parseInt(limitString);
        }
        List<String> result = new LinkedList<String>();
        for (Hit hit : TupeloStore.getInstance().getSearch().search(searchString, limit, offset) ) {
            result.add(hit.getId());
        }
        log.debug("SEARCH returning " + result.size() + " result(s)");
        response.setContentType("text/html");
        response.getWriter().write(formatList(result, true));
        response.getWriter().flush();
    }

    // deal with lists
    String formatList(Iterable<String> members) {
        return formatList(members, false);
    }

    String formatList(Iterable<String> members, boolean ordered) {
        StringWriter sw = new StringWriter();
        sw.append(ordered ? "<ol>" : "<ul>");
        for (String member : members ) {
            sw.append("<li>").append(Xml.escape(member)).append("</li>");
        }
        sw.append(ordered ? "</ol>" : "</ul>");
        return sw.toString();
    }

    List<String> parseList(Document list) throws XPathExpressionException, IOException, SAXException {
        return Xml.getNodeValues(Xml.evaluateXPath("/ul/li/text()", list));
    }

    List<String> parseList(InputStream list) throws XPathExpressionException, IOException, SAXException {
        return parseList(Xml.parse(list));
    }

    List<String> parseList(Reader list) throws XPathExpressionException, IOException, SAXException {
        return parseList(Xml.parse(list));
    }

}
