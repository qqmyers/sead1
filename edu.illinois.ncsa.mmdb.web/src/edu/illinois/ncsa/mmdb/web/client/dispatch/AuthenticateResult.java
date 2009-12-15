/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Returns if the user has been successfully authenticated
 * on the server side and what their session id is.
 * 
 * @author Luigi Marini
 *
 */
public class AuthenticateResult implements Result {

	private static final long serialVersionUID = 7928542556558554697L;

	private boolean authenticated;

	private String sessionId;
	
	public AuthenticateResult() {}
	
	public AuthenticateResult(boolean authenticated, String sessionId) {
		this.authenticated = authenticated;
		this.sessionId= sessionId;
	}
	
	/**
	 * @return the authenticated
	 */
	public boolean getAuthenticated() {
		return authenticated;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

}
