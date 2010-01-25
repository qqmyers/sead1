/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;


/**
 * @author lmarini
 *
 */
@SuppressWarnings("serial")
public class GetGeoPoint extends SubjectAction<GetGeoPointResult>{
	
	@SuppressWarnings("unused")
	private GetGeoPoint() {}
	
	public GetGeoPoint(String id) {
		super(id);
	}

	/** @deprecated use setUri */
	public void setId(String id) {
		setUri(id);
	}

	/** @deprecated use getUri */
	public String getId() {
		return getUri();
	}

}
