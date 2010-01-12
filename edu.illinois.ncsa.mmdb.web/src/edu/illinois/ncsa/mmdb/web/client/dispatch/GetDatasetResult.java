/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;
import java.util.HashSet;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;

/**
 * Return a dataset and associated previews if available.
 * 
 * @author Luigi Marini
 * 
 */
public class GetDatasetResult implements Result {

	private static final long serialVersionUID = -86488013616325220L;

	private DatasetBean dataset;

	private Collection<PreviewImageBean> previews;

	private String pyramidUrl = null;
	
	public GetDatasetResult() {
	}

	public GetDatasetResult(DatasetBean datasetBean,
			Collection<PreviewImageBean> previews, String pyramidUrl) {
		setDataset(datasetBean);
		setPreviews(previews);
		setPyramidUrl(pyramidUrl);
	}

	public void setDataset(DatasetBean dataset) {
		this.dataset = dataset;
	}

	public DatasetBean getDataset() {
		return dataset;
	}

	public void setPreviews(Collection<PreviewImageBean> previews) {
		this.previews = previews;
	}

	public Collection<PreviewImageBean> getPreviews() {
		if (previews == null) {
			return new HashSet<PreviewImageBean>();
		}
		return previews;
	}

	public String getPyramidUrl() {
		return pyramidUrl;
	}

	public void setPyramidUrl(String pyramidUrl) {
		this.pyramidUrl = pyramidUrl;
	}

}
