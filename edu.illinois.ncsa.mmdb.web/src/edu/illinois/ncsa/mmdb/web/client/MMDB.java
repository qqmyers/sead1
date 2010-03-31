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
import com.google.gwt.user.client.ui.Button;
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
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewCollectionEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetHandler;
import edu.illinois.ncsa.mmdb.web.client.event.CancelEvent;
import edu.illinois.ncsa.mmdb.web.client.event.CancelHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUploadedHandler;
import edu.illinois.ncsa.mmdb.web.client.place.PlaceService;
import edu.illinois.ncsa.mmdb.web.client.ui.CollectionPage;
import edu.illinois.ncsa.mmdb.web.client.ui.DatasetWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.HomePage;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginPage;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginStatusWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.NewPasswordPage;
import edu.illinois.ncsa.mmdb.web.client.ui.NotEnabledPage;
import edu.illinois.ncsa.mmdb.web.client.ui.RequestNewPasswordPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SearchBox;
import edu.illinois.ncsa.mmdb.web.client.ui.SearchResultsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SignupPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SparqlPage;
import edu.illinois.ncsa.mmdb.web.client.ui.TagPage;
import edu.illinois.ncsa.mmdb.web.client.ui.TagsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.TitlePanel;
import edu.illinois.ncsa.mmdb.web.client.ui.UploadPage;
import edu.illinois.ncsa.mmdb.web.client.ui.UserManagementPage;
import edu.illinois.ncsa.mmdb.web.client.ui.WatermarkTextBox;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

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
	public static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	public static ArrayList<String> groups;

	/**
	 * Dispatch service. Should be the only service needed. All commands should
	 * go through this endpoint. To learn more look up gwt-dispatch and the
	 * command pattern.
	 */
	public static final MyDispatchAsync dispatchAsync = new MyDispatchAsync();

	/** Event bus for propagating events in the interface **/
	public static final HandlerManager eventBus = new HandlerManager(null);

	/** The upload button */
	private Anchor uploadButton;

	/** The upload widget in the upload toolbar */
	private FlowPanel uploadPanel;

	/** Main content panel **/
	private static final FlowPanel mainContainer = new FlowPanel();

	/** Place support for history management **/
	private PlaceService placeService;

	public static LoginStatusWidget loginStatusWidget;
	
	private String previousHistoryToken = new String();

	private Label breadcrumb;
	
	private static UserSessionState sessionState;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		// navigation menu
		initNavMenu();
		
		// breadcrumb
		breadcrumb = new Label("breadcrumb > bread > crumb");
		breadcrumb.addStyleName("breadcrumb");
		RootPanel.get("breadcrumb").add(breadcrumb);
		
		// main content
		mainContainer.addStyleName("relativePosition");
		RootPanel.get("mainContainer").add(mainContainer);

		// log events
		logEvent(eventBus);

		// TODO place support for history management
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
		if(RootPanel.get("navMenu") == null) {
			GWT.log("BARF! failed to get rootpanel", null);
		}
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
		// tags
		Hyperlink tagsLink = new Hyperlink("Tags",
				"tags");
		tagsLink.addStyleName("navMenuLink");
		navMenu.add(tagsLink);
		// bullet
		HTML bullet3 = new HTML("&bull;");
		bullet3.addStyleName("whiteText");
		navMenu.add(bullet3);
		// upload link
		Hyperlink uploadLink = new Hyperlink("Upload", "upload");
		uploadLink.addStyleName("navMenuLink");
		navMenu.add(uploadLink);

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
		
		// search box
		SearchBox searchBox = new SearchBox("Search");
		RootPanel.get("searchMenu").add(searchBox);

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

	public static void listDatasets() {
		listDatasets(null);
	}
	
	public static void listDatasets(String inCollection) {
		mainContainer.clear();

		TitlePanel titlePanel = new TitlePanel("Datasets");
		if(inCollection != null) {
			titlePanel = new TitlePanel(inCollection); // FIXME use collection name
		}

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

		PagingDatasetTableView pagingView = new PagingDatasetTableView();
		pagingView.addStyleName("datasetTable");
		PagingDatasetTablePresenter datasetTablePresenter = new PagingDatasetTablePresenter(
				pagingView, eventBus);
		datasetTablePresenter.bind();

		mainContainer.add(pagingView.asWidget());
		
		/*
		if (!eventBus.isEventHandled(DatasetDeletedEvent.TYPE)) {
			eventBus.addHandler(DatasetDeletedEvent.TYPE,
					new DatasetDeletedHandler() {
						public void onDeleteDataset(DatasetDeletedEvent event) {
							ListDatasets query = new ListDatasets();
							query.setOrderBy(uriForSortKey(sortKey));
							query.setDesc(descForSortKey(sortKey));
							query.setLimit(adjustedPageSize);
							query.setOffset(pageOffset+adjustedPageSize-1);
							dispatchAsync.execute(query,
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
		*/
	}
	
	/**
	 * List collections.
	 * 
	 * FIXME move code to ListCollectionsPage
	 */
	private void listCollections() {
		mainContainer.clear();

		TitlePanel titlePanel = new TitlePanel("Collections");
		mainContainer.add(titlePanel);

		PagingCollectionTableView view = new PagingCollectionTableView();
		view.addStyleName("datasetTable");
		PagingCollectionTablePresenter presenter = new PagingCollectionTablePresenter(
				view, eventBus);
		presenter.bind();
		
		view.setNumberOfPages(0);

		mainContainer.add(view.asWidget());
		
		// create collection
		FlowPanel addCollectionPanel = new FlowPanel();
		final WatermarkTextBox addCollectionBox = new WatermarkTextBox("",
				"Collection name");
		addCollectionPanel.add(addCollectionBox);
		Button addButton = new Button("Add", new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				final CollectionBean collection = new CollectionBean();
				collection.setTitle(addCollectionBox.getText());

				dispatchAsync.execute(new AddCollection(collection, getSessionState().getUsername()),
						new AsyncCallback<AddCollectionResult>() {

							@Override
							public void onFailure(Throwable arg0) {
								GWT.log("Failed creating new collection", arg0);
							}

							@Override
							public void onSuccess(AddCollectionResult arg0) {
								AddNewCollectionEvent event = new AddNewCollectionEvent(
										collection);
								GWT.log("Firing event add collection "
										+ collection.getTitle(), null);
								eventBus.fireEvent(event);
							}
						});
			}
		});
		addCollectionPanel.add(addButton);
		mainContainer.add(addCollectionPanel);
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
	
	/**
	 * @Deprecated Use UploadPage.java instead
	 */
	native void deployDndApplet(String credentials) /*-{
		var attributes = {
		code:'edu.illinois.ncsa.mmdb.web.client.dnd.DropUploader',
		archive:'dnd/DropUploader-490.jar,dnd/lib/commons-codec-1.2.jar,dnd/lib/commons-httpclient-3.0.1.jar,dnd/lib/commons-httpclient-contrib-ssl-3.1.jar,dnd/lib/commons-logging-1.0.4.jar',
		width:60,
		height:60
		};
		var parameters = {
		jnlp_href: 'dropuploader.jnlp',
		statusPage: $wnd.document.URL,
		"credentials": credentials,
		background: "0x006699",
		};
		$wnd.deployJava.runApplet(attributes, parameters, '1.5');
		$wnd.document.getElementById('dndApplet').innerHTML = $wnd.deployJava.getDocument();
	}-*/;

	/**
	 * @Deprecated Use UploadPage.java instead
	 */
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

