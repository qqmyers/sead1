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
package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AdminAddUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AdminAddUserResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EditRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EditRole.ActionType;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsers;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsersResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.PermissionSetting;
import edu.illinois.ncsa.mmdb.web.client.ui.ConfirmDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.SignupPage;
import edu.illinois.ncsa.mmdb.web.client.ui.TitlePanel;
import edu.illinois.ncsa.mmdb.web.common.DefaultRole;

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
public class UserManagementWidget extends Composite {

    private final FlowPanel                mainPanel;

    private final FlexTable                activeUsersTable;
    private final FlexTable                inactiveUsersTable;

    private final DispatchAsync            dispatchAsync;

    private final HashMap<String, Integer> columnByRole;

    public UserManagementWidget(DispatchAsync dispatchAsync) {

        this.dispatchAsync = dispatchAsync;

        mainPanel = new FlowPanel();
        mainPanel.addStyleName("page");
        initWidget(mainPanel);

        VerticalPanel usersPanel = new VerticalPanel();
        usersPanel.addStyleName("userManagementMain");

        // users table
        activeUsersTable = createUsersTable();
        inactiveUsersTable = createUsersTable();
        usersPanel.add(discloseAs(createUserPanel(), "Add user", "Add user", true));
        usersPanel.add(discloseAs(inactiveUsersTable, "Inactive users", "Inactive users", true));
        usersPanel.add(discloseAs(activeUsersTable, "Active users", false));
        mainPanel.add(usersPanel);

        // necessary so that the main container wraps around center panel
        SimplePanel clearFloat = new SimplePanel();
        clearFloat.addStyleName("clearFloat");
        mainPanel.add(clearFloat);

        // load users and roles from server side
        columnByRole = new HashMap<String, Integer>();
        loadRolesAndUsers();
    }

