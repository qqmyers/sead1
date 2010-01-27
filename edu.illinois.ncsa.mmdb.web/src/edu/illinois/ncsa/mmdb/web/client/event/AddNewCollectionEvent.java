package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

import edu.uiuc.ncsa.cet.bean.CollectionBean;

public class AddNewCollectionEvent extends GwtEvent<AddNewCollectionHandler> {
	public static final GwtEvent.Type<AddNewCollectionHandler> TYPE = new GwtEvent.Type<AddNewCollectionHandler>();

	CollectionBean collection;
	
	public AddNewCollectionEvent(CollectionBean c) {
		setCollection(c);
	}
	public CollectionBean getCollection() {
		return collection;
	}

	public void setCollection(CollectionBean collection) {
		this.collection = collection;
	}

	@Override
	protected void dispatch(AddNewCollectionHandler handler) {
		handler.onAddNewCollection(this);
	}

	@Override
	public GwtEvent.Type<AddNewCollectionHandler> getAssociatedType() {
		return TYPE;
	}
}
