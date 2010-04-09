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
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Foaf;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.RequestNewPassword;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RequestNewPasswordResult;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.ContextAuthentication;

/**
 * Request a new password.
 * 
 * @author Luigi Marini
 * 
 */
public class RequestNewPasswordHandler implements ActionHandler<RequestNewPassword, RequestNewPasswordResult> {

    /** Commons logging **/
    private static Log       log             = LogFactory.getLog(RequestNewPasswordHandler.class);

    private static final int PASSWORD_LENGTH = 6;

    @Override
    public RequestNewPasswordResult execute(RequestNewPassword action, ExecutionContext arg1) throws ActionException {
        String email = action.getEmail();
        Context context = TupeloStore.getInstance().getContext();

        try {
            ContextAuthentication auth = new ContextAuthentication(context);

            Unifier uf = new Unifier();
            uf.addPattern("user", Foaf.MBOX, Resource.literal(email));
            uf.addPattern("user", Rdf.TYPE, Foaf.PERSON);
            uf.addColumnName("user");
            context.perform(uf);
            int count = 0;
            for (Tuple<Resource> row : uf.getResult() ) {
                String password = auth.generatePassword(PASSWORD_LENGTH);
                auth.changePassword(row.get(0), password);
                Mail.sendNewPassword(email, password);
                count++;
            }
            if (count == 0) {
                return new RequestNewPasswordResult(false, "Unable to find account.");
            }
            if (count > 1) {
                log.error(String.format("Found %d people with same email address [%s]!!", count, email));
            }
            return new RequestNewPasswordResult(true, "Your new password has been sent to your email address.");
        } catch (Exception e) {
            log.error("Unable to find account for " + email, e);
            return new RequestNewPasswordResult(false, "Unable to find account.");
        }
    }

    @Override
    public Class<RequestNewPassword> getActionType() {
        return RequestNewPassword.class;
    }

    @Override
    public void rollback(RequestNewPassword arg0, RequestNewPasswordResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub
    }
}
