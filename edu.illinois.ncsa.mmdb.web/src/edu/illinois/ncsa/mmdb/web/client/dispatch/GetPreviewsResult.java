package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;

@SuppressWarnings("serial")
public class GetPreviewsResult implements Result {
	boolean stopAsking = false;
	
	public boolean isStopAsking() {
		return stopAsking;
	}

	public void setStopAsking(boolean stopAsking) {
		this.stopAsking = stopAsking;
	}

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
