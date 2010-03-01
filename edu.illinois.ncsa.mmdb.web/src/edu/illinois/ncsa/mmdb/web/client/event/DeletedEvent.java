package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class DeletedEvent extends GwtEvent<DeletedHandler> {
	public static final com.google.gwt.event.shared.GwtEvent.Type<DeletedHandler> TYPE = new GwtEvent.Type<DeletedHandler>();
	
	String uri;
	
	public DeletedEvent() { }
	
	public DeletedEvent(String uri) {
		setUri(uri);
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String datasetUri) {
		this.uri = datasetUri;
	}

	protected void dispatch(DeletedHandler handler) {
		handler.onDeleted(this);
	}

	public com.google.gwt.event.shared.GwtEvent.Type<DeletedHandler> getAssociatedType() {
		return TYPE;
	}

}
