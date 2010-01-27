/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.ArrayList;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.UserGroupMembership;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserGroupMembershipResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserGroupMembership.Action;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * @author Luigi Marini
 * 
 */
public class UserGroupMembershipHandler implements
		ActionHandler<UserGroupMembership, UserGroupMembershipResult> {

	public static final String MEMBERS_URI = "http://cet.ncsa.uiuc.edu/2009/auth/group/readwrite";

	public static final String ADMINS_URI = "http://cet.ncsa.uiuc.edu/2009/auth/group/admin";

	private static final Resource AUTH_GROUP_MEMBER = Resource
			.uriRef("http://cet.ncsa.uiuc.edu/2009/auth/group/member");

	/** Commons logging **/
	private static Log log = LogFactory
			.getLog(UserGroupMembershipHandler.class);

	@Override
	public UserGroupMembershipResult execute(UserGroupMembership arg0,
			ExecutionContext arg1) throws ActionException {

		Context context = TupeloStore.getInstance().getContext();
		Action action = arg0.getAction();
		Resource user = null;
		if (arg0.getUserURI() != null) {
			user = createUserURI(arg0.getUserURI());
		}
		Resource group = null;
		if (arg0.getGroupURI() != null) {
			group = Resource.uriRef(arg0.getGroupURI());
		}
		TripleWriter writer = new TripleWriter();
		TripleMatcher matcher = new TripleMatcher();

		try {
			switch (action) {
			case ADD:
				writer.add(user, AUTH_GROUP_MEMBER, group);
				context.perform(writer);
				context.sync();
				if (arg0.getGroupURI().equals(MEMBERS_URI)) {
					notify(user);
				}
				break;
			case REMOVE:
				writer.remove(user, AUTH_GROUP_MEMBER, group);
				context.perform(writer);
				context.sync();
				break;
			case GET_GROUPS:
				ArrayList<String> groups = new ArrayList<String>();
				matcher.setSubject(user);
				matcher.setPredicate(AUTH_GROUP_MEMBER);
				context.perform(matcher);
				for (Triple triple : matcher.getResult()) {
					groups.add(triple.getObject().getString());
				}
				return new UserGroupMembershipResult(null, groups);
			case GET_MEMBERS:
				ArrayList<String> members = new ArrayList<String>();
				matcher.setPredicate(AUTH_GROUP_MEMBER);
				matcher.setObject(group);
				context.perform(matcher);
				for (Triple triple : matcher.getResult()) {
					members.add(triple.getSubject().getString());
				}
				return new UserGroupMembershipResult(members, null);
			default:
				throw new ActionException("Unrecognized action " + action);
			}
		} catch (OperatorException e) {
			e.printStackTrace();
			return new UserGroupMembershipResult();
		}
		return new UserGroupMembershipResult();
	}

	/**
	 * 
	 * @param user
	 */
	private void notify(Resource user) {
		log.debug("Notifying " + user + " of change in permissions.");
		PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance()
				.getBeanSession());
		try {
			PersonBean personBean = pbu.get(user);
			Mail.userAuthorized(personBean.getEmail());
		} catch (OperatorException e) {
			log
					.error(
							"Error sending email. Make sure the mail.properties is available",
							e);
		}
	}

	/**
	 * Create the proper user uri.
	 * 
	 * FIXME this seems like a hack
	 * 
	 * @param user
	 * @return
	 */
	private Resource createUserURI(String user) {
		if (user.startsWith("http://cet.ncsa.uiuc.edu/")) {
			Resource userURI = Resource.uriRef(user);
			log.debug("User id: " + userURI.getString());
			return userURI;
		} else {
			Resource userURI = Resource
					.uriRef(PersonBeanUtil.getPersonID(user));
			log.debug("User id: " + userURI.getString());
			return userURI;
		}
	}

	@Override
	public Class<UserGroupMembership> getActionType() {
		return UserGroupMembership.class;
	}

	@Override
	public void rollback(UserGroupMembership arg0,
			UserGroupMembershipResult arg1, ExecutionContext arg2)
			throws ActionException {
		// TODO Auto-generated method stub

	}

}
