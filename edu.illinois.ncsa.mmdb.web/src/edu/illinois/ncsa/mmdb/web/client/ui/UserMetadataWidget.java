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
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.NamedThing;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataField;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataValue;
import edu.illinois.ncsa.mmdb.web.client.event.PreviewSectionShowEvent;

public class UserMetadataWidget extends Composite {
    String                                 uri;
    private final DispatchAsync            dispatch;
    ListBox                                fieldChoice;
    TextBox                                valueText;
    VerticalPanel                          thePanel;
    Label                                  noFields;
    AddMetadataWidget                      addMetadata;
    FlexTable                              fieldTable;
    Map<String, String>                    labels     = new HashMap<String, String>();
    Map<String, Integer>                   indexLabel = new HashMap<String, Integer>();
    Map<String, Integer>                   listLabel  = new HashMap<String, Integer>();
    protected SortedSet<UserMetadataField> availableFields;
    private final HandlerManager           eventBus;

    public UserMetadataWidget(String uri, final DispatchAsync dispatch, HandlerManager eventBus) {
        this.uri = uri;
        this.dispatch = dispatch;
        this.eventBus = eventBus;

        // table of user specified metadata fields
        fieldTable = new FlexTable();
        fieldTable.addStyleName("metadataTable");
        fieldTable.getColumnFormatter().setWidth(0, "20%");
        fieldTable.getColumnFormatter().setWidth(1, "40%");
        fieldTable.getColumnFormatter().setWidth(2, "20%");
        fieldTable.getColumnFormatter().setWidth(3, "20%");

        // header
        populateTableHeader();

        thePanel = new VerticalPanel();
        noFields = new Label("No user specified metadata");
        noFields.addStyleName("noMetadata");
        noFields.addStyleName("metadataTableCell");
        noFields.addStyleName("hidden");
        thePanel.add(noFields);
        thePanel.add(fieldTable);

        addMetadata = new AddMetadataWidget(uri, dispatch, null) {
            protected void refresh() {
                super.refresh();
                clearTable();
                showTableFields(true);
            }
        };
        thePanel.add(addMetadata);

        initWidget(thePanel);
    }

    private void populateTableHeader() {
        fieldTable.setText(0, 0, "Field");
        fieldTable.setText(0, 1, "Value");
        fieldTable.setText(0, 2, "Applies To");
        fieldTable.setText(0, 3, "Action");
        fieldTable.getRowFormatter().addStyleName(0, "metadataTableHeader");
    }

    public void showTableFields(final boolean canEdit) {
        // FIXME single get to get fields and values
        addMetadata.showFields(canEdit);
        addMetadata.setVisible(canEdit);
        dispatch.execute(new ListUserMetadataFields(), new AsyncCallback<ListUserMetadataFieldsResult>() {
            public void onFailure(Throwable caught) {
                GWT.log("Error retrieving available list of User Specified Metadata fields", caught);
            }

            public void onSuccess(ListUserMetadataFieldsResult result) {
                availableFields = result.getFieldsSortedByName();
                GWT.log("available fields: " + availableFields);
                if (availableFields.size() > 0) {

                    dispatch.execute(new GetUserMetadataFields(uri), new AsyncCallback<GetUserMetadataFieldsResult>() {
                        public void onFailure(Throwable caught) {
                            GWT.log("Error retrieving User Specified Information", caught);
                        }

                        public void onSuccess(GetUserMetadataFieldsResult result) {
                            Set<String> predicates = result.getThingsOrderedByName().keySet();
                            if (predicates.size() == 0) {
                                addNoFields();
                            } else {
                                for (String predicate : predicates ) {
                                    String label = result.getThingNames().get(predicate);
                                    SortedSet<UserMetadataValue> values = NamedThing.orderByName(result.getValues().get(predicate));
                                    addNewField(predicate, label, values, canEdit);
                                }
                            }
                        }
                    });

                }
            }
        });
    }

    /**
     * Given the name of a field, find the first row that has that name in
     * column 0.
     * 
     * @param predicate
     * @return
     */

    private int getRowForField(String predicate) {
        for (int row = 1; row < fieldTable.getRowCount(); row++ ) {
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
    private void addNewField(final String predicate, final String label, final Collection<UserMetadataValue> values, boolean canEdit) {

        removeNoFields();
        int row = getRowForField(predicate);
        // if not in table, add at the end
        if (row == -1) {
            row = fieldTable.getRowCount();
        }

        int i = 0;
        for (final UserMetadataValue value : values ) {
            fieldTable.insertRow(row);
            // field name
            Label predicateLabel = new Label(label);
            predicateLabel.setTitle(predicate);
            fieldTable.setWidget(row, 0, predicateLabel);
            if (i++ != 0) {
                predicateLabel.addStyleName("hidden");
            }
            // field value
            Hyperlink namelink = new Hyperlink();
            if (value.getUri() != null) {
                namelink.setTargetHistoryToken("search?q=" + value.getUri() + "&f=" + predicate);
            } else {
                namelink.setTargetHistoryToken("search?q=" + value.getName() + "&f=" + predicate);
            }
            namelink.setText(value.getName());
            fieldTable.setWidget(row, 1, namelink);

            //placeholder for Applies To
            if (value.getSectionMarker() == null) {
                fieldTable.setWidget(row, 2, new Label("Document"));
            } else {
                final Anchor anchor = new Anchor(value.getSectionMarker());
                anchor.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        PreviewSectionShowEvent show = new PreviewSectionShowEvent();
                        show.setSection(anchor.getText());
                        eventBus.fireEvent(show);
                    }
                });
                fieldTable.setWidget(row, 2, anchor);
            }

            if (canEdit) {
                FlowPanel links = new FlowPanel();
                // edit link
                final Anchor editAnchor = new Anchor("Edit");
                editAnchor.setTitle("Edit the value for this property");
                editAnchor.addStyleName("metadataTableAction");
                editAnchor.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        addMetadata.editValue(predicate, value);
                    }
                });
                links.add(editAnchor);
                // remove link
                Anchor removeAnchor = new Anchor("Remove");
                removeAnchor.setTitle("Remove this property from your dataset");
                removeAnchor.addStyleName("metadataTableAction");
                removeAnchor.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        addMetadata.removeValue(predicate, value, true);
                    }
                });
                links.add(removeAnchor);
                fieldTable.setWidget(row, 3, links);
            }
            row++;
        }
        styleRows();

    }

    /**
     * Shows the "no field" label and hides the metadata table.
     */
    private void addNoFields() {
        fieldTable.addStyleName("hidden");
        noFields.removeStyleName("hidden");

    }

    /**
     * Hides the "no field" label and shows the metadata table.
     */
    private void removeNoFields() {
        fieldTable.removeStyleName("hidden");
        noFields.addStyleName("hidden");
    }

    /**
     * Clears the contents of the table.
     */
    private void clearTable() {
        fieldTable.removeAllRows();
        populateTableHeader();
    }

    /**
     * Style rows with css.
     */
    private void styleRows() {
        int rows = fieldTable.getRowCount();
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
