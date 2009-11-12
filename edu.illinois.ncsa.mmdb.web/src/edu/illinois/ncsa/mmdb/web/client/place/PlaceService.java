/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.place;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;

/**
 * @author lmarini
 *
 */
public class PlaceService implements ValueChangeHandler<String> {

	private final HandlerManager eventBus;

	public PlaceService(HandlerManager eventBus) {
		this.eventBus = eventBus;
		
		History.addValueChangeHandler(this);
	}

	public HandlerManager getEventBus() {
		return eventBus;
	}

	/**
	 * History token has changed. Fire the proper place request event.
	 */
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		PlaceRequest placeRequest = new PlaceRequest(event.getValue());
		eventBus.fireEvent(new PlaceRequestEvent(placeRequest));
	}
}
