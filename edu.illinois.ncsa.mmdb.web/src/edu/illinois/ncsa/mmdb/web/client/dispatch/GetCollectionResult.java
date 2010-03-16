/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

/**
 * @author lmarini
 *
 */
@SuppressWarnings("serial")
public class GetCollectionResult implements Result {
	
	private CollectionBean collection;
	private int collectionSize;

	public GetCollectionResult() {}
	
	public GetCollectionResult(CollectionBean collection, int collectionSize) {
		this.collection = collection;
		this.collectionSize = collectionSize;
	}

	/**
	 * @return the collection
	 */
	public CollectionBean getCollection() {
		return collection;
	}

	public int getCollectionSize() {
		return collectionSize;
	}

	public void setCollectionSize(int collectionSize) {
		this.collectionSize = collectionSize;
	}
}
