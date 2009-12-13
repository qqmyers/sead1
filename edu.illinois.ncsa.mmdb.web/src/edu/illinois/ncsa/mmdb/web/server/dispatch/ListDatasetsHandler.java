package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tables;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasetsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

public class ListDatasetsHandler implements ActionHandler<ListDatasets, ListDatasetsResult> {

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance()
			.getBeanSession();

	/** Datasets DAO **/
	private static DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetDatasetsHandler.class);


	private List<String> listDatasetUris(String orderBy, boolean desc, int limit, int offset) {
		Unifier u = new Unifier();
		u.setColumnNames("s","o");
		u.addPattern("s",Rdf.TYPE,dbu.getType());
		u.addPattern("s",Resource.uriRef(orderBy),"o");
		if(limit > 0) {
			u.setLimit(limit);
		}
		u.setOffset(offset);
		if(desc) { u.addOrderByDesc("o"); }
		else { u.addOrderBy("o"); }
		try {
			TupeloStore.getInstance().getContext().perform(u);
			List<String> result = new LinkedList<String>();
			for(Resource r : Tables.getColumn(u.getResult(),0)) {
				result.add(r.getString());
			}
			return result;
		} catch(OperatorException x) {
			return new LinkedList<String>();
		}
	}

	private List<DatasetBean> listDatasets(String orderBy, boolean desc,
			int limit, int offset) {
		try {
			List<String> uris = listDatasetUris(orderBy,desc,limit,offset);
			// FIXME debug
			for(String uri : uris) {
				DatasetBean dataset = dbu.get(uri);
				log.info(dataset.getDate()+": "+dataset.getLabel());
			}
			// FIXME end debug
			return dbu.get(uris);
		} catch(Exception x) {
			return new LinkedList<DatasetBean>();
		}
	}

	public ListDatasetsResult execute(ListDatasets arg0, ExecutionContext arg1)
			throws ActionException {
		// TODO Auto-generated method stub
		List<String> datasetUris = listDatasetUris(arg0.getOrderBy(), arg0.getDesc(), arg0.getLimit(), arg0.getOffset());
		List<DatasetBean> datasets = new LinkedList<DatasetBean>();
		Map<DatasetBean,Collection<PreviewImageBean>> previews = new HashMap<DatasetBean,Collection<PreviewImageBean>>();
		for(String datasetUri : datasetUris) {
			// FIXME in this kludge, a server-side call is made directly
			// against the get dataset command handler (not using the
			// dispatch mechanism). the code for fetching previews should
			// be abstracted above both commands.
			GetDataset gd = new GetDataset(datasetUri);
			GetDatasetHandler handler = new GetDatasetHandler();
			GetDatasetResult result = handler.execute(gd, null);
			DatasetBean dataset = result.getDataset();
			datasets.add(dataset);
			previews.put(dataset, result.getPreviews());
		}
		return new ListDatasetsResult(datasets, previews);
	}

	public Class<ListDatasets> getActionType() {
		// TODO Auto-generated method stub
		return ListDatasets.class;
	}

	public void rollback(ListDatasets arg0, ListDatasetsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}
}
