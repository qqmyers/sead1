package edu.illinois.ncsa.medici.geowebapp.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.illinois.ncsa.medici.geowebapp.shared.LayerInfo;

public interface MediciProxyServiceAsync {

	void getTags(AsyncCallback<String[]> callback);

	void getLayers(String tag, AsyncCallback<LayerInfo[]> callback);

}
