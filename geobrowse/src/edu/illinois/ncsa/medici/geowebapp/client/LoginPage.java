package edu.illinois.ncsa.medici.geowebapp.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.medici.geowebapp.client.TitlePanel;

/**
 * @author Jim Myers
 * 
 */
public class LoginPage extends Composite {

	private final FlowPanel mainPanel;

	private TextBox usernameBox;
	private PasswordTextBox passwordBox;
	private SimplePanel feedbackPanel;
	private Label progressLabel;
	private final Geo_webapp mainWindow;

	/**
	 * @param dispatchasync
	 * 
	 */
	public LoginPage(Geo_webapp mainWindow, String status) {

		this.mainWindow = mainWindow;

		mainPanel = new FlowPanel();

		mainPanel.addStyleName("page");

		initWidget(mainPanel);

		// page title
		mainPanel.add(createPageTitle());

		// login form
		mainPanel.add(createLoginForm(status));
	}

	/**
	 * 
	 * @return
	 */
	private Widget createPageTitle() {
		return (Widget) new TitlePanel("Login");
	}

	/**
	 * 
	 * @return
	 */
	private Widget createLoginForm(String status) {
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

		// DeferredCommand.addCommand
		Scheduler.get().scheduleDeferred(new Command() {
			@Override
			public void execute() {
				usernameBox.setFocus(true);
			}
		});

		// sign up
		table.setWidget(1, 3, new Anchor("Sign up", Geo_webapp.getMediciUrl()
				+ "/#signup"));

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
		table.setWidget(2, 3,
				new Anchor("Forgot Password?", Geo_webapp.getMediciUrl()
						+ "/#requestNewPassword"));

		Button submitButton = new Button("Login", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				progressLabel.removeStyleName("warningMsg");
				progressLabel.setText("Logging in...");
				authenticate();
			}
		});

		submitButton.setTabIndex(3);

		table.setWidget(3, 1, submitButton);

		progressLabel = new Label();
		setProgressText(status);

		table.setWidget(4, 1, progressLabel);
		table.getFlexCellFormatter().setColSpan(4, 1, 2);
		table.getFlexCellFormatter().setHorizontalAlignment(4, 0,
				HasAlignment.ALIGN_CENTER);

		return table;
	}

	private void setProgressText(String status) {
		String message = "";
		if (status != null) {
			if (status.equals("unauthorized")) {
				message = "Incorrect username or password";
			} else if (status.equals("forbidden")) {
				message = "You do not have permission to use this interface.";
			}
		}
		progressLabel.setText(message);
		progressLabel.setStylePrimaryName("warningMsg");
	}

	private void resetPassword() {
		passwordBox.setText("");
	}

	protected void authenticate() {
		String password = passwordBox.getText().length() > 0 ? passwordBox
				.getText() : "(none)";
		mainWindow.getAuthSvc().login(usernameBox.getText(), password,
				new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {

						final String loginStatus = result;
						mainWindow.getAuthSvc().getUsername(
								new AsyncCallback<String>() {
									@Override
									public void onSuccess(String name) {
										setProgressText(loginStatus);
										resetPassword();
										mainWindow.setLoginState(name,
												loginStatus);
									}

									@Override
									public void onFailure(Throwable caught) {
										mainWindow.getLoginStatusWidget()
												.loggedOut();
										resetPassword();
									}
								});
					}

					@Override
					public void onFailure(Throwable caught) {
						mainWindow.getLoginStatusWidget().loggedOut();
						resetPassword();
					}
				});
	}

}
