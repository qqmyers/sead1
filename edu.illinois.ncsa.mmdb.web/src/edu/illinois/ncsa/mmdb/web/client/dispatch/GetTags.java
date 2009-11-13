package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Retrieve tags for a particular resource.
 * 
 * @author Luigi Marini
 *
 */
public class GetTags implements Action<GetTagsResult> {

	private static final long serialVersionUID = -5163853958905153584L;
	private String id;

	/**
	 * For serialization only.
	 */
	public GetTags() {}
	
	public GetTags(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
}
