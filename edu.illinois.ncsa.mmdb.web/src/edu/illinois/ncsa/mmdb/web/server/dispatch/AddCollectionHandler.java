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

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollection;
import edu.illinois.ncsa.mmdb.web.client.ui.AddCollectionResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

/**
 * Create a new collection.
 * 
 * @author Luigi Marini
 *
 */
public class AddCollectionHandler implements ActionHandler<AddCollection, AddCollectionResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(AddCollectionHandler.class);
	
	@Override
	public AddCollectionResult execute(AddCollection arg0, ExecutionContext arg1)
			throws ActionException {
		
		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);
		
		try {
			log.debug("Adding collection " + arg0.getCollection().getTitle());
			
			// FIXME why doesn't update work and we have to use registerAndSave?
//			CollectionBean collection = cbu.update(arg0.getCollection());
			
			CollectionBean collection = arg0.getCollection();
			collection.setCreationDate(new Date());
			beanSession.registerAndSave(arg0.getCollection());
		} catch (Exception e) {
			log.error("Error creating new collection", e);
		}
		return new AddCollectionResult();
	}

	@Override
	public Class<AddCollection> getActionType() {
		return AddCollection.class;
	}

	@Override
	public void rollback(AddCollection arg0, AddCollectionResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
