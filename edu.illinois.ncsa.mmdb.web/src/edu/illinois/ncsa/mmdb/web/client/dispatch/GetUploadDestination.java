package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

public class GetUploadDestination implements Action<GetUploadDestinationResult> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3006697582357395425L;
	String sessionKey;

	public GetUploadDestination() { }
	
	public GetUploadDestination(String sessionKey) {
		setSessionKey(sessionKey);
	}
	
	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
}
