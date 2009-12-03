package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class CancelEvent extends GwtEvent<CancelHandler> {
	public static final GwtEvent.Type<CancelHandler> TYPE = new GwtEvent.Type<CancelHandler>();
	
	@Override
	protected void dispatch(CancelHandler handler) {
		handler.onCancel(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<CancelHandler> getAssociatedType() {
		return TYPE;
	}

}
