/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.place;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;

/**
 * An attempt to centralize the handling of history tokens and places in the
 * application.
 * 
 * @author Luigi Marini
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
	
	/**
	 * Parse the parameters in the history token after the '?'
	 * 
	 * @return
	 */
	public static Map<String, String> getParams() {
		Map<String, String> params = new HashMap<String, String>();
		String paramString = History.getToken().substring(
				History.getToken().indexOf("?") + 1);
		if (!paramString.isEmpty()) {
			for (String paramEntry : paramString.split("&")) {
				String[] terms = paramEntry.split("=");
				if (terms.length == 2) {
					params.put(terms[0], terms[1]);
				}
			}
		}
		return params;
	}
}
