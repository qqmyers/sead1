/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import net.customware.gwt.dispatch.shared.Result;

/**
 * @author lmarini
 *
 */
public class GetDatasetResult implements Result {

	private static final long serialVersionUID = -86488013616325220L;
	
	private DatasetBean dataset;
	
	public GetDatasetResult() {}
	
	public GetDatasetResult(DatasetBean datasetBean) {
		setDataset(datasetBean);
	}

	public void setDataset(DatasetBean dataset) {
		this.dataset = dataset;
	}

	public DatasetBean getDataset() {
		return dataset;
	}

}
