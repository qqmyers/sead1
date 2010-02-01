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
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

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
	public AddCollectionResult execute(AddCollection action, ExecutionContext arg1)
			throws ActionException {
		
		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		
		PersonBeanUtil pbu = new PersonBeanUtil(beanSession);
		
		CollectionBean collection = action.getCollection();
		
		try {
			log.debug("Adding collection " + action.getCollection().getTitle());
			
			// create person bean from session id
			// FIXME only required until sessionid stores the full uri and not the email address
			String sessionId = action.getSessionId();
			String personID = sessionId;
			if (!sessionId.startsWith(PersonBeanUtil.getPersonID(""))) {
				personID = PersonBeanUtil.getPersonID(sessionId);
			}
			
			try {
				collection.setCreator(pbu.get(personID));
			} catch (Exception e1) {
				log.error("Error getting creator of annotation", e1);
			}
			
			// set creation date
			collection.setCreationDate(new Date());
			
			// save to repository
			beanSession.registerAndSave(action.getCollection());
			
			// FIXME why doesn't update work and we have to use registerAndSave?
//			CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);
//			CollectionBean collection = cbu.update(arg0.getCollection());

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
