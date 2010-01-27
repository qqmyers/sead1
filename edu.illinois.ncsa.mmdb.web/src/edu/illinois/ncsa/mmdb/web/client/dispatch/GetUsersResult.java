/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.PersonBean;

/**
 * List of users in the system.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class GetUsersResult implements Result {
	
	private ArrayList<PersonBean> users;
	
	public GetUsersResult() {
	}
	
	public GetUsersResult(ArrayList<PersonBean> users) {
		this.users = users;
	}

	/**
	 * @return the users
	 */
	public ArrayList<PersonBean> getUsers() {
		return users;
	}
}
