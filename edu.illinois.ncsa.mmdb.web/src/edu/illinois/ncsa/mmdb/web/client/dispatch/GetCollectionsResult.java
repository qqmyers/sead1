/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

/**
 * All the collections in the repository.
 * 
 * @author Luigi Marini
 * 
 */
@SuppressWarnings("serial")
public class GetCollectionsResult implements Result {
	
	private ArrayList<CollectionBean> collections;
	private int count; // total number of collections in the context

	public GetCollectionsResult() {}
	
	public GetCollectionsResult(ArrayList<CollectionBean> collections) {
		this.collections = collections;
	}

	/**
	 * @return the collections
	 */
	public ArrayList<CollectionBean> getCollections() {
		return collections;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
