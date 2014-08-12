package edu.illinois.ncsa.medici.geowebapp.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.illinois.ncsa.medici.geowebapp.shared.LayerInfo;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

public interface WmsProxyServiceAsync {

	void getCapabilities(AsyncCallback<LayerInfo[]> callback);
}
