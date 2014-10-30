package edu.illinois.ncsa.mmdb.web.client.geo.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.illinois.ncsa.mmdb.web.common.geo.LayerInfo;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

@RemoteServiceRelativePath("wmsProxy")
public interface WmsProxyService extends RemoteService {
	LayerInfo[] getCapabilities();

}
