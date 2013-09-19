package org.sead.acr.common;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpSession;
import javax.xml.ws.http.HTTPException;

import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.Queries;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Common base for servlets that make queries to the ACR resteasy sparql endpoint
 */

/**
 * @author Jim
 * 
 */
public abstract class SparqlQueryServlet extends HttpServlet {

	private String redirectResource = "";
	private String returnCode = "";
	private MediciProxy mp = null;

	protected static Log log = LogFactory.getLog(SparqlQueryServlet.class);

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
		log.debug("Doing Get");

		redirectResource = "";
		try {
			HttpSession session = request.getSession(false);
			mp = (MediciProxy) session.getAttribute("proxy");

			// Get Query from derived class - provide tagID parameter if it
			// exists
			String query = getQuery(getTagID(request));

			// Process Query
			String responseJson = mp.getJSONResponse(query);
			if (responseJson == null) {

				throw new HTTPException(HttpServletResponse.SC_FORBIDDEN);
			}
			handleResult(responseJson, request, response);

		} catch (HTTPException he) {
			handleHTTPException(he, response);
		} catch (Exception e) {
			handleException(e, response);
		}
		// Exception handlers may set a resource to redirect to
		if (redirectResource != "") {
			// Also handles error response - If user was not authenticated
			// or if user details could not be loaded
			log.debug("Forwarded StatusCode: " + returnCode);
			request.setAttribute("statusCode", returnCode);
			log.debug("Forwarding to: " + redirectResource);
			RequestDispatcher rd = request
					.getRequestDispatcher(redirectResource);

			rd.forward(request, response);
		} else {
			response.flushBuffer();
		}
	}

	protected String getTagID(HttpServletRequest request) {
		return request.getParameter("tagID");
	}

	protected abstract String getQuery(String tagID);

	protected void handleResult(String responseJson,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// Return results if no exceptions occurred
		PrintWriter pw = response.getWriter();
		response.setContentType("application/json");
		pw.write(responseJson);
		pw.flush();
		pw.close();
	}

	protected void handleHTTPException(HTTPException he,
			HttpServletResponse response) throws IOException {
		if (he.getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			returnCode = Integer.toString(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Unauthorized");

		} else if (he.getStatusCode() == HttpServletResponse.SC_FORBIDDEN) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			returnCode = Integer.toString(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().write("Forbidden");

		}

	}

	protected void handleException(Exception e, HttpServletResponse response)
			throws IOException {
		e.printStackTrace();
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.getWriter().write("error");
		returnCode = Integer.toString(HttpServletResponse.SC_BAD_REQUEST);
		
	}

	protected void setRedirectResource(String redirect) {
		redirectResource = redirect;
	}

	protected boolean isAnonymous() {
		if (mp != null) {
			return mp.isAnonymous();
		} else {
			return (false);
		}
	}
}
