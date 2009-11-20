/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

/**
 * @author lmarini
 *
 */
public class GetDataset implements Action<GetDatasetResult>{

	private static final long serialVersionUID = -3751159066935167385L;
	
	private String id;
	
	private GetDataset() {}
	
	public GetDataset(String id) {
		this.setId(id);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

}
