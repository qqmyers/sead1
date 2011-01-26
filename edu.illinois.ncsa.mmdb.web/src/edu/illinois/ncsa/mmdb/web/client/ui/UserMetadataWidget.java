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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetUserMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataField;

public class UserMetadataWidget extends Composite {
    String              uri;
    MyDispatchAsync     dispatch;
    LabeledListBox      fieldChoice;
    TextBox             valueText;
    VerticalPanel       thePanel;
    Label               noFields;
    FlexTable           fieldTable;
    Map<String, String> labels = new HashMap<String, String>();

    public UserMetadataWidget(String uri, MyDispatchAsync dispatch) {
        this.uri = uri;
        this.dispatch = dispatch;

        fieldTable = new FlexTable();
        fieldTable.setWidth("100%");
        fieldTable.addStyleName("metadataTable");
        fieldTable.getColumnFormatter().setWidth(0, "25%");
        fieldTable.getColumnFormatter().setWidth(1, "30%");
        fieldTable.getColumnFormatter().setWidth(2, "25%");
        fieldTable.getColumnFormatter().setWidth(3, "20%");

        thePanel = new VerticalPanel();
        noFields = new Label("No user specified metadata");
        noFields.addStyleName("noMetadata");
        noFields.addStyleName("metadataTableCell");
        noFields.addStyleName("hidden");
        thePanel.add(noFields);
        thePanel.add(fieldTable);

        initWidget(thePanel);
    }

    public void showFields(final boolean canEdit) {
        // FIXME single get to get fields and values
        dispatch.execute(new ListUserMetadataFields(), new AsyncCallback<ListUserMetadataFieldsResult>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(ListUserMetadataFieldsResult result) {
                SortedSet<UserMetadataField> availableFields = result.getFieldsSortedByName();
                GWT.log("available fields: " + availableFields);
                if (availableFields.size() > 0) {
                    if (canEdit) {
                        addFieldAddControls(availableFields);
                    }
                    dispatch.execute(new GetUserMetadataFields(uri), new AsyncCallback<GetUserMetadataFieldsResult>() {
                        public void onFailure(Throwable caught) {
                        }

                        public void onSuccess(GetUserMetadataFieldsResult result) {
                            Set<String> predicates = result.getThingsOrderedByName().keySet();
                            if (predicates.size() == 0) {
                                addNoFields();
                            } else {
                                for (String predicate : predicates ) {
                                    String label = result.getThingNames().get(predicate);
                                    addNewField(predicate, label, result.getValues().get(predicate), canEdit);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 
     * @param predicate
     * @param label
     * @param value
     */
    private void setProperty(String predicate, String label, String value) {
        Set<String> v = new HashSet<String>();
        v.add(value);
        addNewField(predicate, label, v, true);
    }

    /** number of fields set */
    private int getFieldCount() {
        return fieldTable.getRowCount();
    }

    /**
     * 
     * @param predicate
     * @return
     */
    private int getRowForField(String predicate) {
        for (int row = 0; row < fieldTable.getRowCount(); row++ ) {
            Label l = (Label) fieldTable.getWidget(row, 0);
            if (predicate.equals(l.getTitle())) {
                return row;
            }
        }
        return -1;
    }

    /**
     * 
     * @param predicate
     * @param label
     * @param values
     */
    private void addNewField(final String predicate, String label, Collection<String> values, boolean canEdit) {

        removeNoFields();
        int row = getRowForField(predicate);
        if (row == -1) {
            row = fieldTable.getRowCount();
            fieldTable.insertRow(row);
        }

        Label predicateLabel = new Label(label);
        predicateLabel.setTitle(predicate);
        fieldTable.setWidget(row, 0, predicateLabel);
        VerticalPanel panel = new VerticalPanel();
        for (String value : values ) {
            panel.add(new Label(value));
        }
        fieldTable.setWidget(row, 1, panel);
        if (canEdit) {
            HorizontalPanel linkPanel = new HorizontalPanel();
            Anchor editAnchor = new Anchor("Edit");
            editAnchor.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    //editValue(predicate);
                }
            });
            linkPanel.add(editAnchor);
            Anchor removeAnchor = new Anchor("Remove");
            removeAnchor.addStyleName("multiAnchor");
            removeAnchor.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    removeValue(predicate);
                }
            });
            linkPanel.add(removeAnchor);
            fieldTable.setWidget(row, 3, linkPanel);
        }
        styleRows();

    }

    void addNoFields() {
        if (fieldTable.getRowCount() > 0) {
            fieldTable.removeRow(0); //header row
        }
        noFields.removeStyleName("hidden");

    }

    void removeNoFields() {
        noFields.addStyleName("hidden");
        fieldTable.setWidget(0, 0, new Label("Field"));
        fieldTable.setWidget(0, 1, new Label("Value"));
        fieldTable.setWidget(0, 2, new Label("Applies To"));
        fieldTable.setWidget(0, 3, new Label("Actions"));
    }

    /**
     * 
     * @param availableFields
     */
    private void addFieldAddControls(SortedSet<UserMetadataField> availableFields) {
        // result is field -> label, sorted by alpha label
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.addStyleName("addMetadata");
        horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        thePanel.add(horizontalPanel);

        fieldChoice = new LabeledListBox("Add Field:");
        for (UserMetadataField field : availableFields ) {
            String label = field.getLabel();
            String predicate = field.getUri();
            fieldChoice.addItem(label, predicate);
            labels.put(predicate, label);
        }
        horizontalPanel.add(fieldChoice);

        //		fieldTable.setWidget(row,0,fieldChoice);

        VerticalPanel valuePanel = new VerticalPanel();
        valuePanel.add(new Label("Value to set it to:"));
        valueText = new TextBox();
        valuePanel.add(valueText);
        //		fieldTable.setWidget(row,1,valuePanel);
        horizontalPanel.add(valueText);

        valueText.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    addValue();
                }
            }
        });

