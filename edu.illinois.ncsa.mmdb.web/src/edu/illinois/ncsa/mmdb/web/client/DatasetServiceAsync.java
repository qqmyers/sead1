package edu.illinois.ncsa.mmdb.web.client;

import java.util.HashSet;
import java.util.List;

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

	/**
	 * List datasets in repository
	 * @param orderBy uri of property to order to
	 * @param desc true if order descending
	 * @param limit max number of datasets to return
	 * @param offset number of initial datasets to skip
	 */
	void listDatasetUris(String orderBy, boolean desc, int limit, int offset, AsyncCallback<List<String>> callback);

	/**
	 * List datasets in repository
	 * @param orderBy uri of property to order to
	 * @param desc true if order descending
	 * @param limit max number of datasets to return
	 * @param offset number of initial datasets to skip
	 */
	void listDatasets(String orderBy, boolean desc, int limit, int offset, AsyncCallback<List<DatasetBean>> callback);
}
