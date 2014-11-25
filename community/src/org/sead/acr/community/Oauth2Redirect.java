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

/*
 * @author myersjd@umich.edu
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

import javax.crypto.spec.IvParameterSpec;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sead.acr.common.DataAccess;
import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.sead.acr.common.utilities.Queries;

public class Oauth2Redirect extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8642016544488942571L;

	protected static Log log = LogFactory.getLog(Oauth2Redirect.class);

	public void init(ServletConfig config) throws ServletException {

		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.debug("Redirecting: " + request.getRequestURI() + " " + request.getQueryString());
		Map<String, String[]> paramMap = request.getParameterMap();
		
		String server = request.getParameter("server");
		String query = "";
		for (String p: paramMap.keySet()) {
			if (!p.equals("server")) {
				if(query.length()==0) {
					query += "?" + p +  "=" + paramMap.get(p)[0];
				} else {
					query += "&" + p + "=" + paramMap.get(p)[0];
				}
			}
		}
		response.sendRedirect(server + "oauth2OrcidWindow.html" + query);
	}
}
