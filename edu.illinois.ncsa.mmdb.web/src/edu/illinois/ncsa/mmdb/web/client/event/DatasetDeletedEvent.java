package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class DatasetDeletedEvent extends GwtEvent<DatasetDeletedHandler> {
	public static final com.google.gwt.event.shared.GwtEvent.Type<DatasetDeletedHandler> TYPE = new GwtEvent.Type<DatasetDeletedHandler>();
	
	String datasetUri;
	
	
	public DatasetDeletedEvent() { }
	
	public DatasetDeletedEvent(String datasetUri) {
		setDatasetUri(datasetUri);
	}
	
	public String getDatasetUri() {
		return datasetUri;
	}

	public void setDatasetUri(String datasetUri) {
		this.datasetUri = datasetUri;
	}

	protected void dispatch(DatasetDeletedHandler handler) {
		handler.onDeleteDataset(this);
	}

	public com.google.gwt.event.shared.GwtEvent.Type<DatasetDeletedHandler> getAssociatedType() {
		return TYPE;
	}

}
