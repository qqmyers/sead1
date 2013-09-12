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

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Authenticate;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AuthenticateResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.ui.preview.PreviewPanel;
import edu.illinois.ncsa.mmdb.web.common.Permission;

/**
 * Embedded Widget.
 * First logs in user, checks permission, then displays preview
 */
public class Embed implements EntryPoint {

    /**
     * Dispatch service. Should be the only service needed. All commands should
     * go through this endpoint. To learn more look up gwt-dispatch and the
     * command pattern.
     */
    public final DispatchAsync         dispatchAsync      = new MyDispatchAsync();

    /** Event bus for propagating events in the interface **/
    public static final HandlerManager eventBus           = new HandlerManager(null);

    public static final String         ANONYMOUS          = "http://cet.ncsa.uiuc.edu/2007/person/anonymous";
    public static final String         _sessionCookieName = "JSESSIONID";
    private RootPanel                  rootPanel;
    private String                     uri;
    private int                        width;
    private int                        height;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        rootPanel = RootPanel.get("mmdb-mainContainer");
        if (rootPanel != null) {

            //Set Width + Height
            width = Window.getClientWidth();
            height = Window.getClientHeight();
            GWT.log("Width: " + width + " Height: " + height);

            Map<String, String> params = getParams();
            uri = params.get("id");

            //Ensure a user is already logged in, if not log in anonymous
            checkLogin();

        }
    }

    private void displayPreview() {

        if (uri != null) {

            GWT.log("ID: " + uri);
            final FlowPanel column = new FlowPanel();
            rootPanel.add(column);

            final HorizontalPanel bottom = new HorizontalPanel();
            bottom.addStyleName("bottomRow");
            bottom.setWidth((width - 2) + "px");
            rootPanel.add(bottom);

            final Anchor link = new Anchor();
            link.setHref(GWT.getHostPageBaseURL() + "#dataset?id=" + uri);
            link.setTarget("_parent");

            //Logo
            final Image logo = new Image("images/logo_embedded.png");
            logo.setSize("60px", "24px");
            logo.addStyleName("bottomLogo");
            link.getElement().appendChild(logo.getElement());

            dispatchAsync.execute(new GetDataset(uri), new AsyncCallback<GetDatasetResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Error getting dataset", null);
                    rootPanel.add(new Label("Dataset Not Available"));
                }

                @Override
                public void onSuccess(GetDatasetResult result) {
                    PreviewPanel previewPanel = new PreviewPanel(dispatchAsync, eventBus, true, width, height);
                    previewPanel.drawPreview(result, column, uri);
                    Label title = new Label(shortenTitle(result.getDataset().getTitle()));
                    title.setStyleName("fileTitle");
                    //If width too small, don't bother putting file title
                    if (width > 200) {
                        bottom.add(title);
                    }
                    bottom.add(link);
                }
            });

        } else {
            rootPanel.add(new Label("No Dataset Specified."));
        }
    }

    /**
     * Parse the parameters in the history token after the '?'
     * 
     * @return
     */
    public static Map<String, String> getParams() {
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

    private String shortenTitle(String title) {
        if (title != null && title.length() > 20) {
            return title.substring(0, 20) + "...";
        } else {
            return title;
        }
    }

    /**
     * Check if user is logged in, if so, check for permissions, else
     * login anonymous user
     */
    private void checkLogin() {
        boolean loggedIn = true;
        loggedIn = false;
        final String cookieSessionKey = Cookies.getCookie(_sessionCookieName);
        if (cookieSessionKey != null) {
            GWT.log("Session key: " + cookieSessionKey, null);
        } else {
            loggedIn = false;
        }
        if (!loggedIn) {
            GWT.log("Not logged in, attempting to login as anonymous");

            dispatchAsync.execute(new Authenticate("anonymous", "none"), new AsyncCallback<AuthenticateResult>() {

                @Override
                public void onFailure(Throwable arg0) {
                    //callback.onFailure();
                    GWT.log("Failed authenticating anonymous for embed");
                    loginFail();
                }

                @Override
                public void onSuccess(final AuthenticateResult arg0) {
                    if (arg0.getAuthenticated()) {

                        //Now hit the REST endpoint
                        loginREST();

                    } else {
                        GWT.log("Login: Authentication Failed");
                        loginFail();
                    }
                }
            });
        }
        //Already logged in so check permissions now
        else {
            checkPermissions();
        }
    }

    /**
     * 2nd login hit through REST endpoint
     */
    private void loginREST() {
        String restUrl = "./api/authenticate";
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restUrl);
        builder.setUser(TextFormatter.escapeEmailAddress("anonymous"));
        builder.setPassword("none");
        try {
            builder.sendRequest("", new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    GWT.log("REST Login: Failed Authenticating Anonymous");
                    loginFail();
                }

                public void onResponseReceived(Request request, Response response) {

                    //Log Information
                    String sessionKey = response.getText();
                    GWT.log("REST Auth Status Code = " + response.getStatusCode(), null);
                    GWT.log("User Anonymous associated with Session key " + sessionKey, null);
                    if (response.getStatusCode() > 300) {
                        GWT.log("Authentication failed: " + sessionKey, null);
                        loginFail();
                    }

                    // Check permissions to see if anonymous user can view embedded dataset
                    checkPermissions();

                }
            });
        } catch (RequestException x) {
            GWT.log("Embed Login: Request Builder Exception");
            loginFail();
        }
    }

    /**
     * Check to see if Anonymous user has VIEW DATA permission
     * if so display the widget
     * 
     */
    private void checkPermissions() {
        PermissionUtil rbac = new PermissionUtil(dispatchAsync);
        rbac.doIfAllowed(Permission.VIEW_DATA, null, new PermissionCallback() {
            @Override
            public void onAllowed() {
                GWT.log("User has permission to view embedded widget");
                displayPreview();
            }

            @Override
            public void onDenied() {
                GWT.log("User does not have permission to view embedded widget");
                FlowPanel column = new FlowPanel();
                column.addStyleName("permissionPanel");
                column.setSize(width + "px", height + "px");

                Label noPermission = new Label("Permissions have not been set for Anonymous to View Data. Please contact administrator.");
                noPermission.addStyleName("datasetRightColText");
                noPermission.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

                Anchor viewDataset = new Anchor("View Dataset");
                viewDataset.setHref(GWT.getHostPageBaseURL() + "mmdb.html#dataset?id=" + uri);
                viewDataset.addStyleName("homePageLink");

                Anchor homePage = new Anchor("Medici Home Page");
                homePage.addStyleName("homePageLink");
                homePage.addStyleName("permissionMultiAnchor");
                homePage.setHref(GWT.getHostPageBaseURL());

                column.add(noPermission);
                column.add(viewDataset);
                column.add(homePage);
                rootPanel.add(column);
            }
        }, ANONYMOUS);
    }

    private void loginFail() {
        FlowPanel column = new FlowPanel();
        column.addStyleName("permissionPanel");
        column.setSize(width + "px", height + "px");

        column.add(new Label("Login Server Error"));
        rootPanel.add(column);
    }

}
