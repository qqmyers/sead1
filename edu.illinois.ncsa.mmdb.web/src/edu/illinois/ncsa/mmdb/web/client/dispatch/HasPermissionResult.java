/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

/**
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class HasPermissionResult implements Result {
	
	private boolean permitted;

	public HasPermissionResult() {
	}
	
	public HasPermissionResult(boolean permitted) {
		this.permitted = permitted;
	}

	/**
	 * @return the result
	 */
	public boolean isPermitted() {
		return permitted;
	}

}
