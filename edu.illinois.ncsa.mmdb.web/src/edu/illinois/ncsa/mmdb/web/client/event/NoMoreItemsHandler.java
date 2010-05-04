package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface NoMoreItemsHandler extends EventHandler {
    void onNoMoreItems(NoMoreItemsEvent event);
}
