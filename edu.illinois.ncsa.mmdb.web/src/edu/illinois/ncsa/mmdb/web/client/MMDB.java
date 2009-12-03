package edu.illinois.ncsa.mmdb.web.client;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedHandler;
import edu.illinois.ncsa.mmdb.web.client.place.PlaceService;
import edu.illinois.ncsa.mmdb.web.client.ui.DatasetWidget;
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

	/** Place support for history management **/
	private PlaceService placeService;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		RootPanel.get("mainContainer").add(mainContainer);

		// log events
		logEvent(eventBus);
		
		// place support for history management
//		placeService = new PlaceService(eventBus);
		
		// history support
		History.addValueChangeHandler(this);
		
		parseHistoryToken(History.getToken());
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
				SortedSet<DatasetBean> orderedResult = new TreeSet<DatasetBean>(new Comparator<DatasetBean>() {
					public int compare(DatasetBean arg0, DatasetBean arg1) {
						if(arg0 == arg1) { return 0; }
						String t0 = arg0.getTitle();
						String t1 = arg1.getTitle();
						if(t0 == null && t1 != null) {
							return 1;
						}
						if(t0 != null && t1 == null) {
							return -1;
						}
						if(t0.equals(t1)) {
							return arg0.hashCode() < arg1.hashCode() ? -1 : 1;
							// we already know they're not the same object
							// and they came from a hashset so hash collisions
							// are already a problem before this point
						}
						return t0.compareTo(t1);
					}
				});
				orderedResult.addAll(result.getDatasets());
				for (DatasetBean dataset : orderedResult) {
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
	 * Show information about a particular dataset.
	 */
	private void showDataset() {

//		DatasetView datasetWidget = new DatasetView();
//		DatasetPresenter datasetPresenter = new DatasetPresenter(datasetWidget, eventBus, dispatchAsync);
//		datasetPresenter.bind();
//		
//		mainContainer.clear();
//		mainContainer.add(datasetWidget.asWidget());
		
		DatasetWidget datasetWidget = new DatasetWidget(dispatchAsync);
		mainContainer.clear();
		mainContainer.add(datasetWidget);
		
		String params = History.getToken().substring(History.getToken().lastIndexOf("?")+1);
		
		String[] tokens = params.split("=");
		
		if (tokens[0].equals("id")) {
			datasetWidget.showDataset(tokens[1]);
		}
	}
	
	void uploadDatasets() {
		UploadWidget uploadWidget = new UploadWidget();
		mainContainer.clear();
		mainContainer.add(uploadWidget);
	}
	
	/**
	 * History handler.
	 */
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();
		GWT.log("History changed: " + event.getValue(), null);
		parseHistoryToken(token);
	}
	
	private void parseHistoryToken(String token) {
		if (token.startsWith("dataset")) {
			showDataset();
		} else if(token.startsWith("upload")) {
			uploadDatasets();
		} else if(token.startsWith("listDatasets")) {
			listDatasets();
		} else {
			listDatasets();
		}
	}
}