    /**
     * Create a new user and send a notification email to that user.
     * 
     * @return
     */
    private Panel createUserPanel() {
        FlexTable table = new FlexTable();
        table.getColumnFormatter().setWidth(0, "100px");
        final Label status = new Label("");
        table.setWidget(0, 1, status);
        table.addStyleName("usersTable");
        table.setText(1, 0, "Email");
        final TextBox email = new TextBox();
        table.setWidget(1, 1, email);
        table.setText(2, 0, "First name");
        final TextBox firstName = new TextBox();
        table.setWidget(2, 1, firstName);
        table.setText(3, 0, "Last name");
        final TextBox lastName = new TextBox();
        table.setWidget(3, 1, lastName);
        Button sendInvite = new Button("Invite", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (email.getValue().isEmpty() || firstName.getValue().isEmpty() || lastName.getValue().isEmpty()) {
                    status.setText("All fields must be specified");
                } else if (!email.getValue().matches(SignupPage.EMAIL_REGEX)) {
                    status.setText("Please specify a valid email address");
                } else {
                    dispatchAsync.execute(new AdminAddUser(firstName.getValue(), lastName.getValue(), email.getValue(), MMDB.getSessionState().getCurrentUser().getUri()), new AsyncCallback<AdminAddUserResult>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            GWT.log("Error creating new user", caught);
                            status.setText("Error creating new user");
                        }

                        @Override
                        public void onSuccess(AdminAddUserResult result) {
                            if (result.getError() == null) {
                                GWT.log("Successfully created a new user");
                                status.setText("User successfully created and invited by email. The new user's role has been set to: " + result.getRole() + ". Refresh user list to change their role.");
                                email.setText("");
                                firstName.setText("");
                                lastName.setText("");
                                loadRolesAndUsers();
                            } else {
                                GWT.log("Error creating new user");
                                status.setText(result.getError());
                            }
                        }

                    });
                }
            }
        });
        table.setWidget(4, 1, sendInvite);
        return table;
    }

    private void loadRolesAndUsers() {
        dispatchAsync.execute(new GetPermissions(), new AsyncCallback<GetPermissionsResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error getting user permissions", caught);
            }

            @Override
            public void onSuccess(GetPermissionsResult result) {
                int col = 3;
                for (PermissionSetting role : PermissionUtil.getRoles(result.getSettings()) ) {
                    String roleUri = role.getRoleUri();
                    // don't allow admin to add or remove from special owner and anonymous role
                    if (!roleUri.equals(DefaultRole.OWNER.getUri()) && !roleUri.equals(DefaultRole.ANONYMOUS.getUri())) {
                        String roleName = role.getRoleName();
                        columnByRole.put(roleUri, col);
                        activeUsersTable.setText(0, col, roleName);
                        activeUsersTable.getColumnFormatter().addStyleName(col, "roleColumn");
                        inactiveUsersTable.setText(0, col, roleName);
                        inactiveUsersTable.getColumnFormatter().addStyleName(col, "roleColumn");
                        col++;
                    }
                }
                loadUsers();
            }
        });
    }

    DisclosurePanel discloseAs(Widget w, String openTitle, boolean open) {
        return discloseAs(w, openTitle, openTitle, open);
    }

    DisclosurePanel discloseAs(Widget w, final String openTitle, final String closedTitle, boolean open) {
        final DisclosurePanel dp = new DisclosurePanel(open ? openTitle : closedTitle);
        dp.setAnimationEnabled(true);
        dp.setOpen(open);

        dp.add(w);
        dp.addOpenHandler(new OpenHandler<DisclosurePanel>() {
            @Override
            public void onOpen(OpenEvent<DisclosurePanel> event) {
                dp.getHeaderTextAccessor().setText(openTitle);
            }
        });
        dp.addCloseHandler(new CloseHandler<DisclosurePanel>() {
            @Override
            public void onClose(CloseEvent<DisclosurePanel> event) {
                dp.getHeaderTextAccessor().setText(closedTitle);
            }
        });
        return dp;
    }

    /**
     * Load users from server side.
     */
    private void loadUsers() {
        dispatchAsync.execute(new GetUsers(),
                new AsyncCallback<GetUsersResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error getting users", caught);
                    }

                    @Override
                    public void onSuccess(GetUsersResult result) {
                        ArrayList<GetUsersResult.User> users = result.getUsers();
                        if (users.size() == 0) {
                            activeUsersTable.setText(activeUsersTable.getRowCount() + 1, 0, "No users found.");
                        } else {
                            createRows(users);
                        }
                    }
                });

    }

    /**
     * Create a single row in the table based on user.
     * 
     * @param user
     */
    protected void createRows(final List<GetUsersResult.User> users) {
        if (users.size() == 0) {
            return;
        }
        for (GetUsersResult.User user : users ) {
            Set<String> roles = user.roles;
            FlexTable usersTable = roles.size() == 0 ? inactiveUsersTable : activeUsersTable;

            int row = usersTable.getRowCount();

            usersTable.setText(row, 0, user.name);
            usersTable.setText(row, 1, user.email);
            usersTable.setText(row, 2, user.lastlogin);

            if ((user.name == null) && (user.email == null)) {
                usersTable.setText(row, 0, user.id);
            }

            GWT.log("user " + user.id + " belongs to roles " + roles);
            for (Map.Entry<String, Integer> entry : columnByRole.entrySet() ) {
                final String roleUri = entry.getKey();
                final Integer col = entry.getValue();
                final CheckBox box = new CheckBox();
                final String userid = user.id;
                box.setValue(roles.contains(roleUri));
                box.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        modifyPermissions(userid, roleUri, box);
                    }
                });
                usersTable.setWidget(row, col, box);
            }

            // stripe it
            String rowStyle = (row - 1) % 2 == 0 ? "metadataTableEvenRow" : "metadataTableOddRow";
            usersTable.getRowFormatter().addStyleName(row, rowStyle);
        }
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
                new ConfirmDialog("Role membership not changed", caught.getMessage(), false);
                // FIXME notify user if the error is because they would lock themselves out
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
        flexTable.getColumnFormatter().addStyleName(0, "usersTableNameColumn");
        flexTable.setText(0, 1, "Email");
        flexTable.getColumnFormatter().addStyleName(1, "usersTableEmailColumn");
        flexTable.setText(0, 2, "Last Login");
        flexTable.getColumnFormatter().addStyleName(2, "usersTableNameColumn");
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
