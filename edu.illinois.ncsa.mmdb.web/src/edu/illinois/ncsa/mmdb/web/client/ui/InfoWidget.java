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

import java.util.Set;
import java.util.SortedSet;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionsCallback;
import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.NamedThing;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetInfo;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetInfo.Type;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataValue;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;

/**
 * Create the panel containing the information about the dataset.
 * 
 * @return panel with information about the dataset.
 */
public class InfoWidget extends Composite {

    private final FlowPanel      panel;
    private final DispatchAsync  service;
    private final String         uri;
    private final PermissionUtil rbac;
    String                       creatorName;
    Anchor                       creatorLink;
    String                       httpString = "http://";
    Label                        creatorNameLabel;
    Label                        remainderLabel;

    /*public InfoWidget(DataSetBean data, DispatchAsync service) {*/

    private EditableLabel        fileNameLabel;
    private EditableLabel        mimeTypeLabel;
    private Label                categoryLabel;

    public InfoWidget(DatasetBean data, final DispatchAsync service) {
        this.service = service;
        this.uri = data.getUri();
        rbac = new PermissionUtil(service);

        panel = new FlowPanel();
        panel.addStyleName("datasetRightColSection");
        Label lbl = new Label("Info");
        lbl.addStyleName("datasetRightColHeading");
        panel.add(lbl);

        HorizontalPanel creatorPanel = new HorizontalPanel();
        VerticalPanel creatorDetailsPanel = new VerticalPanel();
        Label creatorLabel = new Label("Creator: ");
        creatorLabel.addStyleName("datasetRightColText");

        creatorNameLabel = new Label();
        creatorDetailsPanel.add(creatorNameLabel);

        creatorLink = new Anchor();
        creatorDetailsPanel.add(creatorLink);

        remainderLabel = new Label();
        creatorDetailsPanel.add(remainderLabel);

        creatorPanel.add(creatorLabel);
        creatorPanel.add(creatorDetailsPanel);

        /*ownerPanel.add(editableLbl);*/

        panel.add(creatorPanel);

        String filename = data.getFilename();
        addInfo("Filename", filename, panel, true, Type.FILENAME);

        String size = TextFormatter.humanBytes(data.getSize());
        addInfo("Size", size, panel, false, Type.NONE);

        String cat = ContentCategory.getCategory(data.getMimeType(), service);
        addInfo("Category", cat, panel, false, Type.CATEGORY);

        String type = data.getMimeType();
        addInfo("MIME\u00a0Type", type, panel, true, Type.MIMETYPE);

        // uploaded by
        lbl = new Label("Uploaded By: ");
        lbl.addStyleName("datasetRightColText");
        PersonBean creator = data.getCreator();
        if (creator != null) {
            lbl.setTitle(creator.getEmail());
            lbl.setText("Uploaded By: " + creator.getName());
            /*editableLbl.setText(creator.getName());*/
        }
        panel.add(lbl);

        String date = "";
        if (data.getDate() != null) {
            date += DateTimeFormat.getShortDateTimeFormat().format(data.getDate());
        }
        addInfo("Uploaded", date, panel, false, Type.NONE);

        // check permission to enable Filename's and MIME type's editor.
        rbac.withPermissions(uri, new PermissionsCallback() {
            @Override
            public void onPermissions(final HasPermissionResult p) {
                if (p.isPermitted(Permission.EDIT_METADATA)) {
                    if (fileNameLabel != null) {
                        fileNameLabel.setEditable(true);
                    }
                    if (mimeTypeLabel != null) {
                        mimeTypeLabel.setEditable(true);
                    }
                }
            }
        }, Permission.EDIT_METADATA);

        initWidget(panel);

        //START - Added by Ram on Nov.21, 2011
        //FIXME : Need to refresh automatically on adding creator metadata
        if (uri != null) {
            service.execute(new ListUserMetadataFields(), new AsyncCallback<ListUserMetadataFieldsResult>() {
                public void onFailure(Throwable caught) {
                    GWT.log("Error retrieving available list of User Specified Metadata fields", caught);
                }

                public void onSuccess(ListUserMetadataFieldsResult result) {

                    service.execute(new GetUserMetadataFields(uri), new AsyncCallback<GetUserMetadataFieldsResult>() {
                        public void onFailure(Throwable caught) {
                            GWT.log("Error retrieving User Specified Information", caught);
                        }

                        public void onSuccess(GetUserMetadataFieldsResult result) {
                            Set<String> predicates = result.getThingsOrderedByName().keySet();
                            if (predicates.size() == 0) {

                            } else {
                                for (String predicate : predicates ) {
                                    if (predicate.toLowerCase().contains("creator")) {

                                        SortedSet<UserMetadataValue> values = NamedThing.orderByName(result.getValues().get(predicate));
                                        if (!values.isEmpty()) {
                                            try {
                                                String creator = values.first().getName();
                                                String link = (creator != null && creator.contains(httpString)) ? creator.substring(creator.indexOf(httpString)) : "";
                                                creatorLink.setHref(link);
                                                creatorLink.setText(link);

                                                String remainder = link.contains(" ") ? link.substring(creatorLink.getText().indexOf(" ") + 1) : "";
                                                remainderLabel.setText(remainder);
                                                //creatorLink = creatorLink.substring(creatorLink.indexOf(" "));
                                                creatorName = creator.replace(link, "");

                                                creatorNameLabel.setText(creatorName);

                                            }
                                            catch (Exception ex) {
                                                GWT.log(ex.getMessage());
                                            }
                                        }
                                        break;
                                    }

                                }
                            }
                        }
                    });

                }

            });
        }

        /*        Label ownerLabel = new Label("Owner: ");
                final EditableLabelSuggestBox editableLbl = new EditableLabelSuggestBox("", true);
                editableLbl.addValueChangeHandler(new ValueChangeHandler<String>() {
                    public void onValueChange(final ValueChangeEvent<String> event) {
                        SetOwner change = new SetOwner(_uri, event.getValue());
                        _service.execute(change, new AsyncCallback<EmptyResult>() {
                            public void onFailure(Throwable caught) {
                                editableLbl.cancel();

                            }

                            public void onSuccess(EmptyResult result) {
                                String text = event.getValue();

                                editableLbl.setText(text);
                            }
                        });
                    }
                });*/
        //END - Added by Ram on Nov.21, 2011

    }

