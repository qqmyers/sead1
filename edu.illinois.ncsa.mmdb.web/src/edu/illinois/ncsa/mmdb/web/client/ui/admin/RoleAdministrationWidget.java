package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.CreateRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DefaultTheRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAccessLevel;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAccessLevelResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.InitializeRoles;
import edu.illinois.ncsa.mmdb.web.client.dispatch.PermissionSetting;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPermissionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRoleAccessLevel;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SubjectResult;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmHandler;
import edu.illinois.ncsa.mmdb.web.client.ui.ConfirmDialog;
import edu.illinois.ncsa.mmdb.web.common.DefaultRole;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.uiuc.ncsa.cet.bean.rbac.medici.PermissionValue;

public class RoleAdministrationWidget extends Composite {
    private final DispatchAsync  dispatch;

    private FlexTable            permissionsTable;
    private Map<String, Integer> columnByRole;
    private Map<String, String>  nameByRole;

    public RoleAdministrationWidget(DispatchAsync dispatchAsync) {
        dispatch = dispatchAsync;

        final FlowPanel mainPanel = new FlowPanel();
        initWidget(mainPanel);

        // permissions table
        permissionsTable = createPermissionsTable();
        permissionsTable.addStyleName("usersTable"); // TODO: make a new style
        mainPanel.add(permissionsTable);

        // set permissions back to defaults
        final Anchor initializeRoles = new Anchor("Set all roles and permissions to defaults");
        initializeRoles.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                initializeRoles.setEnabled(false);
                ConfirmDialog cd = new ConfirmDialog("Initialize permissions", "Are you sure you want to initialize all roles and permissions?\nThis operation cannot be undone, and may affect who can access the system.");
                cd.addConfirmHandler(new ConfirmHandler() {
                    @Override
                    public void onConfirm(ConfirmEvent event) {
                        final Image pending = new Image("./images/loading-small-white.gif");
                        pending.addStyleName("addTagsLink");
                        mainPanel.add(pending);
                        initializeRoles.setText("Setting default roles and permissions");
                        dispatch.execute(new InitializeRoles(), new AsyncCallback<EmptyResult>() {
                            public void onFailure(Throwable caught) {
                                initializeRoles.setText("Default permissions failed");
                                initializeRoles.setEnabled(true);
                            }

                            public void onSuccess(EmptyResult result) {
                                mainPanel.remove(pending);
                                initializeRoles.setText("All roles and permissions set to default");
                                initializeRoles.setEnabled(true);
                                getPermissions();
                            }
                        });
                    }
                });
            }
        });
        mainPanel.add(initializeRoles);

        getPermissions();
    }

    private FlexTable createPermissionsTable() {
        permissionsTable = new FlexTable();
        return permissionsTable;
    }

    void showPermissionSetting(PermissionSetting s) {
        Integer c = columnByRole.get(s.getRoleUri());
        if (c == null) {
            c = permissionsTable.getCellCount(0);
            columnByRole.put(s.getRoleUri(), c);
            nameByRole.put(s.getRoleUri(), s.getRoleName());
            permissionsTable.setText(0, c, s.getRoleName());
            permissionsTable.getColumnFormatter().addStyleName(c, "roleColumn");
        }
        int i = 2;
        for (Permission p : Permission.values() ) {
            if (p == s.getPermission()) {
                CheckBox box = newCheckBox(s);
                permissionsTable.setWidget(i, c, box);
            }
            i++;
        }
    }

    void showAccessLevel(final String role, String[] values, int level) {
        Integer c = columnByRole.get(role);
        if (c == null) {
            GWT.log("Did not find role " + role);
            return;
        }
        final ListBox box = new ListBox();
        for (String x : values ) {
            box.addItem(x);
        }
        box.setSelectedIndex(level);
        permissionsTable.setWidget(1, c, box);

        box.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                dispatch.execute(new SetRoleAccessLevel(role, box.getSelectedIndex()), new AsyncCallback<EmptyResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        box.setEnabled(false);
                        GWT.log("Could not set role access level", caught);
                    }

                    @Override
                    public void onSuccess(EmptyResult result) {
                    }
                });
            }
        });
    }

    void getPermissions() {
        permissionsTable.clear(true);
        permissionsTable.removeAllRows();

        columnByRole = new HashMap<String, Integer>();
        nameByRole = new HashMap<String, String>();

        int i = 0;
        permissionsTable.setText(i++, 0, "");
        permissionsTable.setText(i++, 0, "");
        for (Permission p : Permission.values() ) {
            permissionsTable.setText(i, 0, p.getLabel());
            permissionsTable.getColumnFormatter().addStyleName(0, "usersTableNameColumn");
            String rowStyle = (i + 1) % 2 == 0 ? "metadataTableEvenRow" : "metadataTableOddRow";
            permissionsTable.getRowFormatter().addStyleName(i, rowStyle);
            i++;
        }

        dispatch.execute(new GetAccessLevel(null), new AsyncCallback<GetAccessLevelResult>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(final GetAccessLevelResult accessresult) {
                permissionsTable.setText(1, 0, accessresult.getLabel());

                dispatch.execute(new GetPermissions(), new AsyncCallback<GetPermissionsResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(GetPermissionsResult result) {
                        for (PermissionSetting s : result.getSettings() ) {
                            showPermissionSetting(s);
                        }
                        for (Entry<String, Integer> entry : result.getAccessLevel().entrySet() ) {
                            showAccessLevel(entry.getKey(), accessresult.getLevels(), entry.getValue());
                        }
                        for (Map.Entry<String, Integer> entry : columnByRole.entrySet() ) {
                            final String roleUri = entry.getKey();
                            boolean deletable = true;
                            boolean isDefault = false;
                            //Add delete button to capable role
                            for (DefaultRole r : DefaultRole.special() ) {
                                if (roleUri.equals(r.getUri())) {
                                    deletable = false;
                                }
                            }
                            if (deletable) {
                                final String roleName = nameByRole.get(roleUri);
                                final int c = entry.getValue();
                                int r = Permission.values().length + 4;
                                Anchor deleteRole = new Anchor("Delete");
                                deleteRole.addClickHandler(new ClickHandler() {
                                    @Override
                                    public void onClick(ClickEvent event) {
                                        ConfirmDialog cd = new ConfirmDialog("Delete " + roleName, "Do you really want to delete role \"" + roleName + "\"?");
                                        cd.addConfirmHandler(new ConfirmHandler() {
                                            @Override
                                            public void onConfirm(ConfirmEvent event) {
                                                dispatch.execute(new DeleteRole(roleUri), new AsyncCallback<EmptyResult>() {
                                                    @Override
                                                    public void onFailure(Throwable caught) {
                                                        GWT.log("cannot delete role");
                                                        new ConfirmDialog("Role not deleted", caught.getMessage(), false);
                                                    }

                                                    @Override
                                                    public void onSuccess(EmptyResult result) {
                                                        getPermissions(); // start over.
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                                permissionsTable.setWidget(r, c, deleteRole);
                            }
                            //Add default button to default roles
                            for (DefaultRole r : DefaultRole.values() ) {
                                if (roleUri.equals(r.getUri())) {
                                    isDefault = true;
                                }
                            }
                            if (isDefault) {
                                final String roleName = nameByRole.get(roleUri);
                                final int c = entry.getValue();
                                int r = Permission.values().length + 3;
                                Anchor defaultRole = new Anchor("Default");
                                defaultRole.addClickHandler(new ClickHandler() {
                                    @Override
                                    public void onClick(ClickEvent event) {
                                        ConfirmDialog cd = new ConfirmDialog("Default " + roleName, "Do you really want to set permissions to their default for \"" + roleName + "\"?");
                                        cd.addConfirmHandler(new ConfirmHandler() {
                                            @Override
                                            public void onConfirm(ConfirmEvent event) {
                                                dispatch.execute(new DefaultTheRole(roleUri), new AsyncCallback<EmptyResult>() {
                                                    @Override
                                                    public void onFailure(Throwable caught) {
                                                        GWT.log("Cannot Default role");
                                                        new ConfirmDialog("Role not defaulted", caught.getMessage(), false);
                                                    }

                                                    @Override
                                                    public void onSuccess(EmptyResult result) {
                                                        getPermissions(); // start over.
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                                permissionsTable.setWidget(r, c, defaultRole);
                            }
                        }
                        // now add the "add role" controls
                        VerticalPanel addRolePanel = new VerticalPanel();
                        final TextBox newRoleName = new TextBox();
                        newRoleName.setWidth("10em");
                        final Anchor addRole = new Anchor("Create role");
                        addRole.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                addRole(newRoleName.getText(), addRole);
                            }
                        });
                        newRoleName.addKeyUpHandler(new KeyUpHandler() {
                            public void onKeyUp(KeyUpEvent event) {
                                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                                    addRole(newRoleName.getText(), addRole);
                                }
                            }
                        });
                        addRolePanel.add(newRoleName);
                        addRolePanel.add(addRole);
                        permissionsTable.setWidget(0, permissionsTable.getCellCount(0), addRolePanel);
                    }
                });
            }
        });
    }

    void addRole(String name, Anchor button) {
        if (name.isEmpty()) {
            ConfirmDialog emptyRole = new ConfirmDialog("Error", "Please enter a name for the role", false);
            emptyRole.getOkText().setText("OK");
        } else {
            button.setText("Creating role ...");
            button.setEnabled(false);
            dispatch.execute(new CreateRole(name), new AsyncCallback<SubjectResult>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("error: can't create role");
                }

                @Override
                public void onSuccess(SubjectResult result) {
                    getPermissions(); // start over
                }
            });
        }
    }

    String key(String roleUri, Permission permission) {
        return roleUri + " " + permission.getUri();
    }

    PermissionValue valueFor(boolean condition) {
        return condition ? PermissionValue.ALLOW : PermissionValue.DO_NOT_ALLOW;
    }

    /**
     * Create a checkbox
     * 
     * @param setting
     * @return
     */
    CheckBox newCheckBox(final PermissionSetting setting) {
        final CheckBox box = new CheckBox();
        box.setValue(setting.getValue() == PermissionValue.ALLOW);
        // when the box is checked, make the setting
        box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(final ValueChangeEvent<Boolean> event) {
                PermissionValue desiredValue = event.getValue() ? PermissionValue.ALLOW : PermissionValue.DO_NOT_ALLOW;
                PermissionSetting newSetting = new PermissionSetting(setting.getRoleUri(), setting.getPermission(), desiredValue);
                dispatch.execute(new SetPermissions(newSetting), new AsyncCallback<SetPermissionsResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // failed, so toggle checkbox back, without firing an event
                        box.setValue(!event.getValue());
                        new ConfirmDialog("Permission not changed", caught.getMessage(), false);
                    }

                    @Override
                    public void onSuccess(SetPermissionsResult result) {
                        // succeeded, so no action necessary
                    }
                });
            }
        });
        return box;
    }
}
