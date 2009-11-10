/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.HashSet;

import org.tupeloproject.kernel.BeanSession;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * Handler to retrieve datasets from repository.
 * 
 * @author Luigi Marini
 *
 */
public class GetDatasetsHandler implements ActionHandler<GetDatasets, GetDatasetsResult> {

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

	/** Datasets DAO **/
	private static DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
	
	@Override
	public GetDatasetsResult execute(GetDatasets arg0, ExecutionContext arg1)
			throws ActionException {
		
		HashSet<DatasetBean> datasets = new HashSet<DatasetBean>();
		
		try {
			datasets = new HashSet<DatasetBean>(dbu.getAll());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new GetDatasetsResult(datasets);
	}

	@Override
	public Class<GetDatasets> getActionType() {
		return GetDatasets.class;
	}

	@Override
	public void rollback(GetDatasets arg0, GetDatasetsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
