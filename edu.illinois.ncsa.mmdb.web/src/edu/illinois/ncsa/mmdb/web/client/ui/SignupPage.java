/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA, 2014 U. Michigan.  All rights reserved.
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

import com.google.api.gwt.oauth2.client.Auth;
import com.google.api.gwt.oauth2.client.AuthRequest;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddUserResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GoogleUserInfo;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GoogleUserInfoResult;

/**
 * Page to request an account
 *
 * @author Luigi Marini
 *
 */
public class SignupPage extends Composite {

    public static final String  EMAIL_REGEX = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
    private final DispatchAsync dispatchAsync;
    private final FlowPanel     mainPanel;
    private final Widget        pageTitle;
    private final Widget        signupForm;
    private SimplePanel         feedbackPanel;
    private TextBox             firstNameBox;
    private TextBox             passwordBox;
    private TextBox             emailBox;
    private TextBox             confirmPasswordBox;
    private TextBox             lastNameBox;

    private final String        socialEmail = null;

    public SignupPage(DispatchAsync dispatchAsync) {
        this.dispatchAsync = dispatchAsync;

        mainPanel = new FlowPanel();
        mainPanel.addStyleName("page");
        initWidget(mainPanel);

        // page title
        pageTitle = createPageTitle();
        mainPanel.add(pageTitle);

        // signup form
        signupForm = createSignupForm();
        mainPanel.add(signupForm);
    }

