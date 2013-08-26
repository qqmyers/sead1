/**
 * 
 */
package org.sead.acr.brandedrepository;

/**
 * @author Jim
 *
 */
import java.io.File;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.io.FileUtils;
import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.sead.acr.common.utilities.Queries;

public class Home extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */

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

			String projectInfo = mp.getJSONResponse(Queries.PROJECT_INFO);
			// FIXME - Once we can get project info from Medici w/o credentials,
			// this won't be needed
			// Cache projectInfo for use by login page (before authentication)
			String appPath = request.getContextPath();
			String projInfoFilename = getServletContext().getRealPath(
					"projectInfo.json");
			File projFile = new File(projInfoFilename);
			FileUtils.writeStringToFile(projFile, projectInfo);

			String domain = PropertiesLoader.getProperties().getProperty(
					"domain");
			request.setAttribute("medici", domain);
			request.setAttribute("projectInfo", projectInfo);

			String nextJSP = "/home.jsp";
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
