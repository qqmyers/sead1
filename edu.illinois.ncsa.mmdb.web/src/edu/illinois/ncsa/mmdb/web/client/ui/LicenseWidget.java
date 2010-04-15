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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetLicense;
import edu.illinois.ncsa.mmdb.web.client.dispatch.LicenseResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetLicense;

/**
 * Shows the current license and allows the user to edit the license.
 * 
 * @author Rob Kooper
 * 
 */
public class LicenseWidget extends Composite {

    private final MyDispatchAsync service;
    private final String          resource;
    private LicenseResult         license;

    //private final Image           licenseIcon;
    private final Anchor          licenseText;
    private final Anchor          licenseEdit;
    private final RadioButton     limited;
    private final RadioButton     cc;
    private final TextBox         rightsHolder;
    private final CheckBox        allowRemixing;
    private final CheckBox        allowCommercial;
    private final CheckBox        shareAlike;
    private final TextBox         rights;
    private final TextBox         licenseURL;
    private final CheckBox        allowDownload;
    private final Label           attribution;
    private final Label           lblRights;
    private final Label           lblLicense;

    public LicenseWidget(String resource, MyDispatchAsync service) {
        this(resource, service, true);
    }

    public LicenseWidget(String resource, MyDispatchAsync service, boolean withTitle) {
        this.resource = resource;
        this.service = service;

        // edit panel
        final VerticalPanel licenseEditor = new VerticalPanel();

        // main panel with title
        final VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.addStyleName("datasetRightColSection");
        initWidget(mainPanel);

        if (withTitle) {
            Label title = new Label("License");
            title.addStyleName("datasetRightColHeading");
            mainPanel.add(title);
        }

        attribution = new Label();
        mainPanel.add(attribution);

        licenseText = new Anchor("");
        licenseText.setTarget("_blank");
        mainPanel.add(licenseText);

        // allow user to edit the license
        licenseEdit = new Anchor("Edit");
        mainPanel.add(licenseEdit);

        licenseEdit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                mainPanel.remove(attribution);
                mainPanel.remove(licenseText);
                mainPanel.remove(licenseEdit);
                mainPanel.add(licenseEditor);
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

        Label lbl = new Label("Rights Holder");
        licenseEditor.add(lbl);
        rightsHolder = new TextBox();
        rightsHolder.setWidth("200px");
        licenseEditor.add(rightsHolder);

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
                setLicense();
                showLicense();
                mainPanel.remove(licenseEditor);
                mainPanel.add(attribution);
                mainPanel.add(licenseText);
                mainPanel.add(licenseEdit);
            }
        });

        Anchor cancelLink = new Anchor("Cancel");
        cancelLink.addStyleName("licenseIndent");
        buttons.add(cancelLink);
        cancelLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showLicense();
                mainPanel.remove(licenseEditor);
                mainPanel.add(attribution);
                mainPanel.add(licenseText);
                mainPanel.add(licenseEdit);
            }
        });

        // get the license
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
    }

    private void setLicense() {
        final LicenseResult oldLicense = license;

        license = new LicenseResult();
        if (!rightsHolder.getText().equals("")) {
            license.setRightsHolder(rightsHolder.getText());
        }

        if (cc.getValue()) {
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
            license.setRights(rights.getText());
            license.setLicense(licenseURL.getText());
            license.setAllowDownload(allowDownload.getValue());
        }

        service.execute(new SetLicense(resource, license), new AsyncCallback<EmptyResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error setting license", caught);
                license = oldLicense;
                showLicense();
            }

            @Override
            public void onSuccess(EmptyResult result) {
            }
        });
    }

    private void showLicense() {
        // rights
        String rights = license.getRights().toLowerCase();
        if ("cc-by".equals(rights) || "cc-by-sa".equals(rights) || "cc-by-nd".equals(rights) || "cc-by-nc".equals(rights) || "cc-by-nc-sa".equals(rights) || "cc-by-nc-nd".equals(rights)) {
            // attribution
            if (license.getRightsHolder() == null) {
                attribution.setVisible(false);
            } else {
                attribution.setVisible(true);
                attribution.setText("By : " + license.getRightsHolder());
            }

            // Creative Commons License
            Image icon = new Image("/images/" + rights + ".png");
            licenseText.setText("");
            licenseText.getElement().appendChild(icon.getElement());
            licenseText.setHref("http://creativecommons.org/licenses/" + rights.substring(3) + "/3.0");

            // editor panel
            cc.setValue(true);

            lblRights.setVisible(false);
            this.rights.setVisible(false);
            this.rights.setText("Copyright");

            lblLicense.setVisible(false);
            licenseURL.setVisible(false);
            licenseURL.setText("");

            allowDownload.setVisible(false);
            allowDownload.setValue(true);

            allowCommercial.setVisible(true);
            allowCommercial.setValue(!rights.contains("nc"));

            allowRemixing.setVisible(true);
            allowRemixing.setValue(!rights.contains("nd"));

            shareAlike.setVisible(true);
            shareAlike.setValue(rights.contains("sa"));
            shareAlike.setEnabled(allowRemixing.getValue());

        } else {
            // attribution
            if (license.getRightsHolder() == null) {
                attribution.setVisible(false);
            } else {
                attribution.setVisible(true);
                attribution.setText("By : " + license.getRightsHolder());
            }

            // Other License
            licenseText.setText(license.getRights());
            if ((license.getLicense() != null) && license.getLicense().startsWith("http://")) {
                licenseText.setHref(license.getLicense());
            } else {
                licenseText.setHref(null);
            }

            // editor panel
            limited.setValue(true);

            lblRights.setVisible(true);
            this.rights.setVisible(true);
            this.rights.setText(license.getRights());

            lblLicense.setVisible(true);
            licenseURL.setVisible(true);
            licenseURL.setText(license.getLicense());

            allowDownload.setVisible(true);
            allowDownload.setValue(license.isAllowDownload());

            allowCommercial.setVisible(false);
            allowCommercial.setValue(true);

            allowRemixing.setVisible(false);
            allowRemixing.setValue(true);

            shareAlike.setVisible(false);
            shareAlike.setValue(false);
            shareAlike.setEnabled(allowRemixing.getValue());

        }

        // license holder
        rightsHolder.setText(license.getRightsHolder());

        // allow downloading original only applicable for non Creative Commons License
        //allowDownload.setChecked(result.isAllowDownload());
    }
}
