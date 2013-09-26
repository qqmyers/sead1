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
	
	private static String _server;
	private static String _remoteAPIKey;

	private static boolean _tryAnonymous = false;

	private static Log log = LogFactory.getLog(AuthFilter.class);

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String appPath = request.getContextPath();
		String uri = request.getRequestURI();
		String query = request.getQueryString();
		String username = "";
		String password = "";
		if (query != null) {
			log.debug("URI: " + uri + "?" + query);
		} else {
			log.debug("URI: " + uri);
		}

		// Let the login form page and requests for it's images, css files, etc.
		// through...
		if (!uri.startsWith(appPath + "/login")) {
			// if logging in
			if (uri.startsWith(appPath + "/DoLogin")) {
				log.debug("Attempting to authenticate");
				// Retrieve form info
				username = request.getParameter("userName");
				password = request.getParameter("password");
				try {
					MediciProxy mp = login(username, password, request);
					// If we have proxy credentials, store them in the session
					request.getSession().setAttribute("proxy", mp);
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
					MediciProxy mp = (MediciProxy) session
							.getAttribute("proxy");
					if (mp != null) {
						// Could test the credentials here, but it's an extra
						// http call off to the server - if they don't work when
						// the servlets use them, we'll know.
						// mp.hasValidCredientials() should only be true
						goodCredentials = mp.hasValidCredentials();
					}
				}
				// Redirect to the login form if no credentials
				if (!goodCredentials) {
					// Try anonymous creds
					if (_tryAnonymous) {
						try {
							MediciProxy mp = login("anonymous", "none", request);
							// If we have proxy credentials, store them in the
							// session
							request.getSession().setAttribute("proxy", mp);
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
					// For all cases, we just want to force a login

					request.getRequestDispatcher(_loginpage).forward(request,
							response);
					return;
				}

			}
		}
		chain.doFilter(req, res);

	}

	private MediciProxy login(String username, String password,
			HttpServletRequest request) throws HTTPException {
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

		mp.setCredentials(username, password, _server, _remoteAPIKey);
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

		_loginpage = config.getInitParameter("LoginPage");
		if (_loginpage == null) {
			_loginpage = "/login.html";

		}
		log.debug("Login Page: " + _loginpage);

	}

	public void destroy() {
		// add code to release any resource
	}
}
