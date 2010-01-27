/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * @author Luigi Marini
 * 
 */
public class GetUserHandler implements ActionHandler<GetUser, GetUserResult> {

	@Override
	public GetUserResult execute(GetUser action, ExecutionContext arg1)
			throws ActionException {
		PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance()
				.getBeanSession());
		try {
			PersonBean personBean = pbu.get(PersonBeanUtil.getPersonID(action
					.getEmailAddress()));
			return new GetUserResult(personBean);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new GetUserResult();
	}

	@Override
	public Class<GetUser> getActionType() {
		return GetUser.class;
	}

	@Override
	public void rollback(GetUser arg0, GetUserResult arg1, ExecutionContext arg2)
			throws ActionException {
		// TODO Auto-generated method stub

	}

}
