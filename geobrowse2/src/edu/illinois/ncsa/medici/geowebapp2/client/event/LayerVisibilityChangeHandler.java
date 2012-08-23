package edu.illinois.ncsa.medici.geowebapp2.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface LayerVisibilityChangeHandler extends EventHandler {
	void onLayerVisibilityChanged(LayerVisibilityChangeEvent event);
}
