package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

public class GetUploadDestinationResult implements Result {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4928837325250617988L;
	String historyToken;

	public String getHistoryToken() {
		return historyToken;
	}

	public void setHistoryToken(String historyToken) {
		this.historyToken = historyToken;
	}
	
	
}
