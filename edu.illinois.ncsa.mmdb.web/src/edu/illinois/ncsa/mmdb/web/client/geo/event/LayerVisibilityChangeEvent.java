package edu.illinois.ncsa.mmdb.web.client.geo.event;

import com.google.gwt.event.shared.GwtEvent;

public class LayerVisibilityChangeEvent extends
		GwtEvent<LayerVisibilityChangeHandler> {

	public static Type<LayerVisibilityChangeHandler> TYPE = new Type<LayerVisibilityChangeHandler>();

	private String layerName;
	private boolean visibility = true;

	public LayerVisibilityChangeEvent(String layerName, boolean visibility) {
		this.layerName = layerName;
		this.visibility = visibility;
	}

	@Override
	public Type<LayerVisibilityChangeHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(LayerVisibilityChangeHandler handler) {
		handler.onLayerVisibilityChanged(this);
	}

	public String getLayerName() {
		return layerName;
	}

	public boolean getVisibility() {
		return visibility;
	}

}
