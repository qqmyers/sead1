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
package edu.illinois.ncsa.mmdb.web.server;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.customware.gwt.dispatch.shared.Result;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.PeerFacade;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.kernel.beans.FetchBeanPostprocessor;
import org.tupeloproject.kernel.impl.MemoryContext;
import org.tupeloproject.rdf.ObjectResourceMapping;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.query.OrderBy;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.rdf.xml.RdfXml;
import org.tupeloproject.util.ListTable;
import org.tupeloproject.util.Tables;
import org.tupeloproject.util.Tuple;

import sun.misc.Service;
import edu.illinois.ncsa.cet.search.SearchableTextIndex;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AuthorizedAction;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SubjectAction;
import edu.illinois.ncsa.mmdb.web.rest.RestServlet;
import edu.uiuc.ncsa.cet.bean.CETBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.UriCanonicalizer;
import edu.uiuc.ncsa.cet.bean.tupelo.context.ContextBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.context.ContextConvert;
import edu.uiuc.ncsa.cet.bean.tupelo.context.ContextCreator;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.medici.MediciRbac;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

/**
 * Singleton class to manage a tupelo context and its associated beansession.
 * Context is loaded from disk. By default the location of the context
 * definition ends up in WEB-INF/classes/context.xml
 * 
 * @author Luigi Marini
 * 
 */
public class TupeloStore {
    /** Commons logging **/
    private static Log                                           log                   = LogFactory.getLog(TupeloStore.class);

    /** Default location of serialized tupelo context **/
    private static final String                                  CONTEXT_PATH          = "/context.xml";

    /** URL of extraction service */
    private String                                               extractionServiceURL  = "http://localhost:9856/";

    /** Singleton instance **/
    private static TupeloStore                                   instance;

    /** Tupelo context loaded from disk **/
    private Context                                              context;

    /** Tupelo beansession facing tupelo context **/
    private BeanSession                                          beanSession;

    /** last time a certain uri was asked for extraction */
    private final Map<String, Long>                              lastExtractionRequest = new HashMap<String, Long>();

    /** Dataset count by collection (memoized) */
    private final Map<String, Memoized<Integer>>                 datasetCount          = new HashMap<String, Memoized<Integer>>();

    /** Dataset/collection previews (memoized) */
    private Map<String, Map<String, Memoized<PreviewImageBean>>> previewCache          = null;

    private final Map<Resource, Long>                            beanExp               = new HashMap<Resource, Long>();
    private long                                                 soonestExp            = Long.MAX_VALUE;

    /** FileNameMap to map from extension to MIME type. */
    private MimeMap                                              mimemap;

    /** Use a single previewbeanutil for optimizations */
    private PreviewBeanUtil                                      extractorpbu;

    /**
     * Return singleton instance.
     * 
     * @return singleton TupeloStore
     */
    public static synchronized TupeloStore getInstance() {
        if (instance == null) {
            instance = new TupeloStore();
        }
        return instance;
    }

    /**
     * Use getInstance() to retrieve singleton instance.
     */
    private TupeloStore() {
        // FIXME if there are any problems creating the context or the
        // beansession, these objects remain null. Should this method throw
        // an exception when there is an error creating an instance of the
        // Tupelo store?
        try {
            context = createSerializeContext();
            if (context == null) {
                log.error("no context deserialized!");
            } else {
                log.info("context deserialized: " + context);
            }
            ContextConvert.updateContext(context);
            createBeanSession();
            MimeMap.initializeContext(context);
        } catch (ClassNotFoundException e) {
            log.warn("Could not de-serialize context, missing context-creator?.", e);
        } catch (Exception e) {
            log.warn("Could not de-serialize context.", e);
        }
    }

