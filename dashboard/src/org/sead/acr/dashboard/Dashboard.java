package org.sead.acr.dashboard;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.http.HTTPException;

import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.MimeMap;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.sead.acr.common.utilities.Queries;
import org.sead.acr.common.utilities.json.JSONArray;
import org.sead.acr.common.utilities.json.JSONObject;

public class Dashboard extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2553450304238410849L;

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

		try {
			HttpSession session = request.getSession(false);
			MediciProxy mp = (MediciProxy) session.getAttribute("proxy");
			String collections = mp
					.getJSONResponse(Queries.ALL_TOPLEVEL_COLLECTIONS);
			String recentUploads = mp.getJSONResponse(Queries.RECENT_UPLOADS);
			String creators = mp.getJSONResponse(Queries.TEAM_MEMBERS);
			String projectInfo = mp.getJSONResponse(Queries.PROJECT_INFO);
			String datasets = mp.getJSONResponse(Queries.ALL_DATASETS);

			JSONObject obj = new JSONObject(datasets);
			Map<String, Integer> map = new HashMap<String, Integer>();

			try {
				JSONArray resultArray = obj.getJSONObject("sparql")
						.getJSONObject("results").getJSONArray("result");
				for (int i = 0; i < resultArray.length(); i++) {
					try {
						String filename = resultArray.getJSONObject(i)
								.getJSONObject("binding").getString("literal");
						String nameParts[];
						nameParts = filename.split("\\.");
						if (nameParts.length == 2) {
							String fileExt = nameParts[1];
							String mimeType = MimeMap.findCategory(fileExt);
							if (map.get(mimeType) != null) {
								map.put(mimeType, map.get(mimeType) + 1);
							} else {
								map.put(mimeType, 1);
							}
						}
						// Add filenames with no extension as unknown mime
						// types? Use mimetype info in Medici rather than
						// re-deriving from file extension?
					} catch (org.sead.acr.common.utilities.json.JSONException je) {
						// Why catch and ignore?
					}
				}
			} catch (org.sead.acr.common.utilities.json.JSONException je) {
				// There are zero or one entries - for now we'll just leave the
				// graph blank
			}
			String datasetDistribution = map.toString();
			/*
			 * String collections = ""; String recentUploads = ""; String
			 * creators = "";
			 */
			String projectPath = PropertiesLoader.getProperties().getProperty(
					"domain");
			request.setAttribute("projectPath", projectPath);
			request.setAttribute("collections", collections);
			request.setAttribute("recentUploads", recentUploads);
			request.setAttribute("creators", creators);
			request.setAttribute("projectInfo", projectInfo);
			request.setAttribute("datasetDistribution", datasetDistribution);

			String nextJSP = "/jsp/dashboard.jsp";
			RequestDispatcher dispatcher = getServletContext()
					.getRequestDispatcher(nextJSP);
			dispatcher.forward(request, response);
		} catch (HTTPException e) {
			if (e.getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
				e.printStackTrace();
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.flushBuffer();
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("error");
			response.flushBuffer();
		}
	}
}
