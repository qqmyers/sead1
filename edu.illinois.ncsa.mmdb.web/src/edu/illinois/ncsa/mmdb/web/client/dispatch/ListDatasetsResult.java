package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;

public class ListDatasetsResult implements Result {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2057929768340773206L;
	private List<DatasetBean> datasets;
	private Map<DatasetBean,Collection<PreviewImageBean>> previews;
	
	public ListDatasetsResult() { }
	
	public ListDatasetsResult(List<DatasetBean> result) {
		setDatasets(result);
	}
	public ListDatasetsResult(List<DatasetBean> result, Map<DatasetBean,Collection<PreviewImageBean>> previews) {
		setDatasets(result);
		setPreviews(previews);
	}
	
	public void setDatasets(List<DatasetBean> d) {
		datasets = d;
	}
	public List<DatasetBean> getDatasets() {
		return datasets;
	}
	
	public void setPreviews(Map<DatasetBean,Collection<PreviewImageBean>> p) {
		previews = p;
	}
	public Map<DatasetBean,Collection<PreviewImageBean>> getPreviews() {
		return previews;
	}
	public Collection<PreviewImageBean> getPreviews(DatasetBean b) {
		return getPreviews().get(b);
	}
}
