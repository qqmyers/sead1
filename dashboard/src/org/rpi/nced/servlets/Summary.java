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
import org.rpi.nced.utilties.Queries;
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
			String username = "none";
			String password = "none";
		try {
			//FIXME - do not store/get username/password form file
			username = PropertiesLoader.getProperties().getProperty("username");
			password = PropertiesLoader.getProperties().getProperty("password");
			
			NCEDProxy.getInstance().setCredentials(username, password);			
			
			/*Cookie cookie = new Cookie(userName, password);
			response.addCookie(cookie);*/
			
			String collections = NCEDProxy.getInstance().getJSONResponse(Queries.ALL_TOPLEVEL_COLLECTIONS);
			String recentUploads = NCEDProxy.getInstance().getJSONResponse(Queries.RECENT_UPLOADS);
			String creators = NCEDProxy.getInstance().getJSONResponse(Queries.TEAM_MEMBERS);
			String projectInfo = NCEDProxy.getInstance().getJSONResponse(Queries.PROJECT_INFO);
			String datasets = NCEDProxy.getInstance().getJSONResponse(Queries.ALL_DATASETS);
			
			System.out.println(datasets);
			JSONObject obj = new JSONObject(datasets);
			Map<String, Integer> map = new HashMap<String, Integer>();
			
			try {
			JSONArray resultArray = obj.getJSONObject("sparql").getJSONObject("results").getJSONArray("result");
				for(int i=0; i< resultArray.length(); i++){
					try{
						String filename=resultArray.getJSONObject(i).getJSONObject("binding").getString("literal");
						String nameParts[];
						nameParts= filename.split("\\.");
						if(nameParts.length==2) {
							String fileExt = nameParts[1];
							String mimeType = org.rpi.nced.utilties.MimeMap.findCategory(fileExt);
							if(map.get(mimeType)!=null){
								map.put(mimeType, map.get(mimeType)+1);
							}else{	
								map.put(mimeType, 1);
							}
						}		
						//Add filenames with no extension as unknown mime types? Use mimetype info in Medici rather than rederiving ferom file extension?
					}catch (org.rpi.nced.utilties.json.JSONException je) {
					}
			}	
			}catch (org.rpi.nced.utilties.json.JSONException je) {
				//There are zero or one entries - for now we'll just leave the graph blank
			} catch (Exception e) {
				e.printStackTrace();
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
				e.printStackTrace();
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Unauthorized: " + username + "  " + password);
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
