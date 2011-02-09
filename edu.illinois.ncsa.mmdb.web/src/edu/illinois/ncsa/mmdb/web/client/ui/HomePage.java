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
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.List;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRecentActivity;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRecentActivityResult;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * The home page is user specific. It contains a set of tabs to modify and view
 * user relevant information.
 * 
 * @author Luigi Marini
 * 
 */
public class HomePage extends Page {

    private static final int     MAX_DATASETS = 10;
    protected Widget             userInfoTable;
    private TabPanel             tabPanel;
    private FlowPanel            preferencesPanel;
    private FlowPanel            recentActivityPanel;
    private final PermissionUtil rbac;

    /**
     * Create an instance of home page.
     * 
     * @param dispatchAsync
     */
    public HomePage(DispatchAsync dispatchAsync) {
        super("Home", dispatchAsync);
        rbac = new PermissionUtil(dispatchAsync);
        createTabs();
        createRecentActivityTab();
        createSystemInfoTab();
        createProfileTab();
        //        createPreferencesTab();
        tabPanel.selectTab(0);
    }

    /**
     * A tab to include recently created datasets and collections.
     */
    private void createRecentActivityTab() {
        recentActivityPanel = new FlowPanel();
        tabPanel.add(recentActivityPanel, "Recent Activity");
        getRecentActivity();
    }

    /**
     * A tab to show system info
     */
    private void createSystemInfoTab() {
        tabPanel.add(new SystemInfoWidget(dispatchAsync, false), "System Info");
    }

    /**
     * Get recent activity from server.
     */
    private void getRecentActivity() {
        dispatchAsync.execute(new GetRecentActivity(MMDB.getUsername(), MAX_DATASETS), new AsyncCallback<GetRecentActivityResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error getting recent activity");
            }

            @Override
            public void onSuccess(GetRecentActivityResult result) {
                recentActivityPanel.clear();
                List<DatasetBean> datasets = result.getDatasets();
                if (datasets.size() == 0) {
                    recentActivityPanel.add(new HTML("No recent activity to report."));
                } else {
                    int num = datasets.size() < MAX_DATASETS ? datasets.size() : MAX_DATASETS;
                    recentActivityPanel.add(new Label("Showing latest " + num + " datasets you have uploaded"));
                    for (DatasetBean dataset : datasets ) {
                        recentActivityPanel.add(new DatasetInfoWidget(dataset, dispatchAsync));
                    }
                }
            }
        });
    }

    /**
     * A tab to view and set user preferences.
     */
    private void createPreferencesTab() {
        preferencesPanel = new FlowPanel();
        preferencesPanel.add(new HTML("Set preferences. (not implemented yet)"));
        tabPanel.add(preferencesPanel, "Preferences");
    }

    /**
     * Create the tab that includes profile information for the user.
     */
    private void createProfileTab() {
        tabPanel.add(new ProfileWidget(dispatchAsync), "Profile");
    }

    /**
     * Create tabbed section.
     */
    private void createTabs() {
        tabPanel = new TabPanel();
        tabPanel.setWidth("99%");
        mainLayoutPanel.add(tabPanel);
    }

    @Override
    public void layout() {
        // TODO Auto-generated method stub

    }
}
