/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.mmdb.web.client.dispatch.RequestNewPassword;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RequestNewPasswordResult;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.PasswordManagement;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Request a new password.
 * 
 * @author Luigi Marini
 * 
 */
public class RequestNewPasswordHandler implements
		ActionHandler<RequestNewPassword, RequestNewPasswordResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(RequestNewPasswordHandler.class);

	private static final int PASSWORD_LENGTH = 6;

	@Override
	public RequestNewPasswordResult execute(RequestNewPassword action,
			ExecutionContext arg1) throws ActionException {

		String email = action.getEmail();
		PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance()
				.getBeanSession());

		try {
			PersonBean personBean = pbu.get(PersonBeanUtil.getPersonID(email));
			log.debug("Checking that a user with email '" + email
					+ "' exists. Found user with email '"
					+ personBean.getEmail() + "'");
			if (email.equals(personBean.getEmail())) {
				String newPassword = PasswordManagement
						.generateNewPassword(PASSWORD_LENGTH);
				try {
					PasswordManagement.updatePassword(personBean.getUri(),
							newPassword);
					Mail.sendNewPassword(personBean.getEmail(), newPassword);
					return new RequestNewPasswordResult(true,
							"Your new password has been sent to your email address.");
				} catch (Exception e) {
					log.error("Error mailing password", e);
					return new RequestNewPasswordResult(false,
							"Error mailing password.");
				}
			} else {
				log.debug("Unable to find account for " + email);
				return new RequestNewPasswordResult(false,
						"Unable to find account.");
			}
		} catch (Exception e) {
			log.error("Unable to find account for " + email, e);
			return new RequestNewPasswordResult(false,
					"Unable to find account.");
		}
	}

	@Override
	public Class<RequestNewPassword> getActionType() {
		return RequestNewPassword.class;
	}

	@Override
	public void rollback(RequestNewPassword arg0,
			RequestNewPasswordResult arg1, ExecutionContext arg2)
			throws ActionException {
		// TODO Auto-generated method stub

	}
}
