package org.sead.acr.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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

	public static String getXMLPostResponse(String userName, String password,
			String server, String remoteAPIKey, String query)
			throws MalformedURLException, IOException {
		return getResponse(userName, password, server, remoteAPIKey, query,
				POST, XML);
	}

	public static String getJsonGetResponse(String userName, String password,
			String server, String remoteAPIKey, String query)
			throws MalformedURLException, IOException {
		return getResponse(userName, password, server, remoteAPIKey, query,
				GET, JSON);
	}

	public static String getResponse(String userName, String password,
			String server, String remoteAPIKey, String query, String method,
			String returnType) throws MalformedURLException, IOException {
		HttpURLConnection conn = null;

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

		if (method.equals(GET)) {
			if (!query.equals("")) {
				server = server + "?" + query;
			}
		}

		// Make a connect to the server
		log.debug("Connecting to: " + server);
		URL url = new URL(server);

		conn = (HttpURLConnection) url.openConnection();

		// Put the authentication details in the request
		String userCredentials = "";
		if ((userName.length() != 0) && (password.length() != 0)) {
			userCredentials = userName + ":" + password;
		} else {
			// edu.uiuc.ncsa.cet.bean.tupelo.rbac.Anonymous.USER =
			// "http://cet.ncsa.uiuc.edu/2007/person/anonymous", id =
			// "anonymous"
			// using the constant requires 3 extra jar files, so just using
			// value
			userCredentials = "anonymous:none";
		}

		String encodedUsernamePassword = Base64.encodeToString(
				userCredentials.getBytes(), Base64.NO_WRAP);
		conn.setRequestProperty("Authorization", "Basic "
				+ encodedUsernamePassword);
		
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

		InputStream _inputStream;
		// Read the response
		_inputStream = conn.getInputStream();

		Scanner responseScanner = new Scanner(_inputStream);
		String responseText = responseScanner.useDelimiter("\\A").next();
		// System.out.println(responseText);
		responseScanner.close();

		_inputStream.close();
		conn.disconnect();

		return responseText;

	}
}
