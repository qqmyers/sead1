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
import org.rpi.nced.utilties.Queries;
import org.rpi.nced.utilties.json.XML;

import com.google.gson.Gson;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

public class NCEDProxy {

	static Object padlock = new Object();
	
	//private static Log  log   = LogFactory.getLog(NCEDProxy.class);

	static NCEDProxy _instance;

	private String _username = "";
	private String _password = "";
	private String _server = "";

	public static NCEDProxy getInstance() {
		if (_instance == null) {
			_instance = new NCEDProxy();
		}
		return _instance;
	}


	private NCEDProxy() {
		try {
			new PropertiesLoader().loadProperties();
			_server = PropertiesLoader.getProperties().getProperty("domain");
		} catch (Exception ex) {

		}
	}
	
	public void setCredentials(String username, String password) throws Exception {
		// A dummy query to check if user is authenticated - there is no token
		// based authentication yet.
		String query = "SELECT ?p ?o WHERE { <s> ?p ?o . }";
		// String query =
		// "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/> SELECT ?c ?t WHERE { ?c <rdf:type> <cet:Collection> . ?c <dc:title> ?t . }";
		DataAccess.getResponse(username, password, _server, query);
		_username = username;
		_password = password;
	}
	/*
	public String getContents(String parentID) throws Exception {
		return getJSONResponse(Queries.getCollectionContents(parentID));
	}

	public String getAllPublishedCollections() throws Exception {
		return getJSONResponse(Queries.ALL_PUBLISHED_COLLECTIONS);
	}
	*/
	public String getJSONResponse(String query) throws Exception {
		String responseText = DataAccess.getResponse(_username, _password, _server,	query);

		//JSONObject responseJSONObject = convertToJsonObject(responseText);
		//responseText = responseJSONObject.toString();
		responseText = XML.toJSONObject(responseText).toString();
		/*if(!collectionListisEmpty(responseJSONObject)) {
			if (!singleCollectionReturned(responseJSONObject)) {
				responseText=sortCollections(responseText);
			}
		}
		*/
		return responseText;
	}
/*
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
*/
	//private String convertToJson(String responseText) throws JSONException {

		// Remove the long type associated with length values so that the JSON produced matches 
		// the name=x, literal=y pattern expected in the sortItems Method
		//responseText = responseText.replaceAll(" datatype=\"http://www.w3.org/2001/XMLSchema#long\"","");
		

	//}




/*
	public String getCreators(String tagID) throws Exception {
		return getJSONResponse(Queries.getItemCreators(tagID));
	}
	
	public String getContacts(String tagID) throws Exception {
		return getJSONResponse(Queries.getItemContacts(tagID));
	}
	

	public String getDescriptors(String tagID) throws Exception {
		return getJSONResponse(Queries.getItemDescriptors(tagID));
	}
	
	public String getKeywords(String tagID) throws Exception {
		return getJSONResponse(Queries.getItemKeywords(tagID));
	}
	*/
}
