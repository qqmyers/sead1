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

import edu.illinois.ncsa.mmdb.web.client.dispatch.EditPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EditPermissionsResult;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.PermissionResourceMap;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;

/**
 * Add/remove user permissions.
 * 
 * @author Luigi Marini
 * 
 */
public class EditPermissionsHandler implements
		ActionHandler<EditPermissions, EditPermissionsResult> {

	/** Commons logging **/
	private static Log log = LogFactory.getLog(EditPermissionsHandler.class);

	@Override
	public EditPermissionsResult execute(EditPermissions action,
			ExecutionContext arg1) throws ActionException {

		Resource user = Resource.uriRef(action.getUser());

		Resource permission = PermissionResourceMap.getResource(action
				.getPermission());

		RBAC rbac = new RBAC(TupeloStore.getInstance().getContext());

		try {
			switch (action.getType()) {
			case ADD:
				rbac.addPermission(user, permission);
				emailNotification(user);
				break;
			case REMOVE:
				rbac.removePermission(user, permission);
				break;
			default:
				log.error("Edit permission action type not found"
						+ action.getType());
				throw new ActionException(
						"Edit permission action type not found"
								+ action.getType());
			}
		} catch (OperatorException e) {
			log.error("Error changing permission on user", e);
		}

		return new EditPermissionsResult();
	}

	/**
	 * If email availble, send email notification.
	 * 
	 * @param user
	 */
	private void emailNotification(Resource user) {
		PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance()
				.getBeanSession());

		try {
			PersonBean personBean = pbu.get(user);

			if (personBean.getEmail() != null
					&& !personBean.getEmail().isEmpty()) {
				Mail.userAuthorized(personBean.getEmail());
			} else {
				log.debug("User " + user
						+ " email was null/empty. Email not sent.");
			}
		} catch (OperatorException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public Class<EditPermissions> getActionType() {
		return EditPermissions.class;
	}

	@Override
	public void rollback(EditPermissions arg0, EditPermissionsResult arg1,
			ExecutionContext arg2) throws ActionException {
		// TODO Auto-generated method stub

	}

}
