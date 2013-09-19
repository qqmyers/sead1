package org.sead.acr.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.sead.acr.common.utilities.Base64;
import org.sead.acr.common.utilities.PropertiesLoader;

public class DataAccess {

	public static String getResponse(String userName, String password,
			String server, String remoteAPIKey, String query) throws Exception {
		HttpURLConnection conn = null;
		// Make a connect to the server
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

		// make it a post request
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");

		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		conn.setRequestProperty("Accept", "text/xml");
		String prepend = "";
		
		if((remoteAPIKey != null)&&(remoteAPIKey.length()!=0)) {
			prepend="remoteAPI=" + remoteAPIKey + "&";
		}

		OutputStream outputStream = conn.getOutputStream();
		// write a query to contact the server
		prepend = 	prepend + "query=";
		query = prepend + query;
		outputStream.write(query.getBytes());

		outputStream.flush();
		outputStream.close();
		// System.out.println("Going to fetch response");

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

		return responseText;

	}
}
