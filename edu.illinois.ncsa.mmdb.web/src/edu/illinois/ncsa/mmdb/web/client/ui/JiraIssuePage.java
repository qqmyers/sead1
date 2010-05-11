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
package edu.illinois.ncsa.mmdb.web.client.ui;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.JiraIssue;
import edu.illinois.ncsa.mmdb.web.client.dispatch.JiraIssue.JiraIssueType;

/**
 * This page allows the user to submit a bug/feature to JIRA.
 * 
 * @author Rob Kooper
 * 
 */
public class JiraIssuePage extends Page {

    /**
     * Create an instance of home page.
     * 
     * @param dispatchAsync
     */
    public JiraIssuePage(final DispatchAsync dispatchAsync, final JiraIssueType type) {
        super("Jira Issue", dispatchAsync);

        if (type == JiraIssueType.BUG) {
            setPageTitle("Report a problem");
        } else {
            setPageTitle("Request a feature");
        }

        FlexTable table = new FlexTable();
        mainLayoutPanel.add(table);

        table.setText(0, 0, "Summary:");
        table.getCellFormatter().addStyleName(0, 0, "homePageWidgetRow");

        final TextBox txtSummary = new TextBox();
        txtSummary.setWidth("400px");
        table.setWidget(0, 1, txtSummary);

        table.setText(1, 0, "Description:");
        table.getCellFormatter().addStyleName(1, 0, "homePageWidgetRow");

        final TextArea txtDescription = new TextArea();
        txtDescription.setWidth("400px");
        table.setWidget(1, 1, txtDescription);

        final Anchor submit = new Anchor("Submit");
        submit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                submit.setEnabled(false);
                if (txtSummary.getText().trim().length() == 0) {
                    showFeedbackMessage("Please enter a summary of this issue.");
                    txtSummary.setFocus(true);
                    return;
                }
                if (txtDescription.getText().trim().length() == 0) {
                    showFeedbackMessage("Please enter a description of this issue.");
                    txtDescription.setFocus(true);
                    return;
                }

                JiraIssue issue = new JiraIssue();
                issue.setIssueType(type);
                issue.setSummary(txtSummary.getText().trim());
                issue.setDescription(txtDescription.getText().trim());
                dispatchAsync.execute(issue, new AsyncCallback<EmptyResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error submitting Jira Issue.", caught);
                        showFeedbackMessage("Error submitting Issue, please mail to medici@ncsa.illinois.edu");
                        submit.setEnabled(true);
                    }

                    @Override
                    public void onSuccess(EmptyResult result) {
                        showFeedbackMessage("Thank you for submitting this issue");
                        txtSummary.setText("");
                        txtDescription.setText("");
                        submit.setEnabled(true);
                    }
                });
            }
        });
        table.setWidget(2, 0, submit);
    }

    @Override
    public void layout() {
    }
}
