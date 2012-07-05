package edu.illinois.ncsa.medici.geowebapp.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class LayerOpacityChangeEvent extends
		GwtEvent<LayerOpacityChangeHandler> {

	public static Type<LayerOpacityChangeHandler> TYPE = new Type<LayerOpacityChangeHandler>();

	private String layerName;
	private float opacity = 1.0f;

	public LayerOpacityChangeEvent(String layerName, float opacity) {
		this.layerName = layerName;
		this.opacity = opacity;
	}

	@Override
	public Type<LayerOpacityChangeHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(LayerOpacityChangeHandler handler) {
		handler.onLayerOpacityChanged(this);
	}

	public String getLayerName() {
		return layerName;
	}

	public float getOpacity() {
		return opacity;
	}

}
