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
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.BatchResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetLicense;
import edu.illinois.ncsa.mmdb.web.client.dispatch.LicenseResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetLicense;
import edu.illinois.ncsa.mmdb.web.client.event.BatchCompletedEvent;

/**
 * Shows the current license and allows the user to edit the license.
 * 
 * @author Rob Kooper
 * 
 */
// FIXME refactor and provide license-display-only widget that can be separated from the editing code
// and an editor that fires handleable events
public class LicenseWidget extends Composite {

    private final MyDispatchAsync    service;
    private final Collection<String> resources;
    private LicenseResult            license;
    private HandlerManager           eventBus;

    private FlowPanel                mainPanel;
    private Anchor                   licenseText;
    private Label                    attribution;

    private VerticalPanel            licenseEditor;
    private Anchor                   licenseEdit;
    private RadioButton              pd;
    private RadioButton              cc;
    private RadioButton              limited;
    private TextBox                  rightsHolder;
    private CheckBox                 allowRemixing;
    private CheckBox                 allowCommercial;
    private CheckBox                 shareAlike;
    private TextBox                  rights;
    private TextBox                  licenseURL;
    private CheckBox                 allowDownload;
    private Label                    lblRights;
    private Label                    lblLicense;
    private CheckBox                 myData;
    private Label                    lblRightsHolder;

    public LicenseWidget(Collection<String> batch, MyDispatchAsync service, HandlerManager eventBus, boolean showTitle, boolean canEdit, boolean showEdit) {
        this.resources = batch;
        this.service = service;
        this.eventBus = eventBus;
        init(showTitle, canEdit, showEdit);
    }

    public LicenseWidget(String resource, MyDispatchAsync service, boolean showTitle, boolean canEdit, boolean showEdit) {
        this.resources = new HashSet<String>();
        this.resources.add(resource);
        this.service = service;
        init(showTitle, canEdit, showEdit);
    }

    public void showEditor(boolean showEdit) {
        if (licenseEdit == null) {
            return;
        }
        if (showEdit) {
            mainPanel.remove(attribution);
            mainPanel.remove(licenseText);
            mainPanel.remove(licenseEdit);
            mainPanel.add(licenseEditor);
        } else {
            mainPanel.remove(licenseEditor);
            mainPanel.add(attribution);
            mainPanel.add(licenseText);
            mainPanel.add(licenseEdit);
        }
    }

