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

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionsCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAccessLevel;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAccessLevelResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetAccessLevel;
import edu.illinois.ncsa.mmdb.web.common.Permission;

/**
 * Create the panel containing the access level about the dataset.
 * 
 */
public class AccessLevelWidget extends Composite {
    private final FlowPanel      panel;
    private final DispatchAsync  service;
    private final String         uri;
    private final PermissionUtil rbac;
    private final Label          accessLabel;
    private final ListBox        accessLevel;

    public AccessLevelWidget(String uri, final DispatchAsync service) {
        this.service = service;
        this.uri = uri;
        rbac = new PermissionUtil(service);

        panel = new FlowPanel();
        panel.addStyleName("datasetRightColSection");

        accessLabel = new Label("Access Level");
        accessLabel.addStyleName("datasetRightColHeading");
        panel.add(accessLabel);

        HorizontalPanel hPanel = new HorizontalPanel();
        panel.add(hPanel);

        Label lbl = new Label("Current level:\u00a0");
        lbl.addStyleName("datasetRightColText");
        hPanel.add(lbl);

        accessLevel = new ListBox();
        accessLevel.setEnabled(false);
        accessLevel.addStyleName("datasetRightColListbox");
        hPanel.add(accessLevel);

        accessLevel.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                setAccessLevel();
            }
        });
        getAccessLevel();

        // check permission to enable Filename's and MIME type's editor.
        rbac.withPermissions(uri, new PermissionsCallback() {
            @Override
            public void onPermissions(final HasPermissionResult p) {
                if (p.isPermitted(Permission.EDIT_METADATA)) {
                    accessLevel.setEnabled(true);
                }
            }
        }, Permission.EDIT_METADATA);

        initWidget(panel);
    }

    private void getAccessLevel() {
        service.execute(new GetAccessLevel(uri), new AsyncCallback<GetAccessLevelResult>() {
            public void onFailure(Throwable caught) {
                accessLevel.setEnabled(false);
                GWT.log("Could not get accesslevel information", caught);
            }

            public void onSuccess(GetAccessLevelResult result) {
                accessLabel.setText(result.getLabel());
                accessLevel.clear();
                for (String x : result.getLevels() ) {
                    accessLevel.addItem(x);
                }
                accessLevel.setSelectedIndex(result.getDatasetLevel());
            }
        });
    }

    private void setAccessLevel() {
        service.execute(new SetAccessLevel(uri, accessLevel.getSelectedIndex()), new AsyncCallback<GetAccessLevelResult>() {
            public void onFailure(Throwable caught) {
                accessLevel.setEnabled(false);
                GWT.log("Could not get/set accesslevel information", caught);
            }

            public void onSuccess(GetAccessLevelResult result) {
                accessLabel.setText(result.getLabel());
                accessLevel.clear();
                for (String x : result.getLevels() ) {
                    accessLevel.addItem(x);
                }
                accessLevel.setSelectedIndex(result.getDatasetLevel());
            }
        });
    }
}
