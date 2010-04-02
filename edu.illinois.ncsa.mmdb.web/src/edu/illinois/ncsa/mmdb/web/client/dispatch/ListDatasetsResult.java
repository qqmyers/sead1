package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.List;
import java.util.Set;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class ListDatasetsResult implements Result {
	public static final int COUNT_UNKNOWN = -1;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2057929768340773206L;
	private List<DatasetBean> datasets;
	private Set<String> hasPreviews;
	private int datasetCount; // total number of datasets
	
	public ListDatasetsResult() { }
	
	public ListDatasetsResult(List<DatasetBean> result) {
		setDatasets(result);
	}
	
	public void setDatasets(List<DatasetBean> d) {
		datasets = d;
	}
	public List<DatasetBean> getDatasets() {
		return datasets;
	}
	
	public void setHasPreviews(Set<String> s) {
		hasPreviews = s;
	}
	public Set<String> getHasPreviews() {
		return hasPreviews;
	}
	public void setDatasetCount(int c) {
		datasetCount = c;
	}
	public int getDatasetCount() {
		return datasetCount;
	}
}
