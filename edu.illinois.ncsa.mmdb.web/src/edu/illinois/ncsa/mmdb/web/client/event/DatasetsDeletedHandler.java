package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface DatasetsDeletedHandler extends EventHandler {
    void onDatasetsDeleted(DatasetsDeletedEvent event);
}
