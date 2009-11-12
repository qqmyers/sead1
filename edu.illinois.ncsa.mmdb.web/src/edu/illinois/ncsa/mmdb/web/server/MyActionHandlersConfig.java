/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.illinois.ncsa.mmdb.web.server.dispatch.AnnotateResourceHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDatasetHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDatasetsHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.TagResourceHandler;

/**
 * Setup registry of action handlers when the servlet context is initialized.
 * 
 * @author Luigi Marini
 * 
 */
public class MyActionHandlersConfig implements ServletContextListener {
	public void contextInitialized(ServletContextEvent evt) {
		DispatchUtil.registerHandler(new GetDatasetsHandler());
		DispatchUtil.registerHandler(new GetDatasetHandler());
		DispatchUtil.registerHandler(new AnnotateResourceHandler());
		DispatchUtil.registerHandler(new TagResourceHandler());
	}

	public void contextDestroyed(ServletContextEvent evt) {
	}
}
