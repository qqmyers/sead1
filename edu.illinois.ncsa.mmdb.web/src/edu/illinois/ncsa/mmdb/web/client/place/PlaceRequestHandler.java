package edu.illinois.ncsa.mmdb.web.client.place;

import com.google.gwt.event.shared.EventHandler;

public interface PlaceRequestHandler extends EventHandler {

	void onPlaceRequest(PlaceRequestEvent event);
}
