package edu.illinois.ncsa.medici.geowebapp.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface LayerVisibilityChangeHandler extends EventHandler {
	void onLayerVisibilityChanged(LayerVisibilityChangeEvent event);
}
