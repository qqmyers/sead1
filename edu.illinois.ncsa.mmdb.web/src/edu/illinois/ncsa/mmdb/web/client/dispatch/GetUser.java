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
public class GetUser implements Action<GetUserResult>{

	private String emailAddress;

	public GetUser() {
	}
	
	public GetUser(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 * @return the emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}
}
