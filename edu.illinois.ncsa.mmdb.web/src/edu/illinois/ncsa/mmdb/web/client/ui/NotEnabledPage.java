/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA, 2016, University of Michigan   All rights reserved.
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

import java.util.Set;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmailAdmins;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRoles;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRolesResult;
import edu.illinois.ncsa.mmdb.web.client.ui.admin.SimpleUserManagementWidget;
import edu.illinois.ncsa.mmdb.web.common.DefaultRole;

/**
 * For users that haven't been enabled by an admin yet, or those wanting
 * renewed/new permissions
 *
 * @author Luigi Marini
 * @author myersjd@umich.edu
 *
 */
public class NotEnabledPage extends Composite {

    private final FlowPanel     mainPanel;
    private final FlexTable     mainTable = new FlexTable();

    private final DispatchAsync service;

    public NotEnabledPage(final DispatchAsync dispatch) {
        this.service = dispatch;
        mainPanel = new FlowPanel();
        mainPanel.addStyleName("page");
        initWidget(mainPanel);

        // page title
        mainPanel.add(new TitlePanel("Permission Restriction"));

        addRoleSpecificContent(MMDB.getUsername(), mainPanel);

    }

    private void addRoleSpecificContent(String user, FlowPanel mainPanel2) {

        service.execute(new GetRoles(user), new AsyncCallback<GetRolesResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Can't determine role", caught);
            }

            @Override
            public void onSuccess(GetRolesResult result) {
                Set<String> roles = result.getRoles();
                String role = SimpleUserManagementWidget.UNASSIGNED_ROLE; //Treat as unassigned if no role returned
                if (!roles.isEmpty()) {
                    role = DefaultRole.getNameFromUri((String) roles.toArray()[0]);
                }
                addContent(role);
            }

        });
    }

    protected void addContent(String role) {

        HTML message;
        if (role.equals(SimpleUserManagementWidget.INACTIVE_ROLE)) {
            message = new HTML("<p><b>Welcome Back!</b> The group administrator(s) for this space have disabled your access to this space.</p>" +
                    "<p>If you'd like to request that your access be restored, use the form below to contact them.</p>");

        } else if (role.equals(SimpleUserManagementWidget.UNASSIGNED_ROLE)) {
            message = new HTML("<p><b> Welcome!</b> The group administrator(s) for this space have been notified but have not yet responded to your request.</p>" +
                    "<p>You will receive an email if your are granted access to this space. In the meantime, if you'd like to contact the space admins" +
                    " (e.g. to provide an explanation about why you'd like access), use the form below.</p>");
        } else {

            message = new HTML("<p>You do not currently have permission to access this functionality in the " + role + " role.</p>" +
                    "<p>If you'd like to request more access, use the form below to contact the administrator(s) and tell them what functionality you'd like to be able to use. .</p>");

        }

        // TODO change to more appropriate style
        message.addStyleName("loginForm");

        mainPanel.add(message);

        FlowPanel fp = new FlowPanel();

        mainPanel.addStyleName("newAnnotationMainPanel");

        mainPanel.add(mainTable);

        Label header = new Label("To: " + MMDB._projectName + " Admins: re Access Request");

        header.addStyleName("newCommentHeader");

        mainTable.setWidget(0, 0, header);

        final TextArea descriptionTextArea = new TextArea();

        descriptionTextArea.addStyleName("newCommentTextArea");

        descriptionTextArea.setWidth("500px");

        descriptionTextArea.setSize("500px", "200px");

        mainTable.setWidget(1, 0, descriptionTextArea);

        Label footer = new Label("From: " + MMDB.getSessionState().getCurrentUser().getEmail() + " : Current Role: " + role);
        mainTable.setWidget(2, 0, footer);

        Button submitButton = new Button("Send Email");

        // add new annotation
        submitButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent arg0) {

                service.execute(new EmailAdmins(descriptionTextArea.getText(), MMDB.getUsername()),
                        new AsyncCallback<EmptyResult>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                GWT.log("Failed to email admins ", caught);
                                Window.alert("email was not sent to admins - please contact SEADdatanet@umich.edu " + caught.getLocalizedMessage());
                            }

                            @Override
                            public void onSuccess(EmptyResult result) {
                                descriptionTextArea.setText("");

                            }
                        });

            }

        });

        mainTable.setWidget(3, 0, submitButton);

    }

}
