package org.rpi.nced.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.rpi.nced.proxy.NCEDProxy;
import org.rpi.nced.utilties.Queries;


public class GetDescriptors extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2211240881166296790L;

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
			String responseXML = NCEDProxy.getInstance().getJSONResponse(Queries.getItemDescriptors(tagID));

			PrintWriter pw = response.getWriter();
			response.setContentType("application/json");
			pw.write(responseXML);
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
