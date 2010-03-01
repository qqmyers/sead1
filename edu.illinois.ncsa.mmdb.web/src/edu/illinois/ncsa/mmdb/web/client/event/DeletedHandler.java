package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface DeletedHandler extends EventHandler {
	void onDeleted(DeletedEvent event);
}
