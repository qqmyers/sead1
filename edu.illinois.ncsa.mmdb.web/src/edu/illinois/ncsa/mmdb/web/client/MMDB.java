/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA, 2013 U. Michigan.  All rights reserved.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ConfigurationResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetConfiguration;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GoogleOAuth2Props;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GoogleOAuth2PropsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.JiraIssue.JiraIssueType;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetOrCollectionEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetOrCollectionHandler;
import edu.illinois.ncsa.mmdb.web.client.event.AllDatasetsUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AllDatasetsUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.geo.GeoPage;
import edu.illinois.ncsa.mmdb.web.client.presenter.DynamicTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.ui.AboutPage;
import edu.illinois.ncsa.mmdb.web.client.ui.AccountPage;
import edu.illinois.ncsa.mmdb.web.client.ui.AuthenticationCallback;
import edu.illinois.ncsa.mmdb.web.client.ui.CollectionPage;
import edu.illinois.ncsa.mmdb.web.client.ui.ConfirmDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.ContentCategory;
import edu.illinois.ncsa.mmdb.web.client.ui.DashboardPage;
import edu.illinois.ncsa.mmdb.web.client.ui.DatasetWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.DiscoveryPage;
import edu.illinois.ncsa.mmdb.web.client.ui.JiraIssuePage;
import edu.illinois.ncsa.mmdb.web.client.ui.ListCollectionsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.ListDatasetsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginPage;
import edu.illinois.ncsa.mmdb.web.client.ui.LoginStatusWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.NotEnabledPage;
import edu.illinois.ncsa.mmdb.web.client.ui.RequestNewPasswordPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SearchBox;
import edu.illinois.ncsa.mmdb.web.client.ui.SearchResultsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SelectedItemsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.SignupPage;
import edu.illinois.ncsa.mmdb.web.client.ui.TagPage;
import edu.illinois.ncsa.mmdb.web.client.ui.TagsPage;
import edu.illinois.ncsa.mmdb.web.client.ui.UploadPage;
import edu.illinois.ncsa.mmdb.web.client.ui.admin.AdminPage;
import edu.illinois.ncsa.mmdb.web.client.ui.preview.PreviewPanel;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicTableView;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.uiuc.ncsa.cet.bean.CETBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;

