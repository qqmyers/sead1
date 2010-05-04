package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class NoMoreItemsEvent extends GwtEvent<NoMoreItemsHandler> {

    public static final Type<NoMoreItemsHandler> TYPE = new Type<NoMoreItemsHandler>();

    @Override
    protected void dispatch(NoMoreItemsHandler handler) {
        handler.onNoMoreItems(this);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<NoMoreItemsHandler> getAssociatedType() {
        return TYPE;
    }

}
