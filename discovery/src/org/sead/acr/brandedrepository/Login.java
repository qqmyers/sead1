/**
 * 
 */
package org.sead.acr.brandedrepository;

/**
 * @author Jim
 *
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.io.IOUtils;
import org.sead.acr.common.utilities.PropertiesLoader;

public class Login extends HttpServlet {

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
			// FIXME - read directly from Medici once projectInfo is anonymously
			// available
			String projInfoFilename = getServletContext().getRealPath(
					"projectInfo.json");

			String projectInfo = "No info about Project";
			File projFile = new File(projInfoFilename);
			if (!projFile.exists())
				projFile.createNewFile();
			FileInputStream inputStream = new FileInputStream(projInfoFilename);
			try {
				projectInfo = IOUtils.toString(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				inputStream.close();
			}

			String domain = PropertiesLoader.getProperties().getProperty(
					"domain");
			request.setAttribute("medici", domain);
			request.setAttribute("projectInfo", projectInfo);

			String nextJSP = "/login.jsp";
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
