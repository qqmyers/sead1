/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA 2013 U. Michigan.  All rights reserved.
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
/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.UserSessionState;

/**
 * @author Luigi Marini
 * 
 */
public class LoginPage extends Composite {

    private final FlowPanel     mainPanel;
    private Label               pageTitle;
    private TextBox             usernameBox;
    private PasswordTextBox     passwordBox;
    private SimplePanel         feedbackPanel;
    private Label               progressLabel;
    private final DispatchAsync dispatchasync;
    private final MMDB          mainWindow;

    /**
     * @param dispatchasync
     * 
     */
    public LoginPage(DispatchAsync dispatchasync, MMDB mainWindow) {

        this.dispatchasync = dispatchasync;
        this.mainWindow = mainWindow;

        mainPanel = new FlowPanel();

        mainPanel.addStyleName("page");

        initWidget(mainPanel);

        // page title
        mainPanel.add(createPageTitle());

        // login form
        mainPanel.add(createLoginForm());
    }

    /**
     * 
     * @return
     */
    private Widget createPageTitle() {
        return new TitlePanel("Login");
    }

    /**
     * 
     * @return
     */
    private Widget createLoginForm() {
        FlexTable table = new FlexTable();

        table.addStyleName("loginForm");

        feedbackPanel = new SimplePanel();

        table.setWidget(0, 0, feedbackPanel);

        table.getFlexCellFormatter().setColSpan(0, 0, 2);

        table.getFlexCellFormatter().setHorizontalAlignment(0, 0,
                HasHorizontalAlignment.ALIGN_CENTER);

        Label usernameLabel = new Label("Email:");

        table.setWidget(1, 0, usernameLabel);

        usernameBox = new TextBox();
        usernameBox.getElement().setAttribute("autocapitalize", "none");
        usernameBox.getElement().setAttribute("autocorrect", "off");
        usernameBox.setTabIndex(1);
        usernameBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    authenticate();
                }
            }
        });

        table.setWidget(1, 1, usernameBox);

        DeferredCommand.addCommand(new Command() {
            @Override
            public void execute() {
                usernameBox.setFocus(true);
            }
        });

        // sign up
        table.setWidget(1, 3, new Hyperlink("Sign up", "signup"));

        Label passwordLabel = new Label("Password:");

        table.setWidget(2, 0, passwordLabel);

        passwordBox = new PasswordTextBox();

        passwordBox.setTabIndex(2);

        passwordBox.addKeyUpHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    authenticate();
                }

            }
        });

        table.setWidget(2, 1, passwordBox);

        // forgot password link
        table.setWidget(2, 3, new Hyperlink("Forgot Password?", "requestNewPassword"));

        Button submitButton = new Button("Login", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                progressLabel.setText("Logging in...");
                authenticate();
            }
        });

        submitButton.setTabIndex(3);

        table.setWidget(3, 1, submitButton);

        progressLabel = new Label("");
        table.setWidget(4, 0, progressLabel);
        table.getFlexCellFormatter().setColSpan(4, 0, 2);
        table.getFlexCellFormatter().setHorizontalAlignment(4, 0, HasAlignment.ALIGN_CENTER);

        return table;
    }

    protected void authenticate() {
        String password = passwordBox.getText().length() > 0 ? passwordBox.getText() : "(none)";
        authenticate(usernameBox.getText(), password);
    }

    /**
     * Authenticate against the REST endpoint. If successful, login local and
     * start
     * processing of the next
     * History token.
     * 
     * Called to process logout/login as anonymous from MMDB
     */
    public static void authenticate(final DispatchAsync dispatch, final MMDB mainWindow, final String username, final String password, final AuthenticationCallback callback) {
        logout(new Command() { // ensure we're logged out before authenticating
            public void execute() {
                // Hit the REST authentication endpoint
                //CHANGE TO POST

                //FIXME - deal with RemoteAPIKey!!!!!!!!!!!!!!!!!!!!

                String restUrl = "./api/authenticate";
                RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, restUrl);
                builder.setHeader("Content-type", "application/x-www-form-urlencoded");
                StringBuilder sb = new StringBuilder();
                sb.append("username=" + username);
                sb.append("&password=" + password);
                builder.setRequestData(sb.toString());
                builder.setCallback(new RequestCallback() {
                    public void onError(Request request, Throwable exception) {
                        callback.onFailure();
                    }

                    public void onResponseReceived(Request request, Response response) {
                        // success!
                        String sessionKey = response.getText();

                        GWT.log("REST auth status code = " + response.getStatusCode(), null);
                        if (response.getStatusCode() > 300) {
                            GWT.log("authentication failed: " + sessionKey, null);
                            callback.onFailure();
                        }
                        GWT.log("user " + username + " associated with session key " + sessionKey, null);
                        // login local

                        mainWindow.loginByName(username, sessionKey, callback);
                    }
                });

                try {
                    GWT.log("attempting to authenticate " + username + " against " + restUrl, null);
                    builder.send();
                } catch (RequestException x) {
                    // another error condition
                    callback.onFailure();
                }
            }
        });
    }

    /* Called to process credentials from login form */

    public void authenticate(final String username, final String password) {
        authenticate(dispatchasync, mainWindow, username, password, new AuthenticationCallback() {
            @Override
            public void onFailure() {
                fail();
            }

            @Override
            public void onSuccess(String userUri, String sessionKey) {
                GWT.log("authentication succeeded for " + userUri + " with key " + sessionKey + ", redirecting ...");
            }
        });
    }

    void fail() {
        GWT.log("Failed authenticating", null);
        Label message = new Label(
                "Incorrect username/password combination");
        message.addStyleName("loginError");
        feedbackPanel.clear();
        feedbackPanel.add(message);
        progressLabel.setText("");
    }

    /**
     * logout from server, Set sessionID and local cache of user info to null,
     * and log out of REST servlets
     */
    public static void logout(final Command onSuccess) {
        // now hit the REST authentication endpoint
        //bad creds are sent but not used (server just invalidates session)

        String restUrl = "./api/logout";
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restUrl);

        /* Since we don't use Basic AUth for the app/session, therefore we don't need to reset credentials here 
         * If the user has separately used a download URL in their browser, these two lines would remove it
         * but that is separate from the app, so - at this point - we leave normal browser behavior in place.
         */
        //       builder.setUser("_badCreds_");
        //       builder.setPassword("_reallyReallyBadCreds_");
        try {
            builder.sendRequest("", new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    // do something

                    clearLocalSession();
                    Window.alert("error logging out " + exception.getMessage());
                }

                public void onResponseReceived(Request request, Response response) {

                    if (onSuccess != null) {
                        clearLocalSession();

                        onSuccess.execute();
                    }
                }
            });
        } catch (RequestException x) {
            // another error condition, do something
            clearLocalSession();
            Window.alert("error logging out: " + x.getMessage());
        }
    }

    static protected void clearLocalSession() {

        UserSessionState state = MMDB.getSessionState();

        if (state.getCurrentUser() != null && state.getCurrentUser().getUri() != null) {

            GWT.log("user " + state.getCurrentUser().getUri() + " logging out", null);
            MMDB.clearSessionState();
        }

        // in case anyone is holding refs to the state, zero out the auth information in it
        state.setCurrentUser(null);
        state.setSessionKey(null);

        state.setAnonymous(false); // not logged in as anonymous, or anyone
        MMDB.loginStatusWidget.loggedOut();
        /* Remove session cookie - clearBrowserCreds has caused the server to invalidate it
         * so this isn't strictly necessary
         * 
         */

        String path = Window.Location.getPath();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        Cookies.removeCookie(MMDB._sessionCookieName, path);
    }

}
