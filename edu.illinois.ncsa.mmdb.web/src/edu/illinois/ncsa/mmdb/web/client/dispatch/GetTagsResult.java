package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Retrieve tags for a particular resource.
 * 
 * @author Luigi Marini
 *
 */
public class GetTagsResult implements Result {

	private static final long serialVersionUID = -1166225700734328325L;
	private List<String> tags;

	/**
	 * For serialization only.
	 */
	public GetTagsResult() {}

	public GetTagsResult(List<String> tags) {
		this.tags = tags;
	}

	public List<String> getTags() {
		return tags;
	}
	
}
