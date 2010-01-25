package edu.illinois.ncsa.mmdb.web.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.DatasetTablePresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasetsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetHandler;
import edu.illinois.ncsa.mmdb.web.client.event.CancelEvent;
import edu.illinois.ncsa.mmdb.web.client.event.CancelHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedHandler;
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
import edu.illinois.ncsa.mmdb.web.client.ui.TitlePanel;
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
	public static String uploadAppletCredentials;

	/**
	 * Dispatch service. Should be the only service needed. All commands should
	 * go through this endpoint. To learn more look up gwt-dispatch and the
	 * command pattern.
	 */
	public static final MyDispatchAsync dispatchAsync = new MyDispatchAsync();

	/** Event bus for propagating events in the interface **/
	public static final HandlerManager eventBus = new HandlerManager(null);

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

	Label debugLabel;
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

		// FIXME debug
		debugLabel = new Label();
		debugLabel.addStyleName("whiteText");
		navMenu.add(debugLabel);
		
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
	void goToPage(int page, String sortKey, String listView) {
		History.newItem("listDatasets?page=" + page + "&sort=" + sortKey + "&view=" + listView);
	}

	int page=1, numberOfPages=0;
	String sortKey = "date-desc";
	String listView = "list";

	private void listDatasets() {
		Map<String, String> params = getParams();
		
		// first choose the kind of view
		DatasetTableView datasetTableView = null;
		if(params.containsKey("view") && params.get("view").equals("grid")) {
			listView = "grid";
		} else if(params.containsKey("view") && params.get("view").equals("list")) {
			listView = "list";
		}
		if(listView.equals("grid")) {
			datasetTableView = new DatasetTableFlowGridView();
		} else {
			datasetTableView = new DatasetTableOneColumnView();
		}
		
		// ask the view how many datasets per page
		pageSize = datasetTableView.getPageSize();
		
		// figure out what page we're on
		page = 1;
		if (params.containsKey("page")) {
			try {
				page = Integer.parseInt(params.get("page"));
			} catch (Exception x) {
			}
		}
		// and how we need to sort
		if (params.containsKey("sort")) {
			sortKey = params.get("sort");
		}
		// compute the page offset
		pageOffset = (page - 1) * pageSize;
		
		// now bind the presenter to the view and associate it with the event bus
		DatasetTablePresenter datasetTablePresenter = new DatasetTablePresenter(
				datasetTableView, eventBus);
		datasetTablePresenter.bind();
		//
		
		mainContainer.clear();
		
		TitlePanel titlePanel = new TitlePanel("List all");
		
		Anchor rss = new Anchor();
		rss.setHref("rss.xml");
		rss.addStyleName("rssIcon");
		DOM.setElementAttribute(rss.getElement(),"type","application/rss+xml");
		rss.setHTML("<img src='./images/rss_icon.gif' border='0px' class='navMenuLink'>"); // FIXME hack
		
		titlePanel.addEast(rss);

		mainContainer.add(titlePanel);

		final PagingWidget topPager = new PagingWidget();
		mainContainer.add(createPagingPanel(topPager));

		// datasets table
		mainContainer.add(datasetTableView.asWidget());
		
		final PagingWidget bottomPager = new PagingWidget();
		mainContainer.add(createPagingPanel(bottomPager));

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
						topPager.setNumberOfPages(np);
						bottomPager.setNumberOfPages(np);
						numberOfPages = np;
					}
				});
		
		eventBus.addHandler(DatasetDeletedEvent.TYPE, new DatasetDeletedHandler() {
			public void onDeleteDataset(DatasetDeletedEvent event) {
				dispatchAsync.execute(new ListDatasets(uriForSortKey(sortKey),descForSortKey(sortKey),1,pageOffset+pageSize-1),
						new AsyncCallback<ListDatasetsResult>() {
							public void onFailure(Throwable caught) {
							}
							public void onSuccess(ListDatasetsResult result) {
								for (DatasetBean dataset : result.getDatasets()) {
									GWT.log("Sending event add dataset "
											+ dataset.getTitle(), null);
									AddNewDatasetEvent event = new AddNewDatasetEvent();
									event.setDataset(dataset);
									eventBus.fireEvent(event);
								}
							}
						});
			}
		});
	}

	/**
	 * TODO move to DatasetTableOneColumnView
	 * @return
	 */
	private Widget createPagingPanel(PagingWidget pagingWidget) {
		// paging header
		HorizontalPanel pagingPanel = new HorizontalPanel();
		pagingPanel.addStyleName("datasetsPager");
		pagingPanel.addStyleName("centered"); // special IE-friendly centering style

		pagingWidget.setPage(page);
		pagingWidget.setNumberOfPages(numberOfPages); // updated on listdatasets callback
		pagingWidget
				.addValueChangeHandler(new ValueChangeHandler<Integer>() {
					public void onValueChange(ValueChangeEvent<Integer> event) {
						goToPage(event.getValue(), sortKey, listView);
					}
				});
		pagingPanel.add(pagingWidget);

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
				goToPage(1, sortOptions.getValue(sortOptions
						.getSelectedIndex()), listView);
			}
		});
		pagingPanel.add(sortOptions);
		
		Label viewOptionsLabel = new Label("view:");
		viewOptionsLabel.addStyleName("pagingLabel");
		
		final ListBox viewOptions = new ListBox();
		viewOptions.addItem("list", "list");
		viewOptions.addItem("grid", "grid");
		viewOptions.addStyleName("pagingLabel");
		
		for (int i = 0; i < viewOptions.getItemCount(); i++) {
			if (listView.equals(viewOptions.getValue(i))) {
				viewOptions.setSelectedIndex(i);
			}
		}

		viewOptions.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				goToPage(1, sortKey, viewOptions.getValue(viewOptions.getSelectedIndex()));
			}
		});

		pagingPanel.add(viewOptionsLabel);
		pagingPanel.add(viewOptions);
		
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

	private boolean dndEnabled = false;
	
	public static void setUploadAppletCredentials(String credentials) {
		GWT.log("set upload applet credentials to "+credentials, null);
		uploadAppletCredentials = credentials;
	}
	native void deployDndApplet(String credentials) /*-{
		var attributes = {
			code:'edu.illinois.ncsa.mmdb.web.client.dnd.DropUploader',
			archive:'dnd/DropUploader.jar,dnd/lib/commons-codec-1.2.jar,dnd/lib/commons-httpclient-3.0.1.jar,dnd/lib/commons-httpclient-contrib-ssl-3.1.jar,dnd/lib/commons-logging-1.0.4.jar',
			width:60,
			height:60
		};
		var parameters = {
			jnlp_href: 'dropuploader.jnlp',
			statusPage: $wnd.document.URL,
			"credentials": credentials,
			background: "0x006699"
		};
		$wnd.deployJava.runApplet(attributes, parameters, '1.5');
		$wnd.document.getElementById('dndApplet').innerHTML = $wnd.deployJava.getDocument();
	}-*/;
	
	UploadWidget showUploadMenu() {
		uploadWidget = new UploadWidget();
		uploadWidget.addDatasetUploadedHandler(new DatasetUploadedHandler() {
			public void onDatasetUploaded(DatasetUploadedEvent event) {
				History.newItem("dataset?id=" + event.getDatasetUri());
				hideUploadMenu();
			}
		});
		uploadWidget.addCancelHandler(new CancelHandler() {
			public void onCancel(CancelEvent event) {
				hideUploadMenu();
			}
		});
		HorizontalPanel uploadToolbar = new HorizontalPanel();
		uploadToolbar.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		//uploadToolbar.addStyleName("debugLayout"); // FIXME debug
		VerticalPanel dndPanel = new VerticalPanel();
		dndPanel.setWidth("120px");
		//dndPanel.addStyleName("debugLayout"); // FIXME debug
		final FlowPanel dndApplet = new FlowPanel();
		//dndApplet.addStyleName("debugLayout"); // FIXME debug
		dndApplet.setHeight("60px");
		dndApplet.setWidth("60px");
		dndApplet.getElement().setId("dndApplet");
		dndPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		dndPanel.add(dndApplet);
		if(!dndEnabled) {
			dndApplet.addStyleName("hidden");
		}
		final String disabledMsg = "Click here to drag files and folders from your desktop";
		final String enabledMsg = "Drop files and folders here";
		final Label dndTooltip = new Label(dndEnabled ? enabledMsg : disabledMsg);
		dndTooltip.addStyleName("tooltip");
		//dndTooltip.addStyleName("debugLayout"); // FIXME debug
		dndPanel.add(dndTooltip);
		uploadToolbar.add(dndPanel);
		
		//
		VerticalPanel uploadWidgetPanel = new VerticalPanel();
		uploadWidgetPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		uploadWidgetPanel.add(uploadWidget);
		Label uploadWidgetTooltip = new Label("or choose a file to upload");
		uploadWidgetTooltip.addStyleName("tooltip");
		uploadWidgetPanel.add(uploadWidgetTooltip);
		uploadToolbar.add(uploadWidgetPanel);
		
		//
		toolbar.add(uploadToolbar);

		if(dndEnabled) {
			dndApplet.removeStyleName("hidden");
			deployDndApplet(uploadAppletCredentials);
		} else {
			dndTooltip.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					boolean doit = true;
					if(!dndEnabled) {
						doit = Window.confirm("You will be asked to accept a security exception to allow our drag-and-drop upload tool to access your local files. If you don't wish to accept that security exception, press cancel.");
					}
					if(doit) {
						dndApplet.removeStyleName("hidden");
						deployDndApplet(uploadAppletCredentials);
						dndTooltip.setText(enabledMsg);
						dndEnabled = true;
					}
				}
			});
		}

		uploadMenuVisible = true;
		
		return uploadWidget;
	}

	void hideUploadMenu() {
		toolbar.clear();
		uploadMenuVisible = false;
	}

	/**
	 * Show toolbar to upload datasets.
	 * 
	 * @return
	 */
	void toggleUploadMenu() {
		if (!uploadMenuVisible && checkLogin()) {
			showUploadMenu();
		} else {
			hideUploadMenu();
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
		if(token.startsWith("login")) {
			showLoginPage();
		} else if(checkLogin()) {
			if (token.startsWith("dataset")) {
				showDataset();
			} else if (token.startsWith("listDatasets")) {
				listDatasets();
			} else if (token.startsWith("upload")) { // upload applet support
				showUploadProgress();
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
		// FIXME debug
		/*
		String dl = (cookieSID != null ? "sid="+cookieSID : "")
		+ (MMDB.sessionID != null ? ",sessionID="+MMDB.sessionID : "");
		debugLabel.setText(dl);
		// end debug
		 */
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
