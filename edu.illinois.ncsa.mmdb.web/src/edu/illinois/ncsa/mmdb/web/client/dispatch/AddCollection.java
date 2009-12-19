/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;
import edu.illinois.ncsa.mmdb.web.client.ui.AddCollectionResult;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

/**
 * @author lmarini
 *
 */
@SuppressWarnings("serial")
public class AddCollection implements Action<AddCollectionResult> {

	private CollectionBean collection;

	public AddCollection() {}
	
	public AddCollection(CollectionBean collection) {
		this.collection = collection;
		
	}

	/**
	 * @return the collection
	 */
	public CollectionBean getCollection() {
		return collection;
	}
}
