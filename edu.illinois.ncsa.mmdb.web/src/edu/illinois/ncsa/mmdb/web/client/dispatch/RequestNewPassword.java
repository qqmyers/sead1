/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * @author lmarini
 *
 */
@SuppressWarnings("serial")
public class RequestNewPassword implements Action<RequestNewPasswordResult> {

	private String email;

	public RequestNewPassword() {
	}
	
	public RequestNewPassword(String email) {
		this.email = email;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
}
