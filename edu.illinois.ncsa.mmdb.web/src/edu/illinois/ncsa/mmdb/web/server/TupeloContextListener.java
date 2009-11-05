/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;

/**
 * Context listener to initialize the {@link TupeloStore} at startup and
 * add the tupelo context to the application session so that the tupelo
 * server servlet can have access to it.
 * 
 * @author Luigi Marini
 *
 */
public class TupeloContextListener implements ServletContextListener {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(TupeloContextListener.class);
	
	/** As specified in the tupelo servlet **/
	private static final String CONTEXT_ATTR_NAME = "org.tupeloproject.server.context";

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		log.trace("Servlet context destroyed");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		
		log.trace("Servlet context initialized");
		
		// load context and bean session
		TupeloStore.getInstance();
        
		// retrieve tupelo context from servlet context
		ServletContext servletContext = event.getServletContext();
        
		Context context = (Context) servletContext.getAttribute(CONTEXT_ATTR_NAME);
        
		// if tupelo context does not exist add it to servlet context
		if(context == null) {
			context = TupeloStore.getInstance().getContext();
            servletContext.setAttribute(CONTEXT_ATTR_NAME, context);
        }
	}

}
