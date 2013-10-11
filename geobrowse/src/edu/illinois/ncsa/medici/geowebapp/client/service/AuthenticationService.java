package edu.illinois.ncsa.medici.geowebapp.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("Authenticator")
public interface AuthenticationService  extends RemoteService {
		String login(String username, String password);
		void logout();
		String getUsername();
		String[] getUrls();
	}

