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

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.Role;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EditRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRoles;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRolesResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsers;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsersResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EditRole.ActionType;
import edu.uiuc.ncsa.cet.bean.PersonBean;

/**
 * A page to manage users. Currently only the ability to enable/disable users is
 * implemented.
 * 
 * FIXME: Currently permissions are set directly on users. Checkboxes in this
 * page manipulate permissions on users. Instead checkboxes should manipulate
 * roles for specific users. This will remove the awkward manual setting of
 * users as regular members when a user is made an admin.
 * 
 * @author Luigi Marini
 * 
 */
public class UserManagementPage extends Composite {

    private final FlowPanel                 mainPanel;

    private final Widget                    pageTitle;

    private final FlexTable                 usersTable;

    private final MyDispatchAsync           dispatchAsync;

    private final HashMap<String, CheckBox> usersMemberCheckboxMap;

    private final HashMap<String, CheckBox> usersAdminCheckboxMap;

    public UserManagementPage(MyDispatchAsync dispatchAsync) {

        this.dispatchAsync = dispatchAsync;
        usersMemberCheckboxMap = new HashMap<String, CheckBox>();
        usersAdminCheckboxMap = new HashMap<String, CheckBox>();

        mainPanel = new FlowPanel();
        mainPanel.addStyleName("page");
        initWidget(mainPanel);

        // page title
        pageTitle = createPageTitle();
        mainPanel.add(pageTitle);

        // users table
        usersTable = createUsersTable();
        mainPanel.add(usersTable);

        // load users from server side
        loadUsers();
    }

    /**
     * Load users from server side.
     */
    private void loadUsers() {
        dispatchAsync.execute(new GetUsers(),
                new AsyncCallback<GetUsersResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error getting users", null);
                    }

                    @Override
                    public void onSuccess(GetUsersResult result) {
                        ArrayList<PersonBean> users = result.getUsers();
                        if (users.size() == 0) {
                            usersTable.setText(usersTable.getRowCount() + 1, 0, "No users found.");
                        }
                        for (PersonBean user : users ) {
                            createRow(user);
                        }
                    }
                });

    }

    /**
     * Create a single row in the table based on user.
     * 
     * @param user
     */
    protected void createRow(final PersonBean user) {

        int row = usersTable.getRowCount() + 1;

        usersTable.setText(row, 0, user.getName());
        usersTable.setText(row, 1, user.getEmail());

        if ((user.getName() == null) && (user.getEmail() == null)) {
            usersTable.setText(row, 0, user.getUri());
        }

        // regular member
        final CheckBox memberCheckBox = new CheckBox();
        memberCheckBox.setEnabled(false);
        memberCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                modifyPermissions(user.getUri(), Role.MEMBER, memberCheckBox);
            }
        });
        usersTable.setWidget(row, 2, memberCheckBox);
        usersMemberCheckboxMap.put(user.getUri(), memberCheckBox);

        // admin group
        final CheckBox adminCheckBox = new CheckBox();
        adminCheckBox.setEnabled(false);
        adminCheckBox.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (lastAdmin()) {
                    adminCheckBox.setValue(true);
                    Window.alert("You cannot remove the last admin");
                } else {
                    modifyPermissions(user.getUri(), Role.ADMIN, adminCheckBox);
                }
            }
        });
        usersTable.setWidget(row, 3, adminCheckBox);
        usersAdminCheckboxMap.put(user.getUri(), adminCheckBox);

        // get list of all roles and enable checkboxes
        dispatchAsync.execute(new GetRoles(user.getUri()), new AsyncCallback<GetRolesResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error getting roles for user " + user.getUri(), caught);
            }

            @Override
            public void onSuccess(GetRolesResult result) {
                for (Role role : result.getRoles() ) {
                    switch (role) {
                        case MEMBER:
                            memberCheckBox.setValue(true);
                            break;
                        case ADMIN:
                            adminCheckBox.setValue(true);
                            break;
                        default:
                            GWT.log("Unknown role " + role, null);
                    }
                }
                memberCheckBox.setEnabled(true);
                adminCheckBox.setEnabled(true);
            }
        });
    }

    /**
     * Check if the user is the last administrator
     * 
     * @return
     */
    protected boolean lastAdmin() {
        for (CheckBox checkbox : usersAdminCheckboxMap.values() ) {
            if (checkbox.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add and remove a permission for a particular user, then properly set the
     * value of the checkbox.
     * 
     * FIXME: this should add/remove user from group
     * 
     * @param userUri
     * @param permission
     * @param type
     * @param adminCheckBox
     */
    protected void modifyPermissions(final String userUri, final Role role, final CheckBox checkbox) {
        ActionType type = ActionType.REMOVE;
        if (checkbox.getValue()) {
            type = ActionType.ADD;
        }
        dispatchAsync.execute(new EditRole(userUri, role, type), new AsyncCallback<EmptyResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error changing permissions", caught);
                checkbox.setValue(!checkbox.getValue());
            }

            @Override
            public void onSuccess(EmptyResult result) {
            }

        });
    }

    /**
     * Create a table to host the list of users and the actions available.
     * 
     * @return table
     */
    private FlexTable createUsersTable() {
        FlexTable flexTable = new FlexTable();
        flexTable.addStyleName("usersTable");
        // headers
        flexTable.setText(0, 0, "User");
        flexTable.setText(0, 1, "Email");
        flexTable.setText(0, 2, "Member");
        flexTable.setText(0, 3, "Admin");
        return flexTable;
    }

    /**
     * Create page title
     * 
     * @return title widget
     */
    private Widget createPageTitle() {
        return new TitlePanel("User Management");
    }
}
