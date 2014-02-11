package org.sead.acr.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Scanner;

import java.net.MalformedURLException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.sead.acr.common.utilities.Base64;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataAccess {

	static final String GET = "GET";
	static final String POST = "POST";

	static final String XML = "text/xml";
	static final String JSON = "application/json";

	private static Log log = LogFactory.getLog(DataAccess.class);

	private String sessionId = null;
	private String basicCreds = null;
	private String method = null;
	private String remoteAPIKey = null;
	private String returnType = null;
	private String server = null;
	private boolean useBasicAuth = false;

	public static DataAccess buildXMLPostResponseDataAccess(String username,
			String password, String server, String remoteAPIKey) {
		DataAccess da = new DataAccess();
		da.setUseBasicAuth(true);
		da.setBasicCreds(username, password);
		da.setServer(server);
		da.setRemoteAPIKey(remoteAPIKey);
		da.setMethod(POST);
		da.setReturnType(XML);
		return da;
	}

	public static DataAccess buildSessionXMLPostResponseDataAccess(
			String sessionId, String server, String remoteAPIKey) {
		DataAccess da = new DataAccess();
		da.setUseBasicAuth(false);
		da.setSessionId(sessionId);
		da.setServer(server);
		da.setRemoteAPIKey(remoteAPIKey);
		da.setMethod(POST);
		da.setReturnType(XML);
		return da;
	}

	public static DataAccess buildJsonGETResponseDataAccess(String username,
			String password, String server, String remoteAPIKey) {
		DataAccess da = new DataAccess();
		da.setUseBasicAuth(true);
		da.setBasicCreds(username, password);
		da.setServer(server);
		da.setRemoteAPIKey(remoteAPIKey);
		da.setMethod(GET);
		da.setReturnType(JSON);
		return da;
	}

	public static DataAccess buildCustomDataAccess(String username,
			String password, String server, String remoteAPIKey, String method,
			String returnType) {
		DataAccess da = new DataAccess();
		da.setUseBasicAuth(true);
		da.setBasicCreds(username, password);
		da.setServer(server);
		da.setRemoteAPIKey(remoteAPIKey);
		da.setMethod(method);
		da.setReturnType(returnType);
		return da;
	}

	public static DataAccess buildUnauthenticatedJsonGETResponseDataAccess(
			String server) {
		DataAccess da = new DataAccess();
		da.setUseBasicAuth(false);
		da.setServer(server);
		da.setMethod(GET);
		da.setReturnType(JSON);
		return da;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getBasicCreds() {
		return basicCreds;
	}

	public void setBasicCreds(String username, String password) {
		String userCredentials = username + ":" + password;

		this.basicCreds = Base64.encodeToString(userCredentials.getBytes(),
				Base64.NO_WRAP);
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRemoteAPIKey() {
		return remoteAPIKey;
	}

	public void setRemoteAPIKey(String remoteAPIKey) {
		this.remoteAPIKey = remoteAPIKey;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public boolean getUseBasicAuth() {
		return useBasicAuth;
	}

	public void setUseBasicAuth(boolean useBasicAuth) {
		this.useBasicAuth = useBasicAuth;
	}

	public String getResponse(String query) throws MalformedURLException,
			IOException {

		if (log.isDebugEnabled()) {
			logRequest(query);
		}

		HttpURLConnection conn = null;

		query = buildQuery(query);
		URL requestURL;
		if (method.equals(GET)) {
			if (!query.equals("")) {
				requestURL = new URL(server + "?" + query);
			} else {
				requestURL = new URL(server);
			}
		} else {
			requestURL = new URL(server);
		}

		// Make a connect to the server
		log.debug("Connecting to: " + requestURL.toString());


		conn = (HttpURLConnection) requestURL.openConnection();

		if (useBasicAuth) {
			if (basicCreds == null) {// Put the authentication details in the
										// request
				// edu.uiuc.ncsa.cet.bean.tupelo.rbac.Anonymous.USER =
				// "http://cet.ncsa.uiuc.edu/2007/person/anonymous", id =
				// "anonymous"
				// using the constant requires 3 extra jar files, so just using
				// value
				String userCredentials = "anonymous:none";

				basicCreds = Base64.encodeToString(userCredentials.getBytes(),
						Base64.NO_WRAP);
			}
			conn.setRequestProperty("Authorization", "Basic " + basicCreds);
		}
		if (sessionId != null) {
			conn.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);
		}
		conn.setDoInput(true);
		conn.setUseCaches(false);
		// set method
		conn.setRequestMethod(method);

		conn.setRequestProperty("Accept", returnType);

		if (method.equals(POST)) {
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setDoOutput(true);
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(query.getBytes());

			outputStream.flush();
			outputStream.close();
		}

		// Ensure we got the HTTP 200 response code
		int responseCode = conn.getResponseCode();
		log.debug("Response code: " + responseCode);
		switch (responseCode) {
		case HttpServletResponse.SC_OK:
			break;
		case HttpServletResponse.SC_UNAUTHORIZED:
			throw new HTTPException(responseCode);
		case HttpServletResponse.SC_FORBIDDEN:
			throw new HTTPException(responseCode);
		default:
			break;
		}

		String responseText = null;
		InputStream _inputStream = null;

		try {
			// Read the response
			_inputStream = conn.getInputStream();

			Scanner responseScanner = new Scanner(_inputStream);
			responseText = responseScanner.useDelimiter("\\A").next();

			responseScanner.close();
		} catch (NoSuchElementException nse) {
			responseText = "";
		} finally {
			if(_inputStream!=null) {
			  _inputStream.close();
			}  
		}
		conn.disconnect();

		return responseText;

	}

	protected String buildQuery(String query) throws MalformedURLException {

		// Add remoteAPIKey to query if set
		String prepend = "";
		if ((remoteAPIKey != null) && (remoteAPIKey.length() != 0)) {
			prepend = "remoteAPI=" + remoteAPIKey;
		}
		// write a query to contact the server
		if (query != null) {
			if (!prepend.equals("")) {
				// have both
				query = prepend + "&" + query;
			}
			// else just have query and it's fine as is
		} else {
			// or query is null and we use prepend only, which may be ""
			query = prepend;
		}
		log.debug("Final Query = " + query);
		return query;
	}

	private void logRequest(String query) {
		log.debug("DA Request To: " + server);
		log.debug("DA Request UsingBasicAuth: " + useBasicAuth);
		log.debug("DA Request basicCreds: " + basicCreds);
		log.debug("DA Request session: " + sessionId);
		log.debug("DA Request method: " + method);
		log.debug("DA Request returnType: " + returnType);
		log.debug("DA Request remoteAPIKey: " + remoteAPIKey);

	}

}
