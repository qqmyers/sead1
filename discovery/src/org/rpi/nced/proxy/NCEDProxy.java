package org.rpi.nced.proxy;

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

import org.rpi.nced.dao.DataAccess;
import org.rpi.nced.objects.MainCollection;
import org.rpi.nced.utilties.PropertiesLoader;
import org.rpi.nced.utilties.json.JSONException;
import org.rpi.nced.utilties.json.JSONObject;
import org.rpi.nced.utilties.json.XML;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

public class NCEDProxy {

	static Object padlock = new Object();
	
	//private static Log  log   = LogFactory.getLog(NCEDProxy.class);

	static NCEDProxy _instance;

	public static NCEDProxy getInstance() {
		if (_instance == null) {
			_instance = new NCEDProxy();
		}
		return _instance;
	}


	private NCEDProxy() {
		try {
			new PropertiesLoader().loadProperties();
		} catch (Exception ex) {

		}
	}

	public String getContents(String parentTagID) throws Exception {
		String query = "SELECT ?tagID ?title ?length ?abstract WHERE { <"
				+ parentTagID
				+ "> <http://purl.org/dc/terms/hasPart> ?tagID ."
				+ " "
				+ "?tagID <http://purl.org/dc/elements/1.1/title> ?title ."
				+ " "
				+ "OPTIONAL { ?tagID <tag:tupeloproject.org,2006:/2.0/files/length> ?length .}"
				+ " " + "OPTIONAL { <" + parentTagID
				+ "> <http://purl.org/dc/terms/abstract> ?abstract . } }";
		return getCollections(query);
	}

	public String getAllCollections() throws Exception {
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " "
				+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>"
				+ " "
				+ "Prefix tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>"
				+ " "
				+ "SELECT ?tagID ?title ?abstract ?keywords WHERE {"
				+ " "
				+ "?tagID <rdf:type> <cet:Collection> ."
				+ " "
				+ "?tagID <http://purl.org/dc/terms/issued> ?date ."
				+ " "
				+ "?tagID <dc:title> ?title ."
				+ " "
				+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/abstract> ?abstract . } }";
		return getCollections(query);
	}
	
	private String getCollections(String query) throws Exception {
		String responseText = DataAccess.getResponse(_userName, _password,	query);

		JSONObject responseJSONObject = convertToJsonObject(responseText);
		responseText = responseJSONObject.toString();
		if(!collectionListisEmpty(responseJSONObject)) {
			if (!singleCollectionReturned(responseJSONObject)) {
				responseText=sortCollections(responseText);
			}
		}
	
		return responseText;
	}

	private boolean collectionListisEmpty(JSONObject jsonObject) throws JSONException {
		
		JSONObject sparqlObject = jsonObject.getJSONObject("sparql");
		boolean response = false;
		try {
			//results is a JSONObject (not a String) if there are 1 or more result entries
			sparqlObject.getString("results");
			response = true;
		} catch (JSONException jse) {
		}
		return response;
	}
	
	private boolean singleCollectionReturned(JSONObject jsonObject) throws JSONException {
		JSONObject sparqlObject = jsonObject.getJSONObject("sparql");
		JSONObject resultsObject = sparqlObject.getJSONObject("results");
		boolean response = false;
		try {
			JSONObject jso = resultsObject.getJSONObject("result");
			response = true;
		} catch (JSONException jse) {};
		return response;
	}
	
	private String sortCollections(String jsonResponse) {
		Gson gson = new Gson();
	    		
		//FYI: Binding class (used at the lowest level of the hierarchy in MainCollection) assumes a simple name/literal pair
		//which is broken by typed literals (e.g. length in the getContents query) unless it is stripped in 
		// convertToJSONObject
		MainCollection collectionsResult = gson.fromJson(jsonResponse, MainCollection.class); 
		Collections.sort(collectionsResult.getSparql().getResults().getResult());
		return gson.toJson(collectionsResult, MainCollection.class);
	}

	private String convertToJson(String responseText) throws JSONException {
		return convertToJsonObject(responseText).toString();
	}
	
	private JSONObject convertToJsonObject(String responseText) throws JSONException {

		if (responseText.contains("&")) {
			responseText = responseText.replace("&", "and");
		}

		// Remove the long type associated with length values so that the JSON produced matches 
		// the name=x, literal=y pattern expected in the sortItems Method
		responseText = responseText.replaceAll(" datatype=\"http://www.w3.org/2001/XMLSchema#long\"","");
		
		JSONObject jsonObject = XML.toJSONObject(responseText);

		return jsonObject;
	}

	public void Authenticate(String userName, String password) throws Exception {
		// A dummy query to check if user is authenticated - there is no token
		// based authentication yet.
		String query = "SELECT ?p ?o WHERE { <s> ?p ?o . }";
		// String query =
		// "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/> SELECT ?c ?t WHERE { ?c <rdf:type> <cet:Collection> . ?c <dc:title> ?t . }";
		DataAccess.getResponse(userName, password, query);
	}

	String _userName = "";
	String _password = "";

	public void setUserName(String userName) {
		_userName = userName;
	}

	public void setPassword(String password) {
		_password = password;
	}

	public String getCreators(String tagID) throws Exception {
		String query = "SELECT ?creator WHERE { <" + tagID
				+ "> <http://purl.org/dc/terms/creator> ?creator . }";
		String responseText = DataAccess.getResponse(_userName, _password,
				query);
		return convertToJson(responseText);
	}
	
	public String getContacts(String tagID) throws Exception {
		String query = "SELECT ?contact WHERE { <" + tagID
				+ "> <http://sead-data.net/terms/contact> ?contact . }";
		String responseText = DataAccess.getResponse(_userName, _password,
				query);
		return convertToJson(responseText);
	}
	

	public String getDescriptors(String tagID) throws Exception {
		String query = "SELECT ?name ?descriptor WHERE { <" + tagID
				+ "> <http://purl.org/dc/terms/description> ?descriptor . }";
		String responseText = DataAccess.getResponse(_userName, _password,
				query);
		return convertToJson(responseText);
	}
	
	public String getKeywords(String tagID) throws Exception {
		String query = "SELECT ?name ?keyword WHERE { <" + tagID
				+ "> <http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag> ?keyword . }";
		String responseText = DataAccess.getResponse(_userName, _password,
				query);
		return convertToJson(responseText);
	}
	
}
