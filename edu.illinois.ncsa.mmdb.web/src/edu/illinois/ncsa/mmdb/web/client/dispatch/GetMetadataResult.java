/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Return the metadata attached to a resource.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class GetMetadataResult implements Result {
	private List<Metadata> metadata;
	
	public GetMetadataResult() {
	    metadata = new ArrayList<Metadata>();
	}
	
	public void add(String category, String label, String value) {
	    metadata.add(new Metadata( category, label, value ));
	}
	
	public void setMetadata(List<Metadata> metadata) {
	    this.metadata = metadata;
	}
	
	/**
	 * @return the metadata
	 */
	public List<Metadata> getMetadata() {
		return metadata;
	}
}
