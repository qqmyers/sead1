package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class DatasetUploadedEvent extends GwtEvent<DatasetUploadedHandler> {

	public static final GwtEvent.Type<DatasetUploadedHandler> TYPE = new GwtEvent.Type<DatasetUploadedHandler>();
	
	@Override
	protected void dispatch(DatasetUploadedHandler handler) {
		handler.onDatasetUploaded(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<DatasetUploadedHandler> getAssociatedType() {
		return TYPE;
	}

	private String datasetUri;

	public String getDatasetUri() {
		return datasetUri;
	}

	public void setDatasetUri(String datasetUri) {
		this.datasetUri = datasetUri;
	}
}
