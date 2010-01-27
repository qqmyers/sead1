package edu.illinois.ncsa.mmdb.web.rest;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.util.Base64;
import org.tupeloproject.util.SecureHashMinter;

import edu.illinois.ncsa.mmdb.web.server.Authentication;

public class AuthenticatedServlet extends HttpServlet {
	Log log = LogFactory.getLog(AuthenticatedServlet.class);
	
	public static final String AUTHENTICATED_AS = "edu.illinois.ncsa.mmdb.web.server.auth.authenticatedAs";
	public static final String SESSION_KEYS = "edu.illinois.ncsa.mmdb.web.server.auth.sessionKeys";
	
	/** look up the user's session key. */
    protected String lookupSessionKey(String userId) {
    	for(Map.Entry<String,String> entry : getSessionKeys(getServletContext()).entrySet()) {
    		System.out.println(entry);
    		if(entry.getValue().equals(userId)) {
    			return entry.getKey();
    		}
    	}
    	return null;
	}
    
	@SuppressWarnings("unchecked")
	public static Map<String,String> getSessionKeys(ServletContext context) {
		// FIXME need a way for session keys to expire so this map doesn't take up more and more memory
		Map<String,String> sessionKeys = (Map<String,String>) context.getAttribute(SESSION_KEYS);
		if(sessionKeys == null) {
			sessionKeys = new HashMap<String,String>();
			context.setAttribute(SESSION_KEYS, sessionKeys);		
		}
		return sessionKeys;
	}
	
	public static String setSessionKey(ServletContext context, String userId) {
		String sessionKey = SecureHashMinter.getMinter().mint();
		getSessionKeys(context).put(sessionKey, userId);
		return sessionKey;
	}
	
	public static String getUserId(ServletContext context, String sessionKey) {
		return getSessionKeys(context).get(sessionKey);
	}
	
	public static void clearSessionKey(ServletContext context, String sessionKey) {
		getSessionKeys(context).put(sessionKey, null);
	}

	public static String getSessionKey(HttpServletRequest request) {
		for(Cookie cookie : request.getCookies()) {
			if(cookie.getName().equals("sessionKey")) {
				return cookie.getValue();
			}
		}
		return null;
	}
	
	public void doLogout(HttpServletRequest request, HttpServletResponse response) {
		String sessionKey = getSessionKey(request);
		if(sessionKey != null) {
			clearSessionKey(getServletContext(), sessionKey);
		}
		request.getSession(true).setAttribute(AUTHENTICATED_AS,null);
	}
	
	protected void logout(HttpServletRequest request, HttpServletResponse response) {
		String userId = (String) request.getSession(true).getAttribute(AUTHENTICATED_AS);
		doLogout(request, response);
		log.info("LOGOUT "+userId);
	}
	
	protected String getAuthenticatedUsername(HttpServletRequest request) {
		return (String) request.getSession(true).getAttribute(AUTHENTICATED_AS);
	}
	
	static String getAuthenticatedUser(HttpServletRequest request) {
		return (String) request.getSession(true).getAttribute(AUTHENTICATED_AS);
	}
	
	public static boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
		// is this session already authenticated? if so, no credentials required
		if(getAuthenticatedUser(request) != null) {
			return authorized(request);
		}
		//
		String sessionKey = getSessionKey(request);
		String authenticatedUserId = null;
		if(sessionKey != null) {
			authenticatedUserId = getUserId(context, sessionKey);
		}
		if(authenticatedUserId == null) {
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
				if((username.equals("tupelo") && password.equals("tupelo")) // FIXME workaround
						|| (new Authentication()).authenticate(username, password)) {
					// set the session attribute indicating that we're authenticated
					authenticatedUserId = username;
					// we're authenticating, so we need to generate a session key and put it in the context
					sessionKey = setSessionKey(context, authenticatedUserId);
					Cookie cookie = new Cookie("sessionKey", sessionKey);
					response.addCookie(cookie);
				}
			}
		}
		// now are we authenticated?
		if(authenticatedUserId == null) {
			// no. reject
			return unauthorized(response);
		} else {
			request.getSession(true).setAttribute(AUTHENTICATED_AS, authenticatedUserId);
			// do we have a cookie?
			if(sessionKey != null) {
				// return it
				Cookie cookie = new Cookie("sessionKey", sessionKey);
				response.addCookie(cookie);
			}
			return authorized(request);
		}
	}
	
	static boolean unauthorized(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setHeader("WWW-Authenticate", "BASIC realm=\"mmdb\"");
		return false;
	}
	
	static boolean authorized(HttpServletRequest request) {
		return true;
	}
	
	protected boolean authenticate(HttpServletRequest request, HttpServletResponse response) {
		return doAuthenticate(request,response,getServletContext());
	}
}
