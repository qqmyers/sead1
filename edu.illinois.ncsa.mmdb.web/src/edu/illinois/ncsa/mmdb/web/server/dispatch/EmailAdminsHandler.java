/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2016 University of Michigan  All rights reserved.
 *
 *  * Permission is hereby granted, free of charge, to any person obtaining
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
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import javax.mail.MessagingException;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmailAdmins;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.Mail;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Send email to space admins
 *
 * @author Jim Myers
 *
 */
public class EmailAdminsHandler implements ActionHandler<EmailAdmins, EmptyResult> {
    /** Commons logging **/
    private static Log log = LogFactory.getLog(EmailAdminsHandler.class);

    @Override
    public EmptyResult execute(EmailAdmins arg0, ExecutionContext arg1) throws ActionException {
        try {
            log.debug("Preparing Email");
            String host = TupeloStore.getInstance().getConfiguration(ConfigurationKey.MediciName);
            String email = arg0.getUser(); //default to full ID if we can't get email
            log.debug("User is " + email);
            PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance().getBeanSession());
            try {
                PersonBean pb = pbu.get(arg0.getUser());
                email = pb.getEmail();
                log.debug("User email is " + email);
            } catch (Exception e) {
                log.warn("Could not retrieve user email for " + arg0.getUser(), e);
            }

            String subj = "Access Request Feedback from " + arg0.getUser();
            String body = "The following request was submitted on : " + host +
                    ". To update this person's permissions in your space, go to the \"Users\" tab at https://" + host + "/acr#administration .\n" +
                    "You may also wish to respond directly to this person via email - there is no automated reposnse form the system.\n\n" +
                    "From: " + email + "\n\n" +
                    arg0.getMessage();
            log.debug("Email to admins prepared");
            Mail.sendMessage(Mail.getAdminEmail(), null, subj, body);
            log.debug("Email to admins successfully sent.");
        } catch (MessagingException e) {
            log.warn("Failed to send email.", e);
            throw (new ActionException("Could not send email.", e));
        } catch (Exception e) {
            log.warn("Failed to handle request.", e);
            throw (new ActionException("Could not send email due to unexpected error.", e));
        } catch (Throwable t) {
            log.warn("Thrown: " + t.getLocalizedMessage());
        }

        return new EmptyResult();
    }

    @Override
    public Class<EmailAdmins> getActionType() {
        return EmailAdmins.class;
    }

    @Override
    public void rollback(EmailAdmins action, EmptyResult result, ExecutionContext context) throws ActionException {
        // TODO Auto-generated method stub

    }
}
