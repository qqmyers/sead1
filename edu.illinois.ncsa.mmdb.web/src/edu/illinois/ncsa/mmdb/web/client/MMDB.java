package edu.illinois.ncsa.mmdb.web.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasetsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewCollectionEvent;
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
import edu.illinois.ncsa.mmdb.web.client.ui.HomePage;
import edu.illinois.ncsa.mmdb.web.client.ui.ListCollectionsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginPage;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginStatusWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.NewPasswordPage;
import edu.illinois.ncsa.mmdb.web.client.ui.NotEnabledPage;
import edu.illinois.ncsa.mmdb.web.client.ui.RequestNewPasswordPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SignupPage;
import edu.illinois.ncsa.mmdb.web.client.ui.TagPage;
import edu.illinois.ncsa.mmdb.web.client.ui.TitlePanel;
import edu.illinois.ncsa.mmdb.web.client.ui.UserManagementPage;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
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
	 * TODO switch to using the full uri for session id (instead of email
	 * address)
	 */
	public static String sessionID;
	public static String uploadAppletCredentials;

	public static ArrayList<String> groups;

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

		checkPermissions(History.getToken());
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
		Hyperlink homeLink = new Hyperlink("Home", "home");
		homeLink.addStyleName("navMenuLink");
		navMenu.add(homeLink);
		// bullet
		HTML bullet = new HTML("&bull;");
		bullet.addStyleName("whiteText");
		navMenu.add(bullet);
		// datasets
		Hyperlink listLink = new Hyperlink("Datasets", "listDatasets");
		listLink.addStyleName("navMenuLink");
		navMenu.add(listLink);
		// bullet
		bullet = new HTML("&bull;");
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

		// sign up link
		SimplePanel signupPanel = new SimplePanel();
		signupPanel.addStyleName("signupPanel");
		Anchor signupLink = new Anchor("Sign up");
		signupLink.addStyleName("signupLink");
		signupLink.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				History.newItem("signup");
			}
		});
		signupPanel.add(signupLink);
		RootPanel.get("signup").add(signupPanel);

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

	void goToPage(String action, int page) {
		History.newItem(action + "?page=" + page);
	}

	void goToPage(String action, int page, String sortKey, String listView) {
		History.newItem(action + "?page=" + page + "&sort=" + sortKey
				+ "&view=" + listView);
	}

	int page = 1, numberOfPages = 0;
	String sortKey = "date-desc";
	String viewType = "list";

	private void parsePagingParameters() {
		Map<String, String> params = getParams();

		if(params.containsKey("view")) {
			viewType = params.get("view");
		} else {
			viewType = "list";
		}

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
	}

	private PagingDatasetTableView createListDatasetsView() {
		parsePagingParameters();
		// first choose the kind of view
		DatasetTableView datasetTableView = null;
		if (viewType.equals("grid")) {
			datasetTableView = new DatasetTableFlowGridView();
		} else if(viewType.equals("flow")) {
			datasetTableView = new DatasetTableCoverFlowView();
		} else {
			datasetTableView = new DatasetTableOneColumnView();
		}

		// ask the view how many datasets per page
		pageSize = datasetTableView.getPageSize();

		// now bind the presenter to the view and associate it with the event
		// bus
		PagingDatasetTableView pagingView = new PagingDatasetTableView(page,
				sortKey, viewType);
		pagingView.addStyleName("datasetTable");

		pagingView.setTable(datasetTableView); // here we're wrapping the less
												// generic impl
		PagingDatasetTablePresenter datasetTablePresenter = new PagingDatasetTablePresenter(
				pagingView, eventBus);
		datasetTablePresenter.bind();

		pagingView.addPageChangeHandler(new ValueChangeHandler<Integer>() {
			public void onValueChange(ValueChangeEvent<Integer> event) {
				goToPage("listDatasets", event.getValue(), sortKey, viewType);
			}
		});

		pagingView.addSortKeyChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				goToPage("listDatasets", 1, event.getValue(), viewType);
			}
		});

		pagingView.addViewTypeChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				goToPage("listDatasets", 1, sortKey, event.getValue());
			}
		});

		// finally

		// if we know the number of pages, have the view reflect it
		if (numberOfPages > 0) {
			pagingView.setNumberOfPages(numberOfPages);
		}

		// compute the page size using the table's preferred size
		pageSize = pagingView.getTable().getPageSize();
		// now compute the current page offset
		pageOffset = (page - 1) * pageSize;

		return pagingView;
	}

	private void listDatasets() {
		mainContainer.clear();

		TitlePanel titlePanel = new TitlePanel("List all");

		Anchor rss = new Anchor();
		rss.setHref("rss.xml");
		rss.addStyleName("rssIcon");
		DOM
				.setElementAttribute(rss.getElement(), "type",
						"application/rss+xml");
		rss
				.setHTML("<img src='./images/rss_icon.gif' border='0px' class='navMenuLink'>"); // FIXME
																								// hack

		titlePanel.addEast(rss);

		mainContainer.add(titlePanel);

		// datasets table
		final PagingDatasetTableView listDatasetsView = createListDatasetsView();
		mainContainer.add(listDatasetsView.asWidget());

		int adjustedPageSize = pageSize;
		if(viewType.equals("flow")) {
			adjustedPageSize = 3;
		}
		
		dispatchAsync.execute(new ListDatasets(uriForSortKey(sortKey),
				descForSortKey(sortKey), adjustedPageSize, pageOffset),
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
					GWT.log("Sending event add dataset " + dataset.getTitle(), null);
					AddNewDatasetEvent event = new AddNewDatasetEvent();
					event.setDataset(dataset);
					eventBus.fireEvent(event);
				}
				int np = (result.getDatasetCount() / pageSize) + (result.getDatasetCount() % pageSize != 0 ? 1 : 0);
				listDatasetsView.setNumberOfPages(np);
				numberOfPages = np;
			}
		});

		if (!eventBus.isEventHandled(DatasetDeletedEvent.TYPE)) {
			eventBus.addHandler(DatasetDeletedEvent.TYPE,
					new DatasetDeletedHandler() {
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
	}

	String uriForCollectionSortKey(String key) { // FIXME kludge, make keys full URI's
		if (key.startsWith("title-")) {
			return "http://purl.org/dc/elements/1.1/title";
		} else { // default is creation date
			return "http://purl.org/dc/terms/created";
		}
	}

	private void listCollections() {
		mainContainer.clear();

		TitlePanel titlePanel = new TitlePanel("Collections");
		mainContainer.add(titlePanel);

		parsePagingParameters();

		PagingCollectionTableView view = new PagingCollectionTableView(page,
				sortKey, viewType);
		view.addStyleName("datasetTable");
		PagingCollectionTablePresenter presenter = new PagingCollectionTablePresenter(
				view, eventBus);
		presenter.bind();

		view.addPageChangeHandler(new ValueChangeHandler<Integer>() {
			public void onValueChange(ValueChangeEvent<Integer> event) {
				goToPage("listCollections", event.getValue(), sortKey, viewType);
			}
		});

		view.addSortKeyChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				goToPage("listCollections", 1, event.getValue(), viewType);
			}
		});

		view.addViewTypeChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				goToPage("listCollections", 1, sortKey, event.getValue());
			}
		});

		// we don't know the number of pages
		numberOfPages = 0;

		// once we fix this, this'll update it
		if (numberOfPages > 0) {
			view.setNumberOfPages(numberOfPages);
		} else {
			view.setNumberOfPages(0);
		}

		// for now hardcode the page size
		pageSize = 15;
		// now compute the current page offset
		pageOffset = (page - 1) * pageSize;

		//
		mainContainer.add(view.asWidget());

		// now list the collections
		GetCollections query = new GetCollections();
		query.setSortKey(uriForCollectionSortKey(sortKey));
		query.setDesc(descForSortKey(sortKey));
		query.setOffset(pageOffset);
		query.setLimit(pageSize);

		dispatchAsync.execute(query, new AsyncCallback<GetCollectionsResult>() {
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onSuccess(GetCollectionsResult result) {
				for (CollectionBean collection : result.getCollections()) {
					AddNewCollectionEvent event = new AddNewCollectionEvent(
							collection);
					GWT.log("Firing event add collection "
							+ collection.getTitle(), null);
					eventBus.fireEvent(event);
				}
			}
		});
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
		GWT.log("set upload applet credentials to " + credentials, null);
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
		// uploadToolbar.addStyleName("debugLayout"); // FIXME debug
		VerticalPanel dndPanel = new VerticalPanel();
		dndPanel.setWidth("120px");
		// dndPanel.addStyleName("debugLayout"); // FIXME debug
		final FlowPanel dndApplet = new FlowPanel();
		// dndApplet.addStyleName("debugLayout"); // FIXME debug
		dndApplet.setHeight("60px");
		dndApplet.setWidth("60px");
		dndApplet.getElement().setId("dndApplet");
		dndPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		dndPanel.add(dndApplet);
		if (!dndEnabled) {
			dndApplet.addStyleName("hidden");
		}
		final String disabledMsg = "Click here to drag files and folders from your desktop";
		final String enabledMsg = "Drop files and folders here";
		final Label dndTooltip = new Label(dndEnabled ? enabledMsg
				: disabledMsg);
		dndTooltip.addStyleName("tooltip");
		// dndTooltip.addStyleName("debugLayout"); // FIXME debug
		dndPanel.add(dndTooltip);
		uploadToolbar.add(dndPanel);

		//
		VerticalPanel uploadWidgetPanel = new VerticalPanel();
		uploadWidgetPanel
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		uploadWidgetPanel.add(uploadWidget);
		Label uploadWidgetTooltip = new Label("or choose a file to upload");
		uploadWidgetTooltip.addStyleName("tooltip");
		uploadWidgetPanel.add(uploadWidgetTooltip);
		uploadToolbar.add(uploadWidgetPanel);

		//
		toolbar.add(uploadToolbar);

		if (dndEnabled) {
			dndApplet.removeStyleName("hidden");
			deployDndApplet(uploadAppletCredentials);
		} else {
			dndTooltip.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					boolean doit = true;
					if (!dndEnabled) {
						doit = Window
								.confirm("You will be asked to accept a security exception to allow our drag-and-drop upload tool to access your local files. If you don't wish to accept that security exception, press cancel.");
					}
					if (doit) {
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
	 * History handler. Check if the user has been enabled first, otherwise show
	 * the not enabled page.
	 */
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {

		final String token = event.getValue();

		GWT.log("History changed: " + event.getValue(), null);

		checkPermissions(token);
	}

	/**
	 * Check if user has permission ot view member pages.
	 * 
	 * @param token
	 */
	private void checkPermissions(final String token) {
		if (token.startsWith("login")) {
			showLoginPage();
		} else if (token.startsWith("signup")) {
			showSignupPage();
		} else if (token.startsWith("requestNewPassword")) {
			showRequestNewPasswordPage();
		} else if (checkLogin()) {
			// Check if the user has been activated by an administrator
			dispatchAsync.execute(new HasPermission(MMDB.sessionID,
					Permission.VIEW_MEMBER_PAGES),
					new AsyncCallback<HasPermissionResult>() {

						@Override
						public void onFailure(Throwable caught) {
							GWT
									.log(
											"Error checking if the users has permissions to view member pages",
											caught);
						}

						@Override
						public void onSuccess(HasPermissionResult result) {
							if (result.isPermitted()) {
								parseHistoryToken(token);
							} else {
								showNotEnabledPage();
							}
						}
					});
		}
	}

	/**
	 * Parse history token and show the proper widgets.
	 * 
	 * @param token
	 *            history token (everything after the #)
	 */
	private void parseHistoryToken(String token) {
		if (token.startsWith("login")) {
			showLoginPage();
		} else if (checkLogin()) {
			if (token.startsWith("dataset")) {
				showDataset();
			} else if (token.startsWith("listDatasets")) {
				listDatasets();
			} else if (token.startsWith("upload")) { // upload applet support
				showUploadProgress();
			} else if (token.startsWith("tag")) {
				showTagPage();
			} else if (token.startsWith("listCollections")) {
				listCollections();
			} else if (token.startsWith("collection")) {
				showCollectionPage();
			} else if (token.startsWith("modifyPermissions")) {
				showUsersPage();
			} else if (token.startsWith("signup")) {
				showSignupPage();
			} else if (token.startsWith("newPassword")) {
				showNewPasswordPage();
			} else if (token.startsWith("home")) {
				showHomePage();
			} else {
				listDatasets();
			}
		}
	}

	private void showHomePage() {
		mainContainer.clear();
		mainContainer.add(new HomePage(dispatchAsync));
	}

	private void showNewPasswordPage() {
		mainContainer.clear();
		mainContainer.add(new NewPasswordPage(dispatchAsync));
	}

	private void showRequestNewPasswordPage() {
		mainContainer.clear();
		mainContainer.add(new RequestNewPasswordPage(dispatchAsync));
	}

	/**
	 * For users that haven't been enabled by an admin yet.
	 */
	private void showNotEnabledPage() {
		mainContainer.clear();
		mainContainer.add(new NotEnabledPage());
	}

	/**
	 * Signup on to the system.
	 */
	private void showSignupPage() {
		mainContainer.clear();
		mainContainer.add(new SignupPage(dispatchAsync));
	}

	/**
	 * List users in the system.
	 */
	private void showUsersPage() {
		// Check if the user has view admin pages permission
		dispatchAsync.execute(new HasPermission(MMDB.sessionID,
				Permission.VIEW_ADMIN_PAGES),
				new AsyncCallback<HasPermissionResult>() {

					@Override
					public void onFailure(Throwable caught) {
						GWT
								.log(
										"Error checking if the users has permissions to view admin pages",
										caught);
					}

					@Override
					public void onSuccess(HasPermissionResult result) {
						if (result.isPermitted()) {
							mainContainer.clear();
							mainContainer.add(new UserManagementPage(
									dispatchAsync));
						} else {
							showNoAccessPage();
						}
					}
				});
	}

	/**
	 * A page for when a user doesn't have access to a specific page.
	 */
	private void showNoAccessPage() {
		mainContainer.clear();
		mainContainer.add(new NoAccessPage());
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
	 */
	private void showLoginPage() {
		mainContainer.clear();
		mainContainer.add(new LoginPage(dispatchAsync));
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
