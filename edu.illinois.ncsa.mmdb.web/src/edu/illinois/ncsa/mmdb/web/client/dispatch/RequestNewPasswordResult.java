/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

/**
 * @author lmarini
 * 
 */
@SuppressWarnings("serial")
public class RequestNewPasswordResult implements Result {
	
	private boolean error;
	private String message;

	public RequestNewPasswordResult() {
	}
	
	public RequestNewPasswordResult(boolean error, String message) {
		this.error = error;
		this.message = message;
	}

	/**
	 * @return the error
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
}