    /**
     * Signup form.
     *
     * @return
     */
    private Widget createSignupForm() {
        FlexTable socialTable = new FlexTable();
        socialTable.addStyleName("signupForm");
        Label requestLabel = new Label("To request access to this space using a social account, simply click the appropriate button below. You'll receive email once the project space administrators have reviewed your request.");
        socialTable.setWidget(0, 0, requestLabel);
        socialTable.getFlexCellFormatter().setColSpan(0, 0, 2);

        feedbackPanel = new SimplePanel();
        socialTable.setWidget(1, 0, feedbackPanel);

        socialTable.getFlexCellFormatter().setColSpan(1, 0, 2);

        // Google Oauth2 link
        Anchor googleLogin = new Anchor("Request Access using Google");
        googleLogin.setStylePrimaryName("zocial");
        googleLogin.setStyleName("google", true);
        googleLogin.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                googleSubmit();
            }
        });
        socialTable.setWidget(2, 0, googleLogin);
        socialTable.getFlexCellFormatter().setColSpan(2, 0, 2);

        // Orcid Oauth2 link
        Anchor orcidLogin = new Anchor("Request Access using ORCID");
        orcidLogin.setStylePrimaryName("zocial");
        orcidLogin.setStyleName("orcid", true);

        orcidLogin.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                LoginPage.orcidAuthLogin(Boolean.TRUE);
            }
        });
        socialTable.setWidget(3, 0, orcidLogin);
        socialTable.getFlexCellFormatter().setColSpan(3, 0, 2);

        DisclosurePanel dp = new DisclosurePanel("or Create Local Account");

        FlexTable table = new FlexTable();

        table.addStyleName("loginForm");

        KeyUpHandler submitOnEnterHandler = new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    checkEmailAndSubmit(LoginPage.LOCAL_PROVIDER);
                }

            }
        };

        // first name
        Label firstNameLabel = new Label("First name:");

        table.setWidget(0, 0, firstNameLabel);

        firstNameBox = new TextBox();

        firstNameBox.addKeyUpHandler(submitOnEnterHandler);

        table.setWidget(0, 1, firstNameBox);

        // last name
        Label lastNameLabel = new Label("Last name:");

        table.setWidget(1, 0, lastNameLabel);

        lastNameBox = new TextBox();

        lastNameBox.addKeyUpHandler(submitOnEnterHandler);

        table.setWidget(1, 1, lastNameBox);

        // email
        Label localEmailLabel = new Label("Email:");

        table.setWidget(2, 0, localEmailLabel);

        emailBox = new TextBox();

        emailBox.addKeyUpHandler(submitOnEnterHandler);

        table.setWidget(2, 1, emailBox);

        // password
        Label passwordLabel = new Label("Password:");

        table.setWidget(3, 0, passwordLabel);

        passwordBox = new PasswordTextBox();

        passwordBox.addKeyUpHandler(submitOnEnterHandler);

        table.setWidget(3, 1, passwordBox);

        // confirm password
        Label confirmPasswordLabel = new Label("Confirm password:");

        table.setWidget(4, 0, confirmPasswordLabel);

        confirmPasswordBox = new PasswordTextBox();

        confirmPasswordBox.addKeyUpHandler(submitOnEnterHandler);

        table.setWidget(4, 1, confirmPasswordBox);

        // submit button
        Button submitButton = new Button("Request Access", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                checkEmailAndSubmit(LoginPage.LOCAL_PROVIDER);
            }
        });

        table.setWidget(5, 1, submitButton);

        // set focus
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                firstNameBox.setFocus(true);
            }
        });
        dp.setContent(table);
        socialTable.setWidget(4, 0, dp);
        socialTable.getFlexCellFormatter().setColSpan(4, 0, 2);

        return socialTable;
    }

    protected void checkEmailAndSubmit(final String provider) {
        GetUser getUser = new GetUser();
        if (provider.equals(LoginPage.LOCAL_PROVIDER)) {
            getUser.setEmailAddress(emailBox.getValue());
        } else {
            getUser.setEmailAddress(socialEmail);
        }
        dispatchAsync.execute(getUser,
                new AsyncCallback<GetUserResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error checking if email is already in the system",
                                caught);
                    }

                    @Override
                    public void onSuccess(GetUserResult result) {

                        if (result.getPersonBean() != null) {
                            // user already in the system
                            showFeedbackMessage("You (" + result.getPersonBean().getEmail() + ") already have an account. " +
                                    "Please <a href=\"#login\">Login</a> instead.");
                        } else {
                            if (provider.equals(LoginPage.LOCAL_PROVIDER)) {
                                submit();
                            } else {
                                //Check provider - verify that user
                            }
                        }

                    }
                });
    }

    /**
     * Submit form. First check if the form is properly filled client side. Then
     * check that the email is not already in use. If both true, submit form and
     * show result.
     */
    protected void submit() {

        if (checkForm()) {

            AddUser addUser = new AddUser(firstNameBox.getValue(), lastNameBox
                    .getValue(), emailBox.getValue(), passwordBox.getValue());
            dispatchAsync.execute(addUser, new AsyncCallback<AddUserResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Failed adding user to system", caught);
                }

                @Override
                public void onSuccess(AddUserResult result) {

                    mainPanel.remove(signupForm);
                    final HTML thankyouText = new HTML(
                            "Your request has been received! "
                                    + "A project space administrator will review your submission and notify you when your request has been approved.");
                    thankyouText.addStyleName("loginForm");
                    mainPanel.add(thankyouText);
                }
            });
        }
    }

    private void googleSubmit() {
        AuthRequest req = new AuthRequest(LoginPage.GOOGLE_AUTH_URL, MMDB._googleClientId).withScopes(LoginPage.EMAIL_SCOPE, LoginPage.PROFILE_SCOPE);
        Auth AUTH = Auth.get();
        AUTH.clearAllTokens();
        AUTH.login(req, new Callback<String, Throwable>() {
            @Override
            public void onSuccess(final String token) {
                GWT.log("Successful auth with Google OAuth2 during signup" + token);
                googleRequestAccess(token);
            }

            @Override
            public void onFailure(Throwable caught) {
                //feedback
                showFeedbackMessage("Unable to authenticate with Google.");

            }
        });
    }

    private void googleRequestAccess(String token) {
        dispatchAsync.execute(new GoogleUserInfo(token, true), new AsyncCallback<GoogleUserInfoResult>() {

            @Override
            public void onFailure(Throwable caught) {
                showFeedbackMessage("Unable to make access request. Please contact SEAD.");

            }

            @Override
            public void onSuccess(final GoogleUserInfoResult result) {
                if (result.isCreated()) {
                    GWT.log("Created new user " + result.getUserName() + " " + result.getEmail());
                    mainPanel.remove(signupForm);
                    final HTML thankyouText = new HTML(
                            "Your request has been received! "
                                    + "A project space administrator will review your submission and notify you when your request has been approved.");
                    thankyouText.addStyleName("loginForm");
                    mainPanel.add(thankyouText);

                } else {
                    GWT.log("User already exists: " + result.getUserName() + " " + result.getEmail());
                    showFeedbackMessage("You (" + result.getEmail() + ") already have an account. " +
                            "Please <a href=\"#login\">Login</a> instead.");

                }
            }
        });
    }

    /**
     * Check validity of fields.
     *
     * @return
     *
     *         TODO stronger checks
     */
    private boolean checkForm() {
        if (firstNameBox.getValue().isEmpty()
                || firstNameBox.getValue().isEmpty()) {
            showFeedbackMessage("Please specify your name");
        } else if (emailBox.getValue().isEmpty()
                || !emailBox.getValue().matches(EMAIL_REGEX)) {
            showFeedbackMessage("Please specify a valid email address");
        } else if (passwordBox.getValue().isEmpty()
                || confirmPasswordBox.getValue().isEmpty()) {
            showFeedbackMessage("Password field cannot be left empty");
        } else if (!passwordBox.getValue()
                .equals(confirmPasswordBox.getValue())) {
            showFeedbackMessage("Password and Confirm Password fields need to be the same");
        } else if (passwordBox.getValue().length() < 5) {
            showFeedbackMessage("Please specify a password that is at least 5 characters long");
        } else {
            return true;
        }
        return false;
    }

    /**
     * Check form.
     */

    protected void showFeedbackMessage(String message) {
        HTML messageLabel = new HTML(message);
        messageLabel.addStyleName("loginError");
        feedbackPanel.clear();
        feedbackPanel.add(messageLabel);
    }

    /**
     * Create page title
     *
     * @return title widget
     */
    private Widget createPageTitle() {
        return new TitlePanel("Request Access");
    }
}
