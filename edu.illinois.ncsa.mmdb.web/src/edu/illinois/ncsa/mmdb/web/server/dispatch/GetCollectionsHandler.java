/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.ArrayList;
import java.util.Collection;

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
	public GetCollectionsResult execute(GetCollections arg0,
			ExecutionContext arg1) throws ActionException {
		String memberUri = arg0.getMemberUri();
		if (memberUri == null) {
			try {
				Collection<CollectionBean> list = cbu.getAll();
				log.debug("Retrieved " + list.size() + " collections");
				for (CollectionBean collection : list) {
					log.debug("Collection " + collection.getTitle() + " | "
							+ collection.getCreationDate() + " | "
							+ collection.getLastModifiedDate());
				}
				return new GetCollectionsResult(new ArrayList<CollectionBean>(
						list));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			ArrayList<CollectionBean> collections = new ArrayList<CollectionBean>();
			ArrayList<String> seenUris = new ArrayList<String>();
			Unifier uf = new Unifier();
			uf.addPattern("collection", Rdf.TYPE,
					CollectionBeanUtil.COLLECTION_TYPE);
			uf.addPattern("collection", CollectionBeanUtil.DCTERMS_HAS_PART,
					Resource.uriRef(memberUri));
			uf.setColumnNames("collection", "resource");

			try {
				TupeloStore.getInstance().getContext().perform(uf);

				for (Tuple<Resource> row : uf.getResult()) {
					if (row.get(0) != null) {
						if (!seenUris.contains(row.get(0).getString())) {
							collections.add(cbu.get(row.get(0)));
							seenUris.add(row.get(0).getString());
						}
					}
				}
			} catch (OperatorException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return new GetCollectionsResult(collections);
		}
		return new GetCollectionsResult(new ArrayList<CollectionBean>());
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
