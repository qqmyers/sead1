package edu.illinois.ncsa.mmdb.web.client;

import java.util.HashSet;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * The async counterpart of <code>DatasetService</code>.
 *
 * @author Luigi Marini
 * 
 * @deprecated use gwt-dispatch
 */
public interface DatasetServiceAsync {

	public void getDatasets(AsyncCallback<HashSet<DatasetBean>> callback);

	void getDatasetIds(AsyncCallback<HashSet<String>> callback);

}
