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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EditRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * Add/remove user permissions.
 * 
 * @author Luigi Marini
 * 
 */
public class EditRoleHandler implements ActionHandler<EditRole, EmptyResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(EditRoleHandler.class);

    @Override
    public EmptyResult execute(EditRole action, ExecutionContext arg1) throws ActionException {
        UriRef admin = Resource.uriRef(action.getUser());
        UriRef user = Resource.uriRef(action.getTargetUser());
        UriRef role = Resource.uriRef(action.getRole());
        SEADRbac rbac = TupeloStore.getInstance().getRbac();

        log.debug("user = " + user + ", admin = " + admin);

        try {
            switch (action.getType()) {
                case ADD:
                    try {
                        rbac.addRole(user, role);
                        if (rbac.getRoles(user).size() == 1) {
                            emailNotification(user);
                        }
                    } catch (RBACException x) {
                        log.error("error adding user to role", x);
                    }
                    break;
                case REMOVE:
                    if (!(user.equals(admin) && lockout(rbac, user, role))) {
                        rbac.removeRole(user, role);
                    } else {
                        throw new ActionException("Removing yourself from this role would lock you out of the system.");
                    }
                    break;
                default:
                    log.error("Edit action type not found" + action.getType());
                    throw new ActionException("Error: unknown permissions setting");
            }
        } catch (OperatorException e) {
            log.error("Error changing permission on user", e);
            throw new ActionException("Server error: " + e.getMessage(), e);
        } catch (RBACException e) {
            // TODO Auto-generated catch block
            throw new ActionException("Access control error: " + e.getMessage(), e);
        }

        return new EmptyResult();
    }

    // would removing this user from this role make it impossible for them to administer roles?
    boolean lockout(RBAC rbac, Resource user, Resource role) throws RBACException {
        Collection<Resource> roles = rbac.getRoles(user); // roles the user belongs to
        Set<Resource> neededPermissions = new HashSet<Resource>(); // set of permissions needed to administer roles
        neededPermissions.add(Resource.uriRef(Permission.VIEW_MEMBER_PAGES.getUri())); // get to the home page
        neededPermissions.add(Resource.uriRef(Permission.VIEW_ADMIN_PAGES.getUri())); // get to the admin tab
        neededPermissions.add(Resource.uriRef(Permission.EDIT_ROLES.getUri())); // edit roles
        for (Tuple<Resource> row : rbac.getGlobalPermissions(null) ) { // in all permissions
            int i = 0;
            Resource r = row.get(i++);
            Resource permission = row.get(i++);
            Resource valueType = row.get(i++);
            if (!r.equals(role) && roles.contains(r)) { // the user belongs to this role, and it's not the one we want to remove
                if (valueType.equals(RBAC.ALLOW)) { // it's allowed
                    neededPermissions.remove(permission); // remove it from the perms we need (if it's in that set)
                }
            }
        }
        return neededPermissions.size() != 0; // do we still need perms? if so, it's a lockout
    }

    /**
     * If email availble, send email notification.
     * 
     * @param user
     */
    private void emailNotification(UriRef user) {
        PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance().getBeanSession());

        try {
            PersonBean personBean = pbu.get(user);

            if (personBean.getEmail() != null && !personBean.getEmail().isEmpty()) {
                Mail.userAuthorized(personBean);
            } else {
                log.debug("User " + user + " email was null/empty. Email not sent.");
            }
        } catch (OperatorException e1) {
            log.error("Error emailing notification to user " + user, e1);
        }
    }

    @Override
    public Class<EditRole> getActionType() {
        return EditRole.class;
    }

    @Override
    public void rollback(EditRole arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub
    }

}
