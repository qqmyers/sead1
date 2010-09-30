package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;

public class RoleAdministrationPage extends Composite {
    private final MyDispatchAsync dispatch;

    FlowPanel                     mainPanel;
    TitlePanel                    pageTitle;
    FlexTable                     permissionsTable;

    public RoleAdministrationPage(MyDispatchAsync dispatchAsync) {

        dispatch = dispatchAsync;

        mainPanel = new FlowPanel();
        mainPanel.addStyleName("page");
        initWidget(mainPanel);

        // page title
        pageTitle = new TitlePanel("Access control administration");
        mainPanel.add(pageTitle);

        Label l = new Label("Note: the controls on this page do not currently do anything");
        mainPanel.add(l);

        // permissions table
        permissionsTable = createPermissionsTable();
        mainPanel.add(permissionsTable);
    }

    private FlexTable createPermissionsTable() {
        permissionsTable = new FlexTable();
        permissionsTable.addStyleName("usersTable");
        int i = 0;
        permissionsTable.setText(i++, 0, "Permission");
        for (Permission p : Permission.values() ) {
            permissionsTable.setText(i++, 0, p.getLabel());
        }
        // FIXME for now, hardcode some roles
        addRole("Administrator");
        addRole("Author");
        addRole("Reviewer");
        addRole("Viewer");
        return permissionsTable;
    }

    private void addRole(String label) {
        int j = permissionsTable.getCellCount(0);
        permissionsTable.setText(0, j, label);
        int i = 1;
        for (Permission p : Permission.values() ) {
            permissionsTable.setWidget(i++, j, new CheckBox());
            // FIXME add handler
        }
    }
}
