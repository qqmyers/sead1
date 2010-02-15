/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddUserResult;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.PasswordManagement;

/**
 * Create new user account.
 * 
 * @author Luigi Marini
 * 
 */
public class AddUserHandler implements ActionHandler<AddUser, AddUserResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(AddUserHandler.class);

	
	@Override
	public AddUserResult execute(AddUser arg0, ExecutionContext arg1)
			throws ActionException {

		String name = arg0.getFirstName() + " " + arg0.getLastName();
		String email = arg0.getEmail();

		try {
			PasswordManagement.addUser(name, email, arg0.getPassword());
		} catch (OperatorException e) {
			log.error("Error adding user " + name + " , " + email, e);
		}
		
		Mail.userAdded(email);

		return new AddUserResult();
	}

	@Override
	public Class<AddUser> getActionType() {
		return AddUser.class;
	}

	@Override
	public void rollback(AddUser arg0, AddUserResult arg1, ExecutionContext arg2)
			throws ActionException {
		// TODO Auto-generated method stub

	}

}
