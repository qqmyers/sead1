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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EditRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRoles;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRolesResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsers;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsersResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EditRole.ActionType;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.rbac.medici.DefaultRole;

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

    private final FlowPanel                mainPanel;

    private final Widget                   pageTitle;

    private final FlexTable                usersTable;

    private final MyDispatchAsync          dispatchAsync;

    private final HashMap<String, Integer> columnByRole;

    public UserManagementPage(MyDispatchAsync dispatchAsync) {

        this.dispatchAsync = dispatchAsync;

        mainPanel = new FlowPanel();
        mainPanel.addStyleName("page");
        initWidget(mainPanel);

        // page title
        pageTitle = createPageTitle();
        mainPanel.add(pageTitle);

        Hyperlink pAdmin = new Hyperlink("Administer access control", "accessControl");
        mainPanel.add(pAdmin);

        // users table
        usersTable = createUsersTable();
        mainPanel.add(usersTable);

        columnByRole = new HashMap<String, Integer>();
        // load users and roles from server side
        loadRolesAndUsers();
    }

    private void loadRolesAndUsers() {
        dispatchAsync.execute(new GetPermissions(), new AsyncCallback<GetPermissionsResult>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(GetPermissionsResult result) {
                int col = 2;
                for (Map.Entry<String, String> entry : PermissionUtil.getRoles(result.getSettings()).entrySet() ) {
                    String roleUri = entry.getKey();
                    if (!roleUri.equals(DefaultRole.OWNER.getUri())) {
                        String roleName = entry.getValue();
                        columnByRole.put(roleUri, col);
                        usersTable.setText(0, col, roleName);
                        col++;
                    }
                }
                loadUsers();
            }
        });
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
                        } else {
                            // sort users by name
                            Collections.sort(users, new Comparator<PersonBean>() {
                                @Override
                                public int compare(PersonBean o1, PersonBean o2) {
                                    if (o1.getName() == null) {
                                        return +1;
                                    }
                                    if (o2.getName() == null) {
                                        return -1;
                                    }
                                    if (o1.getName().equals(o2.getName())) {
                                        return o1.getEmail().compareTo(o2.getEmail());
                                    }
                                    return o1.getName().compareTo(o2.getName());
                                }
                            });
                            for (PersonBean user : users ) {
                                createRow(user);
                            }
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

        final int row = usersTable.getRowCount() + 1;

        usersTable.setText(row, 0, user.getName());
        usersTable.setText(row, 1, user.getEmail());

        if ((user.getName() == null) && (user.getEmail() == null)) {
            usersTable.setText(row, 0, user.getUri());
        }

        dispatchAsync.execute(new GetRoles(user.getUri()), new AsyncCallback<GetRolesResult>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(GetRolesResult result) {
                Set<String> roles = result.getRoles();
                GWT.log("user " + user.getUri() + " belongs to roles " + roles);
                for (Map.Entry<String, Integer> entry : columnByRole.entrySet() ) {
                    final String roleUri = entry.getKey();
                    final Integer col = entry.getValue();
                    final CheckBox box = new CheckBox();
                    box.setValue(roles.contains(roleUri));
                    box.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            modifyPermissions(user.getUri(), roleUri, box);
                        }
                    });
                    usersTable.setWidget(row, col, box);
                }
            }
        });
    }

    /**
     * Check if the user is the last administrator
     * 
     * @return
     */
    protected boolean lastAdmin() {
        return false;
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
    protected void modifyPermissions(final String userUri, final String roleUri, final CheckBox checkbox) {
        ActionType type = ActionType.REMOVE;
        if (checkbox.getValue()) {
            type = ActionType.ADD;
        }
        dispatchAsync.execute(new EditRole(userUri, roleUri, type), new AsyncCallback<EmptyResult>() {

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
