/**
 * 
 */
package org.sead.acr.discovery;

/**
 * @author Jim
 *
 */
import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.io.FileUtils;
import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.sead.acr.common.utilities.Queries;

public class Home extends SparqlQueryServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */

	protected String getQuery(String tagID) {
		return Queries.PROJECT_INFO;
	}

	protected void handleResult(String responseJson,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// FIXME - Once we can get project info from Medici w/o credentials,
		// this won't be needed
		// Cache projectInfo for use by login page (before authentication)
		String projInfoFilename = getServletContext().getRealPath(
				"projectInfo.json");
		File projFile = new File(projInfoFilename);
		FileUtils.writeStringToFile(projFile, responseJson);

		String domain = PropertiesLoader.getProperties().getProperty("domain");
		request.setAttribute("medici", domain);
		request.setAttribute("projectInfo", responseJson);
		request.setAttribute("isAnonymous", super.isAnonymous());

		setRedirectResource("/home.jsp");
	}

	protected void handleHTTPException(HTTPException he,
			HttpServletResponse response) throws IOException {
		super.handleHTTPException(he, response);
		setRedirectResource("/login");
	}
}
