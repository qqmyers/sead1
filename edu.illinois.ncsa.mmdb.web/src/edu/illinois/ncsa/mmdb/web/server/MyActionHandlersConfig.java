/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.illinois.ncsa.mmdb.web.server.dispatch.GetDatasetsHandler;

/**
 * Setup registry of action handlers when the servlet context is initialized.
 * 
 * @author Luigi Marini
 * 
 */
public class MyActionHandlersConfig implements ServletContextListener {
	public void contextInitialized(ServletContextEvent evt) {
		DispatchUtil.registerHandler(new GetDatasetsHandler());
	}

	public void contextDestroyed(ServletContextEvent evt) {
	}
}
