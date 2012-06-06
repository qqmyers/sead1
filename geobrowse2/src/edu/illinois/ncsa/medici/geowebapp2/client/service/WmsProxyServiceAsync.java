package edu.illinois.ncsa.medici.geowebapp2.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 *
 */

public interface WmsProxyServiceAsync {

	void getCapabilities(AsyncCallback<String> callback);

}
