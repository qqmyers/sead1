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
import edu.illinois.ncsa.mmdb.web.server.Authentication;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Handles authentication using jaas.
 * 
 * @author Luigi Marini
 * 
 */
public class AuthenticateHandler implements
		ActionHandler<Authenticate, AuthenticateResult> {
	
	@Override
	public AuthenticateResult execute(Authenticate arg0, ExecutionContext arg1)
			throws ActionException {

		String username = arg0.getId();

		String password = arg0.getSecret();
		
		if((new Authentication()).authenticate(username, password)) {
			return new AuthenticateResult(true, username);
		} else {
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
