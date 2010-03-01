package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

public class DeleteAnnotationResult implements Result {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1176911278374742966L;
	
	boolean isDeleted;

	public DeleteAnnotationResult() { }
	
	public DeleteAnnotationResult(boolean isDeleted) {
		setDeleted(isDeleted);
	}
	
	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}