//		toolbar.add(uploadToolbar);

		final String sessionKey = getSessionState().getSessionKey();
		if (dndEnabled) {
			dndApplet.removeStyleName("hidden");
			deployDndApplet(sessionKey);
		} else {
			dndTooltip.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					if(sessionKey == null) {
						Window.confirm("Upload not permitted. Please log in");
					} else {
						boolean doit = true;
						if (!dndEnabled) {
							doit = Window
							.confirm("You will be asked to accept a security exception to allow our drag-and-drop upload tool to access your local files. If you don't wish to accept that security exception, press cancel.");
						}
						if (doit) {
							dndApplet.removeStyleName("hidden");
							deployDndApplet(sessionKey);
							dndTooltip.setText(enabledMsg);
							dndEnabled = true;
						}
					}
				}
			});
		}

		uploadMenuVisible = true;

		return uploadWidget;
	}

	void hideUploadMenu() {
//		toolbar.clear();
		uploadMenuVisible = false;
	}

	/**
	 * Show toolbar to upload datasets.
	 * 
	 * @return
	 */
	void toggleUploadMenu() {
		if (!uploadMenuVisible && checkLogin()) {
			
			// Check if the user has been activated by an administrator
			dispatchAsync.execute(new HasPermission(getSessionState().getUsername(),
					Permission.VIEW_MEMBER_PAGES),
					new AsyncCallback<HasPermissionResult>() {

						@Override
						public void onFailure(Throwable caught) {
							GWT.log("Error checking if the users has " +
									"permissions to view member pages",
											caught);
						}

						@Override
						public void onSuccess(HasPermissionResult result) {
							if (result.isPermitted()) {
								showUploadMenu();
							} else {
								hideUploadMenu();
							}
						}
					});
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
			dispatchAsync.execute(new HasPermission(getSessionState().getUsername(),
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
		} else {
			showLoginPage();
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
			} else if (token.startsWith("listDatasets") && !previousHistoryToken.startsWith("listDatasets")) {
				listDatasets();
			} else if (token.startsWith("upload")) {
				showUploadPage();
			} else if (token.startsWith("tags")) {
				shosTagsPage();
			} else if (token.startsWith("tag")) {
				showTagPage();
			} else if (token.startsWith("listCollections") && !previousHistoryToken.startsWith("listCollections")) {
				listCollections();
			} else if (token.startsWith("listCollections")) {
				// skip default case!
			} else if (token.startsWith("collection")) {
				showCollectionPage();
			} else if (token.startsWith("search")) {
				showSearchResultsPage();
			} else if (token.startsWith("modifyPermissions")) {
				showUsersPage();
			} else if (token.startsWith("signup")) {
				showSignupPage();
			} else if (token.startsWith("newPassword")) {
				showNewPasswordPage();
			} else if (token.startsWith("home")) {
				showHomePage();
			} else if (token.startsWith("sparql")) {
				showSparqlPage();
			} else if(!previousHistoryToken.startsWith("listDatasets")) {
				listDatasets();
			}
		}
		previousHistoryToken = token;
	}

	private void shosTagsPage() {
		GWT.log("Loading Tags Page", null);
		mainContainer.clear();
		mainContainer.add(new TagsPage(dispatchAsync));
	}

	private void showSearchResultsPage() {
		GWT.log("Loading Search Results Page", null);
		mainContainer.clear();
		mainContainer.add(new SearchResultsPage(dispatchAsync, eventBus));
	}

	private void showUploadPage() {
		GWT.log("Loading Upload Page", null);
		mainContainer.clear();
		mainContainer.add(new UploadPage(dispatchAsync));
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
		dispatchAsync.execute(new HasPermission(getSessionState().getUsername(),
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
	 * List users in the system.
	 */
	private void showSparqlPage() {
		// Check if the user has view admin pages permission
		dispatchAsync.execute(new HasPermission(getSessionState().getUsername(),
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
							mainContainer.add(new SparqlPage(dispatchAsync));
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
		if(cookieSID != null) {
			GWT.log("Sid: "+cookieSID, null);
		}
		String cookieSessionKey = Cookies.getCookie("sessionKey");
		if(cookieSessionKey != null) {
			GWT.log("Session key: "+cookieSessionKey,null);
		}
		if (cookieSID != null && cookieSessionKey != null) {
			LoginPage.login(cookieSID, cookieSessionKey);
			return true;
		} else {
			return false;
		}
	}
	
	public static final String DATASET_VIEW_TYPE_PREFERENCE = "datasetViewType";
	public static final String COLLECTION_VIEW_TYPE_PREFERENCE = "collectionViewType";
	
	// session state
	public static UserSessionState getSessionState() {
		if(sessionState == null) {
			sessionState = new UserSessionState();
		}
		return sessionState;
	}
	
	// a common idiom
	/** Get the currently-logged-in username, if any */
	public static String getUsername() {
		return getSessionState().getUsername();
	}
	
	// session preferences
	private static Map<String,String> getSessionPreferences() {
		return getSessionState().getPreferences();
	}
	
	public static String getSessionPreference(String key) {
		return getSessionPreference(key,null);
	}
	public static String getSessionPreference(String key, String defaultValue) {
		String value = getSessionPreferences().get(key);
		return value == null ? defaultValue : value;
	}
	public static void setSessionPreference(String key, String value) {
		getSessionPreferences().put(key,value);
	}
}
