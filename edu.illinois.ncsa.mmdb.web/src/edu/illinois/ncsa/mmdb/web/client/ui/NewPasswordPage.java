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
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Authenticate;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AuthenticateResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.NewPassword;

/**
 * @author Luigi Marini
 * 
 */
public class NewPasswordPage extends Page {

	private SimplePanel feedbackPanel;
	private TextBox currentPasswordBox;
	private PasswordTextBox passwordBox;
	private PasswordTextBox confirmPasswordBox;
	private FlexTable form;

	/**
	 * 
	 * @param dispatchAsync
	 */
	public NewPasswordPage(DispatchAsync dispatchAsync) {
		super("Request New Password", dispatchAsync);
	}

	/**
	 * 
	 */
	@Override
	public void layout() {
		form = createForm();
		mainLayoutPanel.add(form);
	}

	/**
	 * 
	 * @return
	 */
	private FlexTable createForm() {
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
					checkPasswordsAndSubmit();
				}

			}
		};

		// current password
		Label currentPasswordLabel = new Label("Current password:");

		table.setWidget(1, 0, currentPasswordLabel);

		currentPasswordBox = new PasswordTextBox();

		currentPasswordBox.addKeyUpHandler(submitOnEnterHandler);

		table.setWidget(1, 1, currentPasswordBox);

		// password
		Label passwordLabel = new Label("New Password:");

		table.setWidget(4, 0, passwordLabel);

		passwordBox = new PasswordTextBox();

		passwordBox.addKeyUpHandler(submitOnEnterHandler);

		table.setWidget(4, 1, passwordBox);

		// confirm password
		Label confirmPasswordLabel = new Label("Confirm New Password:");

		table.setWidget(5, 0, confirmPasswordLabel);

		confirmPasswordBox = new PasswordTextBox();

		confirmPasswordBox.addKeyUpHandler(submitOnEnterHandler);

		table.setWidget(5, 1, confirmPasswordBox);

		// submit button
		Button submitButton = new Button("Update", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				checkPasswordsAndSubmit();
			}
		});

		table.setWidget(6, 1, submitButton);

		// set focus
		DeferredCommand.addCommand(new Command() {
			@Override
			public void execute() {
				currentPasswordBox.setFocus(true);
			}
		});

		return table;
	}

	/**
	 * 
	 */
	protected void checkPasswordsAndSubmit() {
		
		if (checkForm()) {

			dispatchAsync.execute(new Authenticate(MMDB.getUsername(),
					currentPasswordBox.getText()),
					new AsyncCallback<AuthenticateResult>() {

						@Override
						public void onFailure(Throwable arg0) {
							GWT.log("Failed chacking password", arg0);
						}

						@Override
						public void onSuccess(AuthenticateResult arg0) {
							if (arg0.getAuthenticated()) {
								doUpdatePassword();
							} else {
								showFeedbackMessage("Wrong current password");
							}
						}
					});
		}
	}

	/**
	 * 
	 */
	private void doUpdatePassword() {

		dispatchAsync.execute(new NewPassword(MMDB.getUsername(), passwordBox
				.getText()), new AsyncCallback<EmptyResult>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error updating password", caught);
				showFeedbackMessage("Oops! There was an error updating the password");
			}

			@Override
			public void onSuccess(EmptyResult result) {
				clearForm();
				showFeedbackMessage("Password successfully updated");
			}
		});
	}

	/**
	 * 
	 * @return
	 */
	private boolean checkForm() {

		if (currentPasswordBox.getValue().isEmpty()) {
			showFeedbackMessage("Current password field cannot be left empty");
		} else if (passwordBox.getValue().isEmpty()
				|| confirmPasswordBox.getValue().isEmpty()) {
			showFeedbackMessage("Password fields cannot be left empty");
		} else if (!passwordBox.getValue()
				.equals(confirmPasswordBox.getValue())) {
			showFeedbackMessage("New Password and confirmed new password fields need to be the same");
		} else if (passwordBox.getValue().length() < 5) {
			showFeedbackMessage("Please specify a password that is at least 5 characters long");
		} else {
			return true;
		}
		return false;
	}

	/**
	 * 
	 */
	private void clearForm() {
		currentPasswordBox.setText("");
		passwordBox.setText("");
		confirmPasswordBox.setText("");
	}
}
