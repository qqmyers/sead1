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

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsers;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsersResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Get users in the system.
 * 
 * @author Luigi marini
 *
 */
public class GetUsersHandler implements ActionHandler<GetUsers, GetUsersResult>{

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetUsersHandler.class);
	
	@Override
	public GetUsersResult execute(GetUsers arg0, ExecutionContext arg1)
			throws ActionException {
		BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
		PersonBeanUtil personBeanUtil = new PersonBeanUtil(beanSession);
		try {
			Collection<PersonBean> all = personBeanUtil.getAll();
			return new GetUsersResult(new ArrayList<PersonBean>(all));
		} catch (Exception e) {
			log.error("Error getting user list", e);
		}
		return new GetUsersResult();
	}

	@Override
	public Class<GetUsers> getActionType() {
		return GetUsers.class;
	}

	@Override
	public void rollback(GetUsers arg0, GetUsersResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
