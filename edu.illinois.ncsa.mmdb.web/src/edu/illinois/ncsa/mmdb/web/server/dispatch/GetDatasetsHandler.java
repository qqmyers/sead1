/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.HashSet;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Handler to retrieve datasets from repository.
 * 
 * @author Luigi Marini
 * 
 */
public class GetDatasetsHandler implements
		ActionHandler<GetDatasets, GetDatasetsResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetDatasetsHandler.class);

	@Override
	public GetDatasetsResult execute(GetDatasets arg0, ExecutionContext arg1)
			throws ActionException {
		
		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
		
		HashSet<DatasetBean> datasets = new HashSet<DatasetBean>();

		try {
			datasets = new HashSet<DatasetBean>(dbu.getAll());
		} catch (Exception e) {
			log.error("Error retrieving datasets from Tupelo repository");
			throw new ActionException(
					"Error retrieving datasets from Tupelo repository", e);
		}

		// FIXME here only for debug purposes to help figure out why
		// some times the table listing datasets doesn't show anything
		log.debug("Retrieved " + datasets.size() + " datasets from tupelo.");

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
