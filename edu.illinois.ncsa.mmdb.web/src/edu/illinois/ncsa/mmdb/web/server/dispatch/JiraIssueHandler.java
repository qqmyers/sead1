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
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import javax.mail.MessagingException;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.JiraIssue;
import edu.illinois.ncsa.mmdb.web.client.dispatch.JiraIssue.JiraIssueType;
import edu.illinois.ncsa.mmdb.web.server.Mail;

/**
 * Send email to jira to enter a new jira issue.
 * 
 * @author Rob Kooper
 * 
 */
public class JiraIssueHandler implements ActionHandler<JiraIssue, EmptyResult> {
    //    public static final String JIRA_EMAIL_BUG     = "jira+mmdb.bug@ncsa.illinois.edu";
    //    public static final String JIRA_EMAIL_FEATURE = "jira+mmdb.feature@ncsa.illinois.edu";
    public static final String JIRA_EMAIL_BUG     = "jira@ncsa.illinois.edu";
    public static final String JIRA_EMAIL_FEATURE = "jira@ncsa.illinois.edu";

    /** Commons logging **/
    private static Log         log                = LogFactory.getLog(JiraIssueHandler.class);

    @Override
    public EmptyResult execute(JiraIssue arg0, ExecutionContext arg1) throws ActionException {
        try {
            createJiraIssue(arg0.getIssueType(), arg0.getSummary(), arg0.getDescription());
        } catch (MessagingException e) {
            log.warn("Failed to update context.", e);
            throw (new ActionException("Could not update context.", e));
        }

        return new EmptyResult();
    }

    @Override
    public Class<JiraIssue> getActionType() {
        return JiraIssue.class;
    }

    @Override
    public void rollback(JiraIssue arg0, EmptyResult arg1, ExecutionContext arg2) throws ActionException {
        throw new ActionException("Can not undo a jira issue creation.");
    }

    /**
     * Create a jira issue by sending email.
     */
    public static void createJiraIssue(JiraIssueType type, String summary, String description) throws MessagingException {
        switch (type) {
            case BUG:
                Mail.sendMessage(JIRA_EMAIL_BUG, summary, description);
                break;
            case FEATURE:
                Mail.sendMessage(JIRA_EMAIL_FEATURE, summary, description);
                break;
            default:
                throw (new MessagingException(String.format("Unknown issue type '%s'.", type)));
        }
    }
}
