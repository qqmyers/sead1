package edu.illinois.ncsa.mmdb.web.rest;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.util.Base64;

import edu.illinois.ncsa.mmdb.web.server.Authentication;

public class AuthenticatedServlet extends HttpServlet {
	Log log = LogFactory.getLog(AuthenticatedServlet.class);
	
	public static final String AUTHENTICATED_AS = "edu.illinois.ncsa.mmdb.web.server.auth.authenticatedAs";
	public static final String BASIC_CREDENTIALS = "edu.illinois.ncsa.mmdb.web.server.auth.basicCredentials";
	
	public static boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response) {
		// is this session already authenticated? if so, no credentials required
		if(request.getSession(true).getAttribute(AUTHENTICATED_AS) != null) {
			return true;
		}
		String auth = request.getHeader("Authorization");
		if(auth == null) {
			return unauthorized(response);
		} else {
			// attempt to authenticate provided u/p credentials
			String ap[] = auth.split(" ");
			if(ap.length != 2 && !ap[0].equalsIgnoreCase("basic")) {
				return unauthorized(response);
			}
			String up[] = Base64.decodeToString(ap[1]).split(":");
			if(up.length != 2) {
				return unauthorized(response);
			}
			String username = up[0];
			String password = up[1];
			if((new Authentication()).authenticate(username, password)) {
				// set the session attribute indicating that we're authenticated
				request.getSession().setAttribute(AUTHENTICATED_AS, username);
				request.getSession().setAttribute(BASIC_CREDENTIALS, auth);
				return true;
			} else {
				return unauthorized(response);
			}
		}
	}
	
	static boolean unauthorized(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setHeader("WWW-Authenticate", "BASIC realm=\"mmdb\"");
		return false;
	}
	
	protected boolean authenticate(HttpServletRequest request, HttpServletResponse response) {
		log.debug("AUTHENTICATE session id = "+request.getSession(true).getId());
		return doAuthenticate(request,response);
	}
}
