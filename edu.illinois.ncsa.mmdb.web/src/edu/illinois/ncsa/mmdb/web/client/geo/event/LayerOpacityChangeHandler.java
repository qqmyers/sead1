package edu.illinois.ncsa.mmdb.web.client.geo.event;

import com.google.gwt.event.shared.EventHandler;

public interface LayerOpacityChangeHandler extends EventHandler {
	void onLayerOpacityChanged(LayerOpacityChangeEvent event);
}
