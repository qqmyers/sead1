package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Collection;
import java.util.HashSet;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveFromCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveFromCollectionResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

public class RemoveFromCollectionHandler implements ActionHandler<RemoveFromCollection, RemoveFromCollectionResult> {

	@Override
	public RemoveFromCollectionResult execute(RemoveFromCollection action,
			ExecutionContext exc) throws ActionException {
		
		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);
		Collection<String> resourcesString = action.getResources();
		Collection<Resource> resources = new HashSet<Resource>();
		
		for(String uri : resourcesString) {
			resources.add(Resource.uriRef(uri));
		}
		
		try {
			CollectionBean collectionBean = cbu.get(action.getCollectionUri());
			cbu.removeFromCollection(collectionBean, resources);
		} catch (OperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new RemoveFromCollectionResult();

	}

	@Override
	public Class<RemoveFromCollection> getActionType() {
		return RemoveFromCollection.class;
	}

	@Override
	public void rollback(RemoveFromCollection arg0,
			RemoveFromCollectionResult arg1, ExecutionContext arg2)
			throws ActionException {
		// TODO Auto-generated method stub
		
	}
}
