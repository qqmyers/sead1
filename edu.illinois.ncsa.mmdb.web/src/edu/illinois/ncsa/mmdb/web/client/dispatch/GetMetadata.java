/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Get the metadata attached to a resource.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class GetMetadata implements Action<GetMetadataResult> {

	private String uri;

	public GetMetadata() {}
	
	public GetMetadata(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
}
