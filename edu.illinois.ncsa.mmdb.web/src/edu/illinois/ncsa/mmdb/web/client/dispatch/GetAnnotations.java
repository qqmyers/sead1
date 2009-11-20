/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Retrieve annotations for a particular resource.
 * 
 * @author Luigi Marini
 *
 */
public class GetAnnotations implements Action<GetAnnotationsResult>{

	private static final long serialVersionUID = 2465515526733892956L;
	private String id;

	/**
	 * For serialization only.
	 */
	public GetAnnotations() {}

	public GetAnnotations(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
}
