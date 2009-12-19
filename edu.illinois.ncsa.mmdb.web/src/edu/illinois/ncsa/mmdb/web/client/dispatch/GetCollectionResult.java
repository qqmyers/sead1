/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * @author lmarini
 *
 */
@SuppressWarnings("serial")
public class GetCollectionResult implements Result {
	
	private CollectionBean collection;
	private List<DatasetBean> datasets;

	public GetCollectionResult() {}
	
	public GetCollectionResult(CollectionBean collection, List<DatasetBean> datasets) {
		this.collection = collection;
		this.datasets = datasets;
	}

	/**
	 * @return the collection
	 */
	public CollectionBean getCollection() {
		return collection;
	}

	/**
	 * @return the datasets
	 */
	public List<DatasetBean> getDatasets() {
		return datasets;
	}
}
