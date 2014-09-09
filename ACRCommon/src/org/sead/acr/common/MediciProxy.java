package org.sead.acr.common;

/**
 *    Copyright [2013] [University of Michigan]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Ram Prassana
 * @author Jim Myers
 *
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import javax.xml.ws.http.HTTPException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.sead.acr.common.utilities.PropertiesLoader;

public class MediciProxy {

	static Object padlock = new Object();

	private static Log log = LogFactory.getLog(MediciProxy.class);

	private String _username = null;
	private String _password = null;
	private String _server = null;
	private String _remoteAPIKey = null;
	private boolean _validCredentials = false;

	private boolean testRemoteApi = true;

	// private String _googleAccessToken = null;
	private String _sessionId = null;

	private String _geouser = null;
	private String _geopassword = null;
	private String _geoserver = null;

	static String _sparql_path = "/resteasy/sparql";

	static final String USER_INFO_URL = "https://www.googleapis.com/plus/v1/people/me/openIdConnect?access_token=";
	static final String TOKEN_INFO_URL = "https://www.googleapis.com/oauth2/v1/tokeninfo";

	public MediciProxy() {
	}

	public MediciProxy(boolean requireTest) {
		testRemoteApi = requireTest;
	}

	public void setCredentials(String username, String password, String server) {
		setCredentials(username, password, server, null);
	}

	/*
	 * Stub for testing: setCredentials will read the geo credentials from
	 * properties. This method allows you to set them for testing, but needs to
	 * do more to test the credentials before it is ready for general use (and
	 * nominally we'll have sso so a separate username/password won't be needed)
	 * 
	 * @deprecated
	 */

	public void setGeoCredentials(String username, String password,
			String server) {
		_geouser = username;
		_geopassword = password;
		_geoserver = server;
	}

	public void setCredentials(String username, String password, String server,
			String remoteAPIKey) throws HTTPException {
		log.debug("Setting credentials");
		_username = username;
		_password = password;
		_server = server;
		_remoteAPIKey = remoteAPIKey;

		// FIXME - should be sso with supplied credentials, and externally
		// supplied geoserver address, but
		// hardcoded for now
		// Optional Geoserver info - may or may not be supplied/required by app
		if (_geoserver == null) {
			Properties p = PropertiesLoader.getProperties();

			_geoserver = p.getProperty("geoserver");
			_geouser = p.getProperty("geouser");
			_geopassword = p.getProperty("geopassword");
		}
		try {

			DataAccess authPostDA = new DataAccess();
			authPostDA.setServer(server + "/api/authenticate");
			if (remoteAPIKey != null) {
				authPostDA.setRemoteAPIKey(remoteAPIKey);
			}
			authPostDA.setMethod(DataAccess.POST);
			authPostDA.setReturnType(DataAccess.JSON);
			authPostDA.setRemoteAPIKey(remoteAPIKey);
			String sessionId = authPostDA.getResponse("username=" + username
					+ "&password=" + password);
			if (testRemoteApi) {
				postTestQuery(sessionId, server, remoteAPIKey);
			}
			// If no exception - it worked/wasn't needed and all the params
			// should be stored
			// as valid login info
			_validCredentials = true;
			_sessionId = sessionId;
		} catch (HTTPException he) {
			// Unauthorized or forbidden responses should be forwarded so users
			// can be informed
			_validCredentials = false;
			throw (he);
		} catch (Exception e) {
			// Some form of IO issue
			e.printStackTrace();
			_validCredentials = false;
		}
	}

	public String setGoogleCredentials(String googleAccessToken, String server,
			String remoteAPIKey) {

		String username = null;
		// Store credential
		// Validate and get userinfo @ Google
		String[] client_ids = new String[2];
		client_ids[0] = PropertiesLoader.getProperties().getProperty(
				"google.client_id");
		if (client_ids[0] == null) {
			log.debug("No google web Client ID found");
		}
		client_ids[1] = PropertiesLoader.getProperties().getProperty(
				"google.device_client_id");
		if (client_ids[1] == null) {
			log.debug("No google device Client ID found");
		}

		try {
			username = isValidGoogleToken(client_ids, googleAccessToken);
			if (username != null) {
				// FIXME Ping medici with test query to determine permissions
				DataAccess authenticateDA = new DataAccess();
				authenticateDA.setUseBasicAuth(false);
				authenticateDA.setMethod(DataAccess.POST);
				if (remoteAPIKey != null) {
					authenticateDA.setRemoteAPIKey(remoteAPIKey);
				}
				authenticateDA.setReturnType(DataAccess.XML);
				authenticateDA.setServer(server + "/api/authenticate");

				String sessionId = authenticateDA
						.getResponse("googleAccessToken=" + googleAccessToken);

				log.debug("started session" + sessionId);
				// FIXME: Make medici accept googleaccesstoken on resteasy calls
				// (versus via api/authenticate only)
				if (testRemoteApi) {
					postTestQuery(sessionId, server, remoteAPIKey);
				}

				// If no exception - it worked/wasn't needed and all the params
				// should be
				// stored
				// as valid login info
				_validCredentials = true;
				_server = server;
				_remoteAPIKey = remoteAPIKey;
				_sessionId = sessionId;

			}
		} catch (HTTPException he) {
			// Unauthorized or forbidden responses should be forwarded
			// so users
			// can be informed
			_validCredentials = false;
			throw (he);
		} catch (Exception e) {
			// Some form of IO issue
			e.printStackTrace();
			_validCredentials = false;
		}
		if (!_validCredentials) {
			// reset username to null in the case the google auth worked, but
			// user is forbidden to sparqlquery/use the remoteAPI
			username = null;
		}
		return username;
	}

	private void postTestQuery(String sessionId, String server,
			String remoteAPIKey) throws Exception {
		// A dummy query to check if user is authenticated and has remoteApi
		// permission and remoteAPI key matches
		// - there is no token based authentication yet.

		String query = "query=SELECT ?p ?o WHERE { <s> ?p ?o . }";
		DataAccess xmlPostDA = DataAccess
				.buildSessionXMLPostResponseDataAccess(sessionId, server
						+ _sparql_path, remoteAPIKey);
		xmlPostDA.setSessionId(sessionId);
		xmlPostDA.getResponse(query);

	}

	public boolean hasValidCredentials() {
		return _validCredentials;
	}

	public boolean isAnonymous() {
		boolean is = false;
		log.debug("Checking anon");
		if (_username != null) {
			log.debug("Uname: " + _username);
			if (_username.equals("anonymous")) {
				is = true;
			}
		}
		return is;
	}

	/*
	 * An XML encioded JSON response, so use the newer method
	 * 
	 * @deprecated
	 */
	public String getSparqlJSONResponse(String query) throws IOException,
			MalformedURLException, JSONException {
		return getSparqlXMLResponse(query);
	}

	public String getSparqlXMLResponse(String query) throws IOException,
			MalformedURLException, JSONException {

		DataAccess sparqlQueryDA = new DataAccess();
		sparqlQueryDA.setServer(_server + _sparql_path);
		sparqlQueryDA.setMethod(DataAccess.POST);
		sparqlQueryDA.setReturnType(DataAccess.XML);
		if (_remoteAPIKey != null) {
			sparqlQueryDA.setRemoteAPIKey(_remoteAPIKey);
		}
		sparqlQueryDA.setSessionId(_sessionId);

		String responseText = sparqlQueryDA.getResponse(query);

		// JSONObject responseJSONObject = convertToJsonObject(responseText);
		// responseText = responseJSONObject.toString();
		responseText = XML.toJSONObject(responseText).toString();
		/*
		 * if(!collectionListisEmpty(responseJSONObject)) { if
		 * (!singleCollectionReturned(responseJSONObject)) {
		 * responseText=sortCollections(responseText); } }
		 */
		if (log.isDebugEnabled()) {
			log.debug("Query: " + query);
			log.debug("Response: " + responseText);
		}
		return responseText;
	}

	public String executeAuthenticatedGet(String urlPath, String query)
			throws IOException, MalformedURLException {
		String url = _server;
		if (urlPath != null) {
			url = url + urlPath;
		}
		DataAccess authGetDA = DataAccess
				.buildUnauthenticatedJsonGETResponseDataAccess(url);
		// authGetDA.setBasicCreds(_username, _password);
		authGetDA.setUseBasicAuth(false);
		if (_remoteAPIKey != null) {
			authGetDA.setRemoteAPIKey(_remoteAPIKey);
		}
		authGetDA.setSessionId(_sessionId);
		return authGetDA.getResponse(query);
	}

	public String executeAuthenticatedGeoGet(String urlPath, String query)
			throws IOException, MalformedURLException {
		String url = _geoserver;
		if (urlPath != null) {
			url = url + urlPath;
		}
		DataAccess authGetDA = DataAccess
				.buildUnauthenticatedJsonGETResponseDataAccess(url);
		if ((_geouser != null) && (_geouser.length() != 0)) {
			authGetDA.setBasicCreds(_geouser, _geopassword);
			authGetDA.setUseBasicAuth(true);
		} else {
			authGetDA.setSessionId(_sessionId);
			if (_remoteAPIKey != null) {
				authGetDA.setRemoteAPIKey(_remoteAPIKey);
			}
		}

		return authGetDA.getResponse(query);

	}

	public void logout() {
		DataAccess logoutDA = DataAccess
				.buildUnauthenticatedJsonGETResponseDataAccess(_server
						+ "/api/logout");
		logoutDA.setSessionId(_sessionId);
		try {
			logoutDA.getResponse(null);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			_sessionId = null;
		}

	}

	/**
	 * @ deprecated - use getValidatedGoogleToken instead
	 * 
	 */

	public static String isValidGoogleToken(String[] client_ids,
			String googleAccessToken) {

		try {
			DataAccess jsonGETDA = DataAccess
					.buildUnauthenticatedJsonGETResponseDataAccess(TOKEN_INFO_URL);

			String result = jsonGETDA.getResponse("access_token="
					+ googleAccessToken);
			log.debug("TokenInfo retrieved from Google: " + result);

			JSONObject tokeninfo = new JSONObject(result);

			String audience = tokeninfo.getString("audience");
			boolean verified = tokeninfo.getBoolean("verified_email");
			int i = audience.indexOf("<");
			if (i > 0) {
				log.debug("Audience includes < :" + audience);
				audience = audience.substring(0, i);
			}
			if (verified) {
				for (String id : client_ids) {
					if (audience.equals(id)) {
						return tokeninfo.getString("email");
					}
				}
			}
		} catch (Exception e) {
			log.debug("Google validation exception: " + e.getMessage());
		}

		return null;
	}

	public static JSONObject getValidatedGoogleToken(String[] client_ids,
			String googleAccessToken) {

		try {
			DataAccess jsonGETDA = DataAccess
					.buildUnauthenticatedJsonGETResponseDataAccess(TOKEN_INFO_URL);

			String result = jsonGETDA.getResponse("access_token="
					+ googleAccessToken);
			log.debug("TokenInfo retrieved from Google: " + result);

			JSONObject tokeninfo = new JSONObject(result);
			String audience = tokeninfo.getString("audience");
			boolean verified = tokeninfo.getBoolean("verified_email");
			int i = audience.indexOf("<");
			if (i > 0) {
				log.debug("Audience includes < :" + audience);
				audience = audience.substring(0, i);
			}
			if (verified) {
				for (String id : client_ids) {
					if (audience.equals(id)) {
						return tokeninfo;
					}
				}
			}
		} catch (Exception e) {
			log.debug("Google validation exception: " + e.getMessage());
		}

		return null;
	}

}
