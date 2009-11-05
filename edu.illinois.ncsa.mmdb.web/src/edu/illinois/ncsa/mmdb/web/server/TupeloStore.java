package edu.illinois.ncsa.mmdb.web.server;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;

import edu.uiuc.ncsa.cet.bean.ContextBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CETBeans;
import edu.uiuc.ncsa.cet.bean.tupelo.ContextBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.ContextConvert;
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

		log.info("Loading serialized context " + fileLocation);

		ContextBeanUtil.setContextBeanStore(file);

		contextBeanUtil = ContextBeanUtil.getContextBeanStore();

		// load default context
		Context context = contextBeanUtil.getDefaultContext();

		contextBeans = contextBeanUtil.getAll();

		return context;
	}

	/**
	 * Bean session.
	 * 
	 * @return
	 */
	public BeanSession getBeanSession() {
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

	public Collection<ContextBean> getContextBeans() {
		return contextBeans;
	}
}
