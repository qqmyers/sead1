package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface AddPreviewHandler extends EventHandler {
	void onPreviewAdded(AddPreviewEvent event);
}
