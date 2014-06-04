package org.sead.acr.community;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sead.acr.common.DataAccess;
import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.sead.acr.common.utilities.Queries;

public class GetProjectInfo extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8642016544488942571L;

	protected static Log log = LogFactory.getLog(GetProjectInfo.class);

	public void init(ServletConfig config) throws ServletException {
		// FixMe move to filter?
		// Get Property file parameter
		String propFile = config.getInitParameter("PropertiesFileName");
		PropertiesLoader.getProperties(propFile);

		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.debug("Doing Get");
		String server = request.getParameter("server");
		String responseJson = null;
		try {

			DataAccess authGetDA = DataAccess
					.buildUnauthenticatedJsonGETResponseDataAccess(server
							+ "/resteasy/sys/config");
			authGetDA.setBasicCreds("anonymous", "none");
			authGetDA.setUseBasicAuth(true);
			authGetDA.setRemoteAPIKey(Home.getServerKey(server));
			responseJson = authGetDA.getResponse(null);

			// responseJson = mp.executeAuthenticatedGet("/resteasy/sys/config",
			// null);
		} catch (IOException io) { // File Not Found when DataAccess can't get
									// info from server (no endpoint)
			response.setStatus(403);
		} catch (HTTPException he) { // when DataAccess can't get info from
										// server (403)
			response.setStatus(403);
		}

		if (responseJson != null) {
			PrintWriter pw = response.getWriter();
			response.setContentType("application/json");
			pw.write(responseJson);
			pw.flush();
			pw.close();
		}
	}

}
