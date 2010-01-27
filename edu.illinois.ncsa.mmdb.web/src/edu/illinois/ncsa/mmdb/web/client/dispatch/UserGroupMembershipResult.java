/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

/**
 * @author lmarini
 *
 */
@SuppressWarnings("serial")
public class UserGroupMembershipResult implements Result {

	private ArrayList<String> members;
	
	private ArrayList<String> groups;
	
	public UserGroupMembershipResult() {
	}
	
	public UserGroupMembershipResult(ArrayList<String> members, ArrayList<String> groups) {
		this.members = members;
		this.groups = groups;
	}

	/**
	 * @param members the members to set
	 */
	public void setMembers(ArrayList<String> members) {
		this.members = members;
	}

	/**
	 * @return the members
	 */
	public ArrayList<String> getMembers() {
		return members;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(ArrayList<String> groups) {
		this.groups = groups;
	}

	/**
	 * @return the groups
	 */
	public ArrayList<String> getGroups() {
		return groups;
	}
}
