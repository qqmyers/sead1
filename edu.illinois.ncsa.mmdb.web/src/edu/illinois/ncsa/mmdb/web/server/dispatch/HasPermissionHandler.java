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
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;

/**
 * @author Luigi Marini
 * 
 */
public class HasPermissionHandler implements
		ActionHandler<HasPermission, HasPermissionResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(HasPermissionHandler.class);

	@Override
	public HasPermissionResult execute(HasPermission action,
			ExecutionContext arg1) throws ActionException {

		RBAC rbac = new RBAC(TupeloStore.getInstance().getContext());

		Resource userUri = createUserURI(action.getUser());

		try {
			switch (action.getPermission()) {
			case VIEW_MEMBER_PAGES:
				log.debug("Checking if user " + userUri + " has permission "
						+ MMDB.VIEW_MEMBER_PAGES);
				if (rbac.checkPermission(userUri, MMDB.VIEW_MEMBER_PAGES)) {
					return new HasPermissionResult(true);
				}
				break;
			case VIEW_ADMIN_PAGES:
				log.debug("Checking if user " + userUri + " has permission "
						+ MMDB.VIEW_ADMIN_PAGES);
				if (rbac.checkPermission(userUri, MMDB.VIEW_ADMIN_PAGES)) {
					return new HasPermissionResult(true);
				}
				break;
			default:
				return new HasPermissionResult(false);
			}
		} catch (OperatorException e) {
			log.error("Error checking user permissions", e);
			return new HasPermissionResult(false);
		}
		return new HasPermissionResult(false);
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
	public Class<HasPermission> getActionType() {
		return HasPermission.class;
	}

	@Override
	public void rollback(HasPermission arg0, HasPermissionResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
