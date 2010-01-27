/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

/**
 * @author lmarini
 *
 */
public class Permissions {

	public enum Permission {
		VIEW_MEMBER_PAGES, VIEW_ADMIN_PAGES
	}
	
	public static boolean checkPermission(String userURI, String page, Permission permission) {
		return true;
	}
}
