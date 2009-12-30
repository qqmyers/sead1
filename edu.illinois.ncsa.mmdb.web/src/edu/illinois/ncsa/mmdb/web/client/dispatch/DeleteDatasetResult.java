package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

public class DeleteDatasetResult implements Result {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3379995469712712669L;
	
	boolean isDeleted;

	public DeleteDatasetResult() { }
	
	public DeleteDatasetResult(boolean isDeleted) {
		setDeleted(isDeleted);
	}
	
	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}
