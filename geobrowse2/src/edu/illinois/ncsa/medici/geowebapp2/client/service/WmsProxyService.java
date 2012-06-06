package edu.illinois.ncsa.medici.geowebapp2.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

@RemoteServiceRelativePath("wmsProxy")
public interface WmsProxyService extends RemoteService {
	String getCapabilities();
}
