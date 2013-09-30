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
	private static String _authenticatedAs = "authenticatedAs";

	private static String _server;
	private static String _remoteAPIKey;
	private static boolean _tryAnonymous = false;

	private static Log log = LogFactory.getLog(AuthenticationServiceImpl.class);

	@Override
	public String login(String username, String password) {
		
		dontCache();
		
		// Destroy any existing credentials
		logout();
		// Verify credentials
		MediciProxy mp = new MediciProxy();

		String result = "success";
		try {
			// Try to use/store credentials
			getRemoteServerProperties();
			mp.setCredentials(username, password, _server, _remoteAPIKey);

			// Create session
			HttpSession session = getThreadLocalRequest().getSession(true);
			// Store MediciProxy
			session.setAttribute(_proxy, mp);
			// Set _authenticatedAs
			session.setAttribute(_authenticatedAs, username);
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

	@Override
	public void logout() {
		// Destroy session
		HttpSession session = getThreadLocalRequest().getSession(false);

		if (session != null) {
			session.invalidate();
		}
	}

	@Override
	public String getUsername() {
		
		dontCache();
		
		getRemoteServerProperties();
		String name = null;
		HttpSession session = getThreadLocalRequest().getSession(false);

		if (session != null) {
			name = (String) session.getAttribute(_authenticatedAs);
		}
		// If null and enableAnonymous==true, try logging in as anonymous
		if ((name == null) && (_tryAnonymous == true)) {
			if (login("anonymous", "none").equals("success")) {
				session = getThreadLocalRequest().getSession(false);

				if (session != null) {
					name = (String) session.getAttribute(_authenticatedAs);
				}
			}
		}
		return name;
	}

	private void getRemoteServerProperties() {

		if (_server == null) {

			// Find Properties file and retrieve the domain/sparql endpoint of
			// the
			// remote Medici instance
			_server = PropertiesLoader.getProperties().getProperty(
					"domain");
			log.debug("Server: " + _server);
			_remoteAPIKey = PropertiesLoader.getProperties()
					.getProperty("remoteAPIKey");
			log.debug("RemoteAPIKey: " + _remoteAPIKey);
			String anonymous = PropertiesLoader.getProperties()
					.getProperty("enableAnonymous");
			if ((anonymous == null) || (!anonymous.equalsIgnoreCase("true"))) {
				_tryAnonymous = false;
			} else {
				_tryAnonymous = true;
			}
			log.debug("Enable Anonymous: " + _tryAnonymous);
		}
	}

	@Override
	public String[] getUrls() {
		dontCache();
		
		String[] urls = new String[2];
		urls[0] = PropertiesLoader.getProperties().getProperty("geoserver") + "/wms";
		urls[1] = PropertiesLoader.getProperties().getProperty("domain");
		
		return urls;
	}

}
