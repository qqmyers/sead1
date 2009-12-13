/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tables;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.illinois.ncsa.mmdb.web.client.DatasetService;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Server side implementation of <code>DatasetService</code>.
 * 
 * @author Luigi Marini
 *
 * @deprecated use gwt-dispatch
 */
public class DatasetServiceImpl extends RemoteServiceServlet implements
		DatasetService {

	private static final long serialVersionUID = 6537147697377708791L;

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

	private static DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
	
	@Override
	public HashSet<DatasetBean> getDatasets() {

		HashSet<DatasetBean> datasets = new HashSet<DatasetBean>();
		
		try {
			datasets = new HashSet<DatasetBean>(dbu.getAll());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return datasets;
	}

	@Override
	public HashSet<String> getDatasetIds() {
		
		HashSet<String> datasets = new HashSet<String>();
		
		try {
			datasets = new HashSet<String>(dbu.getIDs());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return datasets;
	}

	public List<String> listDatasetUris(String orderBy, boolean desc,
			int limit, int offset) {
		Unifier u = new Unifier();
		u.setColumnNames("s");
		u.addPattern("s",Rdf.TYPE,Cet.DATASET);
		u.addPattern("s",Resource.uriRef(orderBy),"o");
		u.setLimit(limit);
		u.setOffset(offset);
		if(!desc) { u.addOrderBy("o"); }
		else { u.addOrderByDesc("o"); }
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

	public List<DatasetBean> listDatasets(String orderBy, boolean desc,
			int limit, int offset) {
		try {
			return dbu.get(listDatasetUris(orderBy,desc,limit,offset));
		} catch(Exception x) {
			return new LinkedList<DatasetBean>();
		}
	}
}
