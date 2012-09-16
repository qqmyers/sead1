package org.rpi.nced.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.rpi.nced.proxy.NCEDProxy;

public class Contents extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5202731150389612465L;

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
			String userName = "govinr2@rpi.edu";
			String password = "C0pper";
			String tagID = request.getParameter("tagID");
			String responseXML = NCEDProxy.getInstance().getContents(userName,
					password, tagID);

			PrintWriter pw = response.getWriter();
			response.setContentType("text/xml");
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
			response.sendRedirect(response
					.encodeRedirectURL("http://localhost:8080/nced_medici/"
							+ redirectionFile));
		}
	}

}
