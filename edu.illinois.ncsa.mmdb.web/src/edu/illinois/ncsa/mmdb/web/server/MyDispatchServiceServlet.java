/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import net.customware.gwt.dispatch.server.service.DispatchServiceServlet;

/**
 * Default dispatch servlet.
 * 
 * @author Luigi Marini
 * 
 */
public class MyDispatchServiceServlet extends DispatchServiceServlet {

	private static final long serialVersionUID = 2464722364321662618L;

	public MyDispatchServiceServlet() {
		super(DispatchUtil.getDispatch());
	}
}
