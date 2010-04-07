/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
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
			log.error("Error emailing notification to user " + user, e1);
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
