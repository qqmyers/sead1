/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

/**
 * Widget showing collections a particular detaset is part of.
 * 
 * @author Luigi Marini
 * 
 */
public class CollectionMembershipWidget extends Composite {

	private final FlowPanel mainContainer;
	
	private final Label titleLabel;

	private Anchor addAnchor;

	private AddToCollectionDialog addToCollectionDialog;

	private final DispatchAsync service;

	private final String datasetURI;

	private FlexTable collectionsPanel;

	/**
	 * Create empty widget showing a title and a add to collection link.
	 */
	public CollectionMembershipWidget(DispatchAsync dispatch, String datasetURI) {
		this.service = dispatch;
		this.datasetURI = datasetURI;
		mainContainer = new FlowPanel();
		mainContainer.addStyleName("datasetRightColSection");
		initWidget(mainContainer);

		titleLabel = new Label("Collections");
		titleLabel.addStyleName("datasetRightColHeading");
		mainContainer.add(titleLabel);
		
		collectionsPanel = new FlexTable();
		collectionsPanel.addStyleName("tagsLinks");
		mainContainer.add(collectionsPanel);
		
		// add to collection anchor
		addAnchor = new Anchor("Add to a collection");
		
		addAnchor.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				showAddToCollectionDialog();
			}
		});
		mainContainer.add(addAnchor);
		loadCollections();
	}

	/**
	 * Popup a dialog to select collection.
	 */
	protected void showAddToCollectionDialog() {
		addToCollectionDialog = new AddToCollectionDialog(service,
				new AddToCollectionHandler());
		addToCollectionDialog.center();
	}

	/**
	 * Add a collection to the list of collections shown.
	 * 
	 * @param collection
	 */
	public void addCollection(CollectionBean collection) {
		
		Hyperlink link = new Hyperlink(collection.getTitle(), "collection?uri="
				+ collection.getUri());
		
		int row = collectionsPanel.getRowCount();
		
		collectionsPanel.setWidget(row, 0, link);
	}
	
	/**
	 * Clear the list of collections.
	 */
	public void clear() {
		collectionsPanel.clear();
	}
	
	/**
	 * Asynchronously load the collections this dataset is part of.
	 */
	private void loadCollections() {
		service.execute(new GetCollections(datasetURI),
				new AsyncCallback<GetCollectionsResult>() {

					@Override
					public void onFailure(Throwable arg0) {
						GWT.log("Error loading collections the dataset is part of", arg0);
					}

					@Override
					public void onSuccess(GetCollectionsResult arg0) {
						ArrayList<CollectionBean> collections = arg0
								.getCollections();
						
						clear();
						if (collections.size() > 0) {
							for (CollectionBean collection : collections) {
								addCollection(collection);
							}
						}
					}
				});
	}
	
	class AddToCollectionHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent arg0) {
			String value = addToCollectionDialog.getSelectedValue();
			if (value != null) {
				GWT.log("Adding " + datasetURI + " to collection " + value, null);
				Collection<String> datasets = new HashSet<String>();
				datasets.add(datasetURI);
				service.execute(new AddToCollection(value, datasets),
						new AsyncCallback<AddToCollectionResult>() {

							@Override
							public void onFailure(Throwable arg0) {
								GWT.log("Error adding dataset to collection",
										arg0);
							}

							@Override
							public void onSuccess(AddToCollectionResult arg0) {
								GWT
										.log(
												"Datasets successfully added to collection",
												null);
								addToCollectionDialog.hide();
								loadCollections();
							}
						});
			}
		}
	}
}
