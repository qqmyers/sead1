package org.rpi.nced.proxy;

import org.rpi.nced.dao.DataAccess;

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

	public String getContents(String tagID) throws Exception {
		String query = "SELECT ?tagID ?title ?length WHERE { <"
				+ tagID
				+ "> <http://purl.org/dc/terms/hasPart> ?tagID .	?tagID <http://purl.org/dc/elements/1.1/title> ?title . OPTIONAL { ?tagID <tag:tupeloproject.org,2006:/2.0/files/length> ?length .} }";
		String responseText = DataAccess.getResponse(_userName, _password,
				query);

		return responseText;

	}

	public String getAllCollections() throws Exception {
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/> Prefix tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/> SELECT ?tagID ?title WHERE { ?tagID <rdf:type> <cet:Collection> . ?tagID <tag:taggedWithTag> ?t . ?tagID <dc:title> ?title . }";
		String responseText = DataAccess.getResponse(_userName, _password,
				query);

		return responseText;
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
}
