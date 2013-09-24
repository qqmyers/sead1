/**
 * 
 */
package org.sead.acr.dashboard;

/**
 * @author Jim
 *
 */
import org.sead.acr.common.SparqlQueryServlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.io.IOUtils;
import org.sead.acr.common.utilities.PropertiesLoader;

public class Login extends SparqlQueryServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */

	/*
	 * If anonymous is allowed, try to login as anonymous and forward to the
	 * home page. If not, or anon doesn't have permission, show login page.)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */

	protected void handleQuery(HttpServletRequest request,
			HttpServletResponse response) throws HTTPException, Exception {

		// FIXME - read directly from Medici with a Query once projectInfo is
		// anonymously
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

		String domain = PropertiesLoader.getProperties().getProperty("domain");
		request.setAttribute("medici", domain);
		request.setAttribute("projectInfo", projectInfo);

		// Report if we're logged in anonymously (and forbidden access)
		String anonymous = PropertiesLoader.getProperties().getProperty(
				"enableAnonymous");
		if ((anonymous != null) && (anonymous.equalsIgnoreCase("true"))) {
			boolean anon = super.isAnonymous();
			request.setAttribute("isAnonymous", anon);
		}

		setRedirectResource("/login.jsp");
	}

	protected String getQuery(String tagID) {
		return null;
	}

}
