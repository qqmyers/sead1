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

import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Foaf;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AdminAddUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AdminAddUserResult;
import edu.illinois.ncsa.mmdb.web.common.DefaultRole;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.AuthenticationException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.ContextAuthentication;

/**
 * Create new user account from admin pages.
 * 
 * @author Luigi Marini
 * 
 */
public class AdminAddUserHandler implements ActionHandler<AdminAddUser, AdminAddUserResult> {

    /** Commons logging **/
    private static Log    log            = LogFactory.getLog(AdminAddUserHandler.class);

    private static String defaultRoleURI = DefaultRole.AUTHOR.getUri();

    @Override
    public AdminAddUserResult execute(AdminAddUser arg0, ExecutionContext arg1) throws ActionException {
        SEADRbac rbac = TupeloStore.getInstance().getRbac();
        String personId = PersonBeanUtil.getPersonID(arg0.getEmail());
        Resource defaultRole = Resource.uriRef(defaultRoleURI);

        if (userExists(arg0.getEmail())) {
            log.error("Tyring to create a user that already exists with email address " + arg0.getEmail());
            return new AdminAddUserResult("Error inviting user. User already exists.");
        } else {
            PersonBean pb = new PersonBean();
            pb.setUri(personId);
            pb.setEmail(arg0.getEmail());
            pb.setName(arg0.getFirstName() + " " + arg0.getLastName());
            String password = createRandomPassword();

            try {
                ContextAuthentication auth = new ContextAuthentication(TupeloStore.getInstance().getContext());
                auth.addUser(pb.getEmail(), pb.getName(), password);
                rbac.addRole(pb, defaultRole);
                Mail.createdNewUser(pb, password);
            } catch (AuthenticationException e) {
                log.error(String.format("Error adding user %s with email %s.", pb.getName(), pb.getEmail()), e);
                return new AdminAddUserResult("Error inviting user. User already exists.");
            } catch (OperatorException e) {
                log.error(String.format("Error setting role %s for user %s with email %s.", defaultRoleURI, pb.getName(), pb.getEmail()), e);
                return new AdminAddUserResult("Error setting default role for user.");
            }
        }
        return new AdminAddUserResult();
    }

    private boolean userExists(String email) {
        Context context = TupeloStore.getInstance().getContext();
        Unifier u = new Unifier();
        u.setColumnNames("uri");
        u.addPattern("uri", Foaf.MBOX, Resource.literal(email));
        try {
            context.perform(u);
            List<Resource> uris = u.getFirstColumn();
            if (uris.size() == 0) {
                log.debug("User not in the system " + email);
                return false;
            } else {
                log.debug("User found in the system " + email);
                return true;
            }
        } catch (OperatorException e) {
            log.error("Error checking if user exists", e);
            return false;
        }

    }

    @Override
    public Class<AdminAddUser> getActionType() {
        return AdminAddUser.class;
    }

    @Override
    public void rollback(AdminAddUser arg0, AdminAddUserResult arg1, ExecutionContext arg2)
            throws ActionException {
        // TODO Auto-generated method stub

    }

    private String createRandomPassword() {
        return RandomStringUtils.randomAlphanumeric(8);
    }

}
