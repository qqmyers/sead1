package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface DatasetDeletedHandler extends EventHandler {
	void onDeleteDataset(DatasetDeletedEvent event);
}
