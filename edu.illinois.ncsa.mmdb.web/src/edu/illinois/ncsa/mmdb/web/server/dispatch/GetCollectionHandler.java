/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.rdf.terms.Dc;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Get datasets in a paricular collection.
 * 
 * @author lmarini
 * 
 */
public class GetCollectionHandler implements
		ActionHandler<GetCollection, GetCollectionResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetCollectionHandler.class);

	@Override
	public GetCollectionResult execute(GetCollection arg0, ExecutionContext arg1)
			throws ActionException {

		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

		CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);
		DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
		
		try {
			CollectionBean collectionBean = cbu.get(arg0.getUri());

			List<DatasetBean> collection = ListDatasetsHandler.listDatasets(Dc.TITLE.getString(), false, 100, 0, arg0.getUri(), dbu);

			return new GetCollectionResult(collectionBean, collection);
		} catch (Exception e) {
			throw new ActionException(e);
		}
	}

	@Override
	public Class<GetCollection> getActionType() {
		return GetCollection.class;
	}

	@Override
	public void rollback(GetCollection arg0, GetCollectionResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
