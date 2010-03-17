package edu.illinois.ncsa.mmdb.web.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.util.CopyFile;
import org.tupeloproject.util.Xml;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.illinois.ncsa.cet.search.Hit;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewImageBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.UriCanonicalizer;

/**
 * RestServlet
 */
public class RestServlet extends AuthenticatedServlet {
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
	
	public static final String PREVIEW_ANY = "/image/preview/";
	
	public static final String PREVIEW_SMALL = "/image/preview/small/";
	
	public static final String PREVIEW_LARGE = "/image/preview/large/";
	
	public static final String SEARCH_INFIX = "/search";
	
	public static final String COLLECTION_PREVIEW = "/collection/preview/";
	
    static RestService restService; // TODO manage this lifecycle better

    static final String SMALL_404 = "preview-100.gif";
    static final String LARGE_404 = "preview-500.gif";
    
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
    	String canonical = request.getRequestURL().toString();
    	String decanonicalized = getUriCanonicalizer(request).decanonicalize(canonical);
    	if(!decanonicalized.matches("^[a-z]+:.*")) {
    		log.warn("canonical url "+canonical+" decanonicalized as "+decanonicalized);
    	}
        return decanonicalized;
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
    
    public static String getSmallPreviewUri(String datasetUri) {
    	return getPreviewUri(datasetUri, GetPreviews.SMALL);
    }
    public static String getLargePreviewUri(String datasetUri) {
    	return getPreviewUri(datasetUri, GetPreviews.LARGE);
    }
    public static String getCollectionPreviewUri(String collectionUri) {
    	return TupeloStore.getInstance().getPreview(collectionUri, GetPreviews.BADGE);
    }
    
    public static String getPreviewUri(String uri, String size) {
    	PreviewImageBean p = getPreview(uri,size);
    	return p == null ? null : p.getUri();
    }
    
    public static PreviewImageBean getPreview(String uri, String size) {
    	BeanSession bs = TupeloStore.getInstance().getBeanSession();
    	PreviewImageBeanUtil pibu = new PreviewImageBeanUtil(bs);
    	try {
    		Collection<PreviewImageBean> previews = pibu.getAssociationsFor(uri);
    		if(previews.size()==0) {
    			TupeloStore.getInstance().extractPreviews(uri);
    			return null;
    		} else {
    			long maxArea = 0L;
    			long minArea = 0L;
    			PreviewImageBean max = null;
    			PreviewImageBean min = null;
    			for(PreviewImageBean preview : previews) {
    				long area = preview.getHeight() * preview.getWidth();
    				if(area > maxArea) { maxArea = area; max = preview; }
    				if(minArea == 0 || area < minArea) { minArea = area; min = preview; }
    			}
    			if(GetPreviews.LARGE.equals(size)) {
    				//log.debug("large preview = "+maxArea+"px "+maxUri);
    				if(maxArea > 100 * 100) {
    					return max;
    				} else {
    					return null; // not big enough to count as "large"
    				}
    			} else {
    				//if(minUri != null) { log.debug("small preview = "+minArea+"px "+minUri); }
    				return min;
    			}
    		}
    	} catch(OperatorException x) {
    		return null;
    	}
    }
    
