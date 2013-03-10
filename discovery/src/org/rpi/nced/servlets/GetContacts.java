package org.rpi.nced.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.rpi.nced.proxy.NCEDProxy;

public class GetContacts extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8642016544488942571L;

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
		String redirectionFile = "";
		try {
			String tagID = request.getParameter("tagID");
			String responseXML = NCEDProxy.getInstance().getContacts(tagID);
			PrintWriter pw = response.getWriter();
			response.setContentType("application/json");
			pw.write(responseXML);
			pw.flush();
			pw.close();
		} catch (HTTPException e) {
			if (e.getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
				redirectionFile = "autherror.html";
			}
		} catch (Exception e) {
			e.printStackTrace();
			redirectionFile = "error.html";
		}

		if (redirectionFile != "") {
			// Also handles error response - If user was not authenticated
			// or if user details could not be loaded
			RequestDispatcher rd = request
					.getRequestDispatcher(redirectionFile);

			rd.forward(request, response);
		}
	}

}