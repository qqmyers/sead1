/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.mmdb.web.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.JiraIssue.JiraIssueType;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetHandler;
import edu.illinois.ncsa.mmdb.web.client.event.AllDatasetsUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AllDatasetsUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.place.PlaceService;
import edu.illinois.ncsa.mmdb.web.client.ui.AuthenticationCallback;
import edu.illinois.ncsa.mmdb.web.client.ui.CollectionPage;
import edu.illinois.ncsa.mmdb.web.client.ui.ConfirmDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.ContentCategory;
import edu.illinois.ncsa.mmdb.web.client.ui.DatasetWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.HomePage;
import edu.illinois.ncsa.mmdb.web.client.ui.JiraIssuePage;
import edu.illinois.ncsa.mmdb.web.client.ui.ListCollectionsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.ListDatasetsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginPage;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginStatusWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.MapPage;
import edu.illinois.ncsa.mmdb.web.client.ui.NotEnabledPage;
import edu.illinois.ncsa.mmdb.web.client.ui.RequestNewPasswordPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SearchBox;
import edu.illinois.ncsa.mmdb.web.client.ui.SearchResultsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SelectedDatasetsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SignupPage;
import edu.illinois.ncsa.mmdb.web.client.ui.TagPage;
import edu.illinois.ncsa.mmdb.web.client.ui.TagsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.UploadPage;
import edu.illinois.ncsa.mmdb.web.client.ui.admin.AdminPage;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicTableView;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;

/**
 * MMDB entry point.
 * 
 * @author Luigi Marini
 * @author Rob Kooper
 */
public class MMDB implements EntryPoint, ValueChangeHandler<String> {
    // FIXME move these into UserSessionState?
    public static final String         DATASET_VIEW_TYPE_PREFERENCE     = "datasetViewType";
    public static final String         DATASET_VIEWSIZE_TYPE_PREFERENCE = "datasetViewSizeType";
    public static final String         COLLECTION_VIEW_TYPE_PREFERENCE  = "collectionViewType";
    public static final String         DATASET_VIEW_SORT_PREFERENCE     = "datasetViewSort";

    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    public static final String         SERVER_ERROR                     = "An error occurred while "
                                                                                + "attempting to contact the server. Please check your network "
                                                                                + "connection and try again.";

    public static ArrayList<String>    groups;

    /**
     * Dispatch service. Should be the only service needed. All commands should
     * go through this endpoint. To learn more look up gwt-dispatch and the
     * command pattern.
     */
    public final DispatchAsync         dispatchAsync                    = new MyDispatchAsync();

    /** Event bus for propagating events in the interface **/
    public static final HandlerManager eventBus                         = new HandlerManager(null);

    /** The upload button */
    private Anchor                     uploadButton;

    /** The upload widget in the upload toolbar */
    private FlowPanel                  uploadPanel;

    /** Main content panel **/
    private static final FlowPanel     mainContainer                    = new FlowPanel();

    /** Place support for history management **/
    private PlaceService               placeService;

    public static LoginStatusWidget    loginStatusWidget;

    private String                     previousHistoryToken             = new String();

    private Label                      breadcrumb;

    private static UserSessionState    sessionState;

    private Label                      debugLabel;
    private HorizontalPanel            navMenu;
    private HTML                       adminBbullet;
    private Hyperlink                  adminLink;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        // get mapping of mime-type -> category from server
        ContentCategory.initialize(dispatchAsync);

        RootPanel rootPanel = RootPanel.get("mmdb-mainContainer");

