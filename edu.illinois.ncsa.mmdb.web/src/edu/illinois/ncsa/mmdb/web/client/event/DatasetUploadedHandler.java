package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface DatasetUploadedHandler extends EventHandler {
	void onDatasetUploaded(DatasetUploadedEvent event);
}
