/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Collection;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.ImagePyramidBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewImageBeanUtil;

/**
 * Retrieve a specific dataset.
 * 
 * @author Luigi Marini
 * 
 */
public class GetDatasetHandler implements
		ActionHandler<GetDataset, GetDatasetResult> {

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance()
			.getBeanSession();

	/** Datasets DAO **/
	private static DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetDatasetHandler.class);

	@Override
	public GetDatasetResult execute(GetDataset arg0, ExecutionContext arg1)
			throws ActionException {
		try {
			DatasetBean datasetBean = dbu.get(arg0.getId());
			datasetBean = dbu.update(datasetBean);

			// preview
			PreviewImageBeanUtil pibu = new PreviewImageBeanUtil(beanSession);
			Collection<PreviewImageBean> previews = pibu
					.getAssociationsFor(arg0.getId());
			for (PreviewImageBean preview : previews) {
				log.debug("Preview " + preview.getLabel() + " "
						+ preview.getWidth() + "x" + preview.getHeight());
			}
			if (previews.isEmpty()) {
				log.debug("No image previews available for " + arg0.getId());
			}

			// FIXME the next query is probably unnecessary, if we can get to the underlying BeanThing
			// representing the dataset which will have this triple in it, or not
			Set<Triple> pyramids =TupeloStore.getInstance().getContext().match(Resource.uriRef(datasetBean.getUri()), ImagePyramidBeanUtil.HAS_PYRAMID, null);
			String pyramid = null;
			if (pyramids.size() > 0) {
			    pyramid = pyramids.iterator().next().getObject().getString();
			}
			return new GetDatasetResult(datasetBean, previews, pyramid);
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
