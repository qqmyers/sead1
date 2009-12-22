package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

public class GetPreviews implements Action<GetPreviewsResult> {
	public static final String SMALL = "small";
	public static final String LARGE = "large";

	/**
	 * 
	 */
	private static final long serialVersionUID = -1275298870501863076L;

	public GetPreviews() { }
	
	String datasetUri;
	
	public GetPreviews(String datasetUri) {
		this.datasetUri = datasetUri;
	}
	
	public String getDatasetUri() {
		return datasetUri;
	}
}
