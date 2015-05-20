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

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddUserResult;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.AuthenticationException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.ContextAuthentication;

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
    public AddUserResult execute(AddUser arg0, ExecutionContext arg1) throws ActionException {

        PersonBean pb = new PersonBean();
        pb.setUri(PersonBeanUtil.getPersonID(arg0.getEmail()));
        pb.setEmail(arg0.getEmail());
        pb.setName(arg0.getFirstName() + " " + arg0.getLastName());
        if (arg0.getEmail() == null) {
            log.error("trying to add null email user: " + pb.getName());
            throw new ActionException("User must have an email address");
        }
        try {
            ContextAuthentication auth = new ContextAuthentication(TupeloStore.getInstance().getContext());
            auth.addUser(pb.getEmail(), pb.getName(), arg0.getPassword());
        } catch (AuthenticationException e) {
            log.error(String.format("Error adding user %s with email %s.", pb.getName(), pb.getEmail()), e);
        }

        Mail.userAdded(pb);

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
