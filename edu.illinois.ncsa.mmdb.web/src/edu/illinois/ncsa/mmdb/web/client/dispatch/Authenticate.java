/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * @author lmarini
 *
 */
public class Authenticate implements Action<AuthenticateResult> {

	private static final long serialVersionUID = -8430645267773291489L;
	private String id;
	private String secret;

	public Authenticate() {}
	
	public Authenticate(String id, String secret) {
		this.id = id;
		this.secret = secret;
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @return the secret
	 */
	public String getSecret() {
		return secret;
	}

	
}
