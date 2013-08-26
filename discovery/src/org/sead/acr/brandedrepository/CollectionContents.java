package org.sead.acr.brandedrepository;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.http.HTTPException;

import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.Queries;

public class CollectionContents extends HttpServlet {

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

		try {
			String tagID = request.getParameter("tagID");
			HttpSession session = request.getSession(false);
			MediciProxy mp = (MediciProxy) session.getAttribute("proxy");
			String responseJson = mp.getJSONResponse(Queries.getCollectionContents(tagID));

			PrintWriter pw = response.getWriter();
			response.setContentType("application/json");
			pw.write(responseJson);
			pw.flush();
			pw.close();
		} catch (HTTPException e) {
			if (e.getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Unauthorized");
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
