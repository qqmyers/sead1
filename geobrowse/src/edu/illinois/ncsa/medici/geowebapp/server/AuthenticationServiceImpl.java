package edu.illinois.ncsa.medici.geowebapp.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.PropertiesLoader;

import edu.illinois.ncsa.medici.geowebapp.client.service.AuthenticationService;

/**
 * @author Jim
 * 
 */
@SuppressWarnings("serial")
public class AuthenticationServiceImpl extends ProxiedRemoteServiceServlet
		implements AuthenticationService {

	/**
	 * 
	 */
	// == edu.illinois.ncsa.mmdb.web.rest.AuthenticatedServlet.AUTHENTICATED_AS
	private static String AUTHENTICATED_AS = "edu.illinois.ncsa.mmdb.web.server.auth.authenticatedAs";

	private static String _server;
	private static String _geoserver;
	private static String _proxiedgeoserver;

	private static String _remoteAPIKey;
	private static boolean _tryAnonymous = false;

	private static Log log = LogFactory.getLog(AuthenticationServiceImpl.class);

	public String login(String username, String password) {
		return login(username, password, null);
	}

	public String login(String googleAccessToken) {
		return login(null, null, googleAccessToken);
	}

	protected String login(String username, String password,
			String googleAccessToken) {

		dontCache();

		// Destroy any existing credentials
		logout();
		// Verify credentials
		MediciProxy mp = new MediciProxy();

		String result = "success";
		try {
			// Try to use/store credentials
			getRemoteServerProperties();
			if (username != null) {
				mp.setCredentials(username, password, _server, _remoteAPIKey);
			} else if (googleAccessToken != null) {
				username = mp.setGoogleCredentials(googleAccessToken, _server,
						_remoteAPIKey);
			} else {
				log.debug("No credentials available");
			}

			//These are null when using the geoproxy
			String geouser = PropertiesLoader.getProperties().getProperty("geouser");
			String geopassword = PropertiesLoader.getProperties().getProperty("geopassword");
			//In that case, rely on the proxy in medici - _geoserver is already set correctly
			mp.setGeoCredentials(geouser, geopassword, _geoserver);

			// Create session
			HttpSession session = getThreadLocalRequest().getSession(true);
			// Store MediciProxy
			session.setAttribute(_proxy, mp);
			// Set _authenticatedAs
			session.setAttribute(AUTHENTICATED_AS, username);
		} catch (HTTPException he) {
			if (he.getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
				result = "unauthorized";

			} else if (he.getStatusCode() == HttpServletResponse.SC_FORBIDDEN) {
				result = "forbidden";

			}

			// result will be false
			log.warn("authentication Failure: " + result);
			log.warn("user: " + username);
		}
		return result;
	}

	public void logout() {
		// Destroy session
		HttpSession session = getThreadLocalRequest().getSession(false);

		if (session != null) {
			session.invalidate();
		}
	}

	public String getUsername() {

		dontCache();

		getRemoteServerProperties();
		String name = null;
		HttpSession session = getThreadLocalRequest().getSession(false);

		if (session != null) {
			name = (String) session.getAttribute(AUTHENTICATED_AS);
		}
		// If null and enableAnonymous==true, try logging in as anonymous
		if ((name == null) && (_tryAnonymous == true)) {
			if (login("anonymous", "none").equals("success")) {
				session = getThreadLocalRequest().getSession(false);

				if (session != null) {
					name = (String) session.getAttribute(AUTHENTICATED_AS);
				}
			}
		}
		return name;
	}

	public String getGoogleClientId() {
		String id = PropertiesLoader.getProperties().getProperty(
				"google.client_id");

		return id;
	}

	private void getRemoteServerProperties() {

		if (_server == null) {

			// Find Properties file and retrieve the domain/sparql endpoint of
			// the
			// remote Medici instance
			_server = PropertiesLoader.getProperties().getProperty("domain");
			_proxiedgeoserver = PropertiesLoader.getProperties().getProperty("proxiedgeoserver");
			_geoserver = PropertiesLoader.getProperties().getProperty("geoserver");
			log.debug("Server: " + _server);
			_remoteAPIKey = PropertiesLoader.getProperties().getProperty(
					"remoteAPIKey");
			log.debug("RemoteAPIKey: " + _remoteAPIKey);
			String anonymous = PropertiesLoader.getProperties().getProperty(
					"enableAnonymous");
			if ((anonymous == null) || (!anonymous.equalsIgnoreCase("true"))) {
				_tryAnonymous = false;
			} else {
				_tryAnonymous = true;
			}
			log.debug("Enable Anonymous: " + _tryAnonymous);
		}
	}

	public String[] getUrls() {
		dontCache();
		getRemoteServerProperties();
		String[] urls = new String[2];
		urls[1] = _server;
		urls[0] = _geoserver;
		urls[0] = urls[0] + "/wms";

		return urls;
	}

	public static String getProxiedGeoServer() {
		String server = _proxiedgeoserver;
		if(server == null) {
			server = _geoserver;
		}
		return server;
	}
}
