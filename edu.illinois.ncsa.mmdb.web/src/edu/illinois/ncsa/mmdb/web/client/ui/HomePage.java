/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUser;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ReindexLucene;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ReindexLuceneResult;
import edu.uiuc.ncsa.cet.bean.PersonBean;

/**
 * The home page is user specific. It contains a set of tabs to modify and view
 * user relevant information.
 * 
 * @author Luigi Marini
 * 
 */
public class HomePage extends Page {

	protected Widget userInfoTable;
	private TabPanel tabPanel;
	private FlowPanel profilePanel;
	private FlowPanel preferencesPanel;
	private FlowPanel recentActivityPanel;
	private FlowPanel adminPanel;

	/**
	 * Create an instance of home page.
	 * 
	 * @param dispatchAsync
	 */
	public HomePage(DispatchAsync dispatchAsync) {
		super("Home", dispatchAsync);
		createTabs();
		createRecentActivityTab();
		createProfileTab();
		createPreferencesTab();
		getUserInfo();
		checkAdminPermissions();
		tabPanel.selectTab(0);
	}

	/**
	 * A tab to include recently created datasets and collections.
	 */
	private void createRecentActivityTab() {
		recentActivityPanel = new FlowPanel();
		recentActivityPanel.add(new HTML(
				"No recent activity to report. (not implemented yet)"));
		tabPanel.add(recentActivityPanel, "Recent Activity");
	}

	/**
	 * A tab to view and set user preferences.
	 */
	private void createPreferencesTab() {
		preferencesPanel = new FlowPanel();
		preferencesPanel
				.add(new HTML("Set preferences. (not implemented yet)"));
		tabPanel.add(preferencesPanel, "Preferences");
	}

	/**
	 * Create the tab that includes profile information for the user.
	 */
	private void createProfileTab() {
		profilePanel = new FlowPanel();
		tabPanel.add(profilePanel, "Profile");
	}

	/**
	 * Create tabbed section.
	 */
	private void createTabs() {
		tabPanel = new TabPanel();
		tabPanel.setWidth("99%");
		mainLayoutPanel.add(tabPanel);
	}

	/**
	 * Check if the user has admin permissions. If so show a tab with admin-level functions.
	 */
	private void checkAdminPermissions() {
		dispatchAsync.execute(new HasPermission(MMDB.getUsername(),
				Permission.VIEW_ADMIN_PAGES),
				new AsyncCallback<HasPermissionResult>() {

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Error checking for admin privileges", caught);
					}

					@Override
					public void onSuccess(HasPermissionResult result) {
						if (result.isPermitted()) {
							createAdminTab();
						}
					}
				});
	}

	/**
	 * Create a tab that includes things that only admins can do.
	 */
	protected void createAdminTab() {
		adminPanel = new FlowPanel();
		Hyperlink permissionsLink = new Hyperlink("Modify Permissions", "modifyPermissions");
		adminPanel.add(permissionsLink);
		Hyperlink sparqlLink = new Hyperlink("Run SPARQL Query", "sparql");
		adminPanel.add(sparqlLink);
		adminPanel.add(new Label("Update Context"));
		final Anchor luceneAnchor = new Anchor("Reindex Lucene");
		luceneAnchor.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				luceneAnchor.setEnabled(false);
				luceneAnchor.setText("Requesting...");
				dispatchAsync.execute(new ReindexLucene(), new AsyncCallback<ReindexLuceneResult>() {
					public void onFailure(Throwable caught) {
						new ConfirmDialog("Error","Error reindexing");
						luceneAnchor.setText("Reindex Lucene");
						luceneAnchor.setEnabled(true);
					}
					public void onSuccess(ReindexLuceneResult result) {
						new ConfirmDialog("Started","Queued "+result.getNumberQueued()+" dataset(s) for reindexing");
						luceneAnchor.setText("Reindex Lucene");
						luceneAnchor.setEnabled(true);
					}
				});
			}
		});
		adminPanel.add(luceneAnchor);
		tabPanel.add(adminPanel, "Administrator");
	}

	/**
	 * Get basic user info and add it to the profile tab.
	 */
	private void getUserInfo() {
		dispatchAsync.execute(new GetUser(MMDB.getUsername()),
				new AsyncCallback<GetUserResult>() {

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Error get user", caught);
					}

					@Override
					public void onSuccess(GetUserResult result) {
						userInfoTable = createUserInfo(result.getPersonBean());
						profilePanel.add(userInfoTable);
					}
				});

	}

	/**
	 * Layout information about the user.
	 * @param personBean
	 */
	protected Widget createUserInfo(PersonBean personBean) {
		FlexTable table = new FlexTable();
		table.setText(0, 0, "Name:");
		table.setText(0, 1, personBean.getName());
		table.getCellFormatter().addStyleName(0, 0, "homePageWidgetRow");
		table.setText(1, 0, "Email:");
		table.setText(1, 1, personBean.getEmail());
		table.getCellFormatter().addStyleName(1, 0, "homePageWidgetRow");
		table.setWidget(2, 0, new Hyperlink("Request New Password",
		"newPassword"));
		table.getFlexCellFormatter().setColSpan(2, 0, 2);
		table.getCellFormatter().addStyleName(2, 0, "homePageWidgetRow");
		return table;
	}

	@Override
	public void layout() {
		// TODO Auto-generated method stub

	}
}
