package edu.illinois.ncsa.medici.geowebapp.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface LayerOpacityChangeHandler extends EventHandler {
	void onLayerOpacityChanged(LayerOpacityChangeEvent event);
}