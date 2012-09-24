package org.rpi.nced.proxy;

import org.rpi.nced.dao.DataAccess;
import org.rpi.nced.utilties.json.JSONException;
import org.rpi.nced.utilties.json.JSONObject;
import org.rpi.nced.utilties.json.XML;

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

	}

	public String getContents(String parentTagID) throws Exception {
		String query = "SELECT ?tagID ?title ?length ?abstract WHERE { <"
				+ parentTagID
				+ "> <http://purl.org/dc/terms/hasPart> ?tagID .	?tagID <http://purl.org/dc/elements/1.1/title> ?title . OPTIONAL { ?tagID <tag:tupeloproject.org,2006:/2.0/files/length> ?length .} OPTIONAL { <"
				+ parentTagID
				+ "> <http://purl.org/dc/terms/abstract> ?abstract . } }";
		String responseText = DataAccess.getResponse(_userName, _password,
				query);

		return convertToJson(responseText);

	}

	public String getAllCollections() throws Exception {
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/> Prefix tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/> SELECT ?tagID ?title ?abstract WHERE { ?tagID <rdf:type> <cet:Collection> . ?tagID <tag:taggedWithTag> ?t . ?tagID <dc:title> ?title . OPTIONAL { ?tagID <http://purl.org/dc/terms/abstract> ?abstract . } }";
		String responseText = DataAccess.getResponse(_userName, _password,
				query);
		return convertToJson(responseText);
	}

	private String convertToJson(String responseText) throws JSONException {

		if (responseText.contains("&")) {
			responseText = responseText.replace("&", "and");
		}

		JSONObject jsonObject = XML.toJSONObject(responseText);
		System.out.println(jsonObject.toString());
		return jsonObject.toString();
	}

	public void Authenticate(String userName, String password) throws Exception {
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/> SELECT ?c ?t WHERE { ?c <rdf:type> <cet:Collection> . ?c <dc:title> ?t . }";
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

	public String getDescriptors(String tagID) throws Exception {
		String query = "SELECT ?name ?descriptor WHERE { <" + tagID
				+ "> <http://purl.org/dc/terms/description> ?descriptor . }";
		String responseText = DataAccess.getResponse(_userName, _password,
				query);
		return convertToJson(responseText);
	}
}
