/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Return the metadata attached to a resource.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class GetMetadataResult implements Result {

	private ArrayList<ArrayList<String>> metadata;
	
	public GetMetadataResult() {}
	
	public GetMetadataResult(ArrayList<ArrayList<String>> metadata) {
		this.metadata = metadata;
	}

	/**
	 * @return the metadata
	 */
	public ArrayList<ArrayList<String>> getMetadata() {
		return metadata;
	}
}
