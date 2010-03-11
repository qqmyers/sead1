package edu.illinois.ncsa.mmdb.web.client.dispatch;


@SuppressWarnings("serial")
public class GetPreviews extends SubjectAction<GetPreviewsResult> {
	public static final String SMALL = "small";
	public static final String LARGE = "large";
	public static final String BADGE = "badge"; // collection badge

	public GetPreviews() { }
	
	public GetPreviews(String uri) {
		super(uri);
	}
	
	/** @deprecated use getUri */
	public String getDatasetUri() {
		return getUri();
	}
}
