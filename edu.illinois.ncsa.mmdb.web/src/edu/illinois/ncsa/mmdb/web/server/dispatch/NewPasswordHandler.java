/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.NewPassword;
import edu.illinois.ncsa.mmdb.web.server.PasswordManagement;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Create new user password.
 * 
 * @author Luigi Marini
 * 
 */
public class NewPasswordHandler implements
		ActionHandler<NewPassword, EmptyResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(NewPasswordHandler.class);

	@Override
	public EmptyResult execute(NewPassword action, ExecutionContext arg1)
			throws ActionException {

		String user = action.getUser();
		String password = action.getPassword();
		PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance()
				.getBeanSession());

		try {
			PersonBean personBean = pbu.get(PersonBeanUtil.getPersonID(user));
			// update password
			PasswordManagement.updatePassword(personBean.getUri(), password);
		} catch (Exception e1) {
			log.error("Error retrieving user", e1);
			throw new ActionException("Error retrieving user", e1);
		}

		return new EmptyResult();
	}

	@Override
	public Class<NewPassword> getActionType() {
		return NewPassword.class;
	}

	@Override
	public void rollback(NewPassword arg0, EmptyResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
