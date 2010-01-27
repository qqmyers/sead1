package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface PageChangedHandler extends EventHandler {
	void onPageChanged(PageChangedEvent event);
}
