/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Get a tag.
 * 
 * @author Luigi Marini
 *
 */
public class GetTag implements Action<GetDatasetsResult> {

	private static final long serialVersionUID = 3488521062486339622L;
	private String title;
	
	/** For serialization only **/
	public GetTag() {}

	public GetTag(String title) {
		this.title = title;
	}

	public String getUri() {
		return title;
	}
}
