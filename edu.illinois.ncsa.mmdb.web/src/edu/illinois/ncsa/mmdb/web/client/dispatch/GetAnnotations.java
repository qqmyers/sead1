/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;


/**
 * Retrieve annotations for a particular resource.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class GetAnnotations extends SubjectAction<GetAnnotationsResult>{
	/**
	 * For serialization only.
	 */
	public GetAnnotations() {}

	public GetAnnotations(String id) {
		super(id);
	}

	/** @deprecated use getUri() */
	public String getId() {
		return getUri();
	}
}
