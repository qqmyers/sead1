package edu.illinois.ncsa.mmdb.web.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUploadDestination;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUploadDestinationResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasetsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetHandler;
import edu.illinois.ncsa.mmdb.web.client.event.CancelEvent;
import edu.illinois.ncsa.mmdb.web.client.event.CancelHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedHandler;
import edu.illinois.ncsa.mmdb.web.client.place.PlaceService;
import edu.illinois.ncsa.mmdb.web.client.ui.CollectionPage;
import edu.illinois.ncsa.mmdb.web.client.ui.DatasetWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.ListCollectionsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginPage;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginStatusWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.PagingWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.TagPage;
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

	public static String sessionID;

	/**
	 * Dispatch service. Should be the only service needed. All commands should
	 * go through this endpoint. To learn more look up gwt-dispatch and the
	 * command pattern.
	 */
	public static final MyDispatchAsync dispatchAsync = new MyDispatchAsync();

	/** Event bus for propagating events in the interface **/
	private final HandlerManager eventBus = new HandlerManager(null);

	/** Toolbar above main content panel */
	private FlowPanel toolbar;

	/** The upload button */
	private Anchor uploadButton;

	/** The upload widget in the upload toolbar */
	private FlowPanel uploadPanel;

	/** Main content panel **/
	private FlowPanel mainContainer;

	/** Place support for history management **/
	private PlaceService placeService;

	public static LoginStatusWidget loginStatusWidget;

	/**
	 * Session id - user login for when the user is logged in, null if the user
	 * hasn't been authenticated
	 */
	private final String sessionId = null;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		// navigation menu
		initNavMenu();

		// toolbar
		toolbar = new FlowPanel();
		RootPanel.get("toolbar").add(toolbar);

		// upload panel
		uploadPanel = new FlowPanel();
		RootPanel.get("uploadPanel").add(uploadPanel);

		// main content
		mainContainer = new FlowPanel();
		mainContainer.addStyleName("relativePosition");
		RootPanel.get("mainContainer").add(mainContainer);

		// log events
		logEvent(eventBus);

		// place support for history management
		// placeService = new PlaceService(eventBus);

		// history support
		History.addValueChangeHandler(this);

		parseHistoryToken(History.getToken());
	}

	/**
	 * Navigation menu at the top of the page.
	 */
	void initNavMenu() {
		RootPanel.get("navMenu").clear();
		HorizontalPanel navMenu = new HorizontalPanel();
		RootPanel.get("navMenu").add(navMenu);
		// datasets
		Hyperlink listLink = new Hyperlink("Datasets", "listDatasets");
		listLink.addStyleName("navMenuLink");
		navMenu.add(listLink);
		// bullet
		HTML bullet = new HTML("&bull;");
		bullet.addStyleName("whiteText");
		navMenu.add(bullet);
		// collections
		Hyperlink collectionsLink = new Hyperlink("Collections",
				"listCollections");
		collectionsLink.addStyleName("navMenuLink");
		navMenu.add(collectionsLink);
		// bullet
		HTML bullet2 = new HTML("&bull;");
		bullet2.addStyleName("whiteText");
		navMenu.add(bullet2);
		// upload link
		uploadButton = new Anchor("Upload");
		uploadButton.setStyleName("navMenuLink");
		uploadButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent click) {
				toggleUploadMenu();
			}
		});
		navMenu.add(uploadButton);
		//

		// login menu
		loginStatusWidget = new LoginStatusWidget();
		RootPanel.get("loginMenu").add(loginStatusWidget);

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
	int pageOffset = 0;
	int pageSize = 10;

	String uriForSortKey(String key) { // FIXME kludge, make keys full URI's
		if (key.startsWith("title-")) {
			return "http://purl.org/dc/elements/1.1/title";
		} else { // default is date
			return "http://purl.org/dc/elements/1.1/date";
		}
	}

	boolean descForSortKey(String key) {
		return !key.endsWith("-asc"); // default is descending
	}

	void goToPage(int page) {
		History.newItem("listDatasets?page=" + page);
	}

	void goToPage(int page, String sortKey) {
		History.newItem("listDatasets?page=" + page + "&sort=" + sortKey);
	}

	int page;
	String sortKey = "date-desc";
	PagingWidget datasetListPager = null;

	private void listDatasets() {
		Map<String, String> params = getParams();
		page = 1;
		if (params.containsKey("page")) {
			try {
				page = Integer.parseInt(params.get("page"));
			} catch (Exception x) {
			}
		}
		if (params.containsKey("sort")) {
			sortKey = params.get("sort");
		}
		pageOffset = (page - 1) * pageSize;
		DatasetTableOneColumnView datasetTableWidget = new DatasetTableOneColumnView();
		DatasetTablePresenter datasetTablePresenter = new DatasetTablePresenter(
				datasetTableWidget, eventBus);
		datasetTablePresenter.bind();
		mainContainer.clear();
		
		HorizontalPanel titlePanel = new HorizontalPanel();
		titlePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		titlePanel.addStyleName("titlePanel");
		
		Label titleLabel = new Label("List all");
		titleLabel.addStyleName("pageTitle");

		Anchor rss = new Anchor();
		rss.setHref("rss.xml");
		rss.addStyleName("rssIcon");
		DOM.setElementAttribute(rss.getElement(),"type","application/rss+xml");
		rss.setHTML("<img src='./images/rss_icon.gif' border='0px' class='navMenuLink'>"); // FIXME hack

		titlePanel.add(titleLabel);
		titlePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		titlePanel.add(rss);
		
		mainContainer.add(titlePanel);

		mainContainer.add(createPagingPanel());

		// datasets table
		mainContainer.add(datasetTableWidget.asWidget());
		
		mainContainer.add(createPagingPanel());

		// TODO add a way to switch between the two views
		// DatasetTableView datasetTableWidget = new DatasetTableView();

		dispatchAsync.execute(new ListDatasets(uriForSortKey(sortKey),
				descForSortKey(sortKey), pageSize, pageOffset),
				new AsyncCallback<ListDatasetsResult>() {
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
					public void onSuccess(ListDatasetsResult result) {
						for (DatasetBean dataset : result.getDatasets()) {
							GWT.log("Sending event add dataset "
									+ dataset.getTitle(), null);
							AddNewDatasetEvent event = new AddNewDatasetEvent();
							event.setDataset(dataset);
							eventBus.fireEvent(event);
						}
						int np = result.getDatasetCount() / pageSize;
						datasetListPager.setNumberOfPages(np);
					}
				});
	}

	/**
	 * TODO move to DatasetTableOneColumnView
	 * @return
	 */
	private Widget createPagingPanel() {
		// paging header
		HorizontalPanel pagingPanel = new HorizontalPanel();
		pagingPanel.addStyleName("datasetsPager");
		pagingPanel.addStyleName("centered"); // special IE-friendly centering
												// style

		datasetListPager = new PagingWidget(page);
		datasetListPager
				.addValueChangeHandler(new ValueChangeHandler<Integer>() {
					public void onValueChange(ValueChangeEvent<Integer> event) {
						goToPage(event.getValue(), sortKey);
					}
				});
		pagingPanel.add(datasetListPager);

		Label sortBy = new Label("sort by: ");
		sortBy.addStyleName("pagingLabel");
		pagingPanel.add(sortBy);

		final ListBox sortOptions = new ListBox();
		sortOptions.addItem("Date: newest first", "date-desc");
		sortOptions.addItem("Date: oldest first", "date-asc");
		sortOptions.addItem("Title: A-Z", "title-asc");
		sortOptions.addItem("Title: Z-A", "title-desc");
		sortOptions.addStyleName("pagingLabel");

		for (int i = 0; i < sortOptions.getItemCount(); i++) {
			if (sortKey.equals(sortOptions.getValue(i))) {
				sortOptions.setSelectedIndex(i);
			}
		}

		sortOptions.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				goToPage(page, sortOptions.getValue(sortOptions
						.getSelectedIndex()));
			}
		});
		pagingPanel.add(sortOptions);
		return pagingPanel;
	}

	/**
	 * Parse the parameters in the history token after the '?'
	 * 
	 * @return
	 */
	Map<String, String> getParams() {
		Map<String, String> params = new HashMap<String, String>();
		String paramString = History.getToken().substring(
				History.getToken().indexOf("?") + 1);
		if (!paramString.isEmpty()) {
			for (String paramEntry : paramString.split("&")) {
				String[] terms = paramEntry.split("=");
				if (terms.length == 2) {
					params.put(terms[0], terms[1]);
				}
			}
		}
		return params;
	}

	/**
	 * Show information about a particular dataset.
	 */
	private void showDataset() {

		// DatasetView datasetWidget = new DatasetView();
		// DatasetPresenter datasetPresenter = new
		// DatasetPresenter(datasetWidget, eventBus, dispatchAsync);
		// datasetPresenter.bind();
		//		
		// mainContainer.clear();
		// mainContainer.add(datasetWidget.asWidget());

		if (checkLogin()) {
			DatasetWidget datasetWidget = new DatasetWidget(dispatchAsync);
			mainContainer.clear();
			mainContainer.add(datasetWidget);

			String datasetUri = getParams().get("id"); // FIXME should use
														// "uri?"
			if (datasetUri != null) {
				datasetWidget.showDataset(datasetUri);
			}
		}
	}

	private boolean uploadMenuVisible = false;
	private UploadWidget uploadWidget;

	UploadWidget showUploadMenu() {
		uploadWidget = new UploadWidget();
		uploadWidget.addDatasetUploadedHandler(new DatasetUploadedHandler() {
			public void onDatasetUploaded(DatasetUploadedEvent event) {
				History.newItem("dataset?id=" + event.getDatasetUri());
				DOM.getElementById("uploadToolbar").addClassName("hidden");
				uploadMenuVisible = false;
				uploadPanel.clear();
			}
		});
		uploadWidget.addCancelHandler(new CancelHandler() {
			public void onCancel(CancelEvent event) {
				DOM.getElementById("uploadToolbar").addClassName("hidden");
				uploadMenuVisible = false;
				uploadPanel.clear();
			}
		});
		uploadPanel.add(uploadWidget);
		DOM.getElementById("uploadToolbar").removeClassName("hidden");


		return uploadWidget;
	}

	void hideUploadMenu() {
		toolbar.clear();
		DOM.getElementById("uploadToolbar").addClassName("hidden");
		uploadMenuVisible = false;
	}

	/**
	 * Show toolbar to upload datasets.
	 * 
	 * @return
	 */
	void toggleUploadMenu() {
		toolbar.clear();
		uploadPanel.clear();
		if (!uploadMenuVisible) {
			showUploadMenu();
			uploadMenuVisible = true;
		} else {
			DOM.getElementById("uploadToolbar").addClassName("hidden");
			uploadMenuVisible = false;
		}
	}

	/**
	 * This is for when the POST doesn't come from an HTML form but from a
	 * client that also controls the browser (e.g., an AJAX client or Java
	 * applet); in that case said client can direct the browser to
	 * #upload?session={sessionkey} to trigger the GWT upload progress bar for
	 * the upload.
	 */
	void showUploadProgress() {
		String sessionKey = getParams().get("session");
		if (sessionKey != null) {
			showUploadProgress(sessionKey);
		}
	}

	void showUploadProgress(String sessionKey) {
		if (sessionKey != null) {
			if (!uploadMenuVisible) {
				showUploadMenu(); // pop up the upload interface
			}
			// show the progress bar for the session key
			String uploadServletUrl = GWT.getModuleBaseURL() + "UploadBlob";
			uploadWidget.showProgress(sessionKey, uploadServletUrl);
		}
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

	/**
	 * Parse history token and show the proper widgets.
	 * 
	 * @param token
	 *            history token (everything after the #)
	 */
	private void parseHistoryToken(String token) {
		if (token.startsWith("dataset")) {
			showDataset();
		} else if (token.startsWith("listDatasets")) {
			listDatasets();
		} else if (token.startsWith("upload")) { // upload applet support
			showUploadProgress();
		} else if (token.startsWith("login")) {
			showLoginPage();
		} else if (token.startsWith("tag")) {
			showTagPage();
		} else if (token.startsWith("listCollections")) {
			showListCollectionsPage();
		} else if (token.startsWith("collection")) {
			showCollectionPage();
		} else {
			listDatasets();
		}
	}

	/**
	 * List all collections.
	 */
	private void showListCollectionsPage() {
		mainContainer.clear();
		mainContainer.add(new ListCollectionsPage(dispatchAsync, eventBus));
	}

	/**
	 * Show a specific collection.
	 */
	private void showCollectionPage() {
		mainContainer.clear();
		mainContainer.add(new CollectionPage(getParams().get("uri"),
				dispatchAsync, eventBus));
	}

	/**
	 * Show a specific tag page.
	 */
	private void showTagPage() {
		mainContainer.clear();
		mainContainer.add(new TagPage(getParams().get("title"), dispatchAsync,
				eventBus));
	}

	/**
	 * Show a set of widgets to authenticate with the server.
	 * 
	 * FIXME currently uses a adhoc parsing of the history token. The generic
	 * parameter parsing gets confused with parsing a parameter inside a
	 * parameter (multiple '=')
	 * 
	 */
	private void showLoginPage() {
		mainContainer.clear();
		mainContainer.add(new LoginPage());
	}

	/**
	 * If user not logged in redirect to the required login page.
	 * 
	 * @return true if logged in already, false if not
	 */
	public boolean checkLogin() {
		String cookieSID = Cookies.getCookie("sid");
		if (cookieSID != null) {
			LoginPage.login(cookieSID);
		}
		if (MMDB.sessionID == null) {
			History.newItem("login?p=" + History.getToken());
			return false;
		}
		return true;
	}
}
