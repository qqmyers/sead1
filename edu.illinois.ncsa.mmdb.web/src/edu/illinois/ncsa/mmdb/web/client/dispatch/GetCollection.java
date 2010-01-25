/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;


/**
 * @author lmarini
 *
 */
@SuppressWarnings("serial")
public class GetCollection extends SubjectAction<GetCollectionResult> {
	public GetCollection() {}
	
	public GetCollection(String uri) {
		super(uri);
	}
}
