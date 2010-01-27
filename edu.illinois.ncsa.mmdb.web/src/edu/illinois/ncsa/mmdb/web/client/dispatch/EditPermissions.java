/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;
import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;

/**
 * @author lmarini
 *
 */
@SuppressWarnings("serial")
public class EditPermissions implements Action<EditPermissionsResult> {
	
	private String user;
	private Permission permission;
	private PermissionActionType type;

	public enum PermissionActionType {
		ADD, REMOVE
	}
	
	public EditPermissions() {
	}
	
	public EditPermissions(String user, Permission permission, PermissionActionType type) {
		this.user = user;
		this.permission = permission;
		this.type = type;
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

	/**
	 * @return the type
	 */
	public PermissionActionType getType() {
		return type;
	}
}
