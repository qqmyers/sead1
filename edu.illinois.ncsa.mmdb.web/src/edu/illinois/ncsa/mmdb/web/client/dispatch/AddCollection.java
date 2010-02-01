/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;
import edu.illinois.ncsa.mmdb.web.client.ui.AddCollectionResult;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

/**
 * Create new collection.
 * 
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class AddCollection implements Action<AddCollectionResult> {

	private CollectionBean collection;
	private String sessionId;

	public AddCollection() {}
	
	public AddCollection(CollectionBean collection, String sessionId) {
		this.collection = collection;
		this.sessionId = sessionId;
	}

	/**
	 * Get the new collection bean.
	 * 
	 * @return the collection
	 */
	public CollectionBean getCollection() {
		return collection;
	}

	/**
	 * Get the sessionId used.
	 * 
	 * @return sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}
}
