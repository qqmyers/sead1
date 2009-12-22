package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uiuc.ncsa.cet.bean.PreviewImageBean;

import net.customware.gwt.dispatch.shared.Result;

public class GetPreviewsResult implements Result {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8437174571690183109L;

	public GetPreviewsResult() {
		previews = new HashMap<String,PreviewImageBean>();
	}
	
	Map<String,PreviewImageBean> previews;
	
	public GetPreviewsResult(PreviewImageBean smallPreview, PreviewImageBean largePreview) {
		this();
		setPreview(GetPreviews.SMALL,smallPreview);
		setPreview(GetPreviews.LARGE,largePreview);
	}
	
	public PreviewImageBean getPreview(String size) {
		return previews.get(size);
	}
	public void setPreview(String size, PreviewImageBean preview) {
		previews.put(size,preview);
	}
	
	public Map<String,PreviewImageBean> getPreviews() {
		return previews; // for serialization
	}
	public void setPreviews(Map<String,PreviewImageBean> p) {
		previews = p; // for serialization 
	}
}
