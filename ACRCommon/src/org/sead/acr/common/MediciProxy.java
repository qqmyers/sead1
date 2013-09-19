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

import org.sead.acr.common.utilities.PropertiesLoader;
import org.sead.acr.common.utilities.Queries;
import org.sead.acr.common.utilities.json.JSONException;
import org.sead.acr.common.utilities.json.JSONObject;
import org.sead.acr.common.utilities.json.XML;

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

	public MediciProxy() {
		try {
			_server = PropertiesLoader.getProperties().getProperty("domain");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setCredentials(String username, String password, String server) {
		setCredentials(username, password, server, null);
	}

	public void setCredentials(String username, String password, String server,
			String remoteAPIKey) throws HTTPException {
		try {
			// A dummy query to check if user is authenticated - there is no
			// token based authentication yet.

			String query = "SELECT ?p ?o WHERE { <s> ?p ?o . }";
			DataAccess.getResponse(username, password, server, remoteAPIKey,
					query);
			// If no exception - it worked and all the params should be stored
			// as valid login info
			_username = username;
			_password = password;
			_server = server;
			_remoteAPIKey = remoteAPIKey;
			_validCredentials = true;
		} catch (HTTPException he) {
			// Unauthorized or forbidden repsonses should be forwarded so users
			// can be informed
			_username = null;
			_password = null;
			_server = null;
			_validCredentials = false;
			throw (he);
		} catch (Exception e) {
			// Some form of IO issue
			e.printStackTrace();
			_username = null;
			_password = null;
			_server = null;
			_validCredentials = false;
		}
	}

	public boolean hasValidCredentials() {
		return _validCredentials;
	}

	public boolean isAnonymous() {
		boolean is = false;
		if (_username != null) {
			if (_username.equals("anonymous")) {
				is = true;
			}
		}
		return is;
	}

	public String getJSONResponse(String query) throws Exception {
		String responseText = DataAccess.getResponse(_username, _password,
				_server, _remoteAPIKey, query);

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
}
