/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.uiuc.ncsa.cet.bean.PersonBean;

/**
 * @author Luigi Marini
 * 
 */
public class HomePage extends Page {

	private final FlexTable linksPanel;
	protected Widget userInfoTable;
	private SimplePanel flushPanel;

	/**
	 * 
	 * @param dispatchAsync
	 */
	public HomePage(DispatchAsync dispatchAsync) {
		super("Home", dispatchAsync);
		linksPanel = createLinksPanel();
		mainLayoutPanel.add(linksPanel);
		mainLayoutPanel.addStyleName("inline");
		flushPanel = new SimplePanel();
		flushPanel.addStyleName("clearBoth");
		mainLayoutPanel.add(flushPanel);
		getUserInfo();
		checkAdminPermissions();
	}

	private void checkAdminPermissions() {
		dispatchAsync.execute(new HasPermission(MMDB.sessionID,
				Permission.VIEW_ADMIN_PAGES),
				new AsyncCallback<HasPermissionResult>() {

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Error checking for admin privileges", caught);
					}

					@Override
					public void onSuccess(HasPermissionResult result) {
						if (result.isPermitted()) {
							linksPanel.setWidget(linksPanel.getRowCount(), 0,
									new Hyperlink("Modify Permissions", "modifyPermissions"));
							// FIXME
							linksPanel.setWidget(linksPanel.getRowCount(), 0,
									new Hyperlink("Run SPARQL query", "sparql"));
						}
					}
				});
	}

	/**
	 * 
	 */
	private void getUserInfo() {
		dispatchAsync.execute(new GetUser(MMDB.sessionID),
				new AsyncCallback<GetUserResult>() {

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Error get user", caught);
					}

					@Override
					public void onSuccess(GetUserResult result) {
						userInfoTable = createUserInfo(result.getPersonBean());
						mainLayoutPanel.insert(userInfoTable, 3);
					}
				});

	}

	/**
	 * 
	 * @param personBean
	 */
	protected Widget createUserInfo(PersonBean personBean) {
		FlexTable table = new FlexTable();
		table.addStyleName("homePageWidget");
		table.setText(0, 0, "Name:");
		table.setText(0, 1, personBean.getName());
		table.setText(1, 0, "Email:");
		table.setText(1, 1, personBean.getEmail());
		return table;
	}

	/**
	 * 
	 * @return
	 */
	private FlexTable createLinksPanel() {
		FlexTable table = new FlexTable();
		table.addStyleName("homePageWidget");
		table.setWidget(0, 0, new Hyperlink("Create New Password",
				"newPassword"));
		return table;
	}
}
