/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.bard.jaas.UsernamePasswordContextHandler;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Authenticate;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AuthenticateResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Handles authentication using jaas.
 * 
 * @author Luigi Marini
 * 
 */
public class AuthenticateHandler implements
		ActionHandler<Authenticate, AuthenticateResult> {

	private static final String JAAS_CONFIG = "/jaas.config";
	
	/** Commons logging **/
	private static Log log = LogFactory.getLog(AuthenticateHandler.class);

	@Override
	public AuthenticateResult execute(Authenticate arg0, ExecutionContext arg1)
			throws ActionException {

		String username = arg0.getId();

		String password = arg0.getSecret();

		log.debug("Authenticating '" + username + "'");

		if (username.length() == 0) {
			return new AuthenticateResult(false, "");
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
			} catch (LoginException ex) {
				log.debug("Failed to authenticate '" + username + "'");
				return new AuthenticateResult(false, "");
			}

			return new AuthenticateResult(true, username);

		} catch (URISyntaxException e) {
			e.printStackTrace();
			return new AuthenticateResult(false, "");
		}

	}

	@Override
	public Class<Authenticate> getActionType() {
		return Authenticate.class;
	}

	@Override
	public void rollback(Authenticate arg0, AuthenticateResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