    private void init(boolean showTitle, boolean canEdit, boolean showEdit) {
        // edit panel
        licenseEditor = new VerticalPanel();

        // main panel with title
        mainPanel = new FlowPanel();
        mainPanel.addStyleName("datasetRightColSection");
        initWidget(mainPanel);

        if (showTitle) {
            Label title = new Label("License");
            title.addStyleName("datasetRightColHeading");
            mainPanel.add(title);
        }

        licenseText = new Anchor("");
        licenseText.setTarget("_blank");
        licenseText.setStyleName("datasetRightColText");
        licenseText.addStyleName("anchorBlock");
        mainPanel.add(licenseText);

        attribution = new Label();
        mainPanel.add(attribution);

        // allow user to edit the license
        if (canEdit) {
            editPanel();
            showEditor(showEdit);
        }

        // get the license
        if (resources.size() == 1) {
            String resource = resources.iterator().next();
            service.execute(new GetLicense(resource), new AsyncCallback<LicenseResult>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Error retrieving license", caught);
                }

                @Override
                public void onSuccess(LicenseResult result) {
                    license = result;
                    showLicense();
                }
            });
        } else {
            license = new LicenseResult();
            showLicense();
        }
    }

    private void editPanel() {
        licenseEdit = new Anchor("Edit");
        licenseEdit.addStyleName("anchorBlock");
        mainPanel.add(licenseEdit);

        licenseEdit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showEditor(true);
            }
        });

        // Rights Holder [ text ]                        -- http://purl.org/dc/terms/rightsHolder
        //  o Limited copyright
        //    Short text [ All rights reserved ]         -- http://purl.org/dc/terms/rights
        //    URL        [ url to full license text ]    -- http://purl.org/dc/terms/license
        //    x Allow downloading original               -- cet:mmdb/allowDownload
        //  o Creative Commons License                   -- http://purl.org/dc/terms/rights
        //    x [icon] Allow remixing                       http://purl.org/dc/terms/license
        //    x [icon] Allow commercial use
        //    x [icon] Require share-alike

        // in case of cc the rights wil be cc-by-etc same as icon

        myData = new CheckBox("I own the rights.");
        myData.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                lblRightsHolder.setVisible(!myData.getValue());
                rightsHolder.setVisible(!myData.getValue());
            }
        });
        licenseEditor.add(myData);
        lblRightsHolder = new Label("Rights Holder");
        licenseEditor.add(lblRightsHolder);
        rightsHolder = new TextBox();
        rightsHolder.setWidth("200px");
        licenseEditor.add(rightsHolder);

        // Public domain
        pd = new RadioButton("license", "Public Domain");
        pd.setStyleName("licenseButton");
        licenseEditor.add(pd);
        pd.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                allowRemixing.setVisible(false);
                allowCommercial.setVisible(false);
                shareAlike.setVisible(false);
                lblRights.setVisible(false);
                rights.setVisible(false);
                lblLicense.setVisible(false);
                licenseURL.setVisible(false);
                allowDownload.setVisible(false);
            }
        });

        // Edit Creative Commons License
        cc = new RadioButton("license", "Creative Commons");
        cc.setStyleName("licenseButton");
        licenseEditor.add(cc);
        cc.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                allowRemixing.setVisible(true);
                allowCommercial.setVisible(true);
                shareAlike.setVisible(true);
                lblRights.setVisible(false);
                rights.setVisible(false);
                lblLicense.setVisible(false);
                licenseURL.setVisible(false);
                allowDownload.setVisible(false);
            }
        });

        allowCommercial = new CheckBox("Allow commercial use");
        allowCommercial.setStyleName("licenseIndent");
        licenseEditor.add(allowCommercial);

        allowRemixing = new CheckBox("Allow remixing");
        allowRemixing.setStyleName("licenseIndent");
        allowRemixing.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                shareAlike.setEnabled(allowRemixing.getValue());
            }
        });
        licenseEditor.add(allowRemixing);
        shareAlike = new CheckBox("Require share-alike");
        shareAlike.setStyleName("licenseIndent");
        licenseEditor.add(shareAlike);

        // Edit limited copyright
        limited = new RadioButton("license", "Limited Copyright");
        limited.setStyleName("licenseButton");
        licenseEditor.add(limited);
        limited.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                allowRemixing.setVisible(false);
                allowCommercial.setVisible(false);
                shareAlike.setVisible(false);
                lblRights.setVisible(true);
                rights.setVisible(true);
                lblLicense.setVisible(true);
                licenseURL.setVisible(true);
                allowDownload.setVisible(true);
            }
        });

        lblRights = new Label("Rights");
        lblRights.setStyleName("licenseIndent");
        licenseEditor.add(lblRights);
        rights = new TextBox();
        rights.setWidth("180px");
        rights.setStyleName("licenseIndent");
        licenseEditor.add(rights);

        lblLicense = new Label("License URL");
        lblLicense.setStyleName("licenseIndent");
        licenseEditor.add(lblLicense);
        licenseURL = new TextBox();
        licenseURL.setWidth("180px");
        licenseURL.setStyleName("licenseIndent");
        licenseEditor.add(licenseURL);

        allowDownload = new CheckBox("Allow downloading?");
        allowDownload.setStyleName("licenseIndent");
        licenseEditor.add(allowDownload);

        // OK / Cancel buttons
        FlowPanel buttons = new FlowPanel();
        licenseEditor.add(buttons);

        Anchor okLink = new Anchor("OK");
        buttons.add(okLink);
        okLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onOK();
            }
        });

        Anchor cancelLink = new Anchor("Cancel");
        cancelLink.addStyleName("licenseIndent");
        buttons.add(cancelLink);
        cancelLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onCancel();
            }
        });
    }

    /**
     * Called when the user presses the ok button
     */
    protected void onOK() {
        if (licenseEdit == null) {
            return;
        }
        setLicense();
        showLicense();
        showEditor(false);
    }

    /**
     * Called when the user presses the cancel button
     */
    protected void onCancel() {
        if (licenseEdit == null) {
            return;
        }
        showLicense();
        showEditor(false);
    }

    /**
     * Save the values in the edit box to the backend.
     */
    private void setLicense() {
        if (licenseEdit == null) {
            return;
        }
        final LicenseResult oldLicense = license;

        license = new LicenseResult();
        if (myData.getValue()) {
            license.setRightsHolderUri(MMDB.getUsername());
            license.setRightsHolder(MMDB.getSessionState().getCurrentUser().getName());
        } else if (!rightsHolder.getText().equals("")) {
            license.setRightsHolder(rightsHolder.getText());
            if (oldLicense != null && (oldLicense.getRightsHolderUri() != null) && oldLicense.getRightsHolder().equals(rightsHolder.getText())) {
                license.setRightsHolderUri(oldLicense.getRightsHolderUri());
            }
        }

        if (pd.getValue()) {
            license.setRights("pddl");
            license.setLicense("http://www.opendatacommons.org/licenses/pddl/summary/");

        } else if (cc.getValue()) {
            String rights = "cc-by";
            if (!allowCommercial.getValue()) {
                rights += "-nc";
            }
            if (!allowRemixing.getValue()) {
                rights += "-nd";
            } else if (shareAlike.getValue()) {
                rights += "-sa";
            }
            license.setRights(rights);
            license.setLicense("http://creativecommons.org/licenses/" + rights.substring(3) + "/3.0");

        } else {
            if (!rights.getText().equals("")) {
                license.setRights(rights.getText());
            }
            if (!licenseURL.getText().equals("")) {
                license.setLicense(licenseURL.getText());
            }
            license.setAllowDownload(allowDownload.getValue());
        }

        final BatchCompletedEvent done = new BatchCompletedEvent();
        // fix me modify the bat
        service.execute(new SetLicense(resources, license), new AsyncCallback<BatchResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error setting license", caught);
                license = oldLicense;
                showLicense();
                for (String r : resources ) {
                    done.setFailure(r, "failed: unknown reason");
                }
                if (eventBus != null) {
                    eventBus.fireEvent(done);
                }
            }

            @Override
            public void onSuccess(BatchResult result) {
                for (String r : result.getSuccesses() ) {
                    done.addSuccess(r);
                }
                for (Map.Entry<String, String> failure : result.getFailures().entrySet() ) {
                    done.setFailure(failure.getKey(), failure.getValue());
                }
                if (eventBus != null) {
                    eventBus.fireEvent(done);
                }
            }
        });
    }

    /**
     * Parse the license, fill in the display box as well as the edit box based
     * on the values.
     */
    private void showLicense() {
        if (license == null) { // nothing to show
            return;
        }

        // attribution
        if (license.getRightsHolder() == null) {
            attribution.setText("");
            attribution.setVisible(false);
        } else {
            attribution.setText("By : " + license.getRightsHolder());
            attribution.setVisible(true);
        }

        // check to see the license type
        String rights = license.getRights().toLowerCase();
        if (rights.equals("pddl")) {
            // Public Domain License
            Image icon = new Image("images/cc-pd.png");
            licenseText.setText("");
            licenseText.getElement().appendChild(icon.getElement());
            licenseText.setHref(license.getLicense());

        } else if ("cc-by".equals(rights) || "cc-by-sa".equals(rights) || "cc-by-nd".equals(rights) || "cc-by-nc".equals(rights) || "cc-by-nc-sa".equals(rights) || "cc-by-nc-nd".equals(rights)) {
            // Creative Commons License
            Image icon = new Image("images/" + rights + ".png");
            icon.addStyleName("imageNoBorder");
            licenseText.setText("");
            licenseText.getElement().appendChild(icon.getElement());
            licenseText.setHref(license.getLicense());

        } else {
            // Other License
            licenseText.setText(license.getRights());
            if ((license.getLicense() != null) && license.getLicense().startsWith("http://")) {
                licenseText.setHref(license.getLicense());
            } else {
                licenseText.setHref("javascript:;");
            }
        }

        // rest is edit part
        if (licenseEdit == null) {
            return;
        }

        // rightsholder
        if (MMDB.getUsername().equals(license.getRightsHolderUri())) {
            myData.setValue(true);
            lblRightsHolder.setVisible(false);
            rightsHolder.setVisible(false);
        } else {
            myData.setValue(false);
            lblRightsHolder.setVisible(true);
            rightsHolder.setVisible(true);
            if (license.getRightsHolder() == null) {
                rightsHolder.setText("");
            } else {
                rightsHolder.setText(license.getRightsHolder());
            }
        }

        if (rights.equals("pddl")) {
            // Public Domain License
            pd.setValue(true);

            allowCommercial.setVisible(false);
            allowCommercial.setValue(true);

            allowRemixing.setVisible(false);
            allowRemixing.setValue(true);

            shareAlike.setVisible(false);
            shareAlike.setValue(false);
            shareAlike.setEnabled(allowRemixing.getValue());

            lblRights.setVisible(false);
            this.rights.setVisible(false);
            this.rights.setText("All Rights Reserved");

            lblLicense.setVisible(false);
            licenseURL.setVisible(false);
            licenseURL.setText("");

            allowDownload.setVisible(false);
            allowDownload.setValue(true);

        } else if ("cc-by".equals(rights) || "cc-by-sa".equals(rights) || "cc-by-nd".equals(rights) || "cc-by-nc".equals(rights) || "cc-by-nc-sa".equals(rights) || "cc-by-nc-nd".equals(rights)) {
            // Creative Commons License
            cc.setValue(true);

            allowCommercial.setVisible(true);
            allowCommercial.setValue(!rights.contains("nc"));

            allowRemixing.setVisible(true);
            allowRemixing.setValue(!rights.contains("nd"));

            shareAlike.setVisible(true);
            shareAlike.setValue(rights.contains("sa"));
            shareAlike.setEnabled(allowRemixing.getValue());

            lblRights.setVisible(false);
            this.rights.setVisible(false);
            this.rights.setText("All Rights Reserved");

            lblLicense.setVisible(false);
            licenseURL.setVisible(false);
            licenseURL.setText("");

            allowDownload.setVisible(false);
            allowDownload.setValue(true);

        } else {
            // Other License
            licenseText.setText(license.getRights());
            if ((license.getLicense() != null) && license.getLicense().startsWith("http://")) {
                licenseText.setHref(license.getLicense());
            } else {
                licenseText.setHref(null);
            }

            // editor panel
            limited.setValue(true);

            allowCommercial.setVisible(false);
            allowCommercial.setValue(true);

            allowRemixing.setVisible(false);
            allowRemixing.setValue(true);

            shareAlike.setVisible(false);
            shareAlike.setValue(false);
            shareAlike.setEnabled(allowRemixing.getValue());

            lblRights.setVisible(true);
            this.rights.setVisible(true);
            this.rights.setText(license.getRights());

            lblLicense.setVisible(true);
            licenseURL.setVisible(true);
            licenseURL.setText(license.getLicense());

            allowDownload.setVisible(true);
            allowDownload.setValue(license.isAllowDownload());
        }
    }
}