/**
 * MMDB entry point.
 *
 * @author Luigi Marini
 * @author Rob Kooper
 * @author Jim Myers
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

    /** Main content panel **/
    private static final FlowPanel     mainContainer                    = new FlowPanel();

    public static LoginStatusWidget    loginStatusWidget;

    public static boolean              credChangeOccuring               = true;

    private String                     previousHistoryToken             = new String();

    private Label                      breadcrumb;

    private static UserSessionState    sessionState;

    private RootPanel                  aboutMenuItem;
    private RootPanel                  datasetsMenuItem;
    private RootPanel                  collectionsMenuItem;
    private RootPanel                  tagsMenuItem;
    private RootPanel                  geoMenuItem;
    private RootPanel                  publishedDataMenuItem;
    private RootPanel                  dashboardMenuItem;
    private RootPanel                  adminMenuItem;
    private RootPanel                  uploadMenuItem;

    public static String               _sessionCookieName               = "JSESSIONID";
    public static String               _googleClientId                  = null;
    public static String               _orcidClientId                   = null;

    public static String               _projectName                     = "SEAD ACR";
    public static String               _projectDescription              = "A generic SEAD Project Space";
    public static boolean              bigData                          = false;                              //Server's bigData flag

    private static void reportUmbrellaError(@NotNull Throwable e) {
        for (Throwable th : ((UmbrellaException) e).getCauses() ) {
            if (th instanceof UmbrellaException) {
                reportUmbrellaError(th);
            } else {
                Window.alert("Error: " + th.getMessage());
            }
        }
    }

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                String s = "SEAD ACR Error:\n An unexpected error (such as a temporary communications issue or server error) has occurred.\n Please refresh your browser page to continue.\n If the issue persists, please report it to SEAD"; //, including the following message:\n" + e.getMessage();
                Window.alert(s + e + e.getMessage());
                reportUmbrellaError(e);
                GWT.log("uncaught exception", e);
            }
        });

        try {
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
                        MMDB.getSessionState().itemSelected(event.getUri());
                    }
                });

                eventBus.addHandler(DatasetUnselectedEvent.TYPE, new DatasetUnselectedHandler() {
                    @Override
                    public void onDatasetUnselected(DatasetUnselectedEvent event) {
                        GWT.log("Dataset unselected " + event.getUri());
                        MMDB.getSessionState().itemUnselected(event.getUri());
                    }
                });

                eventBus.addHandler(AllDatasetsUnselectedEvent.TYPE, new AllDatasetsUnselectedHandler() {
                    @Override
                    public void onAllDatasetsUnselected(AllDatasetsUnselectedEvent event) {
                        GWT.log("All datasets unselected");
                        Set<String> toDeselect = new HashSet<String>(MMDB.getSessionState().getSelectedItems());
                        for (String datasetUri : toDeselect ) {
                            DatasetUnselectedEvent ue = new DatasetUnselectedEvent();
                            ue.setUri(datasetUri);
                            eventBus.fireEvent(ue);
                        }
                    }
                });

                //initNavMenu();

                // TODO place support for history management
                // placeService = new PlaceService(eventBus);

                // history support
                History.addValueChangeHandler(this);
                History.fireCurrentHistoryState();

                //initNavMenu();
            }
        } catch (Exception e) {
            Window.alert("Initialization error: " + e.getMessage());
            reportUmbrellaError(e);
            GWT.log("initialization error", e);
        }

    }

    /**
     * Navigation menu at the top of the page.
     */
    void initNavMenu() {

        RootPanel.get("projectTitle").clear();

        final Anchor projectNameLabel = new Anchor(true);
        projectNameLabel.setText("");
        projectNameLabel.setTitle("");
        projectNameLabel.setHref("");

        RootPanel.get("projectTitle").add(projectNameLabel);
        loginStatusWidget = new LoginStatusWidget();
        RootPanel.get("loginMenu").add(loginStatusWidget);

        LoginPage.setMainWindow(this);

        dispatchAsync.execute(new GetConfiguration(MMDB.getUsername(),
                ConfigurationKey.ProjectName,
                ConfigurationKey.ProjectURL,
                ConfigurationKey.ProjectDescription,
                ConfigurationKey.BigData,
                ConfigurationKey.UseGoogleDocViewer,
                ConfigurationKey.PresentationSortOrder,
                ConfigurationKey.PresentationPageViewType,
                ConfigurationKey.PresentationDataViewLevel,
                ConfigurationKey.OrcidClientId,
                ConfigurationKey.ProjectHeaderLogo,
                ConfigurationKey.ProjectHeaderBackground,
                ConfigurationKey.ProjectHeaderTitleColor
                ), new AsyncCallback<ConfigurationResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not get Config Info", caught);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult result) {
                        bigData = result.getConfiguration(ConfigurationKey.BigData).equalsIgnoreCase("true");
                        _projectName = result.getConfiguration(ConfigurationKey.ProjectName);
                        _projectDescription = result.getConfiguration(ConfigurationKey.ProjectDescription);
                        _orcidClientId = result.getConfiguration(ConfigurationKey.OrcidClientId);
                        projectNameLabel.setHTML(wrapIfNeeded(_projectName));
                        projectNameLabel.setTitle(_projectDescription);
                        projectNameLabel.setHref(result.getConfiguration(ConfigurationKey.ProjectURL));

                        // override default logo, background, title color
                        Document.get().getElementById("project-logo").getFirstChildElement().getFirstChildElement().setAttribute("src", result.getConfiguration(ConfigurationKey.ProjectHeaderLogo));
                        com.google.gwt.dom.client.Element background = Document.get().getElementById("project-header");
                        RootPanel headerPanel = RootPanel.get("project-header");
                        headerPanel.getElement().getStyle().setBackgroundImage("url('" + result.getConfiguration(ConfigurationKey.ProjectHeaderBackground) + "')");
                        headerPanel.getElement().getStyle().setProperty("background-repeat", "repeat-x");
                        projectNameLabel.asWidget().getElement().getStyle().setProperty("color", result.getConfiguration(ConfigurationKey.ProjectHeaderTitleColor));

                        PreviewPanel.setUseGoogleDocViewer(result.getConfiguration(ConfigurationKey.UseGoogleDocViewer).equalsIgnoreCase("true"));
                        DynamicTablePresenter.setInitialKeys(result.getConfiguration(ConfigurationKey.PresentationSortOrder), result.getConfiguration(ConfigurationKey.PresentationPageViewType), result.getConfiguration(ConfigurationKey.PresentationDataViewLevel).equalsIgnoreCase("true") || bigData);

                    }

                    //If the title is long (>40 chars), split it at the first space that exists between the 30% and 70% mark. If no spaces are in this range, don't split.
                    private SafeHtml wrapIfNeeded(String label) {
                        int len = label.length();
                        if (len > 40) {
                            //Wrap close to the half-way point if there's a space
                            int firstPosible = (int) Math.round((len * 0.4));
                            int spaceIndex = label.substring(firstPosible).indexOf(' ');
                            if ((spaceIndex >= 0) && (spaceIndex < (int) Math.round((len * 0.3)))) {
                                //Replace the space with an html break
                                return new SafeHtmlBuilder().appendEscaped(label.substring(0, firstPosible + spaceIndex)).appendHtmlConstant("<br/>").appendEscaped(label.substring(firstPosible + spaceIndex + 1)).toSafeHtml();
                            }
                        }
                        return new SafeHtmlBuilder().appendEscaped(label).toSafeHtml();
                    }

                });

        dispatchAsync.execute(new GoogleOAuth2Props(), new AsyncCallback<GoogleOAuth2PropsResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error retrieving Google OAuth2 properties", caught);
                Window.alert("Bad: " + caught);
            }

            @Override
            public void onSuccess(GoogleOAuth2PropsResult props) {
                _googleClientId = props.getClientId();
            }
        });

        // Panels for menu items
        aboutMenuItem = RootPanel.get("aboutMenuItem");
        datasetsMenuItem = RootPanel.get("listDatasetsMenuItem");
        collectionsMenuItem = RootPanel.get("listCollectionsMenuItem");
        tagsMenuItem = RootPanel.get("tagsMenuItem");
        geoMenuItem = RootPanel.get("geoMenuItem");
        publishedDataMenuItem = RootPanel.get("discoveryMenuItem");
        dashboardMenuItem = RootPanel.get("dashboardMenuItem");
        adminMenuItem = RootPanel.get("administrationMenuItem");
        uploadMenuItem = RootPanel.get("uploadMenuItem");

        // search box
        SearchBox searchBox = new SearchBox("Search");
        searchBox.setWidth("150px"); // maybe add this as CSS style?
        RootPanel.get("searchMenu").add(searchBox);

        setMenuItemVisibility();
    }

    /**
     * For debugging purposes. Monitor events of interest.
     *
     * @param eventBus
     */
    private void logEvent(HandlerManager eventBus) {
        eventBus.addHandler(AddNewDatasetOrCollectionEvent.TYPE,
                new AddNewDatasetOrCollectionHandler() {

                    @Override
                    public void onAddNewDatasetOrCollection(AddNewDatasetOrCollectionEvent event) {
                        GWT.log("Event Logging: Add new dataset event "
                                + ((CETBean.TitledBean) event.getBean()).getTitle(), null);

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
            mainContainer.add(new SelectedItemsPage(dispatchAsync));

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
        changeMenuSelection(previousHistoryToken, token);

        if (token.startsWith("logout")) {
            /* logout triggers an implicit attempt to login as anonymous
             *  (as does checkLogin() if there are no existing credentials).
             *  It's the success or failure of that login that determines whether the
             *  user sees the login screen or (if anonymous has view member page privileges),
             *  the default content screen (currently listDatasets)
             */
            credChangeOccuring = true;
            getSessionState().setCurrentUser(null);
            getSessionState().setLoginProvider("local");
            getSessionState().setAnonymous(true);
            setMenuItemVisibility();

            LoginPage.authenticate("anonymous", "none", new AuthenticationCallback() {
                @Override
                public void onFailure() {
                    if (!token.startsWith("logout_st")) {
                        History.newItem("listDatasets", true);
                    }
                }

                @Override
                public void onSuccess(String userUri, String sessionKey) {
                    if (!token.startsWith("logout_st")) {
                        History.newItem("listDatasets", true);
                    }
                }
            });

        } else if (token.startsWith("login")) {
            /*Once the loginpage is shown, it's internal logic determines what happens next.
            * Right now, if the user succeeds, doWithPermissions("login") gets called
            */
            credChangeOccuring = true;
            if (LoginPage.getAutologin()) {
                //Try to pick up existing credential silently
                LoginPage.oauth2Login(new AuthenticationCallback() {
                    @Override
                    public void onFailure() {
                        showLoginPage();
                    }

                    @Override
                    public void onSuccess(String userUri, String sessionKey) {
                        GWT.log(userUri + " logged in");
                    }
                }, true);

            } else {
                showLoginPage();
            }
        } else if (token.startsWith("signup")) {
            showSignupPage();
        } else if (token.startsWith("requestNewPassword")) {
            showRequestNewPasswordPage();
        } else if (token.startsWith("feedback")) {
            showJiraFeaturePage();
        } else if (token.startsWith("jiraBug")) {
            showJiraBugPage();
        } else if (token.startsWith("jiraFeature")) {
            showJiraFeaturePage();
        } else {
            // Everything except the events above has to have an authenticated and authorized
            // user. checkLogin starts that process and handles further processing of the event
            // (via doWithPermissions()
            checkLogin();
        }
    }

    //Change the selected menu item to have class = "selected" and remove it from the previous entry
    //Ignore tokens such as login that don't match a menu item
    private void changeMenuSelection(String previousHistoryToken, String token) {

        RootPanel oldPanel = getCoreElement(previousHistoryToken);
        if (oldPanel != null) {
            oldPanel.removeStyleName("selected");
        }
        RootPanel newPanel = getCoreElement(token);
        if (newPanel != null) {
            newPanel.addStyleName("selected");
        }
    }

    private RootPanel getCoreElement(String token) {
        String elemString = token;
        // Map requests for indivudal datasets or collections to the generic datasets and collections menu items
        if (token.startsWith("dataset")) {
            elemString = "listDatasetsMenuItem";
        } else if (token.startsWith("collection")) {
            elemString = "listCollectionsMenuItem";
        } else {
            //Else token should correspond directly to a menu item
            if (token.contains("_")) {
                elemString = token.substring(0, token.indexOf("_"));
            }
            elemString += "MenuItem";
        }
        return RootPanel.get(elemString);

    }

    private void setMenuItemVisibility() {
        showBasedOnPermission(aboutMenuItem, Permission.VIEW_SYSTEM);
        showBasedOnPermission(datasetsMenuItem, Permission.VIEW_MEMBER_PAGES);
        showBasedOnPermission(collectionsMenuItem, Permission.VIEW_MEMBER_PAGES);
        showBasedOnPermission(tagsMenuItem, Permission.VIEW_MEMBER_PAGES);
        showBasedOnPermission(geoMenuItem, Permission.VIEW_LOCATION);
        showBasedOnPermission(publishedDataMenuItem, Permission.VIEW_PUBLISHED);
        showBasedOnPermission(dashboardMenuItem, Permission.VIEW_MEMBER_PAGES);
        showBasedOnPermission(adminMenuItem, Permission.VIEW_ADMIN_PAGES);
        showBasedOnPermission(uploadMenuItem, Permission.UPLOAD_DATA);
    }

    private void showBasedOnPermission(final RootPanel menuItem, Permission perm) {

        rbac().doIfAllowed(perm, new PermissionCallback() {
            @Override
            public void onAllowed() {
                menuItem.removeStyleName("hidden");
            }

            @Override
            public void onDenied() {
                menuItem.addStyleName("hidden");
            }
        });

    }

    /**
     * login retrieves an already authenticated users details (PersonBean) from
     * the server,
     * stores it in a local cache of session information, and, if successful,
     * triggers execution
     * of the action for the History token that triggered the call.
     *
     * @param sessionId
     *
     */
    public void retrieveUserInfo(final String userId, final String sessionKey) {
        retrieveUserInfo(null, userId, sessionKey, new AuthenticationCallback() {
            @Override
            public void onFailure() {
            }

            @Override
            public void onSuccess(String userUri, String sessionKey) {
            }
        });
    }

    public void retrieveUserInfoByName(final String username, final String sessionKey, final AuthenticationCallback callback) {
        retrieveUserInfo(username, null, sessionKey, callback);
    }

    //username or userId needs to be non-null, userId will be used if both are provided
    private void retrieveUserInfo(final String username, final String userId, final String sessionKey, final AuthenticationCallback callback) {
        final UserSessionState state = MMDB.getSessionState();

        state.setSessionKey(sessionKey);

        AsyncCallback<GetUserResult> handler = new AsyncCallback<GetUserResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error retrieving user with id " + username);
                callback.onFailure();
            }

            @Override
            public void onSuccess(GetUserResult result) {
                PersonBean personBean = result.getPersonBean();
                state.setCurrentUser(personBean);

                if (result.isAnonymous()) {
                    MMDB.loginStatusWidget.loggedOut();
                    getSessionState().setAnonymous(true);
                    //LoginPage.setAutologin(false);
                    GWT.log("Current user is anonymous");
                } else {
                    MMDB.loginStatusWidget.loggedIn(personBean.getName());
                    getSessionState().setAnonymous(false);

                    GWT.log("Current user set to " + personBean.getUri());
                }
                parseHistoryToken(History.getToken());
                //Before doWithPerm? (most/all(?) callbacks are null now, so minor issue
                callback.onSuccess(username, sessionKey);
            }
        };

        GetUser getUser = new GetUser();
        if (userId != null) {
            getUser.setUserId(userId);
        } else {
            getUser.setUsername(username);
        }
        dispatchAsync.execute(getUser, handler);
    }

    /**
     * Run the action for the given token, subject to a permission check to see
     * if user has permission require to view a given pages (most require
     * VIEW_MEMBER_PAGES).
     * The pages themselves must check further if additional permissions are
     * needed to see
     * their content/parts of their content.
     *
     * Right now, parseHistory gets called via the checkLogin() method, as
     * well as by the login() method (on success, login and logout tokens only).
     * Login/logout trigger viewing the default member page (listDatasets), so
     * the user's permissions need to be checked.
     *
     * Parse history token and show the proper widgets/
     * trigger the appropriate actions.
     *
     * @param token
     *            history token (everything after the #)
     */
    private void parseHistoryToken(String token) {

        if (token.startsWith("upload")) {
            rbac().doIfAllowed(Permission.UPLOAD_DATA, new StandardDenialPermissionCallback(token) {
                @Override
                public void onAllowed() {
                    showUploadPage();
                }
            });

        } else if (token.startsWith("about")) {
            rbac().doIfAllowed(Permission.VIEW_SYSTEM, new StandardDenialPermissionCallback(token) {
                @Override
                public void onAllowed() {
                    showAboutPage();
                }
            });
        } else if (token.startsWith("geo")) {
            rbac().doIfAllowed(Permission.VIEW_LOCATION, new StandardDenialPermissionCallback(token) {
                @Override
                public void onAllowed() {
                    String tag = null;
                    if (this.token.startsWith("geo_tag_")) {
                        tag = URL.decode(this.token.substring("geo_tag_".length()));
                    }
                    showGeoPage(tag);
                }
            });
        } else if (token.startsWith("discovery")) {
            rbac().doIfAllowed(Permission.VIEW_PUBLISHED, new StandardDenialPermissionCallback(token) {
                @Override
                public void onAllowed() {
                    String collection = null;
                    if (token.startsWith("discovery_")) {
                        collection = URL.decode(token.substring("discovery_".length()));
                    }
                    showDiscoveryPage(collection);
                }
            });
        } else if (token.startsWith("administration")) {
            rbac().doIfAllowed(Permission.VIEW_ADMIN_PAGES, new StandardDenialPermissionCallback(token) {
                @Override
                public void onAllowed() {
                    showAdminPage();
                }
            });
        } else {
            rbac().doIfAllowed(Permission.VIEW_MEMBER_PAGES, new StandardDenialPermissionCallback(token) {
                @Override
                public void onAllowed() {

                    if (token.startsWith("dataset")) {
                        showDataset();
                    } else if (token.startsWith("listDatasets") /*&& !previousHistoryToken.startsWith("listDatasets")*/) {
                        showListDatasetsPage();
                    } else if (token.startsWith("tags")) {
                        showTagsPage();
                    } else if (token.startsWith("tag")) {
                        showTagPage();
                    } else if (token.startsWith("listCollections") /*&& !previousHistoryToken.startsWith("listCollections")*/) {
                        listCollections();
                    } else if (token.startsWith("listCollections")) {
                        // skip default case!
                    } else if (token.startsWith("collection")) {
                        showCollectionPage();
                    } else if (token.startsWith("search")) {
                        showSearchResultsPage();
                    } else if (token.startsWith("dashboard")) {
                        showDashboardPage();
                    } else if (token.startsWith("signup")) {
                        showSignupPage();
                    } else if (token.startsWith("account")) {
                        showAccountPage();
                    } else if (token.startsWith("viewSelected")) {
                        showSelected(true);
                    } else if (token.startsWith("editRelationships")) {
                        showSelected(true);
                    } else if (token.startsWith("noneSelected")) {
                        showSelected(false);
                    } else if (token.startsWith("login")) {
                        //just logged in
                        if (!MMDB.getSessionState().isAnonymous()) {
                            showListDatasetsPage();
                            token = "listDatasets";
                            History.newItem("listDatasets", false);
                        } else {
                            showLoginPage();
                        }
                    } else if (token.startsWith("logout_st")) {
                        //Successfully logged in after logout due to timeout/session issue - just go back to where the user was
                        History.back();
                    } else /*if (!previousHistoryToken.startsWith("listDatasets")) */{
                        //Default page
                        showListDatasetsPage();
                    }
                }
            });
        }
        previousHistoryToken = token;
        setMenuItemVisibility();
    }

    private void showAboutPage() {
        GWT.log("Loading Dashboard Page", null);
        mainContainer.clear();
        mainContainer.add(new AboutPage(dispatchAsync, eventBus));
    }

    private void showDashboardPage() {
        GWT.log("Loading Dashboard Page", null);
        mainContainer.clear();
        mainContainer.add(new DashboardPage("Dashboard", dispatchAsync, eventBus));
    }

    private void showDiscoveryPage(String collection) {
        GWT.log("Loading Discovery Page", null);
        mainContainer.clear();
        mainContainer.add(new DiscoveryPage(collection, "Published Collections", dispatchAsync, eventBus));
    }

    private void showAdminPage() {
        GWT.log("Loading Admin Page", null);
        mainContainer.clear();
        mainContainer.add(new AdminPage(dispatchAsync, eventBus));
    }

    private void showGeoPage(String tag) {
        GWT.log("Loading GeoBrowse Page", null);
        mainContainer.clear();
        mainContainer.add(new GeoPage(tag, dispatchAsync, eventBus));
    }

    private void showTagsPage() {
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
        Map<String, String> params = getParams();
        final String datasetUri = params.get("id");

        rbac().doIfAllowed(Permission.UPLOAD_DATA, new AccessOrMessageCallback() {
            @Override
            public void onAllowed() {
                GWT.log("Loading Upload Page", null);
                mainContainer.clear();
                UploadPage up = new UploadPage(dispatchAsync);
                mainContainer.add(up);
                if (datasetUri != null) {
                    up.deriveFromDataset(dispatchAsync, datasetUri);
                }
            }
        });
    }

    private void showAccountPage() {
        mainContainer.clear();
        mainContainer.add(new AccountPage(dispatchAsync));
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
        showLoginPage(false);
    }

    private void showLoginPage(boolean interrupted) {
        setMenuItemVisibility();
        loginStatusWidget.loggedOut();
        mainContainer.clear();
        LoginPage lp = new LoginPage();
        if (interrupted) {
            lp.setFeedback("Session Interrupted/Timed Out. Login again to continue.");
        }
        mainContainer.add(lp);
    }

    /**
     * This method verifies that a user is logged in and then triggers the
     * widget change/action (History token) that triggered this call. If no
     * one is logged in, this method tries to log the anonymous user in. If
     * a server session cookie exists but no local user info exists (e.g. user
     * just hit the
     * browser refresh button and restarted the app, this method will
     * try to re-establish the local cache and continue.
     *
     */
    public void checkLogin() {
        boolean loggedIn = false;

        UserSessionState state = MMDB.getSessionState();
        //GWT.log("User state SessionKey: " + state.getSessionKey());
        //GWT.log("User state name: " + state.getCurrentUser().getName());

        //The existence of a session cookie means the server believes we're logged in
        final String cookieSessionKey = Cookies.getCookie(_sessionCookieName);
        if (cookieSessionKey != null) {
            GWT.log("Session key: " + cookieSessionKey, null);

            loggedIn = true;
        }
        final String localKey = state.getSessionKey();
        if (!loggedIn) {
            //try to log in as preferred user or anonymous
            GWT.log("not logged in, attempting to login as anonymous");
            LoginPage.oauth2Login(new
                    AuthenticationCallback() {

                        @Override
                        public void onFailure() {
                            showLoginPage(localKey != null);
                        }

                        @Override
                        public void onSuccess(String userUri, String
                                sessionKey) {
                            GWT.log("logged in as " + userUri);
                            credChangeOccuring = false;
                        }
                    }, true);
        } else {

            if ((localKey != null) && cookieSessionKey.equals(localKey)) {
                //Assume state.getCurrentUser is correct
                //localKey should only be null or equal to the cookieSessionKey
                parseHistoryToken(History.getToken());
            } else {
                // Check REST auth and get the userID so we can then go retrieve the PersonBean (in login())
                // Having a sessionKey and no local key will occur when the user has hit a browser
                // refresh and restarts the app

                checkRestAuth(cookieSessionKey, new
                        AuthenticationCallback() {

                            @Override
                            public void onFailure() {
                                //Start fresh - login as preferred user if possible or anon if not
                                LoginPage.oauth2Login(new
                                        AuthenticationCallback() {

                                            @Override
                                            public void onFailure() {
                                                showLoginPage(true);
                                            }

                                            @Override
                                            public void onSuccess(String userUri, String
                                                    sessionKey) {
                                                GWT.log("logged in as " + userUri);
                                            }
                                        }, true);
                            }

                            @Override
                            public void onSuccess(String userUri, String
                                    sessionKey) {

                                // login menu
                                //Get the user's info (PersonBean) and store in UserSessionState
                                retrieveUserInfo(userUri, sessionKey);
                                credChangeOccuring = false;
                            }
                        });

            }
        }
    }

    /*Call the REST checkLogin endpoint to verify the user is logged in (session cookie is valid)
     * and return the user's ID for local use.
     */
    public void checkRestAuth(final String sessionID, final AuthenticationCallback callback) {
        String restUrl = "./api/checkLogin";
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restUrl);
        try {
            GWT.log("checking login status @ " + restUrl, null);
            // we need to block.
            builder.sendRequest("", new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    callback.onFailure();
                }

                public void onResponseReceived(Request request, Response response) {
                    // success!
                    GWT.log("REST auth status code = " + response.getStatusCode(), null);
                    if (response.getStatusCode() > 300) {
                        GWT.log("not authenticated to REST services", null);
                        callback.onFailure();
                    } else {
                        String userid = response.getText();
                        callback.onSuccess(userid, sessionID);
                    }
                }
            });
        } catch (RequestException x) {
            callback.onFailure();
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
        getSessionState().getPreferences().put(MMDB.DATASET_VIEWSIZE_TYPE_PREFERENCE, DynamicTableView.PAGE_SIZE_X1);
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

    public abstract class StandardDenialPermissionCallback extends PermissionCallback {

        protected String token;

        public StandardDenialPermissionCallback(String token) {
            this.token = token;
        }

        @Override
        public abstract void onAllowed();

        @Override
        public void onDenied() {
            if (getSessionState().isAnonymous()) {
                if (token.startsWith("logout_st")) {
                    showLoginPage(true);
                } else {
                    showLoginPage();
                }
            } else {
                showNotEnabledPage();
            }
        }

    }

    /* Utility to get handle to current page - caller must check type of page)
     *
     */
    public Widget getPage() {
        Widget widget = mainContainer.getWidget(0);
        if (widget != null) {
            return widget;
        } else {
            return null;
        }
    }

    public FlowPanel getMainContainer() {
        return mainContainer;
    }

}
