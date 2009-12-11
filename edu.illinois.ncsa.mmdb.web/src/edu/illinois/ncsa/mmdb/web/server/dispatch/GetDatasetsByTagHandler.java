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
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTag;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Handler to retrieve datasets from repository tagged with a particular tag.
 * 
 * @author Luigi Marini
 * 
 */
public class GetDatasetsByTagHandler implements
		ActionHandler<GetTag, GetDatasetsResult> {

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance()
			.getBeanSession();

	/** Datasets DAO **/
	private static DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetDatasetsByTagHandler.class);

	@Override
	public Class<GetTag> getActionType() {
		return GetTag.class;
	}

	@Override
	public GetDatasetsResult execute(GetTag arg0, ExecutionContext arg1)
			throws ActionException {

		String tagTitle = arg0.getUri();

		HashSet<DatasetBean> datasets = new HashSet<DatasetBean>();

		Unifier uf = new Unifier();
		uf.addPattern("dataset", Tags.HAS_TAGGING_EVENT, "event");
		uf.addPattern("event", Tags.HAS_TAG_OBJECT, "tag");
		uf.addPattern("tag", Tags.HAS_TAG_TITLE, Resource.literal(tagTitle));
		uf.addPattern("dataset", Rdf.TYPE, Cet.DATASET);
		uf.setColumnNames("dataset");

		try {
			TupeloStore.getInstance().getContext().perform(uf);

			for (Tuple<Resource> row : uf.getResult()) {
				if (row.get(0) != null) {
					datasets.add(dbu.get(row.get(0)));
				}
			}
		} catch (OperatorException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		log.debug("Found " + datasets.size() + " datasets with tag '"
				+ tagTitle + "'");

		return new GetDatasetsResult(datasets);
	}

	@Override
	public void rollback(GetTag arg0, GetDatasetsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
