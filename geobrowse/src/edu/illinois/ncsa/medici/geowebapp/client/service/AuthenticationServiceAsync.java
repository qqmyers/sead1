package edu.illinois.ncsa.medici.geowebapp.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AuthenticationServiceAsync {

	void getUsername(AsyncCallback<String> callback);

	void login(String username, String password, AsyncCallback<String> callback);

	void logout(AsyncCallback<Void> callback);

	void getUrls(AsyncCallback<String[]> callback);

	void login(String googleAccessToken, AsyncCallback<String> callback);

	void getGoogleClientId(AsyncCallback<String> callback);

}
