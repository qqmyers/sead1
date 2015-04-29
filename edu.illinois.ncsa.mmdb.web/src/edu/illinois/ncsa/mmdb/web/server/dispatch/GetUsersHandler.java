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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.Cet;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsers;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsersResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * Get users in the system.
 *
 * @author Luigi marini
 *
 */
public class GetUsersHandler implements ActionHandler<GetUsers, GetUsersResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(GetUsersHandler.class);

    @Override
    public GetUsersResult execute(GetUsers arg0, ExecutionContext arg1) throws ActionException {
        RBAC rbac = new RBAC(TupeloStore.getInstance().getContext());
        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
        PersonBeanUtil personBeanUtil = new PersonBeanUtil(beanSession);
        try {
            GetUsersResult result = new GetUsersResult();
            for (PersonBean pb : personBeanUtil.getAll() ) {
                GetUsersResult.User user = new GetUsersResult.User();
                user.id = pb.getUri();
                user.name = pb.getName();
                user.email = pb.getEmail();
                user.roles = new HashSet<String>();
                try {
                    for (Resource role : rbac.getRoles(Resource.uriRef(pb.getUri())) ) {
                        user.roles.add(role.getString());
                    }
                    result.addUser(user);
                } catch (RBACException exc) {
                    log.error("Could not get roles for user " + user.id + " " + user.name);
                    throw (new ActionException("Could not get roles for user " + user.id, exc));
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                try {
                    Set<Triple> logins = TupeloStore.getInstance().getContext().match(Resource.uriRef(pb.getUri()), Cet.cet("lastLogin"), null);
                    if (logins.size() == 0) {
                        user.lastlogin = "never";
                    } else {
                        String date = logins.iterator().next().getObject().getString();
                        Calendar cal = org.tupeloproject.util.Iso8601.string2Date(date);
                        user.lastlogin = sdf.format(cal.getTime());
                    }
                } catch (OperatorException e) {
                    user.lastlogin = "error";
                }
                try {
                    Set<Triple> logins = TupeloStore.getInstance().getContext().match(Resource.uriRef(pb.getUri()), Cet.cet("retired"), null);
                    if (logins.size() == 0) {
                        user.retired = "never";
                    } else {
                        String date = logins.iterator().next().getObject().getString();
                        Calendar cal = org.tupeloproject.util.Iso8601.string2Date(date);
                        user.retired = sdf.format(cal.getTime());
                    }
                } catch (OperatorException e) {
                    user.retired = "error";
                }
            }
            // sort users by name
            Collections.sort(result.getUsers(), new Comparator<GetUsersResult.User>() {
                @Override
                public int compare(GetUsersResult.User o1, GetUsersResult.User o2) {
                    if (o1.name == null) {
                        return +1;
                    }
                    if (o2.name == null) {
                        return -1;
                    }
                    if (o1.name.equals(o2.name)) {
                        return o1.email.compareToIgnoreCase(o2.email);
                    }
                    return o1.name.compareToIgnoreCase(o2.name);
                }
            });

            return result;
        } catch (Exception e) {
            log.error("Error getting user list", e);
        }
        return new GetUsersResult();
    }

    @Override
    public Class<GetUsers> getActionType() {
        return GetUsers.class;
    }

    @Override
    public void rollback(GetUsers arg0, GetUsersResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
