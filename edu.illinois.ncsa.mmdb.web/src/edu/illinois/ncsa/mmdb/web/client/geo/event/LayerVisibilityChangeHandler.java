package edu.illinois.ncsa.mmdb.web.client.geo.event;

import com.google.gwt.event.shared.EventHandler;

public interface LayerVisibilityChangeHandler extends EventHandler {
	void onLayerVisibilityChanged(LayerVisibilityChangeEvent event);
}
