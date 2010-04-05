/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Get user account.
 * 
 * @author Luigi Marini
 * 
 */
public class GetUserHandler implements ActionHandler<GetUser, GetUserResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(GetUserHandler.class);

	@Override
	public GetUserResult execute(GetUser action, ExecutionContext arg1)
			throws ActionException {

		Context context = TupeloStore.getInstance().getContext();

		PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance()
				.getBeanSession());
		try {
			Resource personID = Resource.uriRef(action.getEmailAddress());
			TripleMatcher tm = new TripleMatcher();
			tm.setSubject(personID);
			context.perform(tm);
			if (tm.getResult().size() > 0) {
				log.debug("User in the system " + personID.getString());
				PersonBean personBean = pbu.get(personID);
				return new GetUserResult(personBean);
			} else {
				log.debug("User not in the system " + personID.getString());
				return new GetUserResult();
			}
		} catch (Exception e) {
			log.error("Error retrieving information about user "
					+ action.getEmailAddress(), e);
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
