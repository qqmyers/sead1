/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.util.HashSet;

import org.tupeloproject.kernel.BeanSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.illinois.ncsa.mmdb.web.client.DatasetService;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Server side implementation of <code>DatasetService</code>.
 * 
 * @author Luigi Marini
 *
 * @deprecated use gwt-dispatch
 */
public class DatasetServiceImpl extends RemoteServiceServlet implements
		DatasetService {

	private static final long serialVersionUID = 6537147697377708791L;

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

	private static DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
	
	@Override
	public HashSet<DatasetBean> getDatasets() {

		HashSet<DatasetBean> datasets = new HashSet<DatasetBean>();
		
		try {
			datasets = new HashSet<DatasetBean>(dbu.getAll());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return datasets;
	}

	@Override
	public HashSet<String> getDatasetIds() {
		
		HashSet<String> datasets = new HashSet<String>();
		
		try {
			datasets = new HashSet<String>(dbu.getIDs());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return datasets;
	}


}
