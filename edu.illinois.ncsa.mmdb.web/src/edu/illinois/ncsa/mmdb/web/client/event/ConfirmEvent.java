package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class ConfirmEvent extends GwtEvent<ConfirmHandler> {
	public static final GwtEvent.Type<ConfirmHandler> TYPE = new GwtEvent.Type<ConfirmHandler>();
	
	@Override
	protected void dispatch(ConfirmHandler handler) {
		handler.onConfirm(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ConfirmHandler> getAssociatedType() {
		return TYPE;
	}

}
