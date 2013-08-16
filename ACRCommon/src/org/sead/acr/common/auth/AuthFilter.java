package org.sead.acr.common.auth;


import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sead.acr.common.MediciProxy;
import org.sead.acr.common.utilities.PropertiesLoader;

import javax.servlet.http.Cookie;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AuthFilter implements Filter {
	
	private String _propFile;
	
	private static Log  log   = LogFactory.getLog(AuthFilter.class);
	
	public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
 
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);
        String appPath = request.getContextPath();
        String  uri = request.getRequestURI();
        String username = "";
    	String password = "";
		
    	log.debug("URI: " + uri);
    	
    	//Let the login form page and requests for it's images, css files, etc. through...
    	if (!uri.startsWith(appPath + "/login")) {
    		//if logging in
    		if (uri.startsWith(appPath + "/DoLogin")) {
    			log.debug("Attempting to authenticate");
    			//Retrieve form info
    			username=request.getParameter("userName");
    			password = request.getParameter("password");
    	
    			//Get new session upon login (avoiding session fixation attack
    			if (session!=null && !session.isNew()) {
    				session.invalidate();
    			}
				session = request.getSession();

				//Setup MediciProxy to handle future remote requests
				//Find Properties file and retrieve the domain/sparql endpoint of the remote Medici instance
				String server = PropertiesLoader.getProperties(_propFile).getProperty("domain");
    			MediciProxy mp = new MediciProxy();

    			//Try to use/store credentials
    			mp.setCredentials(username, password, server);
    			
    			//See if the credentials worked and were stored
    			if (!mp.hasValidCredentials()) {
    				//If not - report error
    				log.debug("Could not authenticate");
    				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    				response.getWriter().write("Unauthorized");
    				response.flushBuffer();
    				return;
    			} else {
    				//If we have proxy credentials, store them in the session
    				session.setAttribute("proxy", mp);
    				log.debug("Authenticated: " + username);
    				return;
    			}
    		} else if (uri.startsWith(appPath + "/DoLogout")) {
    			log.debug("Logging out");
    			if (session!=null && !session.isNew()) {
    				session.invalidate();
    			}
    			response.sendRedirect(appPath + "/login.html");
				return;
    		} else { //Request is for something other than Login (some other servlet that will also use the code below to retrieve existing credentials)
    			boolean goodCredentials = false;
    			if(session!=null) {
    				MediciProxy mp = (MediciProxy) session.getAttribute("proxy");
    				if(mp!=null) {
    					log.debug("Valid Credentials");
    					//Could test the credentials here, but it's an extra http call off to the server - if they don't work when the servlets use them, we'll know
    					//goodCredentials = mp.hasValidCredentials();w
    					goodCredentials = true;
    				}
    			}	
    			//Redirect to the login form if no credentials
    			if(!goodCredentials) {
    				log.debug("No Credentials for: " + uri);
    				response.sendRedirect(appPath + "/login.html?" + uri.substring(appPath.length()+1));  
    				return;
    			}
    		}
    	}
        chain.doFilter(req, res);
    }

	public void init(FilterConfig config) throws ServletException {
         
        //Get Property file parameter
        _propFile = config.getInitParameter("PropertiesFileName");
         
    }
	
    public void destroy() {
        //add code to release any resource
    }
}
