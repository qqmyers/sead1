package org.sead.acr.discovery;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;


import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.sead.acr.common.utilities.Queries;

public class Content extends SparqlQueryServlet {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	protected String getQuery(String tagID) {
		return Queries.PROJECT_INFO;
	}

	protected void handleResult(String responseJson,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		String domain = PropertiesLoader.getProperties().getProperty("domain");
		request.setAttribute("medici", domain);
		request.setAttribute("projectInfo", responseJson);
		request.setAttribute("isAnonymous", super.isAnonymous());
		setRedirectResource("/contents.jsp");
	}
	
	protected void handleHTTPException(HTTPException he,
			HttpServletResponse response) throws IOException {
		super.handleHTTPException(he, response);
		setRedirectResource("/login");
	}

}