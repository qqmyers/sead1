/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;


/**
 * Get all collections.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class GetCollections extends SubjectAction<GetCollectionsResult> {

	public GetCollections() {}

	public GetCollections(String uri) {
		setUri(uri);
	}
	
	/**
	 * @return the uri
	 */
	public String getMemberUri() {
		return getUri();
	}
}
