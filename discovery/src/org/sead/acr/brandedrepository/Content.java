package org.sead.acr.brandedrepository;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.http.HTTPException;

import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.sead.acr.common.utilities.Queries;

public class Content extends HttpServlet {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

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

			String domain = PropertiesLoader.getProperties().getProperty(
					"domain");
			request.setAttribute("medici", domain);
			request.setAttribute("projectInfo", projectInfo);

			String nextJSP = "/contents.jsp";
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