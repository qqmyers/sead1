/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.Collection;
import java.util.HashSet;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.ui.AddToCollection;
import edu.illinois.ncsa.mmdb.web.client.ui.AddToCollectionResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

/**
 * Add a set of resources to a collection.
 * 
 * @author Luigi Marini
 *
 */
public class AddToCollectionHandler implements ActionHandler<AddToCollection, AddToCollectionResult>{

	/** Commons logging **/
	private static Log log = LogFactory.getLog(AddToCollectionHandler.class);
	
	@Override
	public AddToCollectionResult execute(AddToCollection arg0,
			ExecutionContext arg1) throws ActionException {

		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);
		
		Collection<String> resourcesString = arg0.getResources();
		
		Collection<Resource> resources = new HashSet<Resource>();
		
		for(String uri : resourcesString) {
			resources.add(Resource.uriRef(uri));
		}
		
		try {
			CollectionBean collectionBean = cbu.get(arg0.getCollectionUri());
			cbu.addToCollection(collectionBean, resources);
		} catch (OperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new AddToCollectionResult();
	}

	@Override
	public Class<AddToCollection> getActionType() {
		return AddToCollection.class;
	}

	@Override
	public void rollback(AddToCollection arg0, AddToCollectionResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
