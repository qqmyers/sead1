/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

/**
 * List all collections in system.
 * 
 * @author Luigi Marini
 * 
 */
public class ListCollectionsPage extends Composite {

	private final MyDispatchAsync dispatchasync;
	private final HandlerManager eventBus;
	private final FlowPanel mainContainer;
	private final Label noCollectionsLabel;
	private final FlowPanel addCollectionWidget;
	private FlexTable collectionsTable;
	private TitlePanel pageTitle;

	public ListCollectionsPage(MyDispatchAsync dispatchasync,
			HandlerManager eventBus) {
		this.dispatchasync = dispatchasync;
		this.eventBus = eventBus;
		mainContainer = new FlowPanel();
		mainContainer.addStyleName("page");
		initWidget(mainContainer);
		
		mainContainer.add(createPageTitle());

		noCollectionsLabel = new Label("No collections available.");
		mainContainer.add(noCollectionsLabel);

		// add collection widget
		addCollectionWidget = createAddCollectionWidget();
		mainContainer.add(addCollectionWidget);

		retrieveCollections();
	}
	
	/**
	 * 
	 * @return
	 */
	private Widget createPageTitle() {
		pageTitle = new TitlePanel("Collections");
		return pageTitle;
	}

	private FlowPanel createAddCollectionWidget() {
		FlowPanel addCollectionPanel = new FlowPanel();
		final WatermarkTextBox addCollectionBox = new WatermarkTextBox("",
				"Collection name");
		addCollectionPanel.add(addCollectionBox);
		Button addButton = new Button("Add", new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				createNewCollection(addCollectionBox.getText());
			}
		});
		addCollectionPanel.add(addButton);
		return addCollectionPanel;
	}

	/**
	 * 
	 * @param text
	 */
	protected void createNewCollection(String text) {

		CollectionBean collection = new CollectionBean();
		collection.setTitle(text);

		dispatchasync.execute(new AddCollection(collection, MMDB.sessionID),
				new AsyncCallback<AddCollectionResult>() {

					@Override
					public void onFailure(Throwable arg0) {
						GWT.log("Failed creating new collection", arg0);
					}

					@Override
					public void onSuccess(AddCollectionResult arg0) {
						retrieveCollections();
					}
				});
	}

	/**
	 * Retrieve collections from server.
	 */
	private void retrieveCollections() {
		dispatchasync.execute(new GetCollections(),
				new AsyncCallback<GetCollectionsResult>() {

					@Override
					public void onFailure(Throwable arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSuccess(GetCollectionsResult arg0) {
						showCollections(arg0.getCollections());
					}
				});
	}

	/**
	 * 
	 * @param collections
	 */
	protected void showCollections(ArrayList<CollectionBean> collections) {
		if (collections.size() > 0) {
			if (collectionsTable == null) {
				mainContainer.remove(noCollectionsLabel);
				collectionsTable = new FlexTable();
				collectionsTable.addStyleName("datasetTable");
				mainContainer.insert(collectionsTable, 1);
			}
			int row = 0;
			for (CollectionBean collection : collections) {
				Hyperlink link = new Hyperlink(collection.getTitle(),
						"collection?uri=" + collection.getUri());
				collectionsTable.setWidget(row, 0, link);
				collectionsTable.setText(row, 1, collection.getDescription());
				if (collection.getCreationDate() != null) {
					collectionsTable.setText(row, 2, collection.getCreationDate()
							.toString());
				}
				row++;
			}
		}
	}

}