    /**
     * Load context from disk.
     * 
     * @return
     * @throws Exception
     */
    private Context createSerializeContext() throws Exception {
        // context location
        String path = CONTEXT_PATH;

        // Register all context creators. This uses the Service providers method, a poor man version
        // of the eclipse plugin mechanism.
        Iterator<ContextCreator> iter = Service.providers(ContextCreator.class);
        while (iter.hasNext()) {
            ContextBeanUtil.addContextCreator(iter.next());
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        URL fileLocation = findFile(path);
        Context context = null;

        try {
            // load default context
            log.info("Loading serialized context " + fileLocation);
            ContextBeanUtil.setContextBeanStore(fileLocation.openStream());
            context = ContextBeanUtil.getContextBeanStore().getDefaultContext();
        } catch (OperatorException x) {
            // OK, that didn't work, now just use PeerFacade.
            PeerFacade pf = new PeerFacade();
            pf.setContext(new MemoryContext(RdfXml.parse(fileLocation.openStream())));
            context = pf.loadDefaultPeer();
        }
        if (context != null) {
            log.info("Loaded context from " + fileLocation);
            return context;
        } else {
            throw new Exception("no context found!");
        }
    }

    /**
     * Bean session.
     * 
     * @return
     */
    public BeanSession getBeanSession() {
        return beanSession;
    }

    private void setExpirationTime(Object bean) {
        synchronized (beanExp) {
            long now = System.currentTimeMillis();
            long exp = now + 120000;
            if (exp < soonestExp) {
                soonestExp = exp;
            }
            try {
                beanExp.put(beanSession.getSubject(bean), exp);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }

    void expireBeans() {
        long now = System.currentTimeMillis();
        Set<Resource> toNuke = new HashSet<Resource>();
        synchronized (beanExp) {
            if (now > soonestExp) {
                soonestExp = Long.MAX_VALUE;
                for (Map.Entry<Resource, Long> entry : beanExp.entrySet() ) {
                    long exp = entry.getValue();
                    if (exp < now) {
                        toNuke.add(entry.getKey());
                    } else if (exp < soonestExp) {
                        soonestExp = exp;
                    }
                }
                if (toNuke.size() > 0) {
                    for (Object n : toNuke ) {
                        beanExp.remove(n);
                    }
                    log.info("expiring " + toNuke.size() + " bean(s)");
                }
            }
        }
        // now actually expire the beans (now that we're no longer blocking setting expiration times
        for (Resource beanUri : toNuke ) {
            try {
                getBeanSession().deregister(beanUri);
            } catch (OperatorException x) {
                log.error("ERROR: could not expire bean " + beanUri + ": " + x.getMessage());
                x.printStackTrace();
            }
        }
    }

    private void createBeanSession() {
        try {
            beanSession = CETBeans.createBeanSession(context);
            beanSession.setFetchBeanPostprocessor(new FetchBeanPostprocessor() {
                public void postprocess(Object bean) {
                    setExpirationTime(bean);
                }
            });
            if (extractorpbu == null) {
                extractorpbu = new PreviewBeanUtil(beanSession);
            }
        } catch (Exception e) {
            log.error("Could not create bean sessions.", e);
        }
    }

    /**
     * Get the tupelo context.
     * 
     * @return tupelo context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Get rbac
     * 
     * @return
     */
    public MediciRbac getRbac() {
        return new MediciRbac(getContext());
    }

    public boolean isAllowed(AuthorizedAction<? extends Result> action, Permission p) {
        try {
            return getRbac().checkPermission(action.getUser(), null, p);
        } catch (RBACException e) {
            log.error("cannot determine authorization", e);
        }
        return false;
    }

    public boolean isAllowed(SubjectAction<? extends Result> action, Permission p) {
        try {
            return getRbac().checkPermission(action.getUser(), action.getUri(), p);
        } catch (RBACException e) {
            log.error("cannot determine authorization", e);
        }
        return false;
    }

    /**
     * Returns a MimeMap that is initialized with default mappings as well
     * as those stored inside the context.
     * 
     * @return the FileNameMap
     */
    public MimeMap getMimeMap() {
        if (mimemap == null) {
            mimemap = new MimeMap(context);
        }
        return mimemap;
    }

    public boolean authenticate(String username, String password) {
        return (new Authentication()).authenticate(username, password);
    }

    // static utility methods

    public static Object fetchBean(String uri) throws OperatorException {
        return fetchBean(Resource.uriRef(uri));
    }

    public static Object fetchBean(Resource uri) throws OperatorException {
        return getInstance().getBeanSession().fetchBean(uri);
    }

    public static DatasetBean fetchDataset(Resource uri) throws OperatorException {
        return (DatasetBean) fetchBean(uri);
    }

    public static Thing fetchThing(Resource uri) throws OperatorException {
        return getInstance().getBeanSession().getThingSession().fetchThing(uri);
    }

    public static Thing fetchThing(String uri) throws OperatorException {
        return fetchThing(Resource.uriRef(uri));
    }

    public static Collection<Thing> getThings(Resource predicate, Object object) throws OperatorException {
        return getInstance().getBeanSession().getThingSession().getThings(predicate, object);
    }

    public static Collection<Resource> match(Resource predicate, Object object) throws OperatorException {
        Context c = getInstance().getContext();
        return Triple.getSubjectVisitor().visitAll(c.match(null, predicate, ObjectResourceMapping.resource(object)));
    }

    public static InputStream read(CETBean bean) throws OperatorException {
        return read(bean.getUri());
    }

    public static InputStream read(String uri) throws OperatorException {
        return read(Resource.uriRef(uri));
    }

    public static InputStream read(Resource uri) throws OperatorException {
        return getInstance().getContext().read(uri);
    }

    public static void write(String uri, InputStream is) throws OperatorException {
        write(Resource.uriRef(uri), is);
    }

    public static void write(Resource uri, InputStream is) throws OperatorException {
        getInstance().getContext().write(uri, is);
    }

    public static void refetch(CETBean bean) throws OperatorException {
        refetch(bean.getUri());
    }

    public static void refetch(String uri) throws OperatorException {
        refetch(Resource.uriRef(uri));
    }

    public static void refetch(Resource uri) throws OperatorException {
        getInstance().getBeanSession().refetch(uri);
    }

    // URL canonicalization

    // these hardcoded strings make sure the right paths are used
    // TODO: figure out how to get these from the server config or incoming requests

    static final String REST_SERVLET_PATH    = "/api";
    static final String PYRAMID_SERVLET_PATH = "/pyramid";
    static final String MMDB_WEBAPP_PATH     = "/mmdb.html";

    static final String REST_INFIXES[]       = new String[] {
                                             RestServlet.ANY_IMAGE_INFIX,
                                             RestServlet.IMAGE_INFIX,
                                             RestServlet.PREVIEW_ANY,
                                             RestServlet.PREVIEW_SMALL,
                                             RestServlet.PREVIEW_LARGE,
                                             RestServlet.PREVIEW_SMALL_NEW,
                                             RestServlet.PREVIEW_LARGE_NEW,
                                             RestServlet.IMAGE_CREATE_ANON_INFIX,
                                             RestServlet.IMAGE_DOWNLOAD_INFIX,
                                             RestServlet.ANY_COLLECTION_INFIX,
                                             RestServlet.COLLECTION_INFIX,
                                             RestServlet.COLLECTION_CREATE_ANON_INFIX,
                                             RestServlet.COLLECTION_ADD_INFIX,
                                             RestServlet.COLLECTION_REMOVE_INFIX,
                                             RestServlet.COLLECTION_PREVIEW,
                                             RestServlet.COLLECTION_PREVIEW_NEW,
                                             RestServlet.SEARCH_INFIX,
                                             RestServlet.JIRA_ISSUE,
                                             RestServlet.DATASET,
                                                     };

    /**
     * return a path to the iamge in the "/images" directory of the webapp
     * 
     * @throws ServletException
     */
    public static String getImagePath(HttpServletRequest request, String imageName) throws ServletException {
        return getWebappPrefix(request) + request.getContextPath() + "/images/" + imageName;
    }

    public static String getWebappPrefix(HttpServletRequest request) throws ServletException {
        try {
            URL requestUrl = new URL(request.getRequestURL().toString());
            String prefix = requestUrl.getProtocol() + "://" + requestUrl.getHost();
            if (requestUrl.getPort() != -1) {
                prefix = prefix + ":" + requestUrl.getPort();
            }
            return prefix;
        } catch (MalformedURLException x) {
            throw new ServletException("failed to determine webapp prefix", x);
        }
    }

    public String getHistoryTokenUrl(HttpServletRequest request, String historyToken) throws ServletException {
        return getWebappPrefix(request) + MMDB_WEBAPP_PATH + "#" + historyToken;
    }

    static final String CANONICALIZER_SESSION_ATTRIBUTE = "edu.illinois.ncsa.mmdb.web.UriCanonicalizer";

    public UriCanonicalizer getUriCanonicalizer(HttpServletRequest request) throws ServletException {
        UriCanonicalizer canon = (UriCanonicalizer) request.getSession().getAttribute(CANONICALIZER_SESSION_ATTRIBUTE);
        if (canon == null) {
            canon = new UriCanonicalizer();
            String prefix = getWebappPrefix(request);
            for (String infix : REST_INFIXES ) {
                String cp = prefix + request.getContextPath() + REST_SERVLET_PATH + infix;
                canon.setCanonicalUrlPrefix(infix, cp);
            }
            // now handle GWT dataset and collection stuff stuff, hardcoding the HTML path
            canon.setCanonicalUrlPrefix("dataset", prefix + request.getContextPath() + MMDB_WEBAPP_PATH + "#dataset?id=");
            request.getSession().setAttribute(CANONICALIZER_SESSION_ATTRIBUTE, canon);
        }
        return canon;
    }

    /**
     * Sets the URL to use for the extraction service.
     * 
     * @param extractionServiceURL
     *            the URL to use for the extraction service.
     */
    public void setExtractionServiceURL(String extractionServiceURL) {
        this.extractionServiceURL = extractionServiceURL;
    }

    /**
     * Returns the URL of the extraction service.
     * 
     * @return the URL of the extraction service.
     */
    public String getExtractionServiceURL() {
        return extractionServiceURL;
    }

    /**
     * Extract metadata and previews from the given URI.
     * 
     * @param uri
     *            uri of dataset to pass to extraction service.
     * @return the job id at the extractor or null if the job was rejected.
     */
    public String extractPreviews(String uri) {
        return extractPreviews(uri, false);
    }

    public void setExtractorContext(Context extractorContext) throws OperatorException, ClassNotFoundException {
        extractorpbu = new PreviewBeanUtil(CETBeans.createBeanSession(extractorContext));
    }

    /**
     * Extract metadata and previews from the given URI. If rerun is set to true
     * it will tell the extraction service to rerun the extraction.
     * 
     * @param uri
     *            uri of dataset to pass to extraction service.
     * @param rerun
     *            set to true if extraction service should be run again.
     * @return the job id at the extractor or null if the job was rejected.
     */
    public String extractPreviews(String uri, boolean rerun) {
        Long lastRequest = lastExtractionRequest.get(uri);

        // give it a minute
        if (rerun || lastRequest == null || lastRequest < System.currentTimeMillis() - 120000) {
            log.info("EXTRACT PREVIEWS " + uri);
            lastExtractionRequest.put(uri, System.currentTimeMillis());
            try {
                return extractorpbu.callExtractor(extractionServiceURL, uri, null, rerun);
            } catch (Exception e) {
                log.error(String.format("Extraction service %s unavailable", extractionServiceURL), e);
            }
        }
        return null;
    }

    public int countDatasets() {
        return countDatasets(null, false);
    }

    public int countDatasets(final String inCollection, boolean force) {
        return countDatasets(inCollection, null, force);
    }

    public int countDatasets(final String inCollection, final String withTag, boolean force) {
        String key = (inCollection == null ? "noCollection" : inCollection) + " " + (withTag == null ? "noTag" : withTag);
        Memoized<Integer> count = datasetCount.get(key);
        if (count == null) {
            count = new Memoized<Integer>() {
                public Integer computeValue()
                    {
                        return countDatasetsInCollectionWithTag(inCollection, withTag);
                    }
            };
            if (inCollection != null || withTag != null) {
                count.setTtl(10000);
            } else {
                count.setTtl(120000);
            }
            datasetCount.put(key, count);
        }
        return count.getValue(force);
    }

    private int countDatasetsInCollectionWithTag(String inCollection, String withTag) {
        int datasetCount;
        try {
            DatasetBeanUtil dbu = new DatasetBeanUtil(getBeanSession());
            log.debug("counting datasets " + (inCollection != null ? "in collection " + inCollection : "") + (withTag != null ? "with tag " + withTag : "") + "...");
            Unifier u = new Unifier();
            u.setColumnNames("d");
            u.addPattern("d", Rdf.TYPE, dbu.getType());
            if (inCollection != null) {
                u.addPattern(Resource.uriRef(inCollection), DcTerms.HAS_PART, "d");
            }
            if (withTag != null) {
                u.addPattern("d", Tags.HAS_TAGGING_EVENT, "tagEvent");
                u.addPattern("tagEvent", Tags.HAS_TAG_OBJECT, "tag");
                u.addPattern("tag", Tags.HAS_TAG_TITLE, Resource.literal(withTag));
            }
            long now = System.currentTimeMillis();
            HashSet<Resource> set = new HashSet<Resource>(Tables.getColumn(unifyExcludeDeleted(u, "d"), 0));
            datasetCount = set.size();
            long ms = System.currentTimeMillis() - now;
            log.debug("counted " + datasetCount + " non-deleted datasets in " + ms + "ms");
        } catch (Exception x) {
            log.warn("Could not count datasets.", x);
            datasetCount = -1;
        }
        return datasetCount;
    }

    Map<String, Memoized<String>> badgeCache = null;

    public String getBadge(final String collectionUri) {
        if (badgeCache == null) {
            badgeCache = new HashMap<String, Memoized<String>>();
        }
        Memoized<String> mBadge = badgeCache.get(collectionUri);
        if (mBadge == null) {
            mBadge = new Memoized<String>() {
                public String computeValue() {
                    try {
                        Unifier u = new Unifier();
                        u.setColumnNames("member", "date");
                        u.addPattern(Resource.uriRef(collectionUri), DcTerms.HAS_PART, "member");
                        u.addPattern("member", Dc.DATE, "date", true);
                        u.addOrderBy("date");
                        u.addOrderBy("member");
                        u.setLimit(25);
                        for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "member") ) {
                            String datasetUri = row.get(0).getString();
                            String preview = getPreviewUri(datasetUri, GetPreviews.SMALL);
                            if (preview != null) {
                                return datasetUri;
                            }
                        }
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                    return null;
                }
            };
            mBadge.setTtl(60000 * 5); // 5 min
            mBadge.setForceOnNull(true);
            badgeCache.put(collectionUri, mBadge);
        }
        return mBadge.getValue();
    }

    // caches previews.
    public String getPreviewUri(String uri, String size) {
        return RestServlet.getPreviewUri(getPreview(uri, size));
    }

    public PreviewImageBean getPreview(final String uri, final String size) {
        // lazily initialize cache
        if (previewCache == null) {
            previewCache = new HashMap<String, Map<String, Memoized<PreviewImageBean>>>();
        }
        Map<String, Memoized<PreviewImageBean>> sizeCache = previewCache.get(size);
        if (sizeCache == null) {
            sizeCache = new HashMap<String, Memoized<PreviewImageBean>>();
            previewCache.put(size, sizeCache);
        }
        // now look for the memoized value for this uri
        Memoized<PreviewImageBean> mPreview = sizeCache.get(uri);
        if (mPreview == null) {
            if (size.equals(GetPreviews.SMALL) || size.equals(GetPreviews.LARGE)) {
                mPreview = new Memoized<PreviewImageBean>() {
                    public PreviewImageBean computeValue() {
                        return RestServlet.getPreview(uri, size);
                    }
                };
            } else if (size.equals(GetPreviews.BADGE)) {
                mPreview = new Memoized<PreviewImageBean>() {
                    public PreviewImageBean computeValue() {
                        String badge = getBadge(uri);
                        if (badge != null) {
                            return getPreview(badge, GetPreviews.SMALL);
                        }
                        // none of em have previews, or there was an error :(
                        return null;
                    }
                };
            } else {
                log.warn("don't know how to cache preview of size=" + size); // whoops.
                return RestServlet.getPreview(uri, size);
            }
            mPreview.setTtl(1000 * 60 * 60); // 2hr
            mPreview.setForceOnNull(true);
            sizeCache.put(uri, mPreview);
        }
        PreviewImageBean preview = mPreview.getValue();
        //        if (preview == null) {
        //            log.debug("NO PREVIEW for " + uri + "--cache miss, and nothing was found");
        //        }
        return preview;
    }

    /**
     * 
     * @param collectionUri
     * @return
     */
    public String getCollectionBadge(String collectionUri) {
        return getPreviewUri(collectionUri, GetPreviews.BADGE);
    }

    public ListTable<Resource> unifyExcludeDeleted(Unifier u, String subjectVar) throws OperatorException {
        List<String> newColumnNames = new LinkedList<String>(u.getColumnNames());
        newColumnNames.add("_ued");
        u.setColumnNames(newColumnNames);
        u.addPattern(subjectVar, Resource.uriRef("http://purl.org/dc/terms/isReplacedBy"), "_ued", true);
        if (u.getOffset() != 0 || u.getLimit() != Unifier.UNLIMITED) {
            List<OrderBy> newOrderBy = new LinkedList<OrderBy>();
            OrderBy ued = new OrderBy();
            ued.setAscending(true); // FIXME should be false when SQL contexts order correctly, i.e., when TUP-481 is fixed
            ued.setName("_ued");
            newOrderBy.add(ued);
            for (OrderBy ob : u.getOrderBy() ) {
                newOrderBy.add(ob);
            }
            u.setOrderBy(newOrderBy);
        }
        getContext().perform(u);
        int cix = u.getColumnNames().size() - 1;
        ListTable<Resource> result = new ListTable<Resource>();
        for (Tuple<Resource> row : u.getResult() ) {
            if (row.get(cix) == null) {
                result.addRow(row);
            }
        }
        return result;
    }

    Map<String, String> uploadHistory = new HashMap<String, String>();

    public void setHistoryForUpload(String sessionKey, String history) {
        uploadHistory.put(sessionKey, history);
    }

    // only call this once.
    public String getHistoryForUpload(String sessionKey) {
        String history = uploadHistory.get(sessionKey);
        uploadHistory.put(sessionKey, null);
        return history;
    }

    // full-text

    /**
     * Call this when you've added something; e.g., a dataset or a collection.
     * This notifies the store that cached information (e.g., full-text
     * indexing)
     * needs to be computed
     * 
     * @param uri
     *            the uri of the added thing
     */
    public void created(String uri) {
    }

    /**
     * Call this when you've changed something; e.g., a dataset or a collection.
     * This notifies the store that cached information (e.g., full-text
     * indexing)
     * needs to be recomputed.
     * 
     * @param uri
     *            the uri of the changed thing
     */
    public void changed(String uri) {
        try {
            refetch(uri);
        } catch (OperatorException e) {
            // ignore, this may not be refetchable
        }
        indexFullText(uri);
    }

    /**
     * Call this when you've deleted something; e.g., a dataset or a collection.
     * This notifies the store that cached information (e.g., full-text
     * indexing)
     * needs to be recomputed.
     * 
     * @param uri
     *            the uri of the deleted thing
     */
    public void deleted(String uri) {
        deindexFullText(uri);
    }

    SearchableTextIndex<String> search = null;

    public SearchableTextIndex<String> getSearch() {
        return search;
    }

    void setSearch(SearchableTextIndex<String> s) {
        search = s;
    }

    List<String> indexQueue   = new LinkedList<String>();
    List<String> deindexQueue = new LinkedList<String>();

    /**
     * Queue a dataset for full-text (re)indexing
     * 
     * @param datasetUri
     */
    public void indexFullText(String datasetUri) {
        synchronized (indexQueue) {
            synchronized (deindexQueue) {
                if (!indexQueue.contains(datasetUri)) {
                    indexQueue.add(0, datasetUri);
                }
                if (deindexQueue.contains(datasetUri)) {
                    deindexQueue.remove(datasetUri);
                }
            }
        }
    }

    /**
     * Queue a dataset for full-text deindexing
     * 
     * @param datasetUri
     */
    public void deindexFullText(String datasetUri) {
        synchronized (deindexQueue) {
            synchronized (indexQueue) {
                if (!deindexQueue.contains(datasetUri)) {
                    deindexQueue.add(0, datasetUri);
                }
                if (indexQueue.contains(datasetUri)) {
                    indexQueue.remove(datasetUri);
                }
            }
        }
    }

    public int indexFullTextAll() {
        int i = 0;
        int batchSize = 100;
        while (true) {
            Unifier u = new Unifier();
            u.setColumnNames("d", "replaced");
            u.addPattern("d", Rdf.TYPE, Cet.DATASET);
            u.addPattern("d", Dc.DATE, "date");
            u.addPattern("d", DcTerms.IS_REPLACED_BY, "replaced", true);
            u.setLimit(batchSize);
            u.setOffset(i);
            u.addOrderByDesc("date");
            try {
                getContext().perform(u);
                int n = 0;
                for (Tuple<Resource> row : u.getResult() ) {
                    String d = row.get(0).getString();
                    Resource r = row.get(1);
                    if (Rdf.NIL.equals(r)) { // deleted
                        deindexFullText(d);
                    } else {
                        indexFullText(d);
                    }
                    n++;
                }
                if (n < batchSize) {
                    log.info("queued " + (i + n) + " datasets for full-text reindexing @ " + new Date());
                    return i + n;
                }
            } catch (OperatorException x) {
                x.printStackTrace();
                // FIXME deal with busy state
            }
            //
            i += batchSize;
        }
    }

    // consume ft index queue
    public synchronized void consumeFullTextIndexQueue() {
        boolean logged = false;
        if (getSearch() != null) {
            // copy the queues, so we don't block
            List<String> toDeindex = new LinkedList<String>();
            List<String> toIndex = new LinkedList<String>();
            synchronized (deindexQueue) {
                synchronized (indexQueue) {
                    toIndex.addAll(indexQueue);
                    toDeindex.addAll(deindexQueue);
                    indexQueue.clear();
                    deindexQueue.clear();
                }
            }
            if (toDeindex.size() > 0) {
                if (!logged) {
                    log.info("starting full-text reindexing @ " + new Date());
                    logged = true;
                }
                log.info("deindexing " + toDeindex.size() + " deleted dataset(s) @ " + new Date());
                getSearch().deindex(toDeindex);
                log.info("deindexed " + toDeindex.size() + " deleted dataset(s) @ " + new Date());
            }
            if (toIndex.size() > 0) {
                if (!logged) {
                    log.info("starting full-text reindexing @ " + new Date());
                    logged = true;
                }
                long then = System.currentTimeMillis();
                log.info("indexing " + toIndex.size() + " dataset(s) @ " + new Date());
                for (String datasetUri : toIndex ) {
                    getSearch().reindex(datasetUri);
                }
                long elapsed = System.currentTimeMillis() - then;
                double minutes = elapsed / 60000.0;
                log.info("indexed " + toIndex.size() + " dataset(s) in " + minutes + " minutes");
            }
        }
    }

    // ----------------------------------------------------------------------
    // Util functions
    // ----------------------------------------------------------------------

    public static URL findFile(String filename) {
        // Default folder for MMDB stuff
        File dataDir = new File(System.getProperty("user.home"), "NCSA/MMDBServer"); //$NON-NLS-1$ //$NON-NLS-2$
        File file = new File(dataDir, filename);
        if (file.exists()) {
            try {
                URL url = file.toURI().toURL();
                log.info(String.format("Found %s as %s", filename, url.toExternalForm()));
                return url;
            } catch (MalformedURLException e) {
                log.warn("Could not convert file to a URL.", e);
            }
        }

        // install folder (special case the mac installer)
        dataDir = new File("123456").getAbsoluteFile().getParentFile();
        if (dataDir.getAbsolutePath().contains("/Contents/MacOS")) {
            dataDir = dataDir.getParentFile().getParentFile().getParentFile();
        }
        file = new File(dataDir, filename);
        if (file.exists()) {
            try {
                URL url = file.toURI().toURL();
                log.info(String.format("Found %s as %s", filename, url.toExternalForm()));
                return url;
            } catch (MalformedURLException e) {
                log.warn("Could not convert file to a URL.", e);
            }
        }

        // ask classloader
        URL url = TupeloStore.class.getResource(filename);
        log.info(String.format("Found %s as %s", filename, url.toExternalForm()));
        return url;
    }
}