        if (rootPanel != null) {

            // navigation menu
            initNavMenu();

            // breadcrumb
            breadcrumb = new Label("breadcrumb > bread > crumb");
            breadcrumb.addStyleName("breadcrumb");
            RootPanel.get("breadcrumb").add(breadcrumb);

            // main content
            mainContainer.addStyleName("relativePosition");
            RootPanel.get("mmdb-mainContainer").add(mainContainer);

            // log events
            //            logEvent(eventBus);

            eventBus.addHandler(DatasetSelectedEvent.TYPE, new DatasetSelectedHandler() {

                @Override
                public void onDatasetSelected(DatasetSelectedEvent event) {
                    GWT.log("Dataset selected " + event.getUri());
                    MMDB.getSessionState().datasetSelected(event.getUri());
                }
            });

            eventBus.addHandler(DatasetUnselectedEvent.TYPE, new DatasetUnselectedHandler() {
                @Override
                public void onDatasetUnselected(DatasetUnselectedEvent event) {
                    GWT.log("Dataset unselected " + event.getUri());
                    MMDB.getSessionState().datasetUnselected(event.getUri());
                }
            });

            eventBus.addHandler(AllDatasetsUnselectedEvent.TYPE, new AllDatasetsUnselectedHandler() {
                @Override
                public void onAllDatasetsUnselected(AllDatasetsUnselectedEvent event) {
                    GWT.log("All datasets unselected");
                    Set<String> toDeselect = new HashSet<String>(MMDB.getSessionState().getSelectedDatasets());
                    for (String datasetUri : toDeselect ) {
                        DatasetUnselectedEvent ue = new DatasetUnselectedEvent();
                        ue.setUri(datasetUri);
                        eventBus.fireEvent(ue);
                    }
                }
            });

            // TODO place support for history management
            // placeService = new PlaceService(eventBus);

            // history support
            History.addValueChangeHandler(this);

            History.fireCurrentHistoryState();
        }
    }

    /**
     * Navigation menu at the top of the page.
     */
    void initNavMenu() {
        if (RootPanel.get("navMenu") == null) {
            GWT.log("failed to get rootpanel", null);
        }
        RootPanel.get("navMenu").clear();
        navMenu = new HorizontalPanel();
        navMenu.addStyleName("navMenu");
        RootPanel.get("navMenu").add(navMenu);
        // datasets
        final Hyperlink homeLink = new Hyperlink("Home", "home");
        homeLink.addStyleName("navMenuLink");
        navMenu.add(homeLink);
        // bullet
        HTML bullet = new HTML("&bull;");
        bullet.addStyleName("navMenuText");
        navMenu.add(bullet);
        // datasets
        Hyperlink listLink = new Hyperlink("Data", "listDatasets");
        listLink.addStyleName("navMenuLink");
        navMenu.add(listLink);
        // bullet
        bullet = new HTML("&bull;");
        bullet.addStyleName("navMenuText");
        navMenu.add(bullet);
        // collections
        Hyperlink collectionsLink = new Hyperlink("Collections",
                "listCollections");
        collectionsLink.addStyleName("navMenuLink");
        navMenu.add(collectionsLink);
        // bullet
        HTML bullet2 = new HTML("&bull;");
        bullet2.addStyleName("navMenuText");
        navMenu.add(bullet2);
        // tags
        Hyperlink tagsLink = new Hyperlink("Tags",
                "tags");
        tagsLink.addStyleName("navMenuLink");
        navMenu.add(tagsLink);
        // bullet
        HTML bullet3 = new HTML("&bull;");
        bullet3.addStyleName("navMenuText");
        navMenu.add(bullet3);
        // tags
        Hyperlink mapLink = new Hyperlink("Map",
                "map");
        mapLink.addStyleName("navMenuLink");
        navMenu.add(mapLink);
        // bullet
        HTML bullet4 = new HTML("&bull;");
        bullet4.addStyleName("navMenuText");
        navMenu.add(bullet4);
        // upload link
        final Hyperlink uploadLink = new Hyperlink("Upload", "upload");
        uploadLink.addStyleName("navMenuLink");
        navMenu.add(uploadLink);

        adminBbullet = new HTML("&bull;");
        adminBbullet.addStyleName("navMenuText");
        adminBbullet.addStyleName("hidden");
        navMenu.add(adminBbullet);

        // upload link
        adminLink = new Hyperlink("Administration", "administration");
        adminLink.addStyleName("navMenuLink");
        adminLink.addStyleName("hidden");
        navMenu.add(adminLink);

        // FIXME debug
        debugLabel = new Label();
        debugLabel.addStyleName("navMenuText");
        navMenu.add(debugLabel);

        // login menu
        loginStatusWidget = new LoginStatusWidget();
        RootPanel.get("loginMenu").add(loginStatusWidget);

        // search box
        SearchBox searchBox = new SearchBox("Search");
        searchBox.setWidth("150px"); // maybe add this as CSS style?
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

    public void showListDatasetsPage() {
        mainContainer.clear();
        mainContainer.add(new ListDatasetsPage(dispatchAsync, eventBus));
    }

    /**
     * List collections.
     */
    private void listCollections() {
        mainContainer.clear();
        mainContainer.add(new ListCollectionsPage(dispatchAsync, eventBus));
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
            for (String paramEntry : paramString.split("&") ) {
                String[] terms = paramEntry.split("=");
                if (terms.length == 2) {
                    params.put(URL.decodeComponent(terms[0]), URL.decodeComponent(terms[1]));
                }
            }
        }
        return params;
    }

    private PermissionUtil rbac() {
        return new PermissionUtil(dispatchAsync);
    }

    /**
     * Show information about a particular dataset.
     */
    private void showDataset() {
        Map<String, String> params = getParams();
        final String datasetUri = params.get("id");
        final String section = params.get("section");
        rbac().doIfAllowed(Permission.VIEW_DATA, datasetUri, new AccessOrMessageCallback() {
            @Override
            public void onAllowed() {
                DatasetWidget datasetWidget = new DatasetWidget(dispatchAsync, eventBus);
                mainContainer.clear();
                mainContainer.add(datasetWidget);
                if (datasetUri != null) {
                    if (section != null) {
                        datasetWidget.showDataset(datasetUri, URL.decode(section));
                    } else {
                        datasetWidget.showDataset(datasetUri, null);
                    }
                }
            }
        });
    }

    private void showSelected(boolean show) {

        if (show == true) {
            mainContainer.clear();
            mainContainer.add(new SelectedDatasetsPage(dispatchAsync));

        } else {
            ConfirmDialog d = new ConfirmDialog("View Selected", "You must select at least one dataset to view the selected.", false);
            d.getOkText().setText("OK");
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

        if (token.startsWith("logout")) {
            LoginPage.logout();
        } else if (token.startsWith("login")) {
            showLoginPage();
        } else if (token.startsWith("signup")) {
            showSignupPage();
        } else if (token.startsWith("requestNewPassword")) {
            showRequestNewPasswordPage();
        } else if (token.startsWith("jiraBug")) {
            showJiraBugPage();
        } else if (token.startsWith("jiraFeature")) {
            showJiraFeaturePage();
        } else {
            checkLogin();
        }
    }

    /**
     * Check if user has permission ot view member pages.
     * 
     * @param token
     */
    private void checkPermissions(final String token) {
        rbac().doIfAllowed(Permission.VIEW_MEMBER_PAGES, new PermissionCallback() {
            @Override
            public void onAllowed() {
                parseHistoryToken(token);
            }

            @Override
            public void onDenied() {
                if (getSessionState().isAnonymous()) {
                    showLoginPage();
                } else {
                    showNotEnabledPage();
                }
            }
        });

        // admin menu
        rbac().doIfAllowed(Permission.VIEW_ADMIN_PAGES, new PermissionCallback() {
            @Override
            public void onAllowed() {
                adminBbullet.removeStyleName("hidden");
                adminLink.removeStyleName("hidden");
            }

            @Override
            public void onDenied() {
                adminBbullet.addStyleName("hidden");
                adminLink.addStyleName("hidden");
            }
        });
    }

    /**
     * Set the session id, add a cookie and add history token.
     * 
     * @param sessionId
     * 
     */
    public void login(final String userId, final String sessionKey) {
        login(userId, sessionKey, new AuthenticationCallback() {
            @Override
            public void onFailure() {
            }

            @Override
            public void onSuccess(String userUri, String sessionKey) {
            }
        });
    }

    public void login(final String userId, final String sessionKey, final AuthenticationCallback callback) {
        final UserSessionState state = MMDB.getSessionState();
        state.setSessionKey(sessionKey);
        // set cookie
        // TODO move to more prominent place... MMDB? A class with static properties?
        final long DURATION = 1000 * 60 * 60; // 60 minutes
        final Date expires = new Date(System.currentTimeMillis() + DURATION);
        Cookies.setCookie("sessionKey", sessionKey, expires);

        AsyncCallback<GetUserResult> handler = new AsyncCallback<GetUserResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error retrieving user with id " + userId);
                callback.onFailure();
            }

            @Override
            public void onSuccess(GetUserResult result) {
                PersonBean personBean = result.getPersonBean();
                state.setCurrentUser(personBean);
                if (result.isAnonymous()) {
                    MMDB.loginStatusWidget.logout();
                    getSessionState().setAnonymous(true);
                    GWT.log("Current user is anonymous");
                } else {
                    MMDB.loginStatusWidget.login(personBean.getName());
                    getSessionState().setAnonymous(false);
                    GWT.log("Current user set to " + personBean.getUri());
                }
                Cookies.setCookie("sid", personBean.getUri(), expires);
                checkPermissions(History.getToken());
                callback.onSuccess(userId, sessionKey);
            }
        };

        GetUser getUser = new GetUser();
        getUser.setUserId(userId);
        dispatchAsync.execute(getUser, handler);
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
        } else if (token.startsWith("listDatasets") && !previousHistoryToken.startsWith("listDatasets")) {
            showListDatasetsPage();
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
        } else if (token.startsWith("map")) {
            showMapPage();
        } else if (token.startsWith("signup")) {
            showSignupPage();
        } else if (token.startsWith("home")) {
            showHomePage();
        } else if (token.startsWith("viewSelected")) {
            showSelected(true);
        } else if (token.startsWith("editRelationships")) {
            showSelected(true);
        } else if (token.startsWith("noneSelected")) {
            showSelected(false);
        } else if (token.startsWith("administration")) {
            showAdminPage();
        } else if (!previousHistoryToken.startsWith("listDatasets")) {
            showListDatasetsPage();
        }
        previousHistoryToken = token;
    }

    private void showAdminPage() {
        GWT.log("Loading Admin Page", null);
        mainContainer.clear();
        mainContainer.add(new AdminPage(dispatchAsync, eventBus));
    }

    private void showMapPage() {
        GWT.log("Loading Map Page", null);
        mainContainer.clear();
        mainContainer.add(new MapPage(dispatchAsync, eventBus));
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
        rbac().doIfAllowed(Permission.UPLOAD_DATA, new AccessOrMessageCallback() {
            @Override
            public void onAllowed() {
                GWT.log("Loading Upload Page", null);
                mainContainer.clear();
                mainContainer.add(new UploadPage(dispatchAsync));
            }
        });
    }

    private void showHomePage() {
        mainContainer.clear();
        mainContainer.add(new HomePage(dispatchAsync));
    }

    private void showRequestNewPasswordPage() {
        mainContainer.clear();
        mainContainer.add(new RequestNewPasswordPage(dispatchAsync));
    }

    /**
     * Show a form to allow the user to submit a jira bug.
     */
    private void showJiraBugPage() {
        mainContainer.clear();
        mainContainer.add(new JiraIssuePage(dispatchAsync, JiraIssueType.BUG));
    }

    /**
     * Show a form to allow the user to submit a jira feature.
     */
    private void showJiraFeaturePage() {
        mainContainer.clear();
        mainContainer.add(new JiraIssuePage(dispatchAsync, JiraIssueType.FEATURE));
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

    abstract class AccessOrMessageCallback extends PermissionCallback {
        @Override
        public void onDenied() {
            showNoAccessPage();
        }
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
        mainContainer.add(new TagPage(getParams().get("title"), dispatchAsync, eventBus));
    }

    /**
     * Show a set of widgets to authenticate with the server.
     */
    private void showLoginPage() {
        adminBbullet.addStyleName("hidden");
        adminLink.addStyleName("hidden");
        loginStatusWidget.logout();
        mainContainer.clear();
        mainContainer.add(new LoginPage(dispatchAsync, this));
    }

    /**
     * If user not logged in redirect to the required login page.
     * 
     */
    public void checkLogin() {
        boolean loggedIn = true;
        final String cookieSID = Cookies.getCookie("sid");
        if (cookieSID != null) {
            GWT.log("Sid: " + cookieSID, null);
        } else {
            loggedIn = false;
        }
        final String cookieSessionKey = Cookies.getCookie("sessionKey");
        if (cookieSessionKey != null) {
            GWT.log("Session key: " + cookieSessionKey, null);
        } else {
            loggedIn = false;
        }
        if (!loggedIn) {
            //showLoginPage();
            GWT.log("not logged in, attempting to login as anonymous");
            LoginPage.authenticate(dispatchAsync, MMDB.this,
                    "anonymous", "none", new
                    AuthenticationCallback() {

                        @Override
                        public void onFailure() {
                            showLoginPage();
                        }

                        @Override
                        public void onSuccess(String userUri, String
                                sessionKey) {
                            GWT.log("logged in as anonymous");
                        }
                    });
        } else {
            // now check REST auth
            Command onLoggedIn = new Command() {
                public void execute() {
                    login(cookieSID, cookieSessionKey); // do we need to do this?
                }
            };
            Command onNotLoggedIn = new Command() {
                public void execute() {
                    showLoginPage();
                }
            };
            LoginPage.checkRestAuth(onLoggedIn, onNotLoggedIn);
        }
    }

    // session state
    public static UserSessionState getSessionState() {
        if (sessionState == null) {
            sessionState = new UserSessionState();
        }
        return sessionState;
    }

    /** Get rid of the session state; (e.g., when logging out) */
    public static void clearSessionState() {
        getSessionState().initialize();
        initializePreferences();
    }

    /**
     * Put the user session state in its initial state, with default values only
     * and no personal information.
     */
    static void initializePreferences() {
        getSessionState().getPreferences().put(MMDB.DATASET_VIEW_TYPE_PREFERENCE, DynamicTableView.GRID_VIEW_TYPE);
        getSessionState().getPreferences().put(MMDB.DATASET_VIEWSIZE_TYPE_PREFERENCE, DynamicTableView.PAGE_SIZE_X1);
        getSessionState().getPreferences().put(MMDB.COLLECTION_VIEW_TYPE_PREFERENCE, DynamicTableView.LIST_VIEW_TYPE);
    }

    // a common idiom
    /** Get the currently-logged-in username, if any */
    public static String getUsername() {
        PersonBean currentUser = getSessionState().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUri();
        } else {
            return null;
        }
    }

    // session preferences
    private static Map<String, String> getSessionPreferences() {
        return getSessionState().getPreferences();
    }

    public static String getSessionPreference(String key) {
        return getSessionPreference(key, null);
    }

    public static String getSessionPreference(String key, String defaultValue) {
        String value = getSessionPreferences().get(key);
        return value == null ? defaultValue : value;
    }

    public static void setSessionPreference(String key, String value) {
        getSessionPreferences().put(key, value);
    }
}
