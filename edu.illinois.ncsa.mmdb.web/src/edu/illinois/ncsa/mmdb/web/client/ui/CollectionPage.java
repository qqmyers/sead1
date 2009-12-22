/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * A widget showing a collection.
 * 
 * @author Luigi Marini
 * 
 */
public class CollectionPage extends Composite {

	private final String uri;
	private final MyDispatchAsync dispatchasync;
	private final HandlerManager eventBus;
	private final FlowPanel mainContent;
	private final String PREVIEW_URL = "./api/image/preview/small/";
	private Label pageTitle;
	private Label descriptionLabel;
	private Label dateLabel;
	private FlowPanel infoPanel;
	private Label numDatasetsLabel;
	private Label authorLabel;

	public CollectionPage(String uri, MyDispatchAsync dispatchasync,
			HandlerManager eventBus) {
		this.uri = uri;
		this.dispatchasync = dispatchasync;
		this.eventBus = eventBus;
		mainContent = new FlowPanel();
		mainContent.addStyleName("page");
		initWidget(mainContent);
		
		mainContent.add(createPageTitle());
		
		mainContent.add(createInfoPanel());
		
		retrieveCollection();
	}
	
	private Widget createInfoPanel() {
		infoPanel = new FlowPanel();
		infoPanel.addStyleName("collectionInfo");
		authorLabel = new Label("Author");
		infoPanel.add(authorLabel);
		descriptionLabel = new Label("Description");
		infoPanel.add(descriptionLabel);
		dateLabel = new Label("Creation date");
		infoPanel.add(dateLabel);
		numDatasetsLabel = new Label("Number of datasets");
		infoPanel.add(numDatasetsLabel);
		return infoPanel;
	}

	/**
	 * 
	 * @return
	 */
	private Widget createPageTitle() {
		pageTitle = new Label("Collection");
		pageTitle.addStyleName("pageTitle");
		return pageTitle;
	}

	private void retrieveCollection() {
		dispatchasync.execute(new GetCollection(uri), new AsyncCallback<GetCollectionResult>() {

			@Override
			public void onFailure(Throwable arg0) {
				GWT.log("Failed to retrieve collection", arg0);
			}

			@Override
			public void onSuccess(GetCollectionResult arg0) {
				showCollection(arg0.getCollection(), arg0.getDatasets());
			}
		});
	}

	protected void showCollection(CollectionBean collection, List<DatasetBean> datasets) {
		
		pageTitle.setText(collection.getTitle());
		if (collection.getCreator() == null) {
			authorLabel.setText("By Anonymous");
		} else {
			authorLabel.setText(collection.getCreator().getName());
		}
		descriptionLabel.setText(collection.getDescription());
		if (collection.getCreationDate() != null) {
			dateLabel.setText(collection.getCreationDate().toString());
		}
		numDatasetsLabel.setText(datasets.size() + " datasets");
		ArrayList<String> uris = new ArrayList<String>();
		for (DatasetBean dataset : datasets) {
			//Image previewImage = new Image(PREVIEW_URL + dataset.getUri());
//			mainContent.add(previewImage);
			uris.add(dataset.getUri());
		}
		
		GalleryWidget gallery = new GalleryWidget(uris);
		mainContent.add(gallery);
	}

	private void retrieveMembers() {
		// TODO Auto-generated method stub
		
	}

}
