/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.HashSet;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Datasets retrieved.
 * 
 * @author Luigi Marini
 *
 */
public class GetDatasetsResult implements Result {

	private static final long serialVersionUID = 962227750465621779L;
	
	private HashSet<DatasetBean> datasets = new HashSet<DatasetBean>();
	
	/** For serialization only. */
	public GetDatasetsResult(){	
	}
	
	public GetDatasetsResult(HashSet<DatasetBean> datasets) {
		this.datasets = datasets;
	}

	public HashSet<DatasetBean> getDatasets() {
		return datasets;
	}
}
