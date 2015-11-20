/**
 * 
 */
package org.sead.acr.community;

/**
 * @author Jim
 *
 */

import java.io.IOException;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sead.acr.common.utilities.PropertiesLoader;

public class Login extends HttpServlet {

	/** Simple class to read/pass the server to which we should authenticate and the google client id to the login.jsp file
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Log log = LogFactory.getLog(Login.class);


	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.debug("Doing Get");
		Properties p = PropertiesLoader.getProperties("c3pr.properties");
		String domain = p.getProperty("domain");
		request.setAttribute("domain", domain);
	
		String googleClientId = p.getProperty(
				"google.client_id");
		if (googleClientId != null) {
			request.setAttribute("googleClientId", googleClientId);
		}

		
		RequestDispatcher rd = request
				.getRequestDispatcher("/login.jsp");

		rd.forward(request, response);
	}
}