    void addInfo(String name, String value, Panel panel, boolean editable, final Type t) {
        if (value != null && !value.equals("")) {
            // if editable, use label for name and editable label for label
            if (editable) {
                HorizontalPanel hPanel = new HorizontalPanel();
                hPanel.addStyleName("datasetRightColText");
                // \u00a0 = non-breaking space 
                Label lbl = new Label(name + ":\u00a0");
                lbl.setTitle(lbl.getText());
                lbl.addStyleName("datasetRightColText");
                hPanel.add(lbl);
                final EditableLabel editlbl = new EditableLabel(value);
                // save editable label for filename and MIME type
                if (t == Type.FILENAME) {
                    fileNameLabel = editlbl;
                }
                if (t == Type.MIMETYPE) {
                    mimeTypeLabel = editlbl;
                }
                editlbl.setEditable(false);
                editlbl.getLabel().addStyleName("datasetRightColText_Value");
                editlbl.setEditableStyleName("datasetRightColText_Value");
                editlbl.addValueChangeHandler(new ValueChangeHandler<String>() {
                    public void onValueChange(final ValueChangeEvent<String> event) {
                        SetInfo change = new SetInfo(uri, event.getValue(), t);
                        if (t == Type.FILENAME || isValidMIMEType(event.getValue())) {
                            service.execute(change, new AsyncCallback<EmptyResult>() {
                                public void onFailure(Throwable caught) {
                                    editlbl.cancel();
                                }

                                public void onSuccess(EmptyResult result) {
                                    editlbl.setText(event.getValue());
                                    // immediately change Category if MIME type is changed
                                    if (t == Type.MIMETYPE && categoryLabel != null) {
                                        String cat = ContentCategory.getCategory(event.getValue(), service);
                                        categoryLabel.setText("Category:\u00a0" + cat);
                                    }
                                }
                            });
                        }
                    }
                });
                hPanel.add(editlbl);
                panel.add(hPanel);
            }
            // otherwise, use label for the whole line
            else {
                Label lbl = new Label(name + ":\u00a0" + value);
                if (t == Type.CATEGORY) {
                    categoryLabel = lbl;
                }
                lbl.setTitle(lbl.getText());
                lbl.addStyleName("datasetRightColText");
                panel.add(lbl);
            }
        }
    }

    public void add(Label lbl) {
        panel.add(lbl);

    }

    private boolean isValidMIMEType(String MIME) {
        // valid MIME type is X/Y 
        // where X = word that contains only a-z or A-Z
        // and Y = word that contains a-z, A-Z, 0-9, _, -, or .
        RegExp pattern = RegExp.compile("^([a-zA-Z]+)\\/([\\w|\\-|\\.]+)$");
        if (pattern.test(MIME)) {
            return true;
        }
        return false;
    }
}
