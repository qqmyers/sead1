package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class ListDatasetsResult implements Result {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2057929768340773206L;
	private List<DatasetBean> result;
	
	public ListDatasetsResult() { }
	
	public ListDatasetsResult(List<DatasetBean> result) {
		this.result = result;
	}
	
	public List<DatasetBean> getDatasets() {
		return result;
	}
}
