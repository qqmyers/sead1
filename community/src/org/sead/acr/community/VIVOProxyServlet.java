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

	protected static Log log = LogFactory.getLog(Oauth2Redirect.class);

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
		String query = request.getQueryString();

		HttpURLConnection conn = null;

		URL requestURL;
		if (!query.equals("")) {
			requestURL = new URL(
					"http://sead-vivo.d2i.indiana.edu:3030/SEAD-VIVO/sparql?"
							+ query);

			// Make a connect to the server
			log.debug("Connecting to: " + requestURL.toString());

			conn = (HttpURLConnection) requestURL.openConnection();

			conn.setDoInput(true);
			conn.setUseCaches(false);
			InputStream is = conn.getInputStream();
			IOUtils.copy(is,response.getOutputStream());	
			
		}
	}
}

/*
 * Map<String, String[]> paramMap = request.getParameterMap();
 * 
 * String server = request.getParameter("server"); String query = ""; for
 * (String p: paramMap.keySet()) { if (!p.equals("server")) {
 * if(query.length()==0) { query += "?" + p + "=" + paramMap.get(p)[0]; } else {
 * query += "&" + p + "=" + paramMap.get(p)[0]; } } }
 * response.sendRedirect(_vivoServer + request.getQ); } }
 * 
 * Memoized<Integer> count = datasetCount.get(key); if (count == null) { count =
 * new Memoized<Integer>() { public Integer computeValue() { return
 * countDatasetsInCollectionWithTag(inCollection, withTag); } }; if
 * (inCollection != null || withTag != null) { count.setTtl(10000); } else {
 * count.setTtl(120000); } datasetCount.put(key, count); } return
 * count.getValue(force);
 * 
 * 
 * static Memoized<Integer> collectionCount;
 * 
 * static int getCollectionCount() { if (collectionCount == null) {
 * collectionCount = new Memoized<Integer>() { public Integer computeValue() {
 * Unifier u = new Unifier(); u.setColumnNames("c"); u.addPattern("c", Rdf.TYPE,
 * MMDB.COLLECTION_TYPE); try { long then = System.currentTimeMillis();
 * Table<Resource> result = TupeloStore.getInstance().unifyExcludeDeleted(u,
 * "c"); int count = 0; for (Tuple<Resource> row : result ) { count++; } long ms
 * = System.currentTimeMillis() - then; log.debug("counted " + count +
 * " collection(s) in " + ms + "ms"); return count; } catch (OperatorException
 * e) { e.printStackTrace(); return 0; } } }; collectionCount.setTtl(30000); }
 * return collectionCount.getValue(); }
 */