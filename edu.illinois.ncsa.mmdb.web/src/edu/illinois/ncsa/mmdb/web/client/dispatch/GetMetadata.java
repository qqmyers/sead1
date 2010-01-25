/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;


/**
 * Get the metadata attached to a resource.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class GetMetadata extends SubjectAction<GetMetadataResult> {
	public GetMetadata() {}
	
	public GetMetadata(String uri) {
		super(uri);
	}
}
