package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class RefreshEvent extends GwtEvent<RefreshHandler> {
    public static GwtEvent.Type<RefreshHandler> TYPE = new GwtEvent.Type<RefreshHandler>();

    @Override
    protected void dispatch(RefreshHandler handler) {
        handler.onRefresh(this);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<RefreshHandler> getAssociatedType() {
        return TYPE;
    }

}