    @Override  
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	long then = System.currentTimeMillis();
    	try {
    		doDoGet(request, response);
    	} catch(ServletException x) {
    		throw x;
    	} catch(IOException x) {
    		throw x;
    	} finally {
    		long elapsed = System.currentTimeMillis() - then;
    		if(elapsed > 30) {
    			log.debug("REST GET serviced in "+elapsed+"ms");
    		}
    		try {
    			response.getOutputStream().close();
    		} catch(IllegalStateException x) {
    			// may not have been opened; ignore
    		}
    	}
    }
    protected void doDoGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //dumpCrap(request); // FIXME debug
        //dumpHeaders(request); // FIXME debug
    	if(request.getRequestURL().toString().endsWith("logout")) {
    		logout(request, response);
    		return;
    	}
    	if(!authenticate(request,response)) {
    		return;
    	}
        String uri = decanonicalizeUrl(request);
        if(hasPrefix(IMAGE_DOWNLOAD_INFIX,request)) {
            log.trace("DOWNLOAD IMAGE "+uri);
            try {
                String filename = getImageThing(uri).getString(RestService.LABEL_PROPERTY);
                response.setHeader("content-disposition","attachment; filename=\""+filename+"\"");
                CopyFile.copy(restService.retrieveImage(uri), response.getOutputStream());
            } catch(RestServiceException e) {
            	throw new ServletException("failed to retrieve "+request.getRequestURI());
            } catch (OperatorException e) {
            	throw new ServletException("failed to retrieve metadata for "+request.getRequestURI());
			}
        } else if(hasPrefix(PREVIEW_ANY,request)) {
        	String previewUri = null;
        	if(hasPrefix(PREVIEW_SMALL,request)) {
        		log.trace("GET PREVIEW (small) "+uri);
        		previewUri = TupeloStore.getInstance().getPreview(uri, GetPreviews.SMALL);
        		returnImage(request, response, previewUri, SMALL_404, shouldCache404(uri));
        	} else if(hasPrefix(PREVIEW_LARGE,request)) {
        		log.trace("GET PREVIEW (large) "+uri);
        		previewUri = TupeloStore.getInstance().getPreview(uri, GetPreviews.LARGE);
        		returnImage(request, response, previewUri, LARGE_404, shouldCache404(uri));
        	} else {
        		log.trace("GET PREVIEW (any) "+uri);
        		previewUri = getPreviewUri(uri, PREVIEW_ANY);
            	returnImage(request, response, previewUri, SMALL_404, shouldCache404(uri));
        	}
        } else if(hasPrefix(COLLECTION_PREVIEW,request)) {
        	log.trace("GET PREVIEW (collection) "+uri);
        	String previewUri = getCollectionPreviewUri(uri);
        	returnImage(request, response, previewUri, SMALL_404);
        } else if(hasPrefix(IMAGE_INFIX,request)) {
            log.trace("GET IMAGE "+uri);
            returnImage(request, response, uri);
        } else if(hasPrefix(COLLECTION_INFIX,request)) {
            log.trace("LIST COLLECTION"+uri);
            try {
                // TODO currently assumes that everything in a collection is an image
                // TODO if that assumption is not correct, will need a way to track canonical URL's per-resource
                // TODO if the collection is huge, this will bloat memory, may need different API to stage
                List<String> canonicalMembers = new LinkedList<String>();
                for(String member : restService.retrieveCollection(uri)) {
                    canonicalMembers.add(canonicalizeUri(member, IMAGE_INFIX, request));
                }
                response.addHeader("Pragma", "no-cache");
                response.setContentType("text/html");
                response.getWriter().write(formatList(canonicalMembers));
            } catch(RestServiceException e) {
            	if(e.isNotFound()) {
            		response.addHeader("Pragma", "no-cache");
            		response.setStatus(404);
            	} else {
            		throw new ServletException("failed to retrieve "+request.getRequestURI());
            	}
            }
        } else if(request.getRequestURL().toString().endsWith("authenticate")) {
        	// we're just authenticating, and that has already been handled. do not report an error.
        	// for convenience, produce the session key as a string
        	response.getWriter().print(lookupSessionKey(getHttpSessionUser(request)));
        	response.getWriter().flush();
        } else if(hasPrefix(SEARCH_INFIX,request)) {
        	doSearch(request,response);
        } else {
            throw new ServletException("unrecognized API call "+request.getRequestURI());
        }
    }
    void returnImage(HttpServletRequest request, HttpServletResponse response, String imageUri) throws IOException, ServletException {
    	returnImage(request,response,imageUri,null);
    }
    void returnImage(HttpServletRequest request, HttpServletResponse response, String imageUri, String image404) throws IOException, ServletException {
    	returnImage(request,response,imageUri,image404,true);
    }
    void returnImage(HttpServletRequest request, HttpServletResponse response, String imageUri, String image404, boolean shouldCache) throws IOException, ServletException {
    	if(imageUri != null) {
    		try {
    			CopyFile.copy(restService.retrieveImage(imageUri), response.getOutputStream());
    			return;
    		} catch(RestServiceException e) {
    			if(!e.isNotFound()) {
    				throw new ServletException("failed to retrieve "+request.getRequestURI());
    			}
    		}
    	}
    	if(image404 != null) {
    		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    		String redirectTo = TupeloStore.getImagePath(request, image404);
    		response.setHeader("Location", redirectTo);
    	} else {
    		response.setStatus(404);
    	}
    	if(!shouldCache) {
    		response.addHeader("Pragma", "no-cache");
    	}
    }
    public static boolean shouldCache404(String datasetUri) {
    	try {
    		TupeloStore.refetch(datasetUri);
    		ThingSession ts = TupeloStore.getInstance().getBeanSession().getThingSession();
    		// FIXME "endTime1" is a kludgy way to represent execution stage information
    		Date endTime = ts.getDate(Resource.uriRef(datasetUri), Cet.cet("metadata/extractor/endTime1"));
    		// if there's an end time, then preview extraction has completed, so we should cache this response
    		return endTime != null;
    	} catch(OperatorException x) {
    		return false; // to be safe, don't cache when we don't know
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
                    log.trace("warning: ignoring all but first content item in multi-part POST");
                } else if(items.size() == 0) {
                    throw new ServletException("no file data in multi-part POST");
                }
                FileItem item = items.get(0);
                md.put(RestService.FORMAT_PROPERTY, item.getContentType());
                md.put(RestService.LABEL_PROPERTY, item.getName());
                md.put(Files.LENGTH, item.getSize());
                md.put(Dc.DATE, new Date()); // uploaded at current date
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
                log.trace("UPLOAD IMAGE "+uri);
                restService.updateImage(uri,md,imageData);
            } catch(RestServiceException e) {
                throw new ServletException("failed to write "+request.getRequestURI());
            }
        } else if(hasPrefix(IMAGE_CREATE_ANON_INFIX,request)) {
            String uri = null;
            try {
                log.trace("UPLOAD IMAGE (anonymous)");
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
    	if(!authenticate(request,response)) {
    		return;
    	}
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
                    log.trace("COLLECTION ADD "+uri);
                    restService.addToCollection(uri,members);
                } else if(hasPrefix(COLLECTION_REMOVE_INFIX,request)) {
                    String uri = decanonicalizeUrl(request);
                    log.trace("COLLECTION REMOVE "+uri);
                    restService.removeFromCollection(uri,members);
                } else if(hasPrefix(COLLECTION_INFIX,request)) { // Update
                    String uri = decanonicalizeUrl(request);
                    log.trace("COLLECTION UPDATE "+uri);
                    restService.updateCollection(uri,members);
                } else { // mint a new URI
                    log.trace("COLLECTION CREATE (anonymous)");
                    String uri = restService.createCollection(members);
                    response.getWriter().write(canonicalizeUri(uri,COLLECTION_INFIX,request)+"\n");
                }
            } catch(RestServiceException e) {
                throw new ServletException("could not modify collection",e);
            }
        } else if(hasPrefix(SEARCH_INFIX,request)) {
        	doSearch(request,response);
        }
    }

    void doSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String searchString = request.getParameter("query");
    	String offsetString = request.getParameter("offset");
    	String limitString = request.getParameter("limit");
    	int offset = 0;
    	int limit = 1000;
    	if(offsetString != null) {
    		offset = Integer.parseInt(offsetString);
    	}
    	if(limitString != null) {
    		limit = Integer.parseInt(limitString);
    	}
    	List<String> result = new LinkedList<String>();
    	for(Hit hit : TupeloStore.getInstance().getSearch().search(searchString, limit, offset)) {
    		result.add(hit.getId());
    	}
    	response.setContentType("text/html");
    	response.getWriter().write(formatList(result,true));
    	response.getWriter().flush();
    }
    // deal with lists
    String formatList(Iterable<String> members) {
    	return formatList(members,false);
    }

    String formatList(Iterable<String> members, boolean ordered) {
        StringWriter sw = new StringWriter();
        sw.append(ordered ? "<ol>" : "<ul>");
        for(String member : members) {
            sw.append("<li>").append(Xml.escape(member)).append("</li>");
        }
        sw.append(ordered ? "</ol>" : "</ul>");
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
