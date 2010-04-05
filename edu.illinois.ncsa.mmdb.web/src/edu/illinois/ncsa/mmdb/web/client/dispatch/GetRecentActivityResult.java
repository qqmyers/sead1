/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * @author Luigi Marini
 *
 */
@SuppressWarnings("serial")
public class GetRecentActivityResult implements Result {

	private List<DatasetBean> datasets;

	public GetRecentActivityResult() {}
	
	public GetRecentActivityResult(List<DatasetBean> datasets) {
		this.datasets = datasets;
	}

	public void setDatasets(List<DatasetBean> datasets) {
		this.datasets = datasets;
	}

	public List<DatasetBean> getDatasets() {
		return datasets;
	}
}
