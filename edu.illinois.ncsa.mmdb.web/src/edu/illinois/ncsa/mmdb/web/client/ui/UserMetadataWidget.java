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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RunSparqlQuery;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RunSparqlQueryResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetUserMetadata;

public class UserMetadataWidget extends Composite {
    static String       availableFieldsQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" +
                                                     "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" +
                                                     "PREFIX cet: <http://cet.ncsa.uiuc.edu/2007/>\r\n" +
                                                     "\r\n" +
                                                     "SELECT ?label ?f\r\n" +
                                                     "WHERE {\r\n" +
                                                     "  ?f <rdf:type> <cet:userMetadataField> .\r\n" +
                                                     "  ?f <rdfs:label> ?label .\r\n" +
                                                     "}\r\n" +
                                                     "ORDER BY ASC(?label)";

    String              uri;
    MyDispatchAsync     dispatch;
    LabeledListBox      fieldChoice;
    TextBox             valueText;
    FlexTable           fieldTable;
    Map<String, String> labels               = new HashMap<String, String>();

    public UserMetadataWidget(String uri, MyDispatchAsync dispatch) {
        this.uri = uri;
        this.dispatch = dispatch;

        fieldTable = new FlexTable();
        fieldTable.setWidth("100%");
        fieldTable.addStyleName("metadataTable");
        fieldTable.getColumnFormatter().setWidth(0, "30%");
        fieldTable.getColumnFormatter().setWidth(1, "50%");
        fieldTable.getColumnFormatter().setWidth(2, "20%");
        initWidget(fieldTable);
    }

    public void showFields(final boolean canEdit) {
        // FIXME single get to get fields and values
        dispatch.execute(new RunSparqlQuery(availableFieldsQuery), new AsyncCallback<RunSparqlQueryResult>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(RunSparqlQueryResult result) {
                if (result.getResult().size() > 0) {
                    if (canEdit) {
                        addFieldAddControls(result.getResult());
                    }
                    dispatch.execute(new GetUserMetadataFields(uri), new AsyncCallback<GetUserMetadataFieldsResult>() {
                        public void onFailure(Throwable caught) {
                        }

                        public void onSuccess(GetUserMetadataFieldsResult result) {
                            for (String predicate : result.getFieldLabels().keySet() ) {
                                addNewField(predicate, result.getFieldLabels().get(predicate), result.getValues().get(predicate), canEdit);
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

    /**
     * 
     * @param predicate
     * @return
     */
    private int getRowForField(String predicate) {
        for (int row = 0; row < fieldTable.getRowCount() - 1; row++ ) {
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
        int row = getRowForField(predicate);
        if (row == -1) {
            row = fieldTable.getRowCount();
            if (fieldChoice != null) {
                row--;
            }
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
            Anchor removeAnchor = new Anchor("Remove");
            removeAnchor.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    removeValue(predicate);
                }
            });
            fieldTable.setWidget(row, 2, removeAnchor);
        }
        styleRows();
    }

    /**
     * 
     * @param result
     */
    private void addFieldAddControls(List<List<String>> result) {

        HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.addStyleName("addMetadata");
        horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        int row = fieldTable.getRowCount();
        fieldTable.getFlexCellFormatter().setColSpan(row, 0, 3);
        fieldTable.setWidget(row, 0, horizontalPanel);

        fieldChoice = new LabeledListBox("Set Field:");
        for (List<String> entry : result ) {
            String label = entry.get(0);
            String predicate = entry.get(1);
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
            }
        });
    }

    /**
     * Style rows with css.
     */
    private void styleRows() {
        int rows = fieldTable.getRowCount();
        if (fieldChoice != null) {
            rows--;
        }
        for (int row = 0; row < rows; row++ ) {
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
