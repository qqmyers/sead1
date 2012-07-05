package edu.illinois.ncsa.medici.geowebapp.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.illinois.ncsa.medici.geowebapp.shared.LayerInfo;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

@RemoteServiceRelativePath("wmsProxy")
public interface WmsProxyService extends RemoteService {
	LayerInfo[] getCapabilities();
}
