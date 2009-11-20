/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.BeanSession;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Retrieve a specific dataset.
 * 
 * @author Luigi Marini
 *
 */
public class GetDatasetHandler implements ActionHandler<GetDataset, GetDatasetResult>{

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

	/** Datasets DAO **/
	private static DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
	
	@Override
	public GetDatasetResult execute(GetDataset arg0, ExecutionContext arg1)
			throws ActionException {
		try {
			DatasetBean datasetBean = dbu.get(arg0.getId());
			datasetBean = dbu.update(datasetBean);
			return new GetDatasetResult(datasetBean);
		} catch (Exception e) {
			throw new ActionException(e);
		}
	}

	@Override
	public Class<GetDataset> getActionType() {
		return GetDataset.class;
	}

	@Override
	public void rollback(GetDataset arg0, GetDatasetResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
