/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

import java.util.HashSet;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Simple GWT rpc service to manage datasets.
 * 
 * @author Luigi Marini
 * 
 * @deprecated use gwt-dispatch
 */
@RemoteServiceRelativePath("dataset")
public interface DatasetService extends RemoteService {

	/** 
	 * Retrieve all datasets in the repository.
	 *  
	 * @return a set of datasets
	 */
	HashSet<DatasetBean> getDatasets();
	
	/**
	 * Retrieve all dataset resources in repository
	 */
	HashSet<String> getDatasetIds();

}