        Button addButton = new Button("Set value");
        addButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                addValue();
            }
        });
        //		fieldTable.setWidget(row,2,addButton);
        horizontalPanel.add(addButton);
    }

    /**
	 * 
	 */
    private void addValue() {
        final String text = valueText.getText();
        final String property = fieldChoice.getSelected();
        SetUserMetadata prop = new SetUserMetadata(uri, property, text);
        valueText.setText("");
        dispatch.execute(prop, new AsyncCallback<EmptyResult>() {
            public void onFailure(Throwable caught) {
                GWT.log("FAILED", caught);
            }

            public void onSuccess(EmptyResult result) {
                GWT.log("set user metadata field");
                setProperty(property, labels.get(property), text);
            }
        });
    }

    /**
     * 
     * @param property
     */
    private void removeValue(final String property) {
        dispatch.execute(new SetUserMetadata(uri, property, new HashSet<String>()), new AsyncCallback<EmptyResult>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(EmptyResult result) {
                int row = getRowForField(property);
                if (row != -1) {
                    fieldTable.removeRow(row);
                    styleRows();
                }
                if (getFieldCount() == 1) {
                    addNoFields();
                }
            }
        });
    }

    /**
     * Style rows with css.
     */
    private void styleRows() {
        int rows = fieldTable.getRowCount();
        fieldTable.getFlexCellFormatter().addStyleName(0, 0, "metadataTitle");
        fieldTable.getFlexCellFormatter().addStyleName(0, 1, "metadataTitle");
        fieldTable.getFlexCellFormatter().addStyleName(0, 2, "metadataTitle");
        fieldTable.getFlexCellFormatter().addStyleName(0, 3, "metadataTitle");
        for (int row = 1; row < rows; row++ ) {
            fieldTable.getFlexCellFormatter().addStyleName(row, 0, "metadataTableCell");
            fieldTable.getFlexCellFormatter().addStyleName(row, 1, "metadataTableCell");
            fieldTable.getFlexCellFormatter().addStyleName(row, 2, "metadataTableCell");
            if (row % 2 == 0) {
                fieldTable.getRowFormatter().removeStyleName(row, "metadataTableOddRow");
                fieldTable.getRowFormatter().addStyleName(row, "metadataTableEvenRow");
            } else {
                fieldTable.getRowFormatter().removeStyleName(row, "metadataTableEvenRow");
                fieldTable.getRowFormatter().addStyleName(row, "metadataTableOddRow");
            }
        }
    }
}
