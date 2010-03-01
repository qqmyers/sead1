/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
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
							int row = linksPanel.getRowCount();
							linksPanel.setWidget(row, 0,
									new Hyperlink("Modify Permissions", "modifyPermissions"));
							linksPanel.getCellFormatter().addStyleName(row, 0, "homePageWidgetRow");
							row = linksPanel.getRowCount();
							// FIXME
							linksPanel.setWidget(row, 0,
									new Hyperlink("Run SPARQL query", "sparql"));
							linksPanel.getCellFormatter().addStyleName(row, 0, "homePageWidgetRow");
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
		Label title = new Label("Profile");
		title.addStyleName("homePageWidgetTitle");
		table.setWidget(0, 0, title);
		table.setText(1, 0, "Name:");
		table.setText(1, 1, personBean.getName());
		table.getCellFormatter().addStyleName(1, 0, "homePageWidgetRow");
		table.setText(2, 0, "Email:");
		table.setText(2, 1, personBean.getEmail());
		table.getCellFormatter().addStyleName(2, 0, "homePageWidgetRow");
		return table;
	}

	/**
	 * 
	 * @return
	 */
	private FlexTable createLinksPanel() {
		FlexTable table = new FlexTable();
		table.addStyleName("homePageWidget");
		Label title = new Label("Links");
		title.addStyleName("homePageWidgetTitle");
		table.setWidget(0, 0, title);
		table.setWidget(1, 0, new Hyperlink("Create New Password",
				"newPassword"));
		table.getCellFormatter().addStyleName(1, 0, "homePageWidgetRow");
		return table;
	}

	@Override
	public void layout() {
		// TODO Auto-generated method stub
		
	}
}
