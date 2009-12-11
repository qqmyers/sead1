/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;

/**
 * @author Luigi Marini
 * 
 */
public class LoginPage extends Composite {

	private final FlowPanel mainPanel;
	private Label pageTitle;
	private TextBox usernameBox;
	private PasswordTextBox passwordBox;
	private SimplePanel feedbackPanel;

	/**
	 * 
	 */
	public LoginPage() {

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
		pageTitle = new Label("Login");
		pageTitle.addStyleName("pageTitle");
		return pageTitle;
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

		Label usernameLabel = new Label("Username:");

		table.setWidget(1, 0, usernameLabel);

		usernameBox = new TextBox();
		
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
				
		Label passwordLabel = new Label("Password:");

		table.setWidget(2, 0, passwordLabel);

		passwordBox = new PasswordTextBox();

		passwordBox.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					authenticate();
				}

			}
		});

		table.setWidget(2, 1, passwordBox);

		Button submitButton = new Button("Login", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				authenticate();
			}
		});

		table.setWidget(3, 1, submitButton);

		return table;
	}

	/**
	 * 
	 */
	protected void authenticate() {
		if (usernameBox.getText().equals("guest")
				&& passwordBox.getText().equals("guest")) {
			login();
		} else {
			Label message = new Label("Incorrect username/password combination");
			message.addStyleName("loginError");
			feedbackPanel.add(message);
		}

	}

	/**
	 * 
	 */
	protected void login() {
		String previousHistory = History.getToken().substring(
				History.getToken().indexOf("?p=") + 3);
		MMDB.sessionID = usernameBox.getText();
		MMDB.loginStatusWidget.login(MMDB.sessionID);
		History.newItem(previousHistory);
	}
}
