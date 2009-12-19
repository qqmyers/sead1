/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * @author lmarini
 *
 */
@SuppressWarnings("serial")
public class GetCollection implements Action<GetCollectionResult> {

	private String uri;

	public GetCollection() {}
	
	public GetCollection(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
}
