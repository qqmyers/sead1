package edu.illinois.ncsa.mmdb.web.client.event;

import java.util.Collection;

import com.google.gwt.event.shared.GwtEvent;

public class DatasetsDeletedEvent extends GwtEvent<DatasetsDeletedHandler> {
    public static GwtEvent.Type<DatasetsDeletedHandler> TYPE = new GwtEvent.Type<DatasetsDeletedHandler>();

    private Collection<String>                          uris;

    public DatasetsDeletedEvent() {
    }

    public DatasetsDeletedEvent(Collection<String> uris) {
        setUris(uris);
    }

    @Override
    protected void dispatch(DatasetsDeletedHandler handler) {
        handler.onDatasetsDeleted(this);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<DatasetsDeletedHandler> getAssociatedType() {
        return TYPE;
    }

    public void setUris(Collection<String> uris) {
        this.uris = uris;
    }

    public Collection<String> getUris() {
        return uris;
    }

}
