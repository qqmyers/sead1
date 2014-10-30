package edu.illinois.ncsa.mmdb.web.client.geo.service;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.illinois.ncsa.mmdb.web.common.geo.LayerInfo;
import edu.illinois.ncsa.mmdb.web.common.geo.LocationInfo;

public interface MediciProxyServiceAsync {

	void getTags(AsyncCallback<String[]> callback);

	void getLayers(String tag, AsyncCallback<LayerInfo[]> callback);

	void getLocations(String tag, AsyncCallback<LocationInfo[]> callback);

}
