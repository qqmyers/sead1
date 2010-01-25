package edu.illinois.ncsa.mmdb.web.client.dispatch;


@SuppressWarnings("serial")
public class GetPreviews extends SubjectAction<GetPreviewsResult> {
	public static final String SMALL = "small";
	public static final String LARGE = "large";

	public GetPreviews() { }
	
	public GetPreviews(String datasetUri) {
		super(datasetUri);
	}
	
	/** @deprecated use getUri */
	public String getDatasetUri() {
		return getUri();
	}
}
