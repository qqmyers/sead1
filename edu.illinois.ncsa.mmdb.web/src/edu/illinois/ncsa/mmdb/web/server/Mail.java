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
package edu.illinois.ncsa.mmdb.web.server;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Foaf;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.DefaultRole;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;

/**
 * Send email for notification purposes.
 * 
 * @author Luigi Marini
 * 
 */
public class Mail {
    /** Commons logging **/
    private static Log log = LogFactory.getLog(Mail.class);

    /**
     * Notify user of authorization change.
     * 
     * @param userAddress
     */
    public static void userAuthorized(PersonBean user) {
        TupeloStore ts = TupeloStore.getInstance();
        String server = ts.getConfiguration(ConfigurationKey.MediciName);
        String presubj = ts.getConfiguration(ConfigurationKey.MailSubject);
        String subject = presubj + " Account Activated";
        String body = String.format("Your account for use on server %s has been activated.", server);
        try {
            sendMessage(new String[] { user.getEmail() }, null, subject, body);
        } catch (MessagingException e) {
            log.error(String.format("Could not send email to '%s' about '%s'.", user.getEmail(), subject), e);
        }
    }

    public static void sendNewPassword(PersonBean user, String newPassword) {
        TupeloStore ts = TupeloStore.getInstance();
        String server = ts.getConfiguration(ConfigurationKey.MediciName);
        String presubj = ts.getConfiguration(ConfigurationKey.MailSubject);
        String subject = presubj + " New Password";
        String body = String.format("Your new password for use on server %s is : %s", server, newPassword);
        try {
            sendMessage(new String[] { user.getEmail() }, null, subject, body);
        } catch (MessagingException e) {
            log.error(String.format("Could not send email to '%s' about '%s'.", user.getEmail(), subject), e);
        }
    }

    public static String[] getAdminEmail() {
        List<String> admins = new ArrayList<String>();

        Unifier uf = new Unifier();
        uf.addPattern("user", RBAC.HAS_ROLE, Resource.uriRef(DefaultRole.ADMINISTRATOR.getUri()));
        uf.addPattern("user", Foaf.MBOX, "email");
        uf.setColumnNames("email");
        try {
            TupeloStore.getInstance().getContext().perform(uf);
            for (Tuple<Resource> row : uf.getResult() ) {
                if (row.get(0) != null) {
                    admins.add(row.get(0).getString());
                }
            }
        } catch (OperatorException e) {
            log.error("Could not get list of admins.", e);
            admins.add(TupeloStore.getInstance().getConfiguration(ConfigurationKey.MailFrom));
        }

        return admins.toArray(new String[0]);
    }

    /**
     * Notify admin of new user registration.
     * 
     * @param userAddress
     */
    public static void userAdded(PersonBean user) {
        TupeloStore ts = TupeloStore.getInstance();
        String server = ts.getConfiguration(ConfigurationKey.MediciName);
        String presubj = ts.getConfiguration(ConfigurationKey.MailSubject);
        String subject = presubj + " New User";
        StringBuilder body = new StringBuilder();
        body.append(String.format("A new user has registered on server %s\n\n", server));
        body.append(String.format("NAME  : %s\n", user.getName()));
        body.append(String.format("EMAIL : %s\n", user.getEmail()));
        try {
            sendMessage(getAdminEmail(), null, subject, body.toString()); //$NON-NLS-1$
        } catch (MessagingException e) {
            log.error(String.format("Could not send email to admins about '%s'.", subject), e);
        }
    }

    /**
     * Notified a user that an account was created for them.
     */
    public static void createdNewUser(PersonBean user, PersonBean admin, String password) {
        TupeloStore ts = TupeloStore.getInstance();
        String server = ts.getConfiguration(ConfigurationKey.MediciName);
        String presubj = ts.getConfiguration(ConfigurationKey.MailSubject);
        String subject = presubj + " Invitation to access data";
        String body = String.format("Welcome to SEAD! - a new way for projects to manage, curate and preserve data.\n\n" +
                "You have been invited by an administrator (cc'd) to access and contribute to the data collection(s) being " +
                "developed by the project using a SEAD Active Content Repository.\n\n" +
                "A user account on the server %s has been created using this email address. You can login using your Google password" +
                " if this email is associated with a google account (recommended). Or, you can use the temporary password %s " +
                "to login via a local account. (You can then change your password information by logging in and going to Home > Profile.)" +
                "\n\nYou have initially been given full read/write access to this repository (an admin may subsequently add/remove privileges)." +
                " Getting Started information is available at http://sead-data.net. Questions can be sent to seaddatanet@umich.edu.", server, password);
        try {
            String adminEmail = null;
            if (admin != null) {
                adminEmail = admin.getEmail();
            }
            if (adminEmail != null) {
                log.debug("Sending invite email to " + user.getEmail() + " cc'd to: " + admin.getEmail());
                sendMessage(new String[] { user.getEmail() }, new String[] { admin.getEmail() }, subject, body);
            } else {
                log.debug("Sending invite email to " + user.getEmail());
                sendMessage(new String[] { user.getEmail() }, null, subject, body);
            }
        } catch (MessagingException e) {
            log.error(String.format("Could not send email to '%s' about '%s'.", user.getEmail(), subject), e);
        }
    }

    /**
     * Send the message
     * 
     * @param url
     * @param rcpt
     * @param subject
     * @param body
     * @throws MessagingException
     */
    public static void sendMessage(String[] rpts, String[] ccList, String subject, String body) throws MessagingException {
        TupeloStore ts = TupeloStore.getInstance();
        String from = ts.getConfiguration(ConfigurationKey.MailFrom);
        String fullname = ts.getConfiguration(ConfigurationKey.MailFullName);

        Properties props = new Properties();
        props.put(ConfigurationKey.MailServer.getPropertyKey(), ts.getConfiguration(ConfigurationKey.MailServer));
        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(from, fullname));
        } catch (UnsupportedEncodingException e) {
            throw (new MessagingException("Could not encode from address.", e));
        }
        for (String rcpt : rpts ) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(rcpt));
        }
        if (ccList != null) {
            for (String cc : ccList ) {
                message.addRecipient(MimeMessage.RecipientType.CC, new InternetAddress(cc));
            }
        }
        message.setSubject(subject); //$NON-NLS-1$
        message.setText(body);
        Transport.send(message);
        log.debug(String.format("Mail sent to recepients with subject '%s'", subject));
    }
}
