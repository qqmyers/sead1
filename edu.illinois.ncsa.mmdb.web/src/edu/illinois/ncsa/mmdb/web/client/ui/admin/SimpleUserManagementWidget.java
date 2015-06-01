package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AdminAddUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AdminAddUserResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EditRole;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EditRole.ActionType;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAccessLevel;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAccessLevelResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRoles;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRolesResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsers;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsersResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRoleAccessLevel;
import edu.illinois.ncsa.mmdb.web.client.ui.ConfirmDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.SignupPage;
import edu.illinois.ncsa.mmdb.web.common.DefaultRole;

/**
 * A page to manage users. Users may be assigned to the roles admin, author, or
 * viewer, or be marked as
 * unassigned (awaiting role assignment) or
 * inactive (decision has been made that no role should be assigned)
 *
 * Users are sorted by last name. Table can filter all, only unassigned, or only
 * inactive. Rows are color coded by role.
 *
 * New users may be invited
 *
 * Anonymous may be allowed to view all, or view public data (data marked with
 * "Data Visibility" public.
 *
 *
 * @author myersjd@umich.edu
 *
 *         derived ~from UserManagementWidget
 *
 */
public class SimpleUserManagementWidget extends Composite {

    private final FlowPanel                              mainPanel;

    private final FlowPanel                              usersTable;
    private final DispatchAsync                          dispatchAsync;

    private final SortedMap<String, GetUsersResult.User> usersMap      = new TreeMap<String, GetUsersResult.User>();

    public static final String                           updated       = "updated";
    public static final String                           updateFailure = "update-failure";

    //.equals(PersonBeanUtil.getAnonymousURI().toString())
    private static final String                          ANONID_STRING = "http://cet.ncsa.uiuc.edu/2007/person/anonymous";

    public SimpleUserManagementWidget(DispatchAsync dispatchAsync) {

        this.dispatchAsync = dispatchAsync;

        mainPanel = new FlowPanel();
        mainPanel.addStyleName("page");
        initWidget(mainPanel);

        mainPanel.addStyleName("userManagementMain");

        // users table
        usersTable = new FlowPanel();

        mainPanel.add(createUserPanel());
        mainPanel.add(usersTable);

        loadUsers();

    }

