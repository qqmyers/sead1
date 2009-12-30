package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.LinkedList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tables;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasetsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
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
		u.setColumnNames("s","o","d");
		u.addPattern("s",Rdf.TYPE,dbu.getType());
		u.addPattern("s",Resource.uriRef("http://purl.org/dc/terms/isReplacedBy"),"d",true);
		u.addPattern("s",Resource.uriRef(orderBy),"o");
		if(limit > 0) {
			u.setLimit(limit);
		}
		u.setOffset(offset);
		u.addOrderBy("d"); // FIXME should be desc, this will only work in SQL contexts currently (TUP-479)
		if(desc) { u.addOrderByDesc("o"); }
		else { u.addOrderBy("o"); }
		try {
			TupeloStore.getInstance().getContext().perform(u);
			List<String> result = new LinkedList<String>();
			for(Tuple<Resource> row : u.getResult()) {
				if(row.get(2) == null) {
					log.debug("NOT DELETED: "+row.get(0));
					result.add(row.get(0).getString());
				} else {
					log.debug("DELETED: "+row.get(0));
				}
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
			log.debug("listed "+uris.size()+" dataset(s)");
			return dbu.get(uris);
		} catch(Exception x) {
			return new LinkedList<DatasetBean>();
		}
	}

	public ListDatasetsResult execute(ListDatasets arg0, ExecutionContext arg1)
			throws ActionException {
		ListDatasetsResult r = new ListDatasetsResult(listDatasets(arg0.getOrderBy(), arg0.getDesc(), arg0.getLimit(), arg0.getOffset()));
		r.setDatasetCount(TupeloStore.getInstance().countDatasets());
		return r;
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
