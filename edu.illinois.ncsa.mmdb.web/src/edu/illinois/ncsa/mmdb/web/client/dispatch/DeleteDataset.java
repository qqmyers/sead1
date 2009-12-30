package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

public class DeleteDataset implements Action<DeleteDatasetResult> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8172249862839242401L;
	
	private String uri;
	
	public DeleteDataset() {}
	
	public DeleteDataset(String uri) {
		this.setUri(uri);
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

}
