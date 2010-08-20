package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class AllOnPageSelectedEvent extends GwtEvent<AllOnPageSelectedHandler> {
    public static GwtEvent.Type<AllOnPageSelectedHandler> TYPE = new GwtEvent.Type<AllOnPageSelectedHandler>();

    @Override
    protected void dispatch(AllOnPageSelectedHandler handler) {
        handler.onAllOnPageSelected(this);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<AllOnPageSelectedHandler> getAssociatedType() {
        return TYPE;
    }

}
