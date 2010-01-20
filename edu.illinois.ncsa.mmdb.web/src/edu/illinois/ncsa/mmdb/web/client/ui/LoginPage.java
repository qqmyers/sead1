/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Date;

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
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
import edu.illinois.ncsa.mmdb.web.client.dispatch.Authenticate;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AuthenticateResult;

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
		final String username = usernameBox.getText();
		final String password = passwordBox.getText();
		MMDB.dispatchAsync.execute(new Authenticate(username, password),
				new AsyncCallback<AuthenticateResult>() {

					@Override
					public void onFailure(Throwable arg0) {
						GWT.log("Failed authenticating", arg0);
					}

					@Override
					public void onSuccess(AuthenticateResult arg0) {
						if (arg0.getAuthenticated()) {
							login(arg0.getSessionId());
							// now hit the REST authentication endpoint
							String restUrl = "./api/authenticate";
							RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restUrl);
							builder.setUser(username);
							builder.setPassword(password);
							try {
								builder.sendRequest("", new RequestCallback() {
									public void onError(Request request, Throwable exception) {
										fail();
									}
									public void onResponseReceived(Request request,	Response response) {
										// success!
										MMDB.uploadAppletCredentials = response.getText();
										redirect();
									}
								});
							} catch(RequestException x) {
								// another error condition
								fail();
							}
						} else {
							fail();
						}

					}
				});
	}

	void fail() {
		Label message = new Label(
				"Incorrect username/password combination");
		message.addStyleName("loginError");
		feedbackPanel.clear();
		feedbackPanel.add(message);
	}
	
	/**
	 * Redirect to previous page.
	 */
	protected void redirect() {
		String previousHistory = History.getToken().substring(
				History.getToken().indexOf("?p=") + 3);
		History.newItem(previousHistory);
	}

	/**
	 * Set the session id, add a cookie and add history token.
	 * 
	 * @param sessionId 
	 * 
	 */
	public static void login(String sessionId) {
		MMDB.sessionID = sessionId;
		MMDB.loginStatusWidget.login(MMDB.sessionID);
		
		// set cookie
		final long DURATION = 1000 * 60 * 5 ; // 5 minutes
	    Date expires = new Date(System.currentTimeMillis() + DURATION);
	    Cookies.setCookie("sid", sessionId, expires, null, "/", false);
	}
	
	/**
	 * Set sessionID to null and remove cookie.
	 */
	public static void logout() {
		MMDB.sessionID = null;
		Cookies.removeCookie("sid");
	}
}
