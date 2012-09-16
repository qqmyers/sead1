package org.rpi.nced.proxy;

import org.rpi.nced.dao.DataAccess;

public class NCEDProxy {

	static Object padlock = new Object();
	
	static NCEDProxy _instance;
	
	public static NCEDProxy getInstance(){
		if(_instance == null){
			_instance = new NCEDProxy();
		}
		return _instance;
	}
	
	private NCEDProxy(){
		
	}
	
	public String getContents(String userName, String password, String tagID) throws Exception {
		String query = "SELECT ?tagID ?title WHERE { <"
				+ tagID
				+ "> <http://purl.org/dc/terms/hasPart> ?tagID .	?tagID <http://purl.org/dc/elements/1.1/title> ?title . }";
		String responseText = DataAccess.getResponse(userName, password, query);
		/*HttpURLConnection conn = null;

		// Make a connect to the server
		URL url = new URL("http://sead.ncsa.illinois.edu/nced/resteasy/sparql");

		// TODO: Check for credentials

		conn = (HttpURLConnection) url.openConnection();

		// Put the authentication details in the request
		String up = "govinr2@rpi.edu" + ":" + "C0pper";

		String encodedUsernamePassword = Base64.encodeToString(up.getBytes(),
				Base64.NO_WRAP);

		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Authorization", "Basic "
				+ encodedUsernamePassword);
		// make it a post request
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");

		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");

		OutputStream outputStream = conn.getOutputStream();

		// // write a query to contact the server
		// String temp =
		// "query=PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/> SELECT ?c ?t WHERE { ?c <rdf:type> <cet:Collection> . ?c <dc:title> ?t . }";
		outputStream.write(query.getBytes());

		outputStream.flush();
		outputStream.close();
		System.out.println("Going to fetch response");

		// Ensure we got the HTTP 200 response code
		int responseCode = conn.getResponseCode();
		switch (responseCode) {
		case HttpServletResponse.SC_OK:
			break;
		case HttpServletResponse.SC_UNAUTHORIZED:
			throw new HTTPException(responseCode);
		default:
			break;
		}

		InputStream _inputStream;
		// Read the response
		_inputStream = conn.getInputStream();

		Scanner responseScanner = new Scanner(_inputStream);
		String responseText = responseScanner.useDelimiter("\\A").next();
		System.out.println(responseText);
		responseScanner.close();

		_inputStream.close();*/

		return responseText;

	}

	public String getAllCollections(String userName, String password) throws Exception {
		// TODO: Complete this method...
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/> Prefix tag: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/> SELECT ?tagID ?title WHERE { ?tagID <rdf:type> <cet:Collection> . ?tagID <tag:taggedWithTag> ?t . ?tagID <dc:title> ?title . }";
		String responseText = DataAccess.getResponse(userName, password, query);
		/*HttpURLConnection conn = null;

		// Make a connect to the server
		URL url = new URL("http://sead.ncsa.illinois.edu/nced/resteasy/sparql");

		// TODO: Check for credentials

		conn = (HttpURLConnection) url.openConnection();

		// Put the authentication details in the request
		String up = "govinr2@rpi.edu" + ":" + "C0pper";

		String encodedUsernamePassword = Base64.encodeToString(up.getBytes(),
				Base64.NO_WRAP);

		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Authorization", "Basic "
				+ encodedUsernamePassword);
		// make it a post request
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");

		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");

		OutputStream outputStream = conn.getOutputStream();

		// // write a query to contact the server
		// String temp =
		// "query=PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/> SELECT ?c ?t WHERE { ?c <rdf:type> <cet:Collection> . ?c <dc:title> ?t . }";
		outputStream.write(query.getBytes());

		outputStream.flush();
		outputStream.close();
		System.out.println("Going to fetch response");

		// Ensure we got the HTTP 200 response code
		int responseCode = conn.getResponseCode();
		switch (responseCode) {
		case HttpServletResponse.SC_OK:
			break;
		case HttpServletResponse.SC_UNAUTHORIZED:
			throw new HTTPException(responseCode);
		default:
			break;
		}

		InputStream _inputStream;
		// Read the response
		_inputStream = conn.getInputStream();

		Scanner responseScanner = new Scanner(_inputStream);
		String responseText = responseScanner.useDelimiter("\\A").next();
		System.out.println(responseText);
		responseScanner.close();

		_inputStream.close();*/

		return responseText;
	}

	public void Authenticate(String userName, String password) throws Exception {
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/> SELECT ?c ?t WHERE { ?c <rdf:type> <cet:Collection> . ?c <dc:title> ?t . }";
		DataAccess.getResponse(userName, password, query);
	}
}
