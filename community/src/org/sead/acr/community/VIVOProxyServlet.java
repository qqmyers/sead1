/* Copyright 2015 University of Michigan
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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sead.acr.common.utilities.Memoized;

public class VIVOProxyServlet extends HttpServlet {

	/**
	 * This class proxies queries to VIVO to allow an https client to get info
	 * from the http accessible vivo instance. Alternative solution would be to
	 * host vivo via https, but the info is public and it may make sense to
	 * cache results.
	 */
	private static final long serialVersionUID = -8642016544488942571L;

	protected static Log log = LogFactory.getLog(VIVOProxyServlet.class);

	/** query response by query (memoized) */
	private final Map<String, Memoized<String>> queryResponses = new HashMap<String, Memoized<String>>();

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.debug("Redirecting: " + request.getRequestURI() + " "
				+ request.getQueryString());
		final String query = request.getQueryString();

		if (!query.equals("")) {
			Memoized<String> answer = queryResponses.get(query);
			if (answer == null) {
				answer = new Memoized<String>() {
					public String computeValue() {
						URL requestURL = null;
						String theAnswer = "";
						try {
							requestURL = new URL(
									"http://sead-vivo.d2i.indiana.edu:3030/SEAD-VIVO/sparql?"
											+ query);

							// Make a connect to the server
							log.debug("Connecting to: " + requestURL.toString());
							HttpURLConnection conn = null;
							conn = (HttpURLConnection) requestURL
									.openConnection();

							conn.setDoInput(true);
							conn.setUseCaches(false);
							InputStream is = conn.getInputStream();
							theAnswer = IOUtils.toString(is);
							IOUtils.closeQuietly(is);

						} catch (MalformedURLException e) {
							log.error("Bad VIVO query URL");
						} catch (IOException e) {
							log.warn("Could not contact VIVO: "
									+ requestURL.toString());
						}
						return theAnswer;
					}
				};
				answer.setTtl(30000);
				queryResponses.put(query, answer);
			}

			IOUtils.write(answer.getValue(), response.getOutputStream());

		}
	}
}
