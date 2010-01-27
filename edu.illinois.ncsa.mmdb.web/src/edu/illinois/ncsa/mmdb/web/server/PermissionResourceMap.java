package edu.illinois.ncsa.mmdb.web.server;

import java.util.HashMap;
import java.util.Map;

import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

/**
 * Map between elements of an enum and resources representing a specific
 * permission. In the future this can be extended to load the map from an
 * external resource, be it a file or a Tupelo context.
 * 
 * @author Luigi Marini
 * 
 */
public class PermissionResourceMap {

	private static Map<Permission, Resource> map;

	static {
		map = new HashMap<Permission, Resource>();
		map.put(Permission.VIEW_ADMIN_PAGES, MMDB.VIEW_ADMIN_PAGES);
		map.put(Permission.VIEW_MEMBER_PAGES, MMDB.VIEW_MEMBER_PAGES);
	}

	public static final Resource getResource(Permission permission) {
		return map.get(permission);
	}
}
