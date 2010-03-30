package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.LinkedHashMap;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Retrieve tags for a particular resource.
 * 
 * @author Luigi Marini
 *
 */
public class GetTagsResult implements Result {

	private static final long serialVersionUID = -1166225700734328325L;
	private LinkedHashMap<String, Integer> tags;

	/**
	 * For serialization only.
	 */
	public GetTagsResult() {}

	public GetTagsResult(LinkedHashMap<String, Integer> tags) {
		this.tags = tags;
	}

	public LinkedHashMap<String, Integer> getTags() {
		return tags;
	}
	
}
