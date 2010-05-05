package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface BatchCompletedHandler extends EventHandler {
    void onBatchCompleted(BatchCompletedEvent event);
}
