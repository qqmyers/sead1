package edu.illinois.ncsa.medici.geowebapp.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MediciProxyServiceAsync {

	void getTags(AsyncCallback<String[]> callback);

}
