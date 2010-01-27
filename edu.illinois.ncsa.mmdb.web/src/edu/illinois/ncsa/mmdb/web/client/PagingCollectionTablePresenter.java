package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.event.AddNewCollectionEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewCollectionHandler;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

public class PagingCollectionTablePresenter extends PagingTablePresenter<CollectionBean> {

	public PagingCollectionTablePresenter(Display<CollectionBean> display,
			HandlerManager eventBus) {
		super(display, eventBus);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void bind() {

		super.bind();

		eventBus.addHandler(AddNewCollectionEvent.TYPE,
				new AddNewCollectionHandler() {
					@Override
					public void onAddNewCollection(AddNewCollectionEvent event) {
						CollectionBean collection = event.getCollection();
						display.addItem(collection.getUri(), collection);
					}
				});
	}

}
