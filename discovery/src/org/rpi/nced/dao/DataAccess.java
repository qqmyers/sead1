package org.rpi.nced.dao;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.rpi.nced.utilties.Base64;



public class DataAccess {

	public static String getResponse(String userName, String password, String server,
			String query) throws Exception {
		HttpURLConnection conn = null;

		//String strURL = PropertiesLoader.getProperties().getProperty("domain");
		// Make a connect to the server
		URL url = new URL(server);

		conn = (HttpURLConnection) url.openConnection();

		// Put the authentication details in the request
		String userCredentials = userName + ":" + password;
		String encodedUsernamePassword = Base64.encodeToString(userCredentials.getBytes(),
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
		conn.setRequestProperty("Accept", "text/xml");

		OutputStream outputStream = conn.getOutputStream();

		// write a query to contact the server
		query = "query=" + query;
		outputStream.write(query.getBytes());

		outputStream.flush();
		outputStream.close();
		//System.out.println("Going to fetch response");

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
		//System.out.println(responseText);
		responseScanner.close();

		_inputStream.close();

		return responseText;

	}
}
