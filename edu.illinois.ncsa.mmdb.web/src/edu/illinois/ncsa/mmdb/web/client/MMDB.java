package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetHandler;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * MMDB entry point.
 * 
 * @author Luigi Marini
 */
public class MMDB implements EntryPoint, ValueChangeHandler<String> {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Dispatch service. Should be the only service needed. All commands
	 * should go through this endpoint. To learn more look up gwt-dispatch
	 * and the command pattern.
	 */
	private MyDispatchAsync dispatchAsync = new MyDispatchAsync();
	
	/** Event bus for propagating events in the interface **/
	private HandlerManager eventBus = new HandlerManager(null);
	
	/** Main content panel **/
	private FlowPanel mainContainer = new FlowPanel();
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		RootPanel.get("mainContainer").add(mainContainer);
		
		// testing method
		listDatasets();

		// log events
		logEvent(eventBus);
		
        final DownloadButton dw = new DownloadButton("http://www.ncsa.uiuc.edu/");
        dw.addStyleName("sendButton");
        mainContainer.add(dw);
	}

	/**
	 * For debugging purposes. Monitor events of interest.
	 * 
	 * @param eventBus
	 */
	private void logEvent(HandlerManager eventBus) {
		eventBus.addHandler(AddNewDatasetEvent.TYPE,
				new AddNewDatasetHandler() {

					@Override
					public void onAddNewDataset(AddNewDatasetEvent event) {
						GWT.log("Event Logging: Add new dataset event "
								+ event.getDataset().getTitle(), null);

					}
				});
	}

	/**
	 * A simple testing method to add the list of dataset names to the page.
	 * This is only to check that the connection with the tupelo repository
	 * works properly.
	 * 
	 * @param eventBus
	 * @param dispatchAsync 
	 */
	private void listDatasets() {

		DatasetTableView datasetTableWidget = new DatasetTableView();
		DatasetTablePresenter datasetTablePresenter = new DatasetTablePresenter(
				datasetTableWidget, eventBus);
		datasetTablePresenter.bind();

		mainContainer.clear();
		mainContainer.add(datasetTableWidget.asWidget());
		
		dispatchAsync.execute(new GetDatasets(), new AsyncCallback<GetDatasetsResult>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error retrieving datasets", null);
				DialogBox dialogBox = new DialogBox();
				dialogBox.setText("Oops");
				dialogBox.add(new Label(SERVER_ERROR));
				dialogBox.setAnimationEnabled(true);
				dialogBox.center();
				dialogBox.show();
			}

			@Override
			public void onSuccess(GetDatasetsResult result) {
				for (DatasetBean dataset : result.getDatasets()) {
					GWT.log("Sending event add dataset " + dataset.getTitle(),
							null);
					AddNewDatasetEvent event = new AddNewDatasetEvent();
					event.setDataset(dataset);
					eventBus.fireEvent(event);
				}
			}
		});

	}
	
	/**
	 * Show information about a partiucular dataset.
	 */
	private void showDataset() {

		DatasetView datasetWidget = new DatasetView();
		DatasetPresenter datasetPresenter = new DatasetPresenter(datasetWidget, eventBus);
		datasetPresenter.bind();
		
		mainContainer.clear();
		mainContainer.add(datasetWidget.asWidget());
	}
	
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();
		GWT.log("History changed: " + event.getValue(), null);
		
		if (token.startsWith("dataset")) {
			showDataset();
		} else {
			listDatasets();
		}
	}
}
