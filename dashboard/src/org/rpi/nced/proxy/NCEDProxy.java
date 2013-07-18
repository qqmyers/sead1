package org.rpi.nced.proxy;

import java.util.Collections;

import org.rpi.nced.dao.DataAccess;
import org.rpi.nced.objects.MainCollection;
import org.rpi.nced.utilties.PropertiesLoader;
import org.rpi.nced.utilties.json.JSONException;
import org.rpi.nced.utilties.json.JSONObject;
import org.rpi.nced.utilties.json.XML;

import com.google.gson.Gson;

public class NCEDProxy {

	static Object padlock = new Object();

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
		String responseText = DataAccess.getResponse(_userName, _password,
				query);

		return convertToJson(responseText);

	}

	public String getAllCollections() throws Exception {
		String responseJSON;
		try{
			String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
					+ " "
					+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>"
					+ " "
					+ "Prefix tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>"
					+ " "
					+ "SELECT ?tagID ?title ?creator ?deleted WHERE {"
					+ " "
					+ "?tagID <rdf:type> <cet:Collection> ."
					+ " "
					+ "?tagID <http://purl.org/dc/terms/creator> ?creator ."
					+ " "
					+ "?tagID <dc:title> ?title ."
					+ " "
					+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/isReplacedBy> ?deleted . } }";
			
			String responseText = DataAccess.getResponse(_userName, _password,
					query);
			responseJSON = sortItems(convertToJson(responseText));
		}catch (Exception e) {
			String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
					+ " "
					+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>"
					+ " "
					+ "Prefix tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>"
					+ " "
					+ "SELECT ?tagID ?title ?creator ?deleted WHERE {"
					+ " "
					+ "?tagID <rdf:type> <cet:Collection> ."
					/*+ " "
					+ "?tagID <http://purl.org/dc/terms/creator> ?creator ."*/
					+ " "
					+ "?tagID <dc:title> ?title ."
					+ " "
					+ "OPTIONAL { ?tagID <http://purl.org/dc/terms/isReplacedBy> ?deleted . } }";
			
			String responseText = DataAccess.getResponse(_userName, _password,
					query);
			responseJSON = sortItems(convertToJson(responseText));
		}
		return responseJSON;
	}
	
	public String getAllDatasets() throws Exception {
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " "
				+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>"
				+ " "
				+ "Prefix tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/>"
				+ " "
				+ "SELECT ?title WHERE {"
				+ " "
				+ "?tagID <rdf:type> <cet:Dataset> ."
				+ " "
				+ "?tagID <dc:title> ?title . }";
		
		String responseText = DataAccess.getResponse(_userName, _password,
				query);
		String responseJSON = convertToJson(responseText);
		return responseJSON;
	}
	
	public String getRecentUploads() throws Exception {
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " "
				+ "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>"
				+ " "
				+ "PREFIX dc: <http://purl.org/dc/elements/1.1/>"
				+ " "
				+ "PREFIX dcterms: <http://purl.org/dc/terms/>"
				+ " "
				+ "SELECT ?tagID ?title ?creator ?date WHERE {"
				+ " "
				+ "?tagID <rdf:type> <cet:Dataset>  ."
				+ " "
				+ "?tagID <dc:title> ?title ."
				+ " "
				+ "?tagID <dc:creator> ?creator ."
				+ " "
				+ "?tagID <dc:date> ?date ."
				+ " "
				+ "OPTIONAL { ?tagID <dcterms:isReplacedBy> ?r } ."
				+"} "
				+"ORDER BY ASC(?replacedBy) DESC(?date) "
				+"LIMIT 15";
		String responseText = DataAccess.getResponse(_userName, _password,
				query);
		
		String responseJSON = convertToJson(responseText);
		return responseJSON;
	}
	
	public String getAllCreators() throws Exception {
		String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
				+ "SELECT ?uri ?name  WHERE {"
				+ " "
				+ "?uri <foaf:name> ?name  ."
				+"} ";
		String responseText = DataAccess.getResponse(_userName, _password,
				query);
		
		String responseJSON = convertToJson(responseText);
		return responseJSON;
	}
	
	public String getProjectInfo() throws Exception{
		String query = "SELECT ?s ?p ?o  WHERE {"
				+ " ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://cet.ncsa.uiuc.edu/2007/mmdb/Configuration> ."
				+ "?s ?p ?o"
				+"} ";
		String responseText = DataAccess.getResponse(_userName, _password,
				query);
		
		String responseJSON = convertToJson(responseText);
		return responseJSON;
	}

	private String sortItems(String jsonResponse) {
		try {
			Gson gson = new Gson();
			MainCollection collectionsResult = gson.fromJson(jsonResponse, MainCollection.class); 
			Collections.sort(collectionsResult.getSparql().getResults().getResult());
			return gson.toJson(collectionsResult, MainCollection.class);
		} catch (Exception e) {
			return jsonResponse;
		}
	}


	private String convertToJson(String responseText) throws JSONException {

		if (responseText.contains("&")) {
			responseText = responseText.replace("&", "and");
		}
		//System.out.println(responseText);
		JSONObject jsonObject = XML.toJSONObject(responseText);
		//System.out.println(jsonObject.toString());
System.out.println(jsonObject.toString());
		return jsonObject.toString();
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
