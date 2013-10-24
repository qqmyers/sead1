package org.sead.acr.common.auth;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.PropertiesLoader;

import javax.servlet.http.Cookie;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthFilter implements Filter {

	private static String _propFile;
	private static String _loginpage;

	private static String _openpath;
	private static String _webauthpath;

	private static String _server;
	private static String _remoteAPIKey;
	
	private static String _googleClientId;

	private static boolean _tryAnonymous = false;

	// == edu.illinois.ncsa.mmdb.web.rest.AuthenticatedServlet.AUTHENTICATED_AS
	private static String AUTHENTICATED_AS = "edu.illinois.ncsa.mmdb.web.server.auth.authenticatedAs";
	private static Log log = LogFactory.getLog(AuthFilter.class);

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String appPath = request.getContextPath();
		String uri = request.getRequestURI();
		String query = request.getQueryString();
		String username = null;
		String password = "";
		String googleAccessToken = null;
		if (query != null) {
			log.debug("URI: " + uri + "?" + query);
		} else {
			log.debug("URI: " + uri);
		}

		// Let the login form page and requests for it's images, css files, etc.
		// through...
		if (!uri.startsWith(appPath + _openpath)) {

			// if logging in
			if (uri.startsWith(appPath + "/DoLogin")) {
				log.debug("Attempting to authenticate");
				// Retrieve form info
				username = request.getParameter("userName");
				password = request.getParameter("password");
				googleAccessToken = request.getParameter("googleAccessToken");

				try {
					MediciProxy mp = login(username, password,
							googleAccessToken, request);
					if(googleAccessToken != null) {
						username = MediciProxy.isValidGoogleToken(_googleClientId, googleAccessToken);
					}
					// If we have proxy credentials, store them in the session
					request.getSession().setAttribute("proxy", mp);
					request.getSession().setAttribute(AUTHENTICATED_AS,
							username);
					log.debug("Authenticated: " + username);
					return;
				} catch (HTTPException he) {
					log.debug("Could not authenticate");
					int status = he.getStatusCode();
					response.setStatus(status); // HttpServletResponse.SC_UNAUTHORIZED
												// or SC_FORBIDDEN
					if (status == HttpServletResponse.SC_UNAUTHORIZED) {
						response.getWriter().write("Unauthorized");
					} else {
						response.getWriter().write("Forbidden");
					}
					response.flushBuffer();
					return;
				}

			} else if (uri.startsWith(appPath + "/DoLogout")) {
				log.debug("Logging out");
				HttpSession session = request.getSession(false);

				if (session != null && !session.isNew()) {
					MediciProxy mp = (MediciProxy) session
							.getAttribute("proxy");
					mp.logout();
					session.invalidate();
				}
				if (_tryAnonymous) {
					// Go home
					response.sendRedirect(appPath);
				} else {
					// Go login
					response.sendRedirect(appPath + _loginpage);
				}
				return;
			} else { // Request is for something other than Login (some other
						// servlet that will retrieve
						// existing credentials)
				HttpSession session = request.getSession(false);
				boolean goodCredentials = false;
				if (session != null) {
					username = (String) session.getAttribute(AUTHENTICATED_AS);
					MediciProxy mp = (MediciProxy) session
							.getAttribute("proxy");
					/*
					 * if login is being handled by other code (e.g. by a GWT
					 * app like mmdb) AUTHENTICATED_AS will be set, but
					 * MediciProxy won't, so we test for the username
					 */
					if (username != null) {
						goodCredentials = true;
					}
					if (mp == null) {
						log.debug("No MediciProxy found in session-->Not using AuthFilter for DoLogin");
					}
					log.debug("Authenticated as: " + username);
				}

				// Protected resources

				// For any resources in the _webauthpath, we want to send web
				// status codes
				// and not redirect to the login resource for auth errors
				// i.e. we expect these resources to be accessed via
				// hyperlinks/user input
				// and don't consider them to be 'part of' the application

				boolean webAuth = false;
				if (_webauthpath != null) {
					if (uri.startsWith(appPath + _webauthpath)) {
						webAuth = true;
					}
				}

				// Redirect to the login form if no credentials
				if (!goodCredentials) {
					// Try anonymous creds
					if (_tryAnonymous) {
						try {
							MediciProxy mp = login("anonymous", "none", null, request);
							// If we have proxy credentials, store them in the
							// session
							request.getSession().setAttribute("proxy", mp);
							request.getSession().setAttribute(AUTHENTICATED_AS,
									"anonymous");
							log.debug("Authenticated as 'anonymous'");
							goodCredentials = true;
							// Proceed to chain.doFilter
						} catch (HTTPException he) {
							int status = he.getStatusCode();
							// HttpServletResponse.SC_UNAUTHORIZED
							// or SC_FORBIDDEN
							request.setAttribute("statusCode",
									Integer.toString(status));
							if (status == HttpServletResponse.SC_UNAUTHORIZED) {
								log.debug("Could not authenticate anonymous");

							} else {
								log.debug("anonymous is forbidden");
							}
						}
					}
				}

				if (!goodCredentials) {
					if (webAuth) {
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					} else {
						// For all resources, we just want to force a login
						log.debug("Redirecting to login URL");
						request.getRequestDispatcher(_loginpage).forward(
								request, response);
					}
					return;
				}

			}
		}
		chain.doFilter(req, res);

	}

	private MediciProxy login(String username, String password,
			String googleAccessToken, HttpServletRequest request)
			throws HTTPException {
		HttpSession session = request.getSession(false);

		// Get new session upon login (avoiding session fixation
		// attack
		if (session != null && !session.isNew()) {
			session.invalidate();
		}
		session = request.getSession(true);

		// Setup MediciProxy to handle future remote requests

		MediciProxy mp = new MediciProxy();

		// Try to use/store credentials

		if (googleAccessToken != null) {
			mp.setGoogleCredentials(googleAccessToken, _server, _remoteAPIKey);
		} else {

			mp.setCredentials(username, password, _server, _remoteAPIKey);
		}
		if (!mp.hasValidCredentials()) {
			// See if the credentials worked and were stored
			// Should always have valid credentials here
			log.warn("Unknown authentication failure ");
			throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
		}

		return mp;
	}

	public void init(FilterConfig config) throws ServletException {

		// Get Property file parameter
		_propFile = config.getInitParameter("PropertiesFileName");
		// Find Properties file and retrieve the domain/sparql endpoint of the
		// remote Medici instance
		_server = PropertiesLoader.getProperties(_propFile).getProperty(
				"domain");
		log.debug("Server: " + _server);
		_remoteAPIKey = PropertiesLoader.getProperties(_propFile).getProperty(
				"remoteAPIKey");
		log.debug("RemoteAPIKey: " + _remoteAPIKey);
		String anonymous = PropertiesLoader.getProperties(_propFile)
				.getProperty("enableAnonymous");
		if ((anonymous == null) || (!anonymous.equalsIgnoreCase("true"))) {
			_tryAnonymous = false;
		} else {
			_tryAnonymous = true;
		}
		log.debug("Enable Anonymous: " + _tryAnonymous);
		
		_googleClientId = PropertiesLoader.getProperties().getProperty(
				"google.client_id");
		log.debug("googleClient ID: " + _googleClientId);

		_loginpage = config.getInitParameter("LoginPage");
		if (_loginpage == null) {
			_loginpage = "/login.html";

		}
		log.debug("Login Page: " + _loginpage);

		_openpath = config.getInitParameter("OpenPath");
		if (_openpath == null) {
			_openpath = "/login";

		}
		log.debug("Open Path: " + _openpath);

		_webauthpath = config.getInitParameter("WebAuthPath");
		if (_webauthpath == null) {
			_webauthpath = null;

		}
		log.debug("Web Auth Path: " + _webauthpath);

	}

	public void destroy() {
		// add code to release any resource
	}
}
