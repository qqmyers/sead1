/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Date;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.ObjectResourceMapping;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

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
		
		try {
			CollectionBean collectionBean = fastGetCollection(cbu, arg0.getUri());

			// FIXME this might be slow for large collections, although its result is memoized per collection
			int collectionSize = TupeloStore.getInstance().countDatasets(arg0.getUri(), false);
			
			return new GetCollectionResult(collectionBean, collectionSize); 
		} catch (Exception e) {
			throw new ActionException(e);
		}
	}

	// FIXME use Rob's BeanFactory instead of this hardcoded way
	private CollectionBean fastGetCollection(CollectionBeanUtil cbu, String uriString) throws OperatorException {
		PersonBeanUtil pbu = new PersonBeanUtil(cbu.getBeanSession());
		Unifier u = new Unifier();
		Resource uri = Resource.uriRef(uriString);
		u.addPattern(uri,Rdf.TYPE,cbu.getType());
		u.addPattern(uri,Dc.CREATOR,"creator",true);
		u.addPattern(uri,Dc.TITLE,"title",true);
		u.addPattern(uri,Dc.DESCRIPTION,"description",true);
		u.addPattern(uri,DcTerms.DATE_CREATED,"dateCreated",true);
		u.addPattern(uri,DcTerms.DATE_MODIFIED,"dateModified",true);
		u.setColumnNames("creator","title","description","dateCreated","dateModified");
		CollectionBean colBean = new CollectionBean();
		TupeloStore.getInstance().getContext().perform(u);
		for(Tuple<Resource> row : u.getResult()) {
			log.debug(row);
			int r = 0;
			Resource creator = row.get(r++);
			Resource title = row.get(r++);
			Resource description = row.get(r++);
			Resource dateCreated = row.get(r++);
			Resource dateModified = row.get(r++);
			colBean.setUri(uriString);
			if(creator != null) {
				colBean.setCreator(pbu.get(creator));
			}
			if(title != null) {
				colBean.setTitle(title.getString());
			}
			if(description != null) {
				colBean.setDescription(description.getString());
			}
			if(dateCreated != null) {
				colBean.setCreationDate((Date)ObjectResourceMapping.object(dateCreated));
			}
			if(dateModified != null) {
				colBean.setLastModifiedDate((Date)ObjectResourceMapping.object(dateModified));
			}
			return colBean;
		}
		return null;
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