    /**
     * Create a new user and send a notification email to that user.
     *
     * @return
     */
    private Panel createUserPanel() {
        FlexTable table = new FlexTable();
        table.getColumnFormatter().setWidth(0, "100px");
        final Label invite = new Label("Send an Invite");
        invite.setStyleName("sectionTitle");
        table.setWidget(0, 0, invite);
        table.getFlexCellFormatter().setColSpan(0, 0, 2);
        final Label status = new Label("");
        table.setWidget(1, 1, status);
        table.addStyleName("usersTable");
        table.setText(2, 0, "Email");
        final TextBox email = new TextBox();
        table.setWidget(2, 1, email);
        table.setText(3, 0, "First name");
        final TextBox firstName = new TextBox();
        table.setWidget(3, 1, firstName);
        table.setText(4, 0, "Last name");
        final TextBox lastName = new TextBox();
        table.setWidget(4, 1, lastName);
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
                                status.setText("User successfully created and invited by email. The new user's role has been set to: " + result.getRole() + ".");
                                email.setText("");
                                firstName.setText("");
                                lastName.setText("");
                                loadUsers();
                            } else {
                                GWT.log("Error creating new user");
                                status.setText(result.getError());
                            }
                        }

                    });
                }
            }
        });
        table.setWidget(5, 1, sendInvite);
        return table;
    }

    /**
     * Load users from server side.
     */
    private void loadUsers() {
        usersMap.clear();
        dispatchAsync.execute(new GetUsers(),
                new AsyncCallback<GetUsersResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error getting users", caught);
                    }

                    @Override
                    public void onSuccess(GetUsersResult result) {
                        ArrayList<GetUsersResult.User> users = result.getUsers();
                        if (users.size() != 0) {
                            for (GetUsersResult.User u : users ) {

                                if (!((u.email == null) || u.email.length() == 0)) { //Anonymous account is currently the only one with no email
                                    String lastFirstName = u.name;
                                    if (lastFirstName != null) {
                                        int i = lastFirstName.lastIndexOf(' ');
                                        if (i != -1) {
                                            lastFirstName = lastFirstName.substring(i + 1) + ", " + lastFirstName.substring(0, i);
                                        }
                                        usersMap.put(lastFirstName, u);
                                    }
                                }
                            }
                        }

                        loadTable();
                    }

                });

    }

    private void loadTable() {
        usersTable.clear();
        FlowPanel anonPanel = new FlowPanel();
        addAnonChoices(anonPanel); //Async construction
        usersTable.add(anonPanel);
        final Label uTable = new Label("User Roles");
        uTable.setStyleName("sectionTitle");
        usersTable.add(uTable);

        addFilters(usersTable);
        addHeaders(usersTable);
        boolean odd = true;
        for (final Map.Entry<String, GetUsersResult.User> u : usersMap.entrySet() ) {
            final FlowPanel fp = new FlowPanel();
            fp.addStyleName("user");
            Label unameLabel = new Label(u.getKey());
            unameLabel.setTitle(u.getValue().email);

            fp.add(unameLabel);
            //With Simple permissions, only one role should be returned and it should be one of the 5 defined here.
            String role = DefaultRole.getNameFromUri(u.getValue().roles.iterator().next());
            fp.addStyleName(role);
            final RoleBox roleBox = getRolesBox(role);
            roleBox.addChangeHandler(new ChangeHandler() {

                @Override
                public void onChange(ChangeEvent event) {

                    if (roleBox.getSelectedIndex() == roleBox.indexOnServer) {//Should not occur - let's test
                        Window.alert("No change");

                    } else {
                        String id = u.getValue().id;
                        //With Simple Perms, adding a role removes others (including unassigned and inactive)
                        dispatchAsync.execute(new EditRole(id, DefaultRole.getUriForName(roleList.get(roleBox.getSelectedIndex())), EditRole.ActionType.ADD), new AsyncCallback<EmptyResult>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                GWT.log("Error changing permissions", caught);
                                new ConfirmDialog("Role membership not changed", caught.getMessage(), false);

                                roleBox.resetToServerIndex();
                            }

                            @Override
                            public void onSuccess(EmptyResult result) {
                                fp.removeStyleName(roleList.get(roleBox.indexOnServer));
                                roleBox.setServerIndex(roleBox.getSelectedIndex());
                                fp.addStyleName(roleList.get(roleBox.indexOnServer));
                            }

                        });

                    }
                }
            });
            fp.add(roleBox);
            fp.add(new Label(u.getValue().lastlogin));
            usersTable.add(fp);

            // stripe it
            fp.addStyleName(odd ? "metadataTableEvenRow" : "metadataTableOddRow");
            odd = !odd;
        }

    }

    private static final String FILTERED = "filter";

    private void addFilters(FlowPanel usersTable2) {
        RadioButton showAllButton = new RadioButton("roleFilter", "Show All");
        showAllButton.setTitle("All users with any role");
        showAllButton.setValue(true);
        RadioButton showActiveButton = new RadioButton("roleFilter", "Show Active");
        showActiveButton.setTitle("Admin, Author, Viewer");
        RadioButton showUnassignedButton = new RadioButton("roleFilter", "Show Unassigned");
        showUnassignedButton.setTitle("Users who haven't been assigned a role or made Inactive");

        showAllButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Iterator<Widget> it = usersTable.iterator();
                while (it.hasNext()) {
                    Widget widget = it.next();
                    if (widget.getStyleName().contains(FILTERED)) {
                        widget.removeStyleName(FILTERED);
                    }
                }
            }
        });

        showActiveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Iterator<Widget> it = usersTable.iterator();
                while (it.hasNext()) {
                    Widget widget = it.next();
                    String style = widget.getStyleName();
                    if (style.contains(DefaultRole.ADMINISTRATOR.getName()) ||
                            style.contains(DefaultRole.AUTHOR.getName()) ||
                            style.contains(DefaultRole.VIEWER.getName())) {
                        widget.removeStyleName(FILTERED);
                    } else if (style.contains("user")) {
                        widget.addStyleName(FILTERED);
                    }
                }
            }
        });

        showUnassignedButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Iterator<Widget> it = usersTable.iterator();
                while (it.hasNext()) {
                    Widget widget = it.next();
                    String style = widget.getStyleName();
                    if (style.contains("user")) {
                        if (style.contains(SimpleUserManagementWidget.UNASSIGNED_ROLE)) {
                            widget.removeStyleName(FILTERED);
                        } else {
                            widget.addStyleName(FILTERED);
                        }
                    }
                }
            }
        });

        FlowPanel filters = new FlowPanel();
        filters.setStyleName("filtergroup");
        filters.add(showUnassignedButton);
        filters.add(showActiveButton);
        filters.add(showAllButton);
        usersTable2.add(filters);
    }

    private int maxAccessLevel;

    private void addAnonChoices(final FlowPanel anonPanel) {

        dispatchAsync.execute(new GetRoles(ANONID_STRING), new AsyncCallback<GetRolesResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Can't determine anon role", caught);
            }

            @Override
            public void onSuccess(GetRolesResult result) {
                if (!result.getRoles().contains(DefaultRole.ANONYMOUS.getUri())) {
                    configureAnonChoices(anonPanel, 0);
                } else {
                    dispatchAsync.execute(new GetAccessLevel(DefaultRole.ANONYMOUS.getUri()), new AsyncCallback<GetAccessLevelResult>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            GWT.log("Can't determine anon access level", caught);
                        }

                        @Override
                        public void onSuccess(GetAccessLevelResult result) {
                            //need to set max level before creating click handlers
                            maxAccessLevel = result.getLevels().length - 1;
                            if (result.getDatasetLevel() == 0) {
                                configureAnonChoices(anonPanel, 2);
                            } else {
                                configureAnonChoices(anonPanel, 1);
                            }

                        }
                    });
                }

            }

        });
    }

    private void configureAnonChoices(FlowPanel anonPanel, int option) {

        final RadioButton loginRequiredButton = new RadioButton("anonRole", "Login Required");
        loginRequiredButton.setTitle("Users must create an account to view any data");
        final RadioButton viewPublicButton = new RadioButton("anonRole", "View Public");
        viewPublicButton.setTitle("Data with \'Public\' Visibility can be viewed anonymously");
        final RadioButton viewAllButton = new RadioButton("anonRole", "View All");
        viewAllButton.setTitle("All data is visible to anonymous users");
        switch (option) {
            case 0:
                loginRequiredButton.setValue(true);
                break;
            case 1:
                viewPublicButton.setValue(true);
                break;
            case 2:
                viewAllButton.setValue(true);
                break;
            default:
                GWT.log("Error - bad option value");
                break;
        }
        viewAllButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                //Assign anon anonymous role and access level 0
                dispatchAsync.execute(new EditRole(ANONID_STRING, DefaultRole.ANONYMOUS.getUri(), ActionType.ADD), new AsyncCallback<EmptyResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Failure setting Anon role", caught);
                        loadTable(); //Error affects radio button display

                    }

                    @Override
                    public void onSuccess(EmptyResult result) {
                        dispatchAsync.execute(new SetRoleAccessLevel(DefaultRole.ANONYMOUS.getUri(), 0), new AsyncCallback<EmptyResult>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                GWT.log("Failure setting Anon access level", caught);
                                loadTable(); //Error affects radio button display
                            }

                            @Override
                            public void onSuccess(EmptyResult result) {
                                //It's all good
                            }

                        });
                    }

                });
            }
        });

        viewPublicButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                //Assign anon anonymous role and access level 0
                dispatchAsync.execute(new EditRole(ANONID_STRING, DefaultRole.ANONYMOUS.getUri(), ActionType.ADD), new AsyncCallback<EmptyResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Failure setting Anon role", caught);
                        loadTable(); //Error affects radio button display

                    }

                    @Override
                    public void onSuccess(EmptyResult result) {
                        dispatchAsync.execute(new SetRoleAccessLevel(DefaultRole.ANONYMOUS.getUri(), maxAccessLevel), new AsyncCallback<EmptyResult>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                GWT.log("Failure setting Anon access level", caught);
                                loadTable(); //Error affects radio button display
                            }

                            @Override
                            public void onSuccess(EmptyResult result) {
                                //It's all good
                            }

                        });
                    }

                });

            }
        });
        loginRequiredButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                //Remove anonymous role
                dispatchAsync.execute(new EditRole(ANONID_STRING, DefaultRole.ANONYMOUS.getUri(), ActionType.REMOVE), new AsyncCallback<EmptyResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Failure setting Anon role", caught);
                        loadTable(); //Error affects radio button display

                    }

                    @Override
                    public void onSuccess(EmptyResult result) {

                    }

                });
            }
        });

        anonPanel.setStyleName("anongroup");
        Label anonLabel = new Label("Anonymous Access Setting:");
        anonLabel.addStyleName("sectionTitle");
        anonLabel.setTitle("Access when not logged in/browsing without an account");
        anonPanel.add(anonLabel);
        anonPanel.add(loginRequiredButton);
        anonPanel.add(viewPublicButton);
        anonPanel.add(viewAllButton);

    }

    public final static String        UNASSIGNED_ROLE = "Unassigned";
    public final static String        INACTIVE_ROLE   = "Inactive";
    private static LinkedList<String> roleList        = new LinkedList<String>();

    private RoleBox getRolesBox(String curRole) {
        if (roleList.isEmpty()) {
            roleList.add(UNASSIGNED_ROLE);
            roleList.add(DefaultRole.ADMINISTRATOR.getName());
            roleList.add(DefaultRole.AUTHOR.getName());
            roleList.add(DefaultRole.VIEWER.getName());
            roleList.add(INACTIVE_ROLE);

        }

        RoleBox roles = new RoleBox(false);

        roles.addItem(UNASSIGNED_ROLE);
        roles.addItem(DefaultRole.ADMINISTRATOR.getName());
        roles.addItem(DefaultRole.AUTHOR.getName());
        roles.addItem(DefaultRole.VIEWER.getName());
        roles.addItem(INACTIVE_ROLE);
        roles.setVisibleItemCount(1);
        int index = roleList.indexOf(curRole);
        if (index < 0) {
            GWT.log("Warning: Attempt to set role to: " + curRole);
            index = 0;
        }
        roles.setServerIndex(index);
        return roles;
    }

    private void addHeaders(FlowPanel usersPanel) {
        // headers
        FlowPanel fp = new FlowPanel();
        fp.addStyleName("userTableHeader");
        Label userLabel = new Label("User");
        userLabel.setTitle("User name and (hover for) email");
        fp.add(userLabel);
        fp.add(new Label("Role"));
        fp.add(new Label("Last Login"));
        usersPanel.add(fp);
    }

    //Adding memory of setting on server side, so we can revert if EditRole fails
    class RoleBox extends ListBox {
        private int indexOnServer = 0;

        public RoleBox(boolean b) {
            super(b);
        }

        public void setServerIndex(int i) {
            indexOnServer = i;
            setSelectedIndex(i);
        }

        public void resetToServerIndex() {
            setSelectedIndex(indexOnServer);
        }
    }
}
