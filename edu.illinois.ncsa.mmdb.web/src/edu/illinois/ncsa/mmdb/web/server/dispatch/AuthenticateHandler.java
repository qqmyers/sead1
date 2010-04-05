/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.mmdb.web.client.dispatch.Authenticate;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AuthenticateResult;
import edu.illinois.ncsa.mmdb.web.server.Authentication;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Authenticate user.
 * 
 * @author Luigi Marini
 * 
 */
public class AuthenticateHandler implements
		ActionHandler<Authenticate, AuthenticateResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(AuthenticateHandler.class);
	
	@Override
	public AuthenticateResult execute(Authenticate arg0, ExecutionContext arg1)
			throws ActionException {

		String username = arg0.getId();

		String password = arg0.getSecret();

		if ((new Authentication()).authenticate(username, password)) {
			log.trace("User successfully authenticated");
			// retrieve full user URI
			// FIXME query rdf in case uri space has changed
			String personID = PersonBeanUtil.getPersonID(username);
			return new AuthenticateResult(true, personID);
		} else {
			log.trace("User failed authentication");
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
