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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Foaf;

import edu.illinois.ncsa.mmdb.web.client.dispatch.CheckUserExists;
import edu.illinois.ncsa.mmdb.web.client.dispatch.CheckUserExistsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Check if a user account exists. If not create one.
 * 
 * @author Luigi Marini
 * 
 */
public class CheckUserExistsHandler implements ActionHandler<CheckUserExists, CheckUserExistsResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(CheckUserExistsHandler.class);

    @Override
    public CheckUserExistsResult execute(CheckUserExists action, ExecutionContext arg1)
            throws ActionException {
        String email = action.getEmail();
        log.debug("Checking if user exists " + email);

        Context context = TupeloStore.getInstance().getContext();

        PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance()
                .getBeanSession());
        try {
            Unifier u = new Unifier();
            u.setColumnNames("uri");
            u.addPattern("uri", Foaf.MBOX, Resource.literal(email));
            context.perform(u);
            List<Resource> uris = u.getFirstColumn();
            if (uris.size() == 1) {
                log.debug("User in the system " + uris.get(0));
                PersonBean pb = pbu.get(uris.get(0));
                log.debug("User retrieved " + pb.getUri());
                return new CheckUserExistsResult(false);
            } else if (uris.size() == 0) {
                log.debug("User not in the system " + email);
                PersonBean pb = createUser(action);
                TupeloStore.getInstance().getBeanSession().save(pb);
                log.debug("User created " + pb.getUri());
                return new CheckUserExistsResult(true);
            } else {
                log.error("Query returned too many users with email " + email);
                return new CheckUserExistsResult(false);
            }
        } catch (Exception e) {
            log.error("Error retrieving information about user "
                    + email, e);
            return new CheckUserExistsResult(false);
        }

    }

    private PersonBean createUser(CheckUserExists action) {
        PersonBean pb = new PersonBean();
        pb.setUri(PersonBeanUtil.getPersonID(action.getEmail()));
        pb.setEmail(action.getEmail());
        pb.setName(action.getName());
        return pb;
    }

    @Override
    public Class<CheckUserExists> getActionType() {
        return CheckUserExists.class;
    }

    @Override
    public void rollback(CheckUserExists arg0, CheckUserExistsResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}