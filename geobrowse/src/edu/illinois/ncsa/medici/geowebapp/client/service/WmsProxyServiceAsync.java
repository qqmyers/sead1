package edu.illinois.ncsa.medici.geowebapp.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 *
 */

public interface WmsProxyServiceAsync {

	void getCapabilities(AsyncCallback<String> callback);

}
