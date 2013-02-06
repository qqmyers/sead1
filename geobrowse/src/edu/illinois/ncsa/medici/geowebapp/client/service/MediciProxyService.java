package edu.illinois.ncsa.medici.geowebapp.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 
 * @author Jong Lee <jonglee1@illinois.edu>
 * 
 */

@RemoteServiceRelativePath("mediciProxy")
public interface MediciProxyService extends RemoteService {
	String[] getTags();
}
