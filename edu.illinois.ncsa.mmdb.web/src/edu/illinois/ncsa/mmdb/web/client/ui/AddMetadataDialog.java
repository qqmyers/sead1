/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.uiuc.edu/
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
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Collection;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.common.Permission;

public class AddMetadataDialog extends DialogBox {

    private final PermissionUtil rbac;

    public AddMetadataDialog(String title, final Collection<String> batch, final DispatchAsync service, final HandlerManager eventBus) {
        super();

        setText(title);

        rbac = new PermissionUtil(service);
        final VerticalPanel thePanel = new VerticalPanel();
        thePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        thePanel.setWidth("550px");
        final Image pending = new Image("./images/loading-small-white.gif");
        thePanel.add(pending);

        final Button cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        //create relationship - widget (if allowed)
        rbac.doIfAllowed(Permission.EDIT_USER_METADATA, new PermissionCallback() {
            @Override
            public void onAllowed() {
                thePanel.remove(pending);
                AddMetadataWidget addMetadata = new AddMetadataWidget(batch, service, eventBus) {
                    protected void addValue() {
                        super.addValue(true);
                        AddMetadataDialog.this.hide();
                    }
                };
                addMetadata.showFields(true);
                thePanel.add(addMetadata);
                thePanel.add(cancelButton);
            }

            @Override
            public void onDenied() {
                thePanel.remove(pending);
                Label notAllowed = new Label("You do not have permission to add metadata");
                thePanel.add(notAllowed);
                thePanel.add(cancelButton);
            }
        });

        add(thePanel);
        center();
    }
}
