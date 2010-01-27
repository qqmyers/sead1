/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;
import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;

/**
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class HasPermission implements Action<HasPermissionResult> {

	private String user;
	private Permission permission;
	
	public HasPermission() {
	}
	
	public HasPermission(String user, Permission permission) {
		this.user = user;
		this.permission = permission;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @return the permission
	 */
	public Permission getPermission() {
		return permission;
	}
}
