package edu.illinois.ncsa.mmdb.web.rest;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.util.Base64;
import org.tupeloproject.util.SecureHashMinter;

import edu.illinois.ncsa.mmdb.web.server.Authentication;

public class AuthenticatedServlet extends HttpServlet {
	static Log log = LogFactory.getLog(AuthenticatedServlet.class);
	
	public static final String AUTHENTICATED_AS = "edu.illinois.ncsa.mmdb.web.server.auth.authenticatedAs";
	public static final String SESSION_KEYS = "edu.illinois.ncsa.mmdb.web.server.auth.sessionKeys";
	
	/** look up the user's session key. */
    protected String lookupSessionKey(String userId) {
    	for(Map.Entry<String,String> entry : getSessionKeys(getServletContext()).entrySet()) {
    		if(userId.equals(entry.getValue())) {
    			return entry.getKey();
    		}
    	}
    	log.info("LOGIN: no session key found for user "+userId);
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
		log.info("LOGIN: generated new session key "+sessionKey+" for user "+userId);
		getSessionKeys(context).put(sessionKey, userId);
		return sessionKey;
	}
	
	public static String getUserId(ServletContext context, String sessionKey) {
		return getSessionKeys(context).get(sessionKey);
	}
	
	public static void clearSessionKey(ServletContext context, String sessionKey) {
		Map<String,String> sk = getSessionKeys(context);
		if(sk.containsKey(sessionKey)) {
			log.debug("LOGOUT: destroying session key "+sessionKey);
			getSessionKeys(context).remove(sessionKey);
		}
	}

	public static String getSessionKey(HttpServletRequest request) {
		if(request.getCookies() == null) {
			return null;
		}
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
		String userId = (String) request.getSession(true).getAttribute(AUTHENTICATED_AS);
		request.getSession(true).setAttribute(AUTHENTICATED_AS,null);
		log.info("LOGOUT "+userId);
	}
	
	protected void logout(HttpServletRequest request, HttpServletResponse response) {
		doLogout(request, response);
	}
	
	protected String getHttpSessionUser(HttpServletRequest request) {
		return (String) request.getSession(true).getAttribute(AUTHENTICATED_AS);
	}
	
	public static boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
		// to authenticate we need either 1) a "sessionKey" cookie, or 2) credentials
		String sessionKey = getSessionKey(request);
		String validUser = null;
		if(sessionKey != null) {
			validUser = getUserId(context, sessionKey);
			if(validUser == null) {
				log.info("LOGIN: session key cookie "+sessionKey+" not found, authentication required");
			} else {
				//log.debug("LOGIN: user "+validUser+" logged in with session key "+sessionKey);
			}
		}
		if(validUser == null) {
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
				if(new Authentication().authenticate(username, password)) {
					// set the session attribute indicating that we're authenticated
					validUser = username;
					log.info("LOGIN: "+username+" logged in with correct username/password credentials");
					// we're authenticating, so we need to generate a session key and put it in the context
					sessionKey = setSessionKey(context, validUser);
					log.info("LOGIN: setting Cookie sessionKey="+sessionKey+" (for user "+validUser+")");
					Cookie cookie = new Cookie("sessionKey", sessionKey);
					cookie.setPath(request.getContextPath());
					response.addCookie(cookie);
				}
			}
		}
		// now are we authenticated?
		if(validUser == null) {
			// no. reject
			log.info("authentication FAILED: user has no cookie or valid credentials");
			return unauthorized(response);
		} else {
			// yes. record the user id in the http session
			HttpSession session = request.getSession(true);
			if(session.getAttribute(AUTHENTICATED_AS) == null) {
				log.info("LOGIN: user "+validUser+" is now authenticated in HTTP session "+session.getId());
				session.setAttribute(AUTHENTICATED_AS, validUser);
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
