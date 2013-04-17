package org.rpi.nced.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.rpi.nced.proxy.NCEDProxy;
import org.rpi.nced.utilties.PropertiesLoader;
import org.rpi.nced.utilties.json.JSONArray;
import org.rpi.nced.utilties.json.JSONObject;

public class Summary extends HttpServlet {

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
			
			String userName = "malviyas@indiana.edu";
			String password = "nothing123";
			
			NCEDProxy.getInstance().Authenticate(userName, password);			
			
			/*Cookie cookie = new Cookie(userName, password);
			response.addCookie(cookie);*/
			
			NCEDProxy.getInstance().setUserName(userName);
			NCEDProxy.getInstance().setPassword(password);
			
			String collections = NCEDProxy.getInstance().getAllCollections();
			String recentUploads = NCEDProxy.getInstance().getRecentUploads();
			String creators = NCEDProxy.getInstance().getAllCreators();
			String projectInfo = NCEDProxy.getInstance().getProjectInfo();
			String datasets = NCEDProxy.getInstance().getAllDatasets();
			
			JSONObject obj = new JSONObject(datasets);
			JSONArray resultArray = obj.getJSONObject("sparql").getJSONObject("results").getJSONArray("result");
			Map<String, Integer> map = new HashMap<String, Integer>();
			for(int i=0; i< resultArray.length(); i++){
				if(resultArray.getJSONObject(i).getJSONObject("binding").getString("literal").split("\\.").length==2) {
					String fileExt = resultArray.getJSONObject(i).getJSONObject("binding").getString("literal").split("\\.")[1];
					String mimeType = org.rpi.nced.utilties.MimeMap.findCategory(fileExt);
					if(map.get(mimeType)!=null){
						map.put(mimeType, map.get(mimeType)+1);
					}else{
						map.put(mimeType, 1);
					}
				}
			}
			String datasetDistribution = map.toString();
			/*String collections = "";
			String recentUploads = "";
			String creators = "";*/
			String projectPath = PropertiesLoader.getProperties().getProperty("projectPath");
			request.setAttribute("projectPath", projectPath);
			request.setAttribute("collections", collections);
			request.setAttribute("recentUploads", recentUploads);
			request.setAttribute("creators", creators);
			request.setAttribute("projectInfo", projectInfo);
			request.setAttribute("datasetDistribution", datasetDistribution);
			
			String nextJSP = "/jsp/summary.jsp";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJSP);
			dispatcher.forward(request,response);
			
		} catch (HTTPException e) {
			if (e.getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Unauthorized");
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
