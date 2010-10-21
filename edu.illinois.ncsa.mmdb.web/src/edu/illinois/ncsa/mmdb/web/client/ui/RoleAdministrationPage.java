package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPermissionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.PermissionSetting;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPermissionsResult;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.rbac.medici.PermissionValue;

public class RoleAdministrationPage extends Composite {
    private final MyDispatchAsync  dispatch;

    FlowPanel                      mainPanel;
    TitlePanel                     pageTitle;
    FlexTable                      permissionsTable;

    Button                         saveButton;
    Button                         cancelButton;

    Map<String, PermissionSetting> changes;
    Map<String, Integer>           columnByRole;

    public RoleAdministrationPage(MyDispatchAsync dispatchAsync) {

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

        saveButton = new Button("Save");
        // on save, submit all changes
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (PermissionSetting setting : changes.values() ) {
                    GWT.log("setting: " + setting.getRoleUri() + " " + setting.getPermission().getLabel());
                }
                dispatch.execute(new SetPermissions(changes.values()), new AsyncCallback<SetPermissionsResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // FIXME notify someone
                    }

                    @Override
                    public void onSuccess(SetPermissionsResult result) {
                        // FIXME notify someone
                    }
                });
            }
        });
        mainPanel.add(saveButton);

        cancelButton = new Button("Cancel");
        // on cancel, undo all checkboxes and get rid of all changes
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (int row = 1; row < permissionsTable.getRowCount(); row++ ) {
                    for (int col = 1; col < permissionsTable.getCellCount(row); col++ ) {
                        Undoable control = (Undoable) permissionsTable.getWidget(row, col);
                        control.undo();
                    }
                }
                changes = new HashMap<String, PermissionSetting>();
                GWT.log("cancelled permission settings");
            }
        });
        mainPanel.add(cancelButton);

        int i = 0;
        permissionsTable.setText(i++, 0, "");
        for (Permission p : Permission.values() ) {
            permissionsTable.setText(i++, 0, p.getLabel());
        }
        getPermissions();

        changes = new HashMap<String, PermissionSetting>();
        columnByRole = new HashMap<String, Integer>();
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
            permissionsTable.setText(0, c, s.getRoleName());
        }
        int i = 1;
        for (Permission p : Permission.values() ) {
            if (p == s.getPermission()) {
                UndoableCheckBox box = newCheckBox(s);
                permissionsTable.setWidget(i, c, box);
            }
            i++;
        }
    }

    void getPermissions() {
        dispatch.execute(new GetPermissions(), new AsyncCallback<GetPermissionsResult>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(GetPermissionsResult result) {
                for (PermissionSetting s : result.getSettings() ) {
                    showPermissionSetting(s);
                }
            }
        });
    }

    String key(String roleUri, Permission permission) {
        return roleUri + " " + permission.getUri();
    }

    void addChange(String roleUri, Permission permission, PermissionValue value) {
        changes.put(key(roleUri, permission), new PermissionSetting(roleUri, permission, value));
    }

    void removeChange(String roleUri, Permission permission) {
        changes.remove(key(roleUri, permission));
    }

    /**
     * Create a checkbox
     * 
     * @param setting
     * @return
     */
    UndoableCheckBox newCheckBox(final PermissionSetting setting) {
        final UndoableCheckBox box = new UndoableCheckBox();
        box.setValue(setting.getValue() == PermissionValue.ALLOW);
        box.mark();
        // when the box is checked, add a change to the list of changes. when it is undone, remove said change
        box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (box.hasChanged()) {
                    addChange(setting.getRoleUri(), setting.getPermission(), event.getValue() ? PermissionValue.ALLOW : PermissionValue.DO_NOT_ALLOW);
                } else {
                    removeChange(setting.getRoleUri(), setting.getPermission());
                }
            }
        });
        return box;
    }
}
