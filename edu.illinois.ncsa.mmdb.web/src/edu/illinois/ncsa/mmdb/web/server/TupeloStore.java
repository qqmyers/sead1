package edu.illinois.ncsa.mmdb.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Foaf;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.xml.RdfXml;
import org.tupeloproject.util.ListTable;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.rest.RestServlet;
import edu.illinois.ncsa.mmdb.web.server.search.Search;
import edu.uiuc.ncsa.cet.bean.CETBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.context.ContextBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.UriCanonicalizer;
import edu.uiuc.ncsa.cet.bean.tupelo.context.ContextBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.context.ContextConvert;
import edu.uiuc.ncsa.cet.tupelo.contexts.ContextCreators;

/**
 * Singleton class to manage a tupelo context and its associated beansession.
 * Context is loaded from disk. By default the location of the context
 * definition ends up in WEB-INF/classes/context.xml
 * 
 * @author Luigi Marini
 * 
 */
public class TupeloStore {

	/** Default location of serialized tupelo context **/
	private static final String CONTEXT_PATH = "/context.xml";

	/** Commons logging **/
	private static Log log = LogFactory.getLog(TupeloStore.class);

	/** Singleton instance **/
	private static TupeloStore instance;

	/** Tupelo context loaded from disk **/
	private Context context;

	/** Tupelo beansession facing tupelo context **/
	private BeanSession beanSession;

	/**
	 * Context beans in case context definition includes multiple context
	 * definitions
	 **/
	private Collection<ContextBean> contextBeans;

	/** Context DAO used to load context definition from disk **/
	private ContextBeanUtil contextBeanUtil;

	/**
	 * Return singleton instance.
	 * 
	 * @return singleton TupeloStore
	 * 
	 * FIXME if there are any problems creating the context or the beansession,
	 * these objects remain null. Should this method throw an exception when 
	 * there is an error creating an instance of the Tupelo store?
	 */
	public static TupeloStore getInstance() {
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
			// setup context creators
			ContextCreators.register();
			context = createSerializeContext();
            //System.out.println(CETBeans.contextToNTriples( context ));
			if(context == null) {
				log.error("no context deserialized!");
			} else {
				log.info("context deserialized: "+context);
				initializeContext(context);
			}
			ContextConvert.updateContext(context);
			beanSession = CETBeans.createBeanSession(context);
		} catch (OperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void initializeContext(Context context) {
		try {
			context.addTriple( Resource.uriRef( PersonBeanUtil.getPersonID( "guest" ) ), Resource.uriRef( "http://cet.ncsa.uiuc.edu/2007/foaf/context/password" ), "guest" );
			context.addTriple( Resource.uriRef( PersonBeanUtil.getPersonID( "guest" ) ), Rdf.TYPE, Foaf.PERSON );
			context.addTriple( Resource.uriRef( PersonBeanUtil.getPersonID( "guest" ) ), Foaf.NAME, "guest" );
		} catch(Exception x) {
			log.error("failed to initialize context",x);
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

	UriCanonicalizer canon;

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
	
    public UriCanonicalizer getUriCanonicalizer() throws ServletException {
    	if(canon == null) {
    		throw new IllegalStateException("cannot call until a request has been made against a servlet");
    	}
    	return canon;
    }
    
    public UriCanonicalizer getUriCanonicalizer(HttpServletRequest request) throws ServletException {
        if(canon == null) {
            canon = new UriCanonicalizer();
            String prefix = getWebappPrefix(request);
            for(String infix : REST_INFIXES) {
            	String cp = prefix + request.getContextPath() + REST_SERVLET_PATH + infix;
            	canon.setCanonicalUrlPrefix(infix, cp);
            }
            // now handle GWT dataset and collection stuff stuff, hardcoding the HTML path
            canon.setCanonicalUrlPrefix("dataset",prefix + request.getContextPath() + MMDB_WEBAPP_PATH + "#dataset?id=");
        }
        return canon;
    }
    
    Map<String,Long> lastExtractionRequest = new HashMap<String,Long>();
    // extraction takes time. don't make repeated requests at short time intervals
    public void extractPreviews(String uri) {
    	Long lastRequest = lastExtractionRequest.get(uri);
    	if(lastRequest == null || lastRequest < System.currentTimeMillis()-120000) { // give it a minute
    		try {
    			Date createTime = getBeanSession().getThingSession().getDate(Resource.uriRef(uri), Cet.cet("metadata/extractor/createTime"));
    			if(createTime == null) {
    				String extractionServiceURL = "http://localhost:9856/"; // FIXME hardcoded
    				log.info("EXTRACT PREVIEWS "+uri);
    				lastExtractionRequest.put(uri, System.currentTimeMillis());
    				BeanSession beanSession = getBeanSession();
    				DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
    				PreviewBeanUtil pbu = new PreviewBeanUtil(beanSession);
    				try {
    					pbu.callExtractor(extractionServiceURL, dbu.get(uri));
    				} catch (Exception e) {
    					log.error("Extraction service " + extractionServiceURL + " unavailable");
    					e.printStackTrace();
    				}
    			}
    		} catch(OperatorException x) {
    			log.error("error checking context for extraction timestamp",x);
    		}
    	}
    }
    int datasetCount = -1;
    long lastDatasetCount = 0;
    public int countDatasets() {
    	return countDatasets(false);
    }
    public int countDatasets(boolean force) {
    	if(force || System.currentTimeMillis() > lastDatasetCount + 120000) {
    		lastDatasetCount = System.currentTimeMillis();
    		try {
    			DatasetBeanUtil dbu = new DatasetBeanUtil(getBeanSession());
    			log.debug("counting datasets...");
    			Unifier u = new Unifier();
    			u.setColumnNames("d");
    			u.addPattern("d",Rdf.TYPE,dbu.getType());
    			datasetCount = unifyExcludeDeleted(u,"d").getRows().size();
    			/*
    			u.addPattern("d",Resource.uriRef("http://purl.org/dc/terms/isReplacedBy"),Rdf.NIL);
    			getContext().perform(u);
    			for(Object o : u.getResult()) { allDatasetCount--; }
    			datasetCount = allDatasetCount;*/
    			long ms = System.currentTimeMillis()-lastDatasetCount;
    	    	log.debug("counted "+datasetCount+" non-deleted datasets in "+ms+"ms");
    		} catch(Exception x) {
    			x.printStackTrace();
    			datasetCount = -1;
    		}
    	}
    	return datasetCount;
    }
    
    public ListTable<Resource> unifyExcludeDeleted(Unifier u, String subjectVar) throws OperatorException {
    	List<OrderBy> newOrderBy = new LinkedList<OrderBy>();
    	List<String> newColumnNames = new LinkedList<String>(u.getColumnNames());
    	newColumnNames.add("_ued");
    	u.setColumnNames(newColumnNames);
    	u.addPattern(subjectVar,Resource.uriRef("http://purl.org/dc/terms/isReplacedBy"),"_ued",true);
    	OrderBy ued = new OrderBy();
    	ued.setAscending(true); // FIXME should be false when SQL contexts order correctly
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
    
    Search search = null;
    public Search getSearch() {
    	if(search == null) {
    		search = new Search();
    	}
    	return search;
    }
}

