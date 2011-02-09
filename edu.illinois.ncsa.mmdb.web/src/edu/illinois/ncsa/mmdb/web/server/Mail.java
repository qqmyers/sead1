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
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.uiuc.ncsa.cet.bean.PersonBean;

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
        String server = ts.getConfiguration(ConfigurationKey.MailServer);
        String presubj = ts.getConfiguration(ConfigurationKey.MailSubject);
        String subject = presubj + " Account Activated";
        String body = String.format("Your account for use on server %s has been activated.", server);
        try {
            sendMessage(user.getEmail(), subject, body);
        } catch (MessagingException e) {
            log.error(String.format("Could not send email to '%s' about '%s'.", user.getEmail(), subject), e);
        }
    }

    public static void sendNewPassword(PersonBean user, String newPassword) {
        TupeloStore ts = TupeloStore.getInstance();
        String server = ts.getConfiguration(ConfigurationKey.MailServer);
        String presubj = ts.getConfiguration(ConfigurationKey.MailSubject);
        String subject = presubj + " New Password";
        String body = String.format("Your new password for use on server %s is : %s", server, newPassword);
        try {
            sendMessage(user.getEmail(), subject, body);
        } catch (MessagingException e) {
            log.error(String.format("Could not send email to '%s' about '%s'.", user.getEmail(), subject), e);
        }
    }

    /**
     * Notify admin of new user registration.
     * 
     * @param userAddress
     */
    public static void userAdded(PersonBean user) {
        TupeloStore ts = TupeloStore.getInstance();
        String server = ts.getConfiguration(ConfigurationKey.MailServer);
        String presubj = ts.getConfiguration(ConfigurationKey.MailSubject);
        String rcpt = ts.getConfiguration(ConfigurationKey.MailFrom);
        String subject = presubj + " New User";
        StringBuilder body = new StringBuilder();
        body.append(String.format("A new user has registered on server %s\n\n", server));
        body.append(String.format("NAME  : %s\n", user.getName()));
        body.append(String.format("EMAIL : %s\n", user.getEmail()));
        try {
            sendMessage(rcpt, subject, body.toString()); //$NON-NLS-1$
        } catch (MessagingException e) {
            log.error(String.format("Could not send email to '%s' about '%s'.", rcpt, subject), e);
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
    public static void sendMessage(String rcpt, String subject, String body) throws MessagingException {
        TupeloStore ts = TupeloStore.getInstance();
        String from = ts.getConfiguration(ConfigurationKey.MailFrom);
        String fullname = ts.getConfiguration(ConfigurationKey.MailFullName);

        Properties props = new Properties();
        props.put(ConfigurationKey.MailServer.getPropertyKey(), ts.getConfiguration(ConfigurationKey.MailServer));
        Session session = Session.getDefaultInstance(null);
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(from, fullname));
        } catch (UnsupportedEncodingException e) {
            throw (new MessagingException("Could not encode from address.", e));
        }
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(rcpt));
        message.setSubject(subject); //$NON-NLS-1$
        message.setText(body);
        Transport.send(message);
        log.debug(String.format("Mail sent to %s with subject '%s'", rcpt, subject));
    }
}
