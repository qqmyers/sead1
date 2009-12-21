package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

public class GetPreviewsResult implements Result {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8437174571690183109L;

	public GetPreviewsResult() { }
	
	List<String> previews;
	
	public GetPreviewsResult(List<String> previews) {
		this.previews = previews;
	}
	
	public List<String> getPreviews() {
		return previews;
	}
}
