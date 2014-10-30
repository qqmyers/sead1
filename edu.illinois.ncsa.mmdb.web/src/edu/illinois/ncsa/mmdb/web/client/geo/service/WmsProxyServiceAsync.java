package edu.illinois.ncsa.mmdb.web.client.geo.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.illinois.ncsa.mmdb.web.common.geo.LayerInfo;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

public interface WmsProxyServiceAsync {

	void getCapabilities(AsyncCallback<LayerInfo[]> callback);
}
