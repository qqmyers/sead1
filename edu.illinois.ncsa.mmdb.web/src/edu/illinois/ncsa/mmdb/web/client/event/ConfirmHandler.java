package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface ConfirmHandler extends EventHandler {
	void onConfirm(ConfirmEvent event);
}
