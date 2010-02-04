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
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tables;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasetsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * TODO Add comments
 * 
 * @author Joe Futrelle
 *
 */
public class ListDatasetsHandler implements
		ActionHandler<ListDatasets, ListDatasetsResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(ListDatasetsHandler.class);

	@Override
	public ListDatasetsResult execute(ListDatasets arg0, ExecutionContext arg1)
			throws ActionException {
		
		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
		
		ListDatasetsResult r = new ListDatasetsResult(listDatasets(arg0
				.getOrderBy(), arg0.getDesc(), arg0.getLimit(), arg0
				.getOffset(), arg0.getInCollection(), dbu));
		r.setDatasetCount(TupeloStore.getInstance().countDatasets(arg0.getInCollection(), false));
		return r;
	}
	
	/**
	 * 
	 * @param orderBy
	 * @param desc
	 * @param limit
	 * @param offset
	 * @param inCollection
	 * @return
	 * @throws OperatorException
	 */
	private static Table<Resource> list(String orderBy, boolean desc,
			int limit, int offset, String inCollection, DatasetBeanUtil dbu)
			throws OperatorException {
		Unifier u = new Unifier();
		u.setColumnNames("s", "title", "date", "mimeType", "size", "creator",
				"o");
		if (inCollection != null) {
			u.addPattern(Resource.uriRef(inCollection), DcTerms.HAS_PART, "s");
		}
		u.addPattern("s", Rdf.TYPE, dbu.getType());
		u.addPattern("s", Dc.TITLE, "title", true);
		u.addPattern("s", Dc.DATE, "date", true);
		u.addPattern("s", Dc.FORMAT, "mimeType", true);
		u.addPattern("s", Files.LENGTH, "size", true);
		// u.addPattern("s",Dc.CREATOR,"creator",true);
		u.addPattern("s", Resource.uriRef(orderBy), "o");
		if (limit > 0) {
			u.setLimit(limit);
		}
		u.setOffset(offset);
		if (desc) {
			u.addOrderByDesc("o");
		} else {
			u.addOrderBy("o");
		}
		return TupeloStore.getInstance().unifyExcludeDeleted(u, "s");
	}

	/**
	 * 
	 * @param orderBy
	 * @param desc
	 * @param limit
	 * @param offset
	 * @param inCollection
	 * @param dbu
	 * @return
	 */
	public static List<String> listDatasetUris(String orderBy, boolean desc,
			int limit, int offset, String inCollection, DatasetBeanUtil dbu) {
		try {
			List<String> result = new LinkedList<String>();
			for (Resource r : Tables.getColumn(list(orderBy, desc, limit,
					offset, inCollection, dbu), 0)) {
				if (!result.contains(r.getString())) {
					result.add(r.getString());
				}
			}
			return result;
		} catch (OperatorException x) {
			log.error("Error listing dataset URIs", x);
			return new LinkedList<String>();
		}
	}

	/**
	 * 
	 * @param orderBy
	 * @param desc
	 * @param limit
	 * @param offset
	 * @param inCollection
	 * @param dbu
	 * @return
	 */
	private List<DatasetBean> listDatasets(String orderBy, boolean desc,
			int limit, int offset, String inCollection, DatasetBeanUtil dbu) {
		try {
			List<String> uris;
			long then = System.currentTimeMillis(); //
			try {
				uris = listDatasetUris(orderBy, desc, limit, offset,
						inCollection, dbu);
			} catch (Exception x) {
				log.error("unable to list datasets", x);
				throw x;
			}
			long between = System.currentTimeMillis();
			try {BeanSession beanSession = TupeloStore.getInstance()
				.getBeanSession();
				List<DatasetBean> result = dbu.get(uris);
				long now = System.currentTimeMillis();
				log.debug("listed " + result.size() + " dataset(s) in "
						+ (now - then) + "ms (" + (between - then) + "/"
						+ (now - between) + " u/b)");
				return result;
			} catch (OperatorException x) {
				log.error("unable to list datasets " + uris);
				throw x;
			}
		} catch (Exception x) {
			log.error("Error listing datasets", x);
			return new LinkedList<DatasetBean>();
		}
	}

	@Override
	public Class<ListDatasets> getActionType() {
		return ListDatasets.class;
	}

	@Override
	public void rollback(ListDatasets arg0, ListDatasetsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}
}
