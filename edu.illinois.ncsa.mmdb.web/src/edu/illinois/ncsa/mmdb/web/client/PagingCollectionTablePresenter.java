package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.event.AddNewCollectionEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewCollectionHandler;
import edu.illinois.ncsa.mmdb.web.client.event.AddPreviewEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddPreviewHandler;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

public class PagingCollectionTablePresenter extends PagingTablePresenter<CollectionBean> {

	public PagingCollectionTablePresenter(CollectionDisplay display,
			HandlerManager eventBus) {
		super(display, eventBus);
		// TODO Auto-generated constructor stub
	}
	
	public interface CollectionDisplay extends Display<CollectionBean> {
		void addBadge(String collectionUri, String badgeUri);
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
		
		eventBus.addHandler(AddPreviewEvent.TYPE,
				new AddPreviewHandler() {
					public void onPreviewAdded(AddPreviewEvent event) {
						((CollectionDisplay)display).addBadge(event.getUri(), event.getPreviewUri());
					}
		});
	}

}
