/*******************************************************************************
 * Copyright 2014 University of Michigan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.sead.acr.community;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Home extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Properties _serverMap = null;
	private String _serverFile = "servermap.properties";

	/**
	 * 
	 */

	protected static Log log = LogFactory.getLog(Home.class);

	public void init(ServletConfig config) throws ServletException {
		String filename = config.getServletContext().getInitParameter(
				"ServerListFile");
		if (filename != null)
			_serverFile = filename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.debug("Doing Get");

		String projects = getServerList().toString();

		request.setAttribute("projects", projects);
		//RequestDispatcher rd = request.getRequestDispatcher("/home.jsp");
		RequestDispatcher rd = request.getRequestDispatcher("/home.jsp");

		rd.forward(request, response);
	}
	protected org.json.JSONArray getServerList() throws IOException {

		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream(_serverFile);
		_serverMap = new Properties();

		// load the inputStream using the Properties
		_serverMap.load(inputStream);

		inputStream.close();
		log.debug(_serverMap.toString());
		return new org.json.JSONArray(_serverMap.keySet());
	}

	public static String getServerKey(String server) {
		if (_serverMap != null) {
			return _serverMap.getProperty(server);
		} else {
			return null;
		}
	}
}
