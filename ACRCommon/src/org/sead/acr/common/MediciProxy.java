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

import java.util.Collections;

import org.sead.acr.common.utilities.json.JSONException;
import org.sead.acr.common.utilities.json.JSONObject;
import org.sead.acr.common.utilities.json.XML;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.sead.acr.common.utilities.Queries;

import java.util.Properties;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.ws.http.HTTPException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MediciProxy {

	static Object padlock = new Object();

	private static Log log = LogFactory.getLog(MediciProxy.class);

	private String _username = null;
	private String _password = null;
	private String _server = null;
	private String _remoteAPIKey = null;
	private boolean _validCredentials = false;

	private String _geouser = null;
	private String _geopassword = null;
	private String _geoserver = null;

	static String _sparql_path = "/resteasy/sparql";

	public MediciProxy() {
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

			// A dummy query to check if user is authenticated - there is no
			// token based authentication yet.

			String query = "query=SELECT ?p ?o WHERE { <s> ?p ?o . }";
			DataAccess.getXMLPostResponse(username, password, server
					+ _sparql_path, remoteAPIKey, query);
			// If no exception - it worked and all the params should be stored
			// as valid login info
			_validCredentials = true;
		} catch (HTTPException he) {
			// Unauthorized or forbidden repsonses should be forwarded so users
			// can be informed
			_validCredentials = false;
			throw (he);
		} catch (Exception e) {
			// Some form of IO issue
			e.printStackTrace();
			_validCredentials = false;
		}
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

	public String getSparqlJSONResponse(String query) throws IOException,
			MalformedURLException, JSONException {
		String responseText = DataAccess.getXMLPostResponse(_username,
				_password, _server + _sparql_path, _remoteAPIKey, query);

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
		return DataAccess.getJsonGetResponse(_username, _password, url,
				_remoteAPIKey, query);
	}

	public String executeAuthenticatedGeoGet(String urlPath, String query)
			throws IOException, MalformedURLException {
		String url = _geoserver;
		if (urlPath != null) {
			url = url + urlPath;
		}
		return DataAccess.getJsonGetResponse(_geouser, _geopassword, url,
				_remoteAPIKey, query);
	}
}
