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
 */
public class DatasetServiceImpl extends RemoteServiceServlet implements
		DatasetService {

	private static final long serialVersionUID = 6537147697377708791L;

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

	@Override
	public HashSet<DatasetBean> getDatasets() {

		DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
		
		HashSet<DatasetBean> datasets = new HashSet<DatasetBean>();
		
		try {
			datasets = new HashSet<DatasetBean>(dbu.getAll());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return datasets;
	}


}
