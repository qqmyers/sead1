/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.ArrayList;
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
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.server.Memoized;
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

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetCollectionsHandler.class);

	/** Badges cache **/
	private Map<String,String> badges = new HashMap<String,String>();

	static Memoized<Integer> collectionCount;
	int getCollectionCount() {
		if(collectionCount == null) {
			collectionCount = new Memoized<Integer>() {
				public Integer computeValue() {
					Unifier u = new Unifier();
					u.setColumnNames("c");
					u.addPattern("c",Rdf.TYPE,Cet.cet("Collection"));
					try {
						long then = System.currentTimeMillis();
						TupeloStore.getInstance().unifyExcludeDeleted(u,"c");
						int count = 0;
						for(Tuple<Resource> row : u.getResult()) {
							count++;
						}
						long ms = System.currentTimeMillis() - then;
						log.debug("counted "+count+" collection(s) in "+ms+"ms");
						return count;
					} catch (OperatorException e) {
						e.printStackTrace();
						return 0;
					}
				}
			};
			collectionCount.setTtl(30000);
		}
		return collectionCount.getValue();
	}
	
	@Override
	public GetCollectionsResult execute(GetCollections query,
			ExecutionContext arg1) throws ActionException {
		
		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);
		
		int limit = query.getLimit();
		int offset = query.getOffset();
		ArrayList<CollectionBean> collections = new ArrayList<CollectionBean>();
		List<Resource> seen = new LinkedList<Resource>(); 
		List<String> badges = new LinkedList<String>();
		try {
			int dups = 1;
			while(dups > 0) {
				int news = 0;
				dups = 0;

				Unifier uf = createUnifier(query, limit, offset);

				Table<Resource> result = TupeloStore.getInstance().unifyExcludeDeleted(uf, "collection");
				
				for (Tuple<Resource> row : result) {
					Resource subject = row.get(0);
					if (subject != null) {
						try {
							if (!seen.contains(subject)) { // FIXME: because of this logic, we may return fewer than the limit!
								CollectionBean colBean = cbu.get(subject);
								collections.add(colBean);
								badges.add(getBadge(colBean.getUri()));
								seen.add(subject);
								news++;
							} else {
								dups++;
							}
						} catch(OperatorException x) {
							log.error("Unable to fetch collection " + subject,x);
						}
					}
				}
				if(limit > 0 && dups > 0) {
					limit = dups;
					offset += news;  // FIXME: wow, this is a hack
				}
			}
		} catch (OperatorException e1) {
			e1.printStackTrace();
		}
		GetCollectionsResult result = new GetCollectionsResult(collections);
		result.setBadges(badges);
		result.setCount(getCollectionCount());
		return result; 
	}
	
	/**
	 * 
	 * @param query
	 * @param limit
	 * @param offset
	 * @return
	 */
	private Unifier createUnifier(GetCollections query, int limit, int offset) {
		Unifier uf = new Unifier();
		uf.addPattern("collection", Rdf.TYPE,
				CollectionBeanUtil.COLLECTION_TYPE);
		if(query.getMemberUri() != null) {
			uf.addPattern("collection", CollectionBeanUtil.DCTERMS_HAS_PART,
					Resource.uriRef(query.getMemberUri()));
		}
		Resource sortKey = DcTerms.DATE_CREATED;
		if(query.getSortKey() != null) {
			sortKey = Resource.uriRef(query.getSortKey());
		}
		uf.addPattern("collection", sortKey, "o", true);
		uf.setColumnNames("collection", "o");
		if(limit > 0) { uf.setLimit(limit); }
		if(offset > 0) { uf.setOffset(offset); }
		if(!query.isDesc()) {
			uf.addOrderBy("o");
		} else {
			uf.addOrderByDesc("o");
		}
		//System.out.println(SparqlQueryFactory.toSparql(uf));
		return uf;
	}
	
	/**
	 * 
	 * @param collectionUri
	 * @return
	 */
	private String getBadge(String collectionUri) {
		String badge = badges.get(collectionUri);
		if(badge == null) {
			try {
				Unifier u = new Unifier();
				u.setColumnNames("member", "date");
				u.addPattern(Resource.uriRef(collectionUri), DcTerms.HAS_PART, "member");
				u.addPattern("member", Dc.DATE, "date", true);
				u.addOrderByDesc("date");
				u.addOrderBy("member");
				u.setLimit(1);
				for(Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "member")) {
					badge = row.get(0).getString();
					if(badges.size() > 200) {
						// crude capacity management
						badges = new HashMap<String,String>();
					}
					badges.put(collectionUri, badge);
					return badge;
				}
			} catch(OperatorException e) {
				log.error("Error getting badges for collection " + collectionUri, e);
			}
		}
		return badge;
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
