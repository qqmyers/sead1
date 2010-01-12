/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// here we capture the URL prefix of the request, for use in canonicalization later
		TupeloStore.getInstance().getUriCanonicalizer(arg0);
		super.service(arg0, arg1);
	}
}
