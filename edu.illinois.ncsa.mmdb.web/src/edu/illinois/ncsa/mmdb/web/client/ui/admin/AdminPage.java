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
package edu.illinois.ncsa.mmdb.web.client.ui.admin;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.ui.Page;
import edu.illinois.ncsa.mmdb.web.common.Permission;

/**
 * The home page is admin specific. It contains a set of tabs to modify and view
 * admin relevant information.
 * 
 * @author Rob Kooper
 * 
 */
public class AdminPage extends Page {
    private final PermissionUtil rbac;

    /**
     * Create an instance of home page.
     * 
     * @param dispatchAsync
     */
    public AdminPage(DispatchAsync dispatch, HandlerManager eventBus) {
        super("Administration", dispatch, eventBus);
        rbac = new PermissionUtil(dispatch);

        HasPermission permission = new HasPermission(MMDB.getUsername(), Permission.VIEW_ADMIN_PAGES, Permission.EDIT_ROLES, Permission.REINDEX_FULLTEXT);
        dispatch.execute(permission, new AsyncCallback<HasPermissionResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error checking for admin privileges", caught);
            }

            @Override
            public void onSuccess(HasPermissionResult permissions) {
                if (permissions.isPermitted(Permission.VIEW_ADMIN_PAGES)) {
                    createTabPanel(permissions);
                } else {
                    Label label = new Label("No administrative privileges.");
                    label.setWidth("99%");
                    mainLayoutPanel.add(label);
                }
            }
        });
    }

    private void createTabPanel(HasPermissionResult permissions) {
        final TabPanel tabPanel = new TabPanel();
        tabPanel.setWidth("99%");
        mainLayoutPanel.add(tabPanel);

        // Configuration
        tabPanel.add(new ConfigurationWidget(dispatchAsync, permissions), "Configuration");

        // Users and permissions
        if (permissions.isPermitted(Permission.EDIT_ROLES)) {
            // users
            tabPanel.insert(new UserManagementWidget(dispatchAsync), "Users", 1);

            // Permissions
            tabPanel.insert(new RoleAdministrationWidget(dispatchAsync), "Permissions", 2);
        }

        // System information
        tabPanel.add(new SystemInfoWidget(dispatchAsync, false), "System Info");

        // SPARQL Queries
        tabPanel.add(new SparqlWidget(dispatchAsync), "SPARQL");

        tabPanel.selectTab(0);
    }

    @Override
    public void layout() {
    }
}
