/**
 * 
 */
package org.sead.acr.community;

/**
 * @author Jim
 *
 */
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.SparqlQueryServlet;
import org.sead.acr.common.utilities.PropertiesLoader;
import org.sead.acr.common.utilities.Queries;

public class Home extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Properties _serverMap = null;
	private String _serverFile = "servermap.properties";

	/**
	 * 
	 */

	protected static Log log = LogFactory.getLog(Home.class);

	public void init(ServletConfig config) throws ServletException {
		String filename = config.getServletContext().getInitParameter(
				"ServerListFile");
		if (filename != null)
			_serverFile = filename;
	}

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

		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream(_serverFile);
		_serverMap = new Properties();

		// load the inputStream using the Properties
		_serverMap.load(inputStream);

		inputStream.close();
		log.debug(_serverMap.toString());
		String projects = "[";
		for (Enumeration<Object> s = _serverMap.keys(); s.hasMoreElements();) {
			String server = (String) (s.nextElement());
			log.debug("Adding: " + server);
			projects = projects + "\"" + server + "\""
					+ (s.hasMoreElements() ? ", " : "");
		}
		projects = projects + "]";

		// String projects=
		// "[\"http://localhost:8080/medici\", \"http://sead.ncsa.illinois.edu/acr\", \"http://nced.ncsa.illinois.edu/acr\", \"http://sead-demo.ncsa.illinois.edu/acr\"]";

		request.setAttribute("projects", projects);
		RequestDispatcher rd = request.getRequestDispatcher("/home.jsp");

		rd.forward(request, response);
	}

	public static String getServerKey(String server) {
		if (_serverMap != null) {
			return _serverMap.getProperty(server);
		} else {
			return null;
		}
	}
}
