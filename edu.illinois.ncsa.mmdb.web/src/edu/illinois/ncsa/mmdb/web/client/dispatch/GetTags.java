package edu.illinois.ncsa.mmdb.web.client.dispatch;


/**
 * Retrieve tags for a particular resource.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class GetTags extends SubjectAction<GetTagsResult> {
	/**
	 * For serialization only.
	 */
	public GetTags() {}
	
	public GetTags(String id) {
		super(id);
	}

	/** @deprecated use getUri */
	public String getId() {
		return getUri();
	}
	
}
