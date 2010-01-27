/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Request for a user to be added to a group.
 * 
 * @author Luigi Marini
 * 
 */
@SuppressWarnings("serial")
public class UserGroupMembership implements Action<UserGroupMembershipResult> {

	public enum Action {
	    ADD, REMOVE, GET_GROUPS, GET_MEMBERS
	}
	
	private String userURI;
	private String groupURI;
	private Action action;


	public UserGroupMembership() {
	}

	public UserGroupMembership(String userURI, String groupURI, Action action) {
		this.userURI = userURI;
		this.groupURI = groupURI;
		this.action = action;
	}

	/**
	 * @return the userURI
	 */
	public String getUserURI() {
		return userURI;
	}

	/**
	 * @return the groupURI
	 */
	public String getGroupURI() {
		return groupURI;
	}

	/**
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}
}
