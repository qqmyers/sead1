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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Authenticate;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AuthenticateResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;

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
	private final MyDispatchAsync dispatchasync;

	/**
	 * @param dispatchasync
	 * 
	 */
	public LoginPage(MyDispatchAsync dispatchasync) {

		this.dispatchasync = dispatchasync;

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
				authenticate();
			}
		});

		submitButton.setTabIndex(3);
		
		table.setWidget(3, 1, submitButton);

		return table;
	}

	/**
	 * Authenticate against the REST endpoint to make sure user is 
	 * authenticated on the server side. If successful, login local.
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
					public void onSuccess(final AuthenticateResult arg0) {
						if (arg0.getAuthenticated()) {
							// now hit the REST authentication endpoint
							String restUrl = "./api/authenticate";
							RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restUrl);
							builder.setUser(username);
							builder.setPassword(password);
							try {
								GWT.log("attempting to authenticate "+username+" against "+restUrl,null);
								builder.sendRequest("", new RequestCallback() {
									public void onError(Request request, Throwable exception) {
										fail();
									}
									public void onResponseReceived(Request request,	Response response) {
										// success!
										String sessionKey = response.getText();
										GWT.log("REST auth status code = "+response.getStatusCode(), null);
										if(response.getStatusCode()>300) {
											GWT.log("authentication failed: "+sessionKey,null);
											fail();
										}
										GWT.log("user "+username+" associated with session key "+sessionKey,null);
										// login local
										login(arg0.getSessionId(), sessionKey);
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
	 * Reload page after being logged in.
	 * 
	 * FIXME remove hardcoded paths
	 */
	protected void redirect() {
		if (History.getToken().startsWith("login")) {
			History.newItem("listDatasets", true);
			// FIXME hack
			MMDB.listDatasets();
		} else {
			History.fireCurrentHistoryState();
		}
	}

	/**
	 * Set the session id, add a cookie and add history token.
	 * 
	 * @param sessionId
	 * 
	 */
	public static void login(String sessionId, String sessionKey) {
		MMDB.sessionID = sessionId;
		MMDB.sessionKey = sessionKey;
		MMDB.loginStatusWidget.login(MMDB.sessionID);

		// set cookie
		// TODO move to more prominent place... MMDB? A class with static properties?
		final long DURATION = 1000 * 60 * 60; // 60 minutes
		Date expires = new Date(System.currentTimeMillis() + DURATION);
		Cookies.setCookie("sid", sessionId, expires);
		Cookies.setCookie("sessionKey", sessionKey, expires);
	}

	public static void clearBrowserCreds() {
		// now hit the REST authentication endpoint with bad creds
		String restUrl = "./api/logout";
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restUrl);
		builder.setUser("_badCreds_");
		builder.setPassword("_reallyReallyBadCreds_");
		try {
			builder.sendRequest("", new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					// do something
					Window.alert("error logging out "+exception.getMessage());
				}
				public void onResponseReceived(Request request,	Response response) {
					// success!
					History.newItem("login"); // FIXME hardcodes destination
				}
			});
		} catch(RequestException x) {
			// another error condition, do something
			Window.alert("error logging out: "+x.getMessage());
		}
	}
	
	/**
	 * Set sessionID to null, remove cookie, and log out of REST servlets
	 */
	public static void logout() {
		if(MMDB.sessionID != null) {
			GWT.log("user "+MMDB.sessionID+" logging out", null);
		}
		MMDB.sessionID = null;
		MMDB.sessionKey = null;
		clearBrowserCreds();
		Cookies.removeCookie("sid");
		Cookies.removeCookie("sessionKey");
	}
}
