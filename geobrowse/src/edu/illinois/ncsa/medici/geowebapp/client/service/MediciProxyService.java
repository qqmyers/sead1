package edu.illinois.ncsa.medici.geowebapp.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.illinois.ncsa.medici.geowebapp.shared.LayerInfo;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

@RemoteServiceRelativePath("mediciProxy")
public interface MediciProxyService extends RemoteService {
	String[] getTags();

	/**
	 * get layers filtered by tag. if tag is null or "", then, it will return all layers 
	 * @param tag
	 * @return array of LayerInfo
	 */
	LayerInfo[] getLayers(String tag);
}
