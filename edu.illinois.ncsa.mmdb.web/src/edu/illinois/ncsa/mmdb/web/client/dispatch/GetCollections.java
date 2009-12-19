/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Get all collections.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class GetCollections implements Action<GetCollectionsResult> {

	private String memberUri;

	public GetCollections() {}

	public GetCollections(String uri) {
		this.memberUri = uri;
	}
	
	/**
	 * @return the uri
	 */
	public String getMemberUri() {
		return memberUri;
	}
}
