/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.ArrayList;
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
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

/**
 * Retrieve collections.
 * 
 * @author Luigi Marini
 * 
 */
public class GetCollectionsHandler implements
		ActionHandler<GetCollections, GetCollectionsResult> {

	/** Tupelo bean session **/
	private static final BeanSession beanSession = TupeloStore.getInstance()
			.getBeanSession();

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetCollectionsHandler.class);

	/** Datasets DAO **/
	private static CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);

	@Override
	public GetCollectionsResult execute(GetCollections query,
			ExecutionContext arg1) throws ActionException {
		String memberUri = query.getMemberUri();
		ArrayList<CollectionBean> collections = new ArrayList<CollectionBean>();
		List<Resource> seen = new LinkedList<Resource>();
		Unifier uf = new Unifier();
		uf.addPattern("collection", Rdf.TYPE,
				CollectionBeanUtil.COLLECTION_TYPE);
		if(memberUri != null) {
			uf.addPattern("collection", CollectionBeanUtil.DCTERMS_HAS_PART,
					Resource.uriRef(memberUri));
		}
		uf.addPattern("collection", DcTerms.DATE_CREATED, "date", true);
		uf.setColumnNames("collection", "date");
		if(query.getLimit() != 0) {
			uf.setLimit(query.getLimit());
		}
		if(query.getOffset() != 0) {
			uf.setOffset(query.getOffset());
		}
		uf.addOrderByDesc("date");
		try {
			TupeloStore.getInstance().getContext().perform(uf);
			
			for (Tuple<Resource> row : uf.getResult()) {
				Resource subject = row.get(0);
				if (subject != null) {
					if (!seen.contains(subject)) {
						CollectionBean colBean = cbu.get(subject);
						collections.add(colBean);
						seen.add(subject);
					}
				}
			}
		} catch (OperatorException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return new GetCollectionsResult(collections);
	}

	@Override
	public Class<GetCollections> getActionType() {
		return GetCollections.class;
	}

	@Override
	public void rollback(GetCollections arg0, GetCollectionsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
