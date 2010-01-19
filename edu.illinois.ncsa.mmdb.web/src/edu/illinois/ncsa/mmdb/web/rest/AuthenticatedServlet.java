package edu.illinois.ncsa.mmdb.web.rest;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tupeloproject.util.Base64;

import edu.illinois.ncsa.mmdb.web.server.Authentication;

public class AuthenticatedServlet extends HttpServlet {
	String AUTHENTICATED_AS_SESSION_ATTRIBUTE = "edu.illinois.ncsa.mmdb.web.server.authenticatedAs";
	
	boolean unauthorized(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setHeader("WWW-Authenticate", "BASIC realm=\"mmdb\"");
		return false;
	}
	
	boolean authenticate(HttpServletRequest request, HttpServletResponse response) {
		String auth = request.getHeader("Authorization");
		if(auth == null) {
			return unauthorized(response);
		} else {
			// first check to see if this session is already authenticated
			if(auth.equals(request.getSession(true).getAttribute(AUTHENTICATED_AS_SESSION_ATTRIBUTE))) {
				// we're already authenticated.
				return true;
			}
			// we need to attempt authentication
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
				request.getSession().setAttribute(AUTHENTICATED_AS_SESSION_ATTRIBUTE, auth);
				return true;
			} else {
				return unauthorized(response);
			}
		}
	}
}
