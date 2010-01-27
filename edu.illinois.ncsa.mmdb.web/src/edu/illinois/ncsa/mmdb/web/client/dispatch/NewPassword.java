/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class NewPassword implements Action<EmptyResult> {
	
	private String user;
	private String password;

	public NewPassword() {}
	
	public NewPassword(String user, String password) {
		this.user = user;
		this.password = password;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
}
