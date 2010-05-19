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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
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

import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.JiraIssue.JiraIssueType;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetHandler;
import edu.illinois.ncsa.mmdb.web.client.event.AllDatasetsUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AllDatasetsUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.place.PlaceService;
import edu.illinois.ncsa.mmdb.web.client.ui.CollectionPage;
import edu.illinois.ncsa.mmdb.web.client.ui.DatasetWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.HomePage;
import edu.illinois.ncsa.mmdb.web.client.ui.JiraIssuePage;
import edu.illinois.ncsa.mmdb.web.client.ui.ListCollectionsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.ListDatasetsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginPage;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginStatusWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.NotEnabledPage;
import edu.illinois.ncsa.mmdb.web.client.ui.RequestNewPasswordPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SearchBox;
import edu.illinois.ncsa.mmdb.web.client.ui.SearchResultsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SignupPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SparqlPage;
import edu.illinois.ncsa.mmdb.web.client.ui.TagPage;
import edu.illinois.ncsa.mmdb.web.client.ui.TagsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.UploadPage;
import edu.illinois.ncsa.mmdb.web.client.ui.UserManagementPage;
import edu.uiuc.ncsa.cet.bean.PersonBean;

/**
 * MMDB entry point.
 * 
 * @author Luigi Marini
 * @author Rob Kooper
 */
public class MMDB implements EntryPoint, ValueChangeHandler<String> {
    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    public static final String          SERVER_ERROR         = "An error occurred while "
                                                                     + "attempting to contact the server. Please check your network "
                                                                     + "connection and try again.";

    public static ArrayList<String>     groups;

    /**
     * Dispatch service. Should be the only service needed. All commands should
     * go through this endpoint. To learn more look up gwt-dispatch and the
     * command pattern.
     */
    public static final MyDispatchAsync dispatchAsync        = new MyDispatchAsync();

    /** Event bus for propagating events in the interface **/
    public static final HandlerManager  eventBus             = new HandlerManager(null);

    /** The upload button */
    private Anchor                      uploadButton;

    /** The upload widget in the upload toolbar */
    private FlowPanel                   uploadPanel;

    /** Main content panel **/
    private static final FlowPanel      mainContainer        = new FlowPanel();

    /** Place support for history management **/
    private PlaceService                placeService;

    public static LoginStatusWidget     loginStatusWidget;

    private String                      previousHistoryToken = new String();

    private Label                       breadcrumb;

    private static UserSessionState     sessionState;

    private Label                       debugLabel;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

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
        HorizontalPanel navMenu = new HorizontalPanel();
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
        Hyperlink listLink = new Hyperlink("Datasets", "listDatasets");
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
        // upload link
        Hyperlink uploadLink = new Hyperlink("Upload", "upload");
        uploadLink.addStyleName("navMenuLink");
        navMenu.add(uploadLink);

        // FIXME debug
        debugLabel = new Label();
        debugLabel.addStyleName("navMenuText");
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

        DatasetWidget datasetWidget = new DatasetWidget(dispatchAsync);
        mainContainer.clear();
        mainContainer.add(datasetWidget);

        String datasetUri = getParams().get("id"); // FIXME should use
        // "uri?"
        if (datasetUri != null) {
            datasetWidget.showDataset(datasetUri);
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
        // Check if the user has been activated by an administrator
        dispatchAsync.execute(new HasPermission(getSessionState().getCurrentUser().getUri(),
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

    /**
     * Set the session id, add a cookie and add history token.
     * 
     * @param sessionId
     * 
     */
    public void login(final String userId, final String sessionKey) {
        final UserSessionState state = MMDB.getSessionState();
        state.setSessionKey(sessionKey);
        // set cookie
        // TODO move to more prominent place... MMDB? A class with static properties?
        final long DURATION = 1000 * 60 * 60; // 60 minutes
        final Date expires = new Date(System.currentTimeMillis() + DURATION);
        Cookies.setCookie("sessionKey", sessionKey, expires);

        GetUser getUser = new GetUser();
        getUser.setUserId(userId);
        MMDB.dispatchAsync.execute(getUser, new AsyncCallback<GetUserResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error retrieving user with id " + userId);
            }

            @Override
            public void onSuccess(GetUserResult result) {
                PersonBean personBean = result.getPersonBean();
                state.setCurrentUser(personBean);
                MMDB.loginStatusWidget.login(personBean.getName());
                GWT.log("Current user set to " + personBean.getUri());
                Cookies.setCookie("sid", personBean.getUri(), expires);
                checkPermissions(History.getToken());
            }
        });

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
        } else if (token.startsWith("modifyPermissions")) {
            showUsersPage();
        } else if (token.startsWith("signup")) {
            showSignupPage();
        } else if (token.startsWith("home")) {
            showHomePage();
        } else if (token.startsWith("sparql")) {
            showSparqlPage();
        } else if (!previousHistoryToken.startsWith("listDatasets")) {
            showListDatasetsPage();
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

    /**
     * List users in the system.
     */
    private void showUsersPage() {
        // Check if the user has view admin pages permission
        dispatchAsync.execute(new HasPermission(getSessionState().getCurrentUser().getUri(),
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
        dispatchAsync.execute(new HasPermission(getSessionState().getCurrentUser().getUri(),
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
        Command onNotLoggedIn = new Command() {
            public void execute() {
                showLoginPage();
            }
        };
        if (!loggedIn) {
            onNotLoggedIn.execute();
        } else {
            // now check REST auth
            Command onLoggedIn = new Command() {
                public void execute() {
                    login(cookieSID, cookieSessionKey); // ?
                }
            };
            LoginPage.checkRestAuth(onLoggedIn, onNotLoggedIn);
        }
    }

    public static final String DATASET_VIEW_TYPE_PREFERENCE    = "datasetViewType";
    public static final String COLLECTION_VIEW_TYPE_PREFERENCE = "collectionViewType";

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
