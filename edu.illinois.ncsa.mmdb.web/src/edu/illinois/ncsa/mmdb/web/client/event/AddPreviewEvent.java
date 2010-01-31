package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class AddPreviewEvent extends GwtEvent<AddPreviewHandler> {
	public static final GwtEvent.Type<AddPreviewHandler> TYPE = new GwtEvent.Type<AddPreviewHandler>();

	String uri;
	String previewUri;
	
	public AddPreviewEvent() { }
	
	public AddPreviewEvent(String uri, String previewUri) {
		setUri(uri);
		setPreviewUri(previewUri);
	}
	
	@Override
	protected void dispatch(AddPreviewHandler handler) {
		handler.onPreviewAdded(this);
	}

	@Override
	public GwtEvent.Type<AddPreviewHandler> getAssociatedType() {
		return TYPE;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getPreviewUri() {
		return previewUri;
	}

	public void setPreviewUri(String previewUri) {
		this.previewUri = previewUri;
	}

	
}
