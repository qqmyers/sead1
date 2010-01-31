package edu.illinois.ncsa.mmdb.web.server;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.bard.jaas.UsernamePasswordContextHandler;

public class Authentication {
	private static final String JAAS_CONFIG = "/jaas.config";
	
	/** Commons logging **/
	private static Log log = LogFactory.getLog(Authentication.class);
	
	public boolean authenticate(String username, String password) {
		log.debug("LOGIN: Authenticating '" + username + "' via JAAS");

		if (username.length() == 0) {
			return false;
		}

		URL fileLocation = this.getClass().getResource(JAAS_CONFIG);
		
		try {
			File file = new File(fileLocation.toURI());
			System.setProperty("java.security.auth.login.config", file.getAbsolutePath());

			UsernamePasswordContextHandler handler = new UsernamePasswordContextHandler(
					username, password, TupeloStore.getInstance().getContext());

			Subject subject = new Subject();

			LoginContext ctx = null;

			try {
				ctx = new LoginContext("mmdb", subject, handler);
				ctx.login();
				log.debug("LOGIN: JAAS authentication suceeded for "+username);
			} catch (LoginException ex) {
				log.debug("LOGIN: JAAS authentication FAILED for "+username,ex);
				return false;
			}

			return true;

		} catch (URISyntaxException e) {
			e.printStackTrace();
			return false;
		}
	}
}

