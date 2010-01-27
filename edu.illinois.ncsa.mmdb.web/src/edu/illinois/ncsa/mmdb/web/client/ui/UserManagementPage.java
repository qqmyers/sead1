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

import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EditPermissions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EditPermissionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsers;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsersResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EditPermissions.PermissionActionType;
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

	private final FlowPanel mainPanel;

	private final Widget pageTitle;

	private final FlexTable usersTable;

	private final MyDispatchAsync dispatchAsync;

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
							usersTable.setText(usersTable.getRowCount() + 1, 0,
									"No users found.");
						}
						for (PersonBean user : users) {
							createRow(user);
						}
						enableCheckBoxes(users);
					}
				});

	}

	/**
	 * Check what permissions each user has and enable checkboxes.
	 * 
	 * @param users
	 */
	protected void enableCheckBoxes(ArrayList<PersonBean> users) {

		for (final PersonBean user : users) {

			// check regular membership
			dispatchAsync.execute(new HasPermission(user.getUri(),
					Permission.VIEW_MEMBER_PAGES),
					new AsyncCallback<HasPermissionResult>() {

						@Override
						public void onFailure(Throwable caught) {
							GWT.log("Error getting permission "
									+ Permission.VIEW_MEMBER_PAGES
									+ " for user " + user.getUri(), caught);
						}

						@Override
						public void onSuccess(HasPermissionResult result) {

							CheckBox checkBox = usersMemberCheckboxMap.get(user
									.getUri());
							if (result.isPermitted()) {
								if (checkBox != null) {
									GWT
											.log(
													"User "
															+ user.getUri()
															+ " has permission to view member pages",
													null);
									checkBox.setValue(true);
								}
							}
							checkBox.setEnabled(true);
						}
					});

			// check admin membership
			dispatchAsync.execute(new HasPermission(user.getUri(),
					Permission.VIEW_ADMIN_PAGES),
					new AsyncCallback<HasPermissionResult>() {

						@Override
						public void onFailure(Throwable caught) {
							GWT.log("Error getting permission "
									+ Permission.VIEW_ADMIN_PAGES
									+ " for user " + user.getUri(), caught);
						}

						@Override
						public void onSuccess(HasPermissionResult result) {

							CheckBox checkBox = usersAdminCheckboxMap.get(user
									.getUri());
							if (result.isPermitted()) {
								if (checkBox != null) {
									GWT
											.log(
													"User "
															+ user.getUri()
															+ " has permission to view admin pages",
													null);
									checkBox.setValue(true);
								}
							}
							checkBox.setEnabled(true);
						}
					});
		}
	}

	/**
	 * Create a single row in the table based on user.
	 * 
	 * @param user
	 */
	protected void createRow(final PersonBean user) {

		int row = usersTable.getRowCount() + 1;

		usersTable.setText(row, 0, user.getUri());

		// regular member
		final CheckBox memberCheckBox = new CheckBox();
		memberCheckBox.setEnabled(false);
		memberCheckBox.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if (memberCheckBox.getValue()) {
					modifyPermissions(user.getUri(),
							Permission.VIEW_MEMBER_PAGES,
							PermissionActionType.ADD, memberCheckBox);
				} else {
					modifyPermissions(user.getUri(),
							Permission.VIEW_MEMBER_PAGES,
							PermissionActionType.REMOVE, memberCheckBox);
				}
			}
		});
		usersTable.setWidget(row, 1, memberCheckBox);
		usersMemberCheckboxMap.put(user.getUri(), memberCheckBox);

		// admin group
		final CheckBox adminCheckBox = new CheckBox();
		adminCheckBox.setEnabled(false);
		adminCheckBox.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if (adminCheckBox.getValue()) {
					modifyPermissions(user.getUri(),
							Permission.VIEW_ADMIN_PAGES,
							PermissionActionType.ADD, adminCheckBox);
					CheckBox memberCheckBox = usersMemberCheckboxMap.get(user
							.getUri());
					modifyPermissions(user.getUri(),
							Permission.VIEW_MEMBER_PAGES,
							PermissionActionType.ADD, memberCheckBox);
				} else {
					if (lastAdmin()) {
						adminCheckBox.setValue(true);
						Window.alert("You cannot remove the last admin");
					} else {
						modifyPermissions(user.getUri(),
								Permission.VIEW_ADMIN_PAGES,
								PermissionActionType.REMOVE, adminCheckBox);
						CheckBox memberCheckBox = usersMemberCheckboxMap
								.get(user.getUri());
						modifyPermissions(user.getUri(),
								Permission.VIEW_MEMBER_PAGES,
								PermissionActionType.REMOVE, memberCheckBox);
					}
				}
			}
		});
		usersTable.setWidget(row, 2, adminCheckBox);
		usersAdminCheckboxMap.put(user.getUri(), adminCheckBox);
	}

	/**
	 * Check if the user is the last administrator
	 * 
	 * @return
	 */
	protected boolean lastAdmin() {
		int checked = 0;
		for (CheckBox checkbox : usersAdminCheckboxMap.values()) {
			if (checkbox.getValue()) {
				checked++;
			}
		}
		if (checked > 0) {
			return false;
		} else {
			return true;
		}
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
	protected void modifyPermissions(final String userUri,
			final Permission permission, final PermissionActionType type,
			final CheckBox checkbox) {

		EditPermissions editPermissions = new EditPermissions(userUri,
				permission, type);

		dispatchAsync.execute(editPermissions,
				new AsyncCallback<EditPermissionsResult>() {

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Error changing permissions", caught);
					}

					@Override
					public void onSuccess(EditPermissionsResult result) {
						// make sure checkbox is in proper state
						switch (type) {
						case ADD:
							checkbox.setValue(true);
							break;
						case REMOVE:
							checkbox.setValue(false);
							break;
						}
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
		flexTable.setText(0, 1, "Member");
		flexTable.setText(0, 2, "Admin");
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
