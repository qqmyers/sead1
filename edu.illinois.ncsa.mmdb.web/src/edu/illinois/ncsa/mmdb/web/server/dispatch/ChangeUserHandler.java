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
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ChangeUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.ContextAuthentication;

/**
 * Create new user password.
 * 
 * @author Luigi Marini
 * 
 */
public class ChangeUserHandler implements ActionHandler<ChangeUser, EmptyResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(ChangeUserHandler.class);

    @Override
    public EmptyResult execute(ChangeUser action, ExecutionContext arg1) throws ActionException {
        UriRef user = Resource.uriRef(action.getUser());

        // change the name
        if (action.getName() != null) {
            PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance().getBeanSession());
            try {
                PersonBean pb = pbu.get(user);
                pb.setName(action.getName());
                pbu.getBeanSession().save(pb);
            } catch (Exception e) {
                log.warn("Could not update user.", e);
                throw (new ActionException("Could not update user.", e));
            }
        }

        // change password
        if ((action.getOldPassword() != null) && (action.getNewPassword() != null)) {
            ContextAuthentication auth = new ContextAuthentication(TupeloStore.getInstance().getContext());
            try {
                if (!auth.checkPassword(user, action.getOldPassword())) {
                    throw (new ActionException("Invalid original password supplied."));
                }
            } catch (Exception e) {
                log.warn("Error checking password", e);
                throw (new ActionException("Error checking password", e));
            }

            try {
                auth.changePassword(user, action.getNewPassword());
            } catch (Exception e) {
                log.warn("Error changing password", e);
                throw new ActionException("Error changing password", e);
            }
        }

        return new EmptyResult();
    }

    @Override
    public Class<ChangeUser> getActionType() {
        return ChangeUser.class;
    }

    @Override
    public void rollback(ChangeUser arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub
    }

}
