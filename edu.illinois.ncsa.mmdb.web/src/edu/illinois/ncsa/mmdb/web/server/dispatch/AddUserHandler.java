/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.OperatorException;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddUserResult;
import edu.illinois.ncsa.mmdb.web.server.PasswordManagement;

/**
 * Add user to system.
 * 
 * @author Luigi Marini
 * 
 */
public class AddUserHandler implements ActionHandler<AddUser, AddUserResult> {

	@Override
	public AddUserResult execute(AddUser arg0, ExecutionContext arg1)
			throws ActionException {

		String name = arg0.getFirstName() + " " + arg0.getLastName();
		String email = arg0.getEmail();
//		String passwordDigest = PasswordDigest.digest(arg0.getPassword());

		try {
			PasswordManagement.addUser(name, email, arg0.getPassword());
		} catch (OperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		Resource personURI = Resource.uriRef(PersonBeanUtil
//				.getPersonID(email));
//
//		Context context = TupeloStore.getInstance().getContext();
//		try {
//			context.addTriple(
//							personURI,
//							Resource.uriRef("http://cet.ncsa.uiuc.edu/2007/foaf/context/password"),
//							passwordDigest);
//			context.addTriple(personURI, Rdf.TYPE, Foaf.PERSON);
//			context.addTriple(personURI, Foaf.NAME, name);
//			context.addTriple(personURI, Foaf.MBOX, email);
//		} catch (OperatorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

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
