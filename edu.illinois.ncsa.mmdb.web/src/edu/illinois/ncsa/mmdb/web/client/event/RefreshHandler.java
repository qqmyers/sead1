package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface RefreshHandler extends EventHandler {
    void onRefresh(RefreshEvent event);
}
