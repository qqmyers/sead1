package org.mitre.dsmiley.httpproxy;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import javax.servlet.http.HttpServlet;

public class GeoProxyServlet extends org.mitre.dsmiley.httpproxy.ProxyServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8547079256992213795L;
	private String _server;
	private String _user;
	private String _password;
	private static String REMOTE_ALLOWED = "edu.illinois.ncsa.mmdb.web.server.auth.RemoteAllowed";

	@Override
	public String getServletInfo() {
		return "A proxy servlet for an ACR Geoserver, built using a proxy servlet by David Smiley, dsmiley@mitre.org";
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		String doLogStr = servletConfig.getInitParameter(P_LOG);
		if (doLogStr != null) {
			this.doLog = Boolean.parseBoolean(doLogStr);
		}
		if (doLog) {
			log("Doing log");
		}
		try {

			// Get Property file parameter
			String _propFile = getInitParameter("PropertiesFileName");

			// _propFile is only used if the PropertiesLoader is not already
			// configured
			_server = PropertiesLoader.getProperties(_propFile).getProperty(
					"geoserver");
			// now the prop file is set for sure
			_user = PropertiesLoader.getProperties().getProperty("geouser");
			_password = PropertiesLoader.getProperties().getProperty(
					"geopassword");
			if (doLog) {
				log(_server);
			}
			if (doLog) {
				log(_user);
			}
			if (doLog) {
				log(_password);
			}

			targetUriObj = new URI(_server);
		} catch (Exception e) {
			throw new RuntimeException(
					"Trying to process targetUri (geoserver) and u/p parameters: "
							+ e, e);
		}
		targetUri = targetUriObj.toString();
		if (doLog) {
			log(targetUri);
		}
		HttpParams hcParams = new BasicHttpParams();
		readConfigParam(hcParams, ClientPNames.HANDLE_REDIRECTS, Boolean.class);
		proxyClient = createHttpClient(hcParams);

		CredentialsProvider credsProvider = new BasicCredentialsProvider();

		credsProvider.setCredentials(new AuthScope(targetUriObj.getHost(),
				targetUriObj.getPort()), new UsernamePasswordCredentials(_user,
				_password));
		((DefaultHttpClient) proxyClient).setCredentialsProvider(credsProvider);
	}

	protected void service(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws ServletException,
			IOException {
		boolean allow = false;
		HttpSession session = servletRequest.getSession(false);
		if (session != null) {
			String remoteAllowed = (String) session
					.getAttribute(REMOTE_ALLOWED);
			if ((remoteAllowed!=null) && (remoteAllowed.equals("true"))) {
				allow = true;
			}
		}
		if (allow) {
			super.service(servletRequest, servletResponse);
		} else {
			if(doLog) log("Could not confirm remote api permission - forbidding");
			servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
		return;
	}

}
