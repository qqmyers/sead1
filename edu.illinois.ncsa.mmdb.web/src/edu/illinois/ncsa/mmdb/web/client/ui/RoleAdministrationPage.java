package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.CreateRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.PermissionSetting;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPermissionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SubjectResult;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmHandler;
import edu.uiuc.ncsa.cet.bean.rbac.medici.DefaultRole;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.rbac.medici.PermissionValue;

public class RoleAdministrationPage extends Composite {
    private final DispatchAsync dispatch;

    FlowPanel                   mainPanel;
    TitlePanel                  pageTitle;
    FlexTable                   permissionsTable;

    Button                      saveButton;
    Button                      cancelButton;

    Map<String, Integer>        columnByRole;
    Map<String, String>         nameByRole;

    public RoleAdministrationPage(DispatchAsync dispatchAsync) {

        dispatch = dispatchAsync;

        mainPanel = new FlowPanel();
        mainPanel.addStyleName("page");
        initWidget(mainPanel);

        // page title
        pageTitle = new TitlePanel("Access control administration");
        mainPanel.add(pageTitle);

        //Label l = new Label("Note: the controls on this page do not currently do anything");
        //mainPanel.add(l);
        Hyperlink userAdminLink = new Hyperlink("Administer users", "modifyPermissions");
        mainPanel.add(userAdminLink);

        // permissions table
        permissionsTable = createPermissionsTable();
        permissionsTable.addStyleName("usersTable"); // TODO: make a new style
        mainPanel.add(permissionsTable);

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
        int i = 1;
        for (Permission p : Permission.values() ) {
            if (p == s.getPermission()) {
                CheckBox box = newCheckBox(s);
                permissionsTable.setWidget(i, c, box);
            }
            i++;
        }
    }

    void getPermissions() {
        permissionsTable.clear(true);

        columnByRole = new HashMap<String, Integer>();
        nameByRole = new HashMap<String, String>();

        int i = 0;
        permissionsTable.setText(i++, 0, "");
        for (Permission p : Permission.values() ) {
            permissionsTable.setText(i, 0, p.getLabel());
            permissionsTable.getColumnFormatter().addStyleName(0, "usersTableNameColumn");
            String rowStyle = (i + 1) % 2 == 0 ? "metadataTableEvenRow" : "metadataTableOddRow";
            permissionsTable.getRowFormatter().addStyleName(i, rowStyle);
            i++;
        }
        dispatch.execute(new GetPermissions(), new AsyncCallback<GetPermissionsResult>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(GetPermissionsResult result) {
                for (PermissionSetting s : result.getSettings() ) {
                    showPermissionSetting(s);
                }
                for (Map.Entry<String, Integer> entry : columnByRole.entrySet() ) {
                    final String roleUri = entry.getKey();
                    boolean deletable = true;
                    for (DefaultRole r : DefaultRole.special() ) {
                        if (roleUri.equals(r.getUri())) {
                            deletable = false;
                        }
                    }
                    if (deletable) {
                        final String roleName = nameByRole.get(roleUri);
                        final int c = entry.getValue();
                        int r = Permission.values().length + 2;
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

    void addRole(String name, Anchor button) {
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
