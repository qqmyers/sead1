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
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddUserResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserResult;

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

        FlexTable table = new FlexTable();

        table.addStyleName("loginForm");

        feedbackPanel = new SimplePanel();

        table.setWidget(0, 0, feedbackPanel);

        table.getFlexCellFormatter().setColSpan(0, 0, 2);

        table.getFlexCellFormatter().setHorizontalAlignment(0, 0,
                HasHorizontalAlignment.ALIGN_CENTER);

        KeyUpHandler submitOnEnterHandler = new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    checkEmailAndSubmit();
                }

            }
        };

        // first name
        Label firstNameLabel = new Label("First name:");

        table.setWidget(1, 0, firstNameLabel);

        firstNameBox = new TextBox();

        firstNameBox.addKeyUpHandler(submitOnEnterHandler);

        table.setWidget(1, 1, firstNameBox);

        // last name
        Label lastNameLabel = new Label("Last name:");

        table.setWidget(2, 0, lastNameLabel);

        lastNameBox = new TextBox();

        lastNameBox.addKeyUpHandler(submitOnEnterHandler);

        table.setWidget(2, 1, lastNameBox);

        // email
        Label emailLabel = new Label("Email:");

        table.setWidget(3, 0, emailLabel);

        emailBox = new TextBox();

        emailBox.addKeyUpHandler(submitOnEnterHandler);

        table.setWidget(3, 1, emailBox);

        // password
        Label passwordLabel = new Label("Password:");

        table.setWidget(4, 0, passwordLabel);

        passwordBox = new PasswordTextBox();

        passwordBox.addKeyUpHandler(submitOnEnterHandler);

        table.setWidget(4, 1, passwordBox);

        // confirm password
        Label confirmPasswordLabel = new Label("Confirm password:");

        table.setWidget(5, 0, confirmPasswordLabel);

        confirmPasswordBox = new PasswordTextBox();

        confirmPasswordBox.addKeyUpHandler(submitOnEnterHandler);

        table.setWidget(5, 1, confirmPasswordBox);

        // submit button
        Button submitButton = new Button("Sign up", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                checkEmailAndSubmit();
            }
        });

        table.setWidget(6, 1, submitButton);

        // set focus
        DeferredCommand.addCommand(new Command() {
            @Override
            public void execute() {
                firstNameBox.setFocus(true);
            }
        });

        return table;
    }

    protected void checkEmailAndSubmit() {
        GetUser getUser = new GetUser();
        getUser.setEmailAddress(emailBox.getValue());
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
                            showFeedbackMessage("The email address you have specified is already in the system. " +
                                    "Please chose a different email address.");
                        } else {
                            submit();
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
                            "Thank you for signing up. "
                                    + "An administrator will review your submission and notify you when your account has been approved.<br>"
                                    + "Until your account has been approved you will be unable to log into the system.");
                    thankyouText.addStyleName("loginForm");
                    mainPanel.add(thankyouText);
                }
            });
        }
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
            showFeedbackMessage("Password and confirmed password fields need to be the same");
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

    /**
     * Check password strength.
     * 
     * @return
     */
    private boolean checkStrengthPassword() {
        // TODO Auto-generated method stub
        return false;
    }

    protected void showFeedbackMessage(String message) {
        Label messageLabel = new Label(message);
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
        return new TitlePanel("Sign up");
    }
}
