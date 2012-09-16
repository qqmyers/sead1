/**
 * 
 */
package org.rpi.nced.servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.rpi.nced.proxy.NCEDProxy;

/**
 * @author Ram
 * 
 */
public class Login extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7681868283138030739L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String redirectionFile = "";
		try {

			String userName = request.getParameter("userName");
			String password = request.getParameter("password");
			
			NCEDProxy.getInstance().Authenticate(userName, password);			
			redirectionFile = "home.html";
			
		} catch (HTTPException e) {
			if (e.getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
				redirectionFile = "autherror.html";
			}
		} catch (Exception e) {
			e.printStackTrace();
			redirectionFile = "error.html";
		}

		// Also handles error response - If user was not authenticated
		// or if user details could not be loaded
		RequestDispatcher rd = request.getRequestDispatcher(redirectionFile);
		
		rd.forward(request, response);

	}
}
