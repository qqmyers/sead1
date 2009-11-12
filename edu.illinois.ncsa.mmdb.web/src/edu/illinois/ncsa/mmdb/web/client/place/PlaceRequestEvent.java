/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.place;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author lmarini
 *
 */
public class PlaceRequestEvent extends GwtEvent<PlaceRequestHandler> {

	public static final GwtEvent.Type<PlaceRequestHandler> TYPE = new GwtEvent.Type<PlaceRequestHandler>();
	
	private final PlaceRequest request;
	
	public PlaceRequestEvent(PlaceRequest request) {
		this.request = request;
	}
	
	@Override
	protected void dispatch(PlaceRequestHandler handler) {
		handler.onPlaceRequest(this);
	}

	@Override
	public GwtEvent.Type<PlaceRequestHandler> getAssociatedType() {
		return TYPE;
	}

	public PlaceRequest getRequest() {
		return request;
	}

}
