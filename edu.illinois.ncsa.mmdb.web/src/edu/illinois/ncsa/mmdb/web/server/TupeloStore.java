package edu.illinois.ncsa.mmdb.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

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
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.xml.RdfXml;
import org.tupeloproject.util.ListTable;
import org.tupeloproject.util.Tuple;

import sun.misc.Service;
import edu.illinois.ncsa.cet.search.SearchableTextIndex;
import edu.illinois.ncsa.mmdb.web.rest.RestServlet;
import edu.uiuc.ncsa.cet.bean.CETBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.context.ContextBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.UriCanonicalizer;
import edu.uiuc.ncsa.cet.bean.tupelo.context.ContextBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.context.ContextConvert;
import edu.uiuc.ncsa.cet.bean.tupelo.context.ContextCreator;

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
    private static Log                     log                   = LogFactory.getLog( TupeloStore.class );

	/** Default location of serialized tupelo context **/
    private static final String            CONTEXT_PATH          = "/context.xml";
	
	/** URL of extraction service */
    private String                         extractionServiceURL  = "http://localhost:9856/";

	/** Singleton instance **/
    private static TupeloStore             instance;

	/** Tupelo context loaded from disk **/
    private Context                        context;

    /** Tupelo context to be used by the extractor */
    private Context extractorContext;
    
	/** Tupelo beansession facing tupelo context **/
    private BeanSession                    beanSession;

	/**  Context beans in case context definition includes multiple context definitions **/
    private Collection<ContextBean>        contextBeans;
	
	/** last time a certain uri was asked for extraction */
    private Map<String, Long>              lastExtractionRequest = new HashMap<String, Long>();

	/** Context DAO used to load context definition from disk **/
    private ContextBeanUtil                contextBeanUtil;
	
	/** Dataset count by collection (memoized) */
    private Map<String, Memoized<Integer>> datasetCount          = new HashMap<String, Memoized<Integer>>();
    
    /** Badge (collection preview) by collection (memoized) */
    private Map<String, Memoized<String>> collectionBadges = new HashMap<String,Memoized<String>>();
    
    /** Timer to schedule re-occurring jobs */
    private Timer                          timer                 = new Timer();

	/**
	 * Return singleton instance.
	 * 
	 * @return singleton TupeloStore
	 * 
	 * FIXME if there are any problems creating the context or the beansession,
	 * these objects remain null. Should this method throw an exception when 
	 * there is an error creating an instance of the Tupelo store?
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

		try {
			context = createSerializeContext();
			if(context == null) {
				log.error("no context deserialized!");
			} else {
				log.info("context deserialized: "+context);
			}
			ContextConvert.updateContext(context);
			beanSession = CETBeans.createBeanSession(context);
        } catch (ClassNotFoundException e) {
            log.warn("Could not de-serialize context, missing context-creator?.", e);
		} catch (Exception e) {
		    log.warn("Could not de-serialize context.", e);
		}
		
		// count datasets every hour
		// FIXME hack to force read of context every hour to solve MMDB-491
		timer.schedule( new TimerTask() {
            @Override
            public void run()
            {
                countDatasets( null, true );
            }
		    
		}, 0, 60 * 60 * 1000 );
	}
	
 
	/**
	 * Load context from disk.
	 * 
	 * @return
	 * @throws Exception
	 */
	private Context createSerializeContext() throws Exception {

	     // Register all context creators. This uses the Service providers method, a poor man version
	     // of the eclipse plugin mechanism.
        Iterator<ContextCreator> iter = Service.providers( ContextCreator.class );
        while ( iter.hasNext() ) {
            ContextBeanUtil.addContextCreator( iter.next() );
        }
	    
		// context location
		String path = CONTEXT_PATH;

		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		URL fileLocation = this.getClass().getResource(path);
		File file = new File(fileLocation.toURI());

		Context context = null;
		
		try {
			log.info("Loading serialized context " + fileLocation);
			
			ContextBeanUtil.setContextBeanStore(file);
			
			contextBeanUtil = ContextBeanUtil.getContextBeanStore();
			
			// load default context
			context = contextBeanUtil.getDefaultContext();
			
			contextBeans = contextBeanUtil.getAll();
		} catch(OperatorException x) {
			// OK, that didn't work, now just use PeerFacade.
			PeerFacade pf = new PeerFacade();
			pf.setContext(new MemoryContext(RdfXml.parse(new FileInputStream(file))));
			context = pf.loadDefaultPeer();
			// only 3 lines of code, now was that so hard?
		}
		if(context != null) {
			log.info("Loaded context from "+fileLocation);
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
	Map<Resource,Long> beanExp = new HashMap<Resource,Long>();
	long soonestExp = Long.MAX_VALUE;
	public synchronized BeanSession getBeanSession() {
		if(beanSession != null) {
			return beanSession;
		}
		try {
			beanSession = CETBeans.createBeanSession(context);
			beanSession.setFetchBeanPostprocessor(new FetchBeanPostprocessor() {
				public void postprocess(Object bean) {
					synchronized(beanExp) {
						try {
							long now = System.currentTimeMillis();
							long exp = now + 30000; // 30s
							if(exp < soonestExp) {
								soonestExp = exp;
							}
							beanExp.put(beanSession.getSubject(bean), exp);
							if(now > soonestExp) {
								soonestExp = Long.MAX_VALUE;
								Set<Resource> toNuke = new HashSet<Resource>();
								for(Map.Entry<Resource,Long> entry : beanExp.entrySet()) {
									exp = entry.getValue();
									if(exp < now) {
										try {
											beanSession.deregister(entry.getKey());
											toNuke.add(entry.getKey());
										} catch (OperatorException e) {
											log.error("could not expire bean",e);
										}
									} else if(exp < soonestExp) {
										soonestExp = exp;
									}
								}
								if(toNuke.size()>0) {
									for(Object n : toNuke) {
										beanExp.remove(n);
									}
									log.info("expiring "+toNuke.size()+" bean(s)");
								}
							}
						} catch(OperatorException x) {
							log.error("no bean subject",x);
						}
					}
				}
			});
		} catch (OperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return beanSession;
	}

	/**
	 * Get the tupelo context.
	 * 
	 * @return tupelo context
	 */
	public Context getContext() {
		return context;
	}

	// FIXME not used
	public Collection<ContextBean> getContextBeans() {
		return contextBeans;
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
		write(Resource.uriRef(uri),is);
	}
	public static void write(Resource uri, InputStream is) throws OperatorException {
		getInstance().getContext().write(uri,is);
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
	
	static final String REST_SERVLET_PATH = "/api";
	static final String PYRAMID_SERVLET_PATH = "/pyramid";
	static final String MMDB_WEBAPP_PATH = "/mmdb.html";
	
	static final String REST_INFIXES[] = new String[] {
            RestServlet.ANY_IMAGE_INFIX,
            RestServlet.IMAGE_INFIX,
            RestServlet.PREVIEW_ANY,
            RestServlet.PREVIEW_SMALL,
            RestServlet.PREVIEW_LARGE,
            RestServlet.IMAGE_CREATE_ANON_INFIX,
            RestServlet.IMAGE_DOWNLOAD_INFIX,
            RestServlet.ANY_COLLECTION_INFIX,
            RestServlet.COLLECTION_INFIX,
            RestServlet.COLLECTION_CREATE_ANON_INFIX,
            RestServlet.COLLECTION_ADD_INFIX,
            RestServlet.COLLECTION_REMOVE_INFIX,
            RestServlet.SEARCH_INFIX,
    };

	String getWebappPrefix(HttpServletRequest request) throws ServletException {
		try {
            URL requestUrl = new URL(request.getRequestURL().toString());
            String prefix = requestUrl.getProtocol()+"://"+requestUrl.getHost();
            if(requestUrl.getPort() != -1) {
                prefix = prefix + ":" + requestUrl.getPort();
            }
            return prefix;
		} catch(MalformedURLException x) {
			throw new ServletException("failed to determine webapp prefix",x);
		}
	}

	public String getHistoryTokenUrl(HttpServletRequest request, String historyToken) throws ServletException {
		return getWebappPrefix(request) + MMDB_WEBAPP_PATH + "#" + historyToken;
	}

	static final String CANONICALIZER_SESSION_ATTRIBUTE = "edu.illinois.ncsa.mmdb.web.UriCanonicalizer";
	
    public UriCanonicalizer getUriCanonicalizer(HttpServletRequest request) throws ServletException {
    	UriCanonicalizer canon = (UriCanonicalizer) request.getSession().getAttribute(CANONICALIZER_SESSION_ATTRIBUTE);
        if(canon == null) {
            canon = new UriCanonicalizer();
            String prefix = getWebappPrefix(request);
            for(String infix : REST_INFIXES) {
            	String cp = prefix + request.getContextPath() + REST_SERVLET_PATH + infix;
            	canon.setCanonicalUrlPrefix(infix, cp);
            }
            // now handle GWT dataset and collection stuff stuff, hardcoding the HTML path
            canon.setCanonicalUrlPrefix("dataset",prefix + request.getContextPath() + MMDB_WEBAPP_PATH + "#dataset?id=");
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
    public void setExtractionServiceURL( String extractionServiceURL )
    {
        this.extractionServiceURL = extractionServiceURL;
    }

    /**
     * Returns the URL of the extraction service.
     * 
     * @return the URL of the extraction service.
     */
    public String getExtractionServiceURL()
    {
        return extractionServiceURL;
    }
    
    /**
     * Extract metadata and previews from the given URI.
     * 
     * @param uri
     *            uri of dataset to pass to extraction service.
     * @return the job id at the extractor or null if the job was rejected.
     */
    public String extractPreviews( String uri )
    {
        return extractPreviews( uri, false );
    }

    
    public Context getExtractorContext() {
    	if(extractorContext == null) {
    		return getContext();
    	} else {
    		return extractorContext;
    	}
	}

	public void setExtractorContext(Context extractorContext) {
		this.extractorContext = extractorContext;
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
    public String extractPreviews( String uri, boolean rerun )
    {
        Long lastRequest = lastExtractionRequest.get( uri );

        // give it a minute
        if ( rerun || lastRequest == null || lastRequest < System.currentTimeMillis() - 120000 ) {
            log.info( "EXTRACT PREVIEWS " + uri );
            lastExtractionRequest.put( uri, System.currentTimeMillis() );
            BeanSession beanSession = getExtractorContext().getBeanSession();
            PreviewBeanUtil pbu = new PreviewBeanUtil( beanSession );
            try {
                return pbu.callExtractor( extractionServiceURL, uri, null, rerun );
            } catch ( Exception e ) {
                log.error( String.format( "Extraction service %s unavailable", extractionServiceURL ), e );
            }
        }
        return null;
    }

	public int countDatasets()
    {
        return countDatasets( null, false );
    }

    public int countDatasets( final String inCollection, boolean force )
    {
        String key = inCollection == null ? "all" : inCollection;
        Memoized<Integer> count = datasetCount.get( key );
        if ( count == null ) {
            count = new Memoized<Integer>() {
                public Integer computeValue()
                {
                    return countDatasetsInCollection( inCollection );
                }
            };
            count.setTtl( 120000 );
            datasetCount.put( key, count );
        }
        return count.getValue( force );
    }

    private int countDatasetsInCollection( String inCollection )
    {
        int datasetCount;
        try {
            DatasetBeanUtil dbu = new DatasetBeanUtil( getBeanSession() );
            log.debug( "counting datasets " + (inCollection != null ? "in collection " + inCollection : "") + "..." );
            Unifier u = new Unifier();
            u.setColumnNames( "d" );
            u.addPattern( "d", Rdf.TYPE, dbu.getType() );
            if ( inCollection != null ) {
                u.addPattern( Resource.uriRef( inCollection ), DcTerms.HAS_PART, "d" );
            }
            long now = System.currentTimeMillis();
            datasetCount = unifyExcludeDeleted( u, "d" ).getRows().size();
            long ms = System.currentTimeMillis() - now;
            log.debug( "counted " + datasetCount + " non-deleted datasets in " + ms + "ms" );
        } catch ( Exception x ) {
            log.warn("Could not ccount datasets.", x);
            datasetCount = -1;
        }
        return datasetCount;
    }
    
	/**
	 * 
	 * @param collectionUri
	 * @return
	 */
	public String getCollectionBadge(final String collectionUri) {
        Memoized<String> mBadge = collectionBadges.get(collectionUri);
        if ( mBadge == null ) {
            mBadge = new Memoized<String>() {
                public String computeValue() {
        			try {
        				Unifier u = new Unifier();
        				u.setColumnNames("member", "date");
        				u.addPattern(Resource.uriRef(collectionUri), DcTerms.HAS_PART, "member");
        				u.addPattern("member", Dc.DATE, "date", true);
        				u.addOrderByDesc("date");
        				u.addOrderBy("member");
        				u.setLimit(25);
        				for(Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "member")) {
        					String datasetUri = row.get(0).getString();
        					if(RestServlet.getSmallPreviewUri(datasetUri) != null) {
        						return datasetUri;
        					}
        				}
        			} catch(OperatorException e) {
        				log.error("Error getting badges for collection " + collectionUri, e);
        			}
    				// none of em have previews, or there was an error :(
    				return null;
                }
            };
            mBadge.setTtl( 120000 );
            collectionBadges.put(collectionUri, mBadge);
        }
        return mBadge.getValue();
	}
	
    public ListTable<Resource> unifyExcludeDeleted(Unifier u, String subjectVar) throws OperatorException {
    	List<OrderBy> newOrderBy = new LinkedList<OrderBy>();
    	List<String> newColumnNames = new LinkedList<String>(u.getColumnNames());
    	newColumnNames.add("_ued");
    	u.setColumnNames(newColumnNames);
    	u.addPattern(subjectVar,Resource.uriRef("http://purl.org/dc/terms/isReplacedBy"),"_ued",true);
    	OrderBy ued = new OrderBy();
    	ued.setAscending(true); // FIXME should be false when SQL contexts order correctly, i.e., when TUP-481 is fixed
    	ued.setName("_ued");
    	newOrderBy.add(ued);
    	for(OrderBy ob : u.getOrderBy()) {
    		newOrderBy.add(ob);
    	}
    	u.setOrderBy(newOrderBy);
    	getContext().perform(u);
    	int cix = u.getColumnNames().size() - 1;
    	ListTable<Resource> result = new ListTable<Resource>();
    	for(Tuple<Resource> row : u.getResult()) {
    		if(row.get(cix) == null) {
    			result.addRow(row);
    		}
    	}
    	return result;
    }
    Map<String,String> uploadHistory = new HashMap<String,String>();
    public void setHistoryForUpload(String sessionKey, String history) {
    	uploadHistory.put(sessionKey, history);
    }
    // only call this once.
    public String getHistoryForUpload(String sessionKey) {
    	String history = uploadHistory.get(sessionKey);
    	uploadHistory.put(sessionKey,null);
    	return history;
    }
    
    SearchableTextIndex<String> search = null;
    public SearchableTextIndex<String> getSearch() {
    	return search;
    }
    void setSearch(SearchableTextIndex<String> s) {
    	search = s;
    }
}

