package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class AllDatasetsUnselectedEvent extends GwtEvent<AllDatasetsUnselectedHandler> {
    public static GwtEvent.Type<AllDatasetsUnselectedHandler> TYPE = new GwtEvent.Type<AllDatasetsUnselectedHandler>();

    @Override
    protected void dispatch(AllDatasetsUnselectedHandler handler) {
        handler.onAllDatasetsUnselected(this);

    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<AllDatasetsUnselectedHandler> getAssociatedType() {
        return TYPE;
    }
}
