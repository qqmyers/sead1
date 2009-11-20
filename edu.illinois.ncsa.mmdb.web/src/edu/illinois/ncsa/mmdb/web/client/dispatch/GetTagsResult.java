package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Set;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Retrieve tags for a particular resource.
 * 
 * @author Luigi Marini
 *
 */
public class GetTagsResult implements Result {

	private static final long serialVersionUID = -1166225700734328325L;
	private Set<String> tags;

	/**
	 * For serialization only.
	 */
	public GetTagsResult() {}

	public GetTagsResult(Set<String> tags) {
		this.tags = tags;
	}

	public Set<String> getTags() {
		return tags;
	}
	
}
