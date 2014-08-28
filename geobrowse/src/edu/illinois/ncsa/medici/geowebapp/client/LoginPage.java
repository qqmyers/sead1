package edu.illinois.ncsa.medici.geowebapp.client;

import java.io.IOException;

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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Jim Myers
 * 
 *         Derived from edu.illinois.ncsa.mmdb.web.client.ui.LoginPage
 * 
 */
public class LoginPage extends Composite {

	private final FlowPanel mainPanel;

	private TextBox usernameBox;
	private PasswordTextBox passwordBox;
	private SimplePanel feedbackPanel;
	private Label progressLabel;
	private final Geo_webapp mainWindow;
	private String googleClientId = null;

	// Google Oauth2
	//Should get from Google discovery doc...(https://accounts.google.com/.well-known/openid-configuration)
	private final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
	private final String EMAIL_SCOPE = "email";
	private final String PROFILE_SCOPE = "profile";

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
		final String finalStatus = status;
		mainWindow.getAuthSvc().getGoogleClientId(new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error retrieving Google OAuth2 properties", caught);
				mainPanel.add(createLoginForm(finalStatus));
			}

			@Override
			public void onSuccess(String result) {
				googleClientId = result;
				mainPanel.add(createLoginForm(finalStatus));
			}
		});
		// login form
		
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


		if (googleClientId != null) {
			// Google Oauth2 link
			Anchor googleLogin = new Anchor("Login using Google");
			googleLogin.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					googleAuthLogin();
				}
			});
			table.setWidget(5, 0, googleLogin);
			table.getFlexCellFormatter().setColSpan(5, 0, 2);
			table.getFlexCellFormatter().setHorizontalAlignment(5, 0,
					HasAlignment.ALIGN_CENTER);
		}

		return table;
	}

	/**
	 * Login using google oauth2.
	 */
	private void googleAuthLogin() {

		mainWindow.getAuthSvc().getGoogleClientId(new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error retrieving Google OAuth2 properties", caught);
			}

			@Override
			public void onSuccess(String result) {
				final String googleClientId = result;

				// URL authrequestUrl = = new URL(
				// "https://accounts.google.com/o/oauth2/auth?scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile&state=%2Fprofile&redirect_uri="
				// + getMediciUrl() +
				// "%2Foauth2callback&response_type=token&client_id=" +
				// googleClientId);

				AuthRequest req = new AuthRequest(AUTH_URL, googleClientId)
						.withScopes(EMAIL_SCOPE, PROFILE_SCOPE);
				Auth AUTH = Auth.get();
				//Remove to avoid popup!!!!!
				AUTH.clearAllTokens();
				AUTH.login(req, new Callback<String, Throwable>() {
					@Override
					public void onSuccess(String token) {
						GWT.log("Successful login " + token);
						final String googleAccessToken = token;
						mainWindow.getAuthSvc().login(token,
								new AsyncCallback<String>() {
									@Override
									public void onSuccess(String result) {

										final String loginStatus = result;
										setProgressText(loginStatus);

										if (loginStatus.equals("forbidden")
												|| loginStatus
														.equals("unauthorized")) {
											mainWindow.getLoginStatusWidget()
													.loggedOut();
											resetPassword();

										} else {
											mainWindow
													.getAuthSvc()
													.getUsername(
															new AsyncCallback<String>() {
																@Override
																public void onSuccess(
																		final String name) {

																	googleRemoteLogin(
																			name,
																			googleAccessToken,
																			new AsyncCallback<String>() {
																				@Override
																				public void onSuccess(
																						String name) {

																					if (name.length() == 0) {
																						name = null;
																					}
																					mainWindow
																							.setLoginState(
																									name,
																									loginStatus);
																				}

																				@Override
																				public void onFailure(
																						Throwable caught) {
																					// Non-fatal
																					mainWindow
																							.setLoginState(
																									name,
																									loginStatus);
																				}
																			});

																}

																@Override
																public void onFailure(
																		Throwable caught) {
																	mainWindow
																			.getLoginStatusWidget()
																			.loggedOut();
																	resetPassword();
																}
															});

										}
									}

									public void onFailure(Throwable caught) {
										Window.alert("Login failed "
												+ caught.toString());
									}
								});
					}

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Login failed " + caught.toString());
					}
				});
			}
		});
	}

	private void setProgressText(String status) {
		String message = "";
		if (status != null) {
			if (status.equals("unauthorized")) {
				message = "Incorrect username or password";
			} else if (status.equals("forbidden")) {
				message = "Login failure or you do not have permission to use this interface.";
			}
		}
		progressLabel.setText(message);
		progressLabel.setStylePrimaryName("warningMsg");
	}

	private void resetPassword() {
		passwordBox.setText("");
	}

	protected void authenticate() {
		final String password = passwordBox.getText().length() > 0 ? passwordBox
				.getText() : "(none)";
		mainWindow.getAuthSvc().login(usernameBox.getText(), password,
				new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {

						final String loginStatus = result;
						setProgressText(loginStatus);

						if (loginStatus.equals("forbidden")
								|| loginStatus.equals("unauthorized")) {
							mainWindow.getLoginStatusWidget().loggedOut();
							resetPassword();

						} else {
							mainWindow.getAuthSvc().getUsername(
									new AsyncCallback<String>() {
										@Override
										public void onSuccess(final String name) {

											remoteLogin(
													name,
													password,
													new AsyncCallback<String>() {
														@Override
														public void onSuccess(
																String name) {
															resetPassword();
															mainWindow
																	.setLoginState(
																			name,
																			loginStatus);
														}

														@Override
														public void onFailure(
																Throwable caught) {
															// Non-fatal -
															// currently happens
															// if app and mmdb
															// are not on the
															// same server
															mainWindow
																	.setLoginState(
																			name,
																			loginStatus);
															resetPassword();
														}
													});

										}

										@Override
										public void onFailure(Throwable caught) {
											mainWindow.getLoginStatusWidget()
													.loggedOut();
											resetPassword();
										}
									});
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						mainWindow.getLoginStatusWidget().loggedOut();
						resetPassword();
					}
				});
	}

	public static void remoteLogin(final String username, String password,
			final AsyncCallback<String> callback) {
		String restUrl = Geo_webapp.getMediciUrl() + "/api/authenticate";
		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
				restUrl);
		builder.setHeader("Content-type", "application/x-www-form-urlencoded");
		// CORS HEADERS NEEDED: builder.setHeader("Content-type",
		// "application/x-www-form-urlencoded");
		// builder.setHeader("Content-type",
		// "application/x-www-form-urlencoded");

		StringBuilder sb = new StringBuilder();
		sb.append("username=" + username);
		sb.append("&password=" + password);
		builder.setIncludeCredentials(true);
		builder.setRequestData(sb.toString());
		builder.setCallback(new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				callback.onFailure(exception);
			}

			public void onResponseReceived(Request request, Response response) {
				// success!
				String sessionKey = response.getText();
				GWT.log("REST auth status code = " + response.getStatusCode(),
						null);
				if (response.getStatusCode() > 300) {
					GWT.log("authentication failed: " + sessionKey, null);
					callback.onFailure(new IOException());
				}
				GWT.log("user " + username + " associated with session key "
						+ sessionKey, null);
				// login local
				callback.onSuccess(username);
			}
		});

		try {
			GWT.log("attempting to authenticate " + username + " against "
					+ restUrl, null);
			builder.send();

		} catch (RequestException x) {
			// another error condition
			callback.onFailure(x);
		}
	}

	private void googleRemoteLogin(final String username,
			String googleAccessToken, final AsyncCallback<String> callback) {
		String restUrl = Geo_webapp.getMediciUrl() + "/api/authenticate";
		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
				restUrl);
		builder.setHeader("Content-type", "application/x-www-form-urlencoded");
		// CORS HEADERS NEEDED: builder.setHeader("Content-type",
		// "application/x-www-form-urlencoded");
		// builder.setHeader("Content-type",
		// "application/x-www-form-urlencoded");
		builder. setIncludeCredentials(true);
		StringBuilder sb = new StringBuilder();
		sb.append("username=" + username);
		sb.append("&googleAccessToken=" + googleAccessToken);
		builder.setRequestData(sb.toString());
		builder.setCallback(new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				callback.onFailure(exception);
			}

			public void onResponseReceived(Request request, Response response) {
				// success!
				String sessionKey = response.getText();
				GWT.log("REST auth status code = " + response.getStatusCode(),
						null);
				if (response.getStatusCode() > 300) {
					GWT.log("authentication failed: " + sessionKey, null);
					callback.onFailure(new IOException());
				}
				GWT.log("user " + username + " associated with session key "
						+ sessionKey, null);
				// login local
				callback.onSuccess(username);
			}
		});

		try {
			GWT.log("attempting to authenticate " + username + " against "
					+ restUrl, null);
			builder.send();

		} catch (RequestException x) {
			// another error condition
			callback.onFailure(x);
		}
	}
}
