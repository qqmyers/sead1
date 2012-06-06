package edu.illinois.ncsa.medici.geowebapp2.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class LayerOpacityChangeEvent extends
		GwtEvent<LayerOpacityChangeHandler> {

	public static Type<LayerOpacityChangeHandler> TYPE = new Type<LayerOpacityChangeHandler>();

	private String layerName;
	private double opacity = 1.0;

	public LayerOpacityChangeEvent(String layerName, double opacity) {
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

	public double getOpacity() {
		return opacity;
	}

}
