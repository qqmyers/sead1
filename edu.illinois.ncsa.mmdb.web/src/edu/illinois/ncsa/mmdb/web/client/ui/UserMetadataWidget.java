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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSubclasses;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSubclassesResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.NamedThing;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetUserMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserMetadataField;

public class UserMetadataWidget extends Composite {
    String                                 uri;
    private final DispatchAsync            dispatch;
    ListBox                                fieldChoice;
    TextBox                                valueText;
    VerticalPanel                          thePanel;
    Label                                  noFields;
    FlexTable                              fieldTable;
    Map<String, String>                    labels = new HashMap<String, String>();
    private final SimplePanel              newFieldPanel;
    protected InputField                   inputField;
    protected SortedSet<UserMetadataField> availableFields;

    public UserMetadataWidget(String uri, final DispatchAsync dispatch) {
        this.uri = uri;
        this.dispatch = dispatch;

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

        // add new field
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.addStyleName("addMetadata");
        verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        thePanel.add(verticalPanel);

        // list box
        fieldChoice = new ListBox();
        fieldChoice.addStyleName("addMetadataListBox");
        verticalPanel.add(fieldChoice);

        // new field panel
        newFieldPanel = new SimplePanel();
        verticalPanel.add(newFieldPanel);

        // selection handler
        fieldChoice.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                newFieldPanel.clear();
                int index = fieldChoice.getSelectedIndex();
                if (index != 0) {

                    ClickHandler addHandler = new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            addValue();
                        }
                    };

                    ClickHandler clearHandler = new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            newFieldPanel.clear();
                            fieldChoice.setSelectedIndex(0);
                        }
                    };

                    // add input widget based on type
                    UserMetadataField userMetadataField = availableFields.toArray(new UserMetadataField[0])[index - 1];
                    switch (userMetadataField.getType()) {
                        case UserMetadataField.PLAIN:
                            inputField = new PlainField(userMetadataField, addHandler, clearHandler);
                            break;
                        case UserMetadataField.DATATYPE:
                            inputField = new PlainField(userMetadataField, addHandler, clearHandler);
                            break;
                        case UserMetadataField.ENUMERATED:
                            inputField = new ListField(userMetadataField, addHandler, clearHandler);
                            break;
                        case UserMetadataField.CLASS:
                            inputField = new TreeField(dispatch, userMetadataField, addHandler, clearHandler);
                            break;
                        default:
                            inputField = new PlainField(userMetadataField, addHandler, clearHandler);
                            break;
                    }
                    newFieldPanel.add(inputField);
                }
            }
        });

        initWidget(thePanel);
    }

    private void populateTableHeader() {
        fieldTable.setText(0, 0, "Field");
        fieldTable.setText(0, 1, "Value");
        fieldTable.setText(0, 2, "Applies To");
        fieldTable.setText(0, 3, "Action");
        fieldTable.getRowFormatter().addStyleName(0, "metadataTableHeader");
    }

    public void showFields(final boolean canEdit) {
        // FIXME single get to get fields and values
        dispatch.execute(new ListUserMetadataFields(), new AsyncCallback<ListUserMetadataFieldsResult>() {
            public void onFailure(Throwable caught) {
                GWT.log("Error retrieving available list of User Specified Metadata fields", caught);
            }

            public void onSuccess(ListUserMetadataFieldsResult result) {
                availableFields = result.getFieldsSortedByName();
                GWT.log("available fields: " + availableFields);
                if (availableFields.size() > 0) {
                    if (canEdit) {
                        populateTypes(availableFields);
                    }
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
                                    SortedSet<NamedThing> values = NamedThing.orderByName(result.getValues().get(predicate));
                                    List<String> displayedValues = new ArrayList<String>();
                                    for (NamedThing nt : values ) {
                                        displayedValues.add(nt.getName()); // name of thing, or literal value
                                    }
                                    addNewField(predicate, label, displayedValues, canEdit);
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
        List<String> v = new ArrayList<String>();
        v.add(value);
        addNewField(predicate, label, v, true);
    }

    /** number of fields set */
    private int getFieldCount() {
        return fieldTable.getRowCount();
    }

    /**
     * Given the name of a field, find the first row that has that name in
     * column 0.
     * 
     * @param predicate
     * @return
     */

    private TextBox textBox;

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
    private void addNewField(final String predicate, String label, final List<String> values, boolean canEdit) {

        removeNoFields();
        int row = getRowForField(predicate);
        // if not in table, add at the end
        if (row == -1) {
            row = fieldTable.getRowCount();
        }

        for (int i = 0; i < values.size(); i++ ) {
            fieldTable.insertRow(row);
            // field name
            Label predicateLabel = new Label(label);
            predicateLabel.setTitle(predicate);
            fieldTable.setWidget(row, 0, predicateLabel);
            if (i != 0) {
                predicateLabel.addStyleName("hidden");
            }
            // field value
            fieldTable.setWidget(row, 1, new Label(values.get(i)));

            //placeholder for Applies To
            fieldTable.setWidget(row, 2, new Label("Document"));

            if (canEdit) {
                FlowPanel links = new FlowPanel();
                // edit link
                final Anchor editAnchor = new Anchor("Edit");
                editAnchor.addStyleName("metadataTableAction");
                editAnchor.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        PopupPanel popupPanel = new PopupPanel(true);
                        popupPanel.add(new Label("Sorry, not implemented yet."));
                        popupPanel.showRelativeTo(editAnchor);
                    }
                });
                links.add(editAnchor);
                // remove link
                Anchor removeAnchor = new Anchor("Remove");
                removeAnchor.addStyleName("metadataTableAction");
                removeAnchor.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        PopupPanel popupPanel = new PopupPanel(true);
                        popupPanel.add(new Label("Sorry, not implemented yet."));
                        popupPanel.showRelativeTo(editAnchor);
                        //                        removeValue(predicate);
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
     * Widget to create new entry in table.
     * 
     * @param availableFields
     */
    private void populateTypes(SortedSet<UserMetadataField> availableFields) {
        fieldChoice.clear();
        fieldChoice.addItem("Add Field", "");
        for (UserMetadataField field : availableFields ) {
            String label = field.getLabel();
            String predicate = field.getUri();
            fieldChoice.addItem(label, predicate);
            labels.put(predicate, label);
        }
    }

    /**
     * RPC call to add a new entry.
     */
    private void addValue() {
        final String text = inputField.getValue();
        final String metadataUri = inputField.getUri();
        final String property = fieldChoice.getValue(fieldChoice.getSelectedIndex());
        SetUserMetadata prop;
        if (uri == null) {
            GWT.log("Adding new metadata: " + uri + " | " + property + " | " + text);
            prop = new SetUserMetadata(uri, property, text);
        } else {
            GWT.log("Adding new metadata: " + uri + " | " + property + " | " + metadataUri);
            prop = new SetUserMetadata(uri, property, metadataUri, true);
        }
        dispatch.execute(prop, new AsyncCallback<EmptyResult>() {
            public void onFailure(Throwable caught) {
                GWT.log("Failed adding a new entry to the list", caught);
            }

            public void onSuccess(EmptyResult result) {
                GWT.log("User metadata field was successfully set");
                refresh();
            }
        });
    }

    /**
     * Refresh and redraw table.
     */
    private void refresh() {
        clearTable();
        showFields(true);
        newFieldPanel.clear();
        fieldChoice.setSelectedIndex(0);
    }

    /**
     * Clears the contents of the table.
     */
    private void clearTable() {
        fieldTable.removeAllRows();
        populateTableHeader();
    }

    /**
     * RPC call to remove an entry.
     * 
     * @param property
     */
    private void removeValue(final String property) {

        // TODO have to implement a new dispatch to actually delete instead of setting to empty

        dispatch.execute(new SetUserMetadata(uri, property, new HashSet<String>()), new AsyncCallback<EmptyResult>() {
            public void onFailure(Throwable caught) {
                GWT.log("Error removing value", caught);
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
     * 
     * @param property
     */
    private void editValue(String property, final Collection<String> values) {
        //        fieldChoice.setSelected(property);
        for (String value : values ) {
            valueText.setText(value);
        }
        valueText.setFocus(true);

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

    abstract class InputField extends Composite implements HasValue<String> {

        protected final UserMetadataField userMetadataField;

        public InputField(UserMetadataField userMetadataField, ClickHandler addHandler, ClickHandler clearHandler) {
            this.userMetadataField = userMetadataField;
            // TODO switch to divs
            // first div
            FlexTable layout = new FlexTable();
            layout.addStyleName("metadataPlainField");

            layout.setWidget(0, 0, createInputWidget());

            Anchor addAnchor = new Anchor("Add");
            addAnchor.addClickHandler(addHandler);
            layout.setWidget(0, 1, addAnchor);
            Anchor clearAnchor = new Anchor("Clear");
            clearAnchor.addClickHandler(clearHandler);
            layout.setWidget(0, 2, clearAnchor);

            // second row
            FlowPanel appliedToPanel = new FlowPanel();
            appliedToPanel.addStyleName("metadataAppliedPanel");
            appliedToPanel.add(new Label("Will be applied to"));
            RadioButton documentButton = new RadioButton("appliedTo", "document");
            documentButton.setValue(true);
            appliedToPanel.add(documentButton);
            RadioButton sectionButton = new RadioButton("appliedTo", "section");
            appliedToPanel.add(sectionButton);
            Label currentSection = new Label("[current section]");
            appliedToPanel.add(currentSection);
            layout.setWidget(1, 0, appliedToPanel);
            layout.getFlexCellFormatter().setColSpan(1, 0, 3);

            initWidget(layout);
        }

        abstract Widget createInputWidget();

        abstract String getUri();

        @Override
        public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getValue() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setValue(String value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setValue(String value, boolean fireEvents) {
            // TODO Auto-generated method stub

        }

    }

    class PlainField extends InputField {

        private TextBox textBox;

        public PlainField(UserMetadataField userMetadataField, ClickHandler addHandler, ClickHandler clearHandler) {
            super(userMetadataField, addHandler, clearHandler);
        }

        @Override
        public String getValue() {
            return textBox.getValue();
        }

        @Override
        Widget createInputWidget() {
            textBox = new TextBox();
            textBox.setWidth("500px");
            return textBox;
        }

        @Override
        String getUri() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    class ListField extends InputField {

        private ListBox listBox;

        public ListField(UserMetadataField userMetadataField, ClickHandler addHandler, ClickHandler clearHandler) {
            super(userMetadataField, addHandler, clearHandler);
        }

        @Override
        public String getValue() {
            return listBox.getItemText(listBox.getSelectedIndex());
        }

        @Override
        Widget createInputWidget() {
            listBox = new ListBox();
            listBox.setWidth("500px");
            listBox.addItem("Select...", "");
            for (NamedThing namedThing : userMetadataField.getRange() ) {
                listBox.addItem(namedThing.getName(), namedThing.getUri());
            }
            return listBox;
        }

        @Override
        String getUri() {
            // TODO Auto-generated method stub
            return listBox.getValue(listBox.getSelectedIndex());
        }
    }

    class TreeField extends InputField {

        private Tree                tree;
        private final DispatchAsync dispatch;
        private TaxonomyTreeItem    root;

        public TreeField(DispatchAsync dispatch, UserMetadataField userMetadataField, ClickHandler addHandler, ClickHandler clearHandler) {
            super(userMetadataField, addHandler, clearHandler);
            this.dispatch = dispatch;
            populateTree();
        }

        private void populateTree() {

            Iterator<NamedThing> iterator = userMetadataField.getRange().iterator();
            while (iterator.hasNext()) {
                NamedThing thing = iterator.next();
                root = new TaxonomyTreeItem(thing.getName(), thing.getUri());
            }
            populateChildren(root);
            tree.addItem(root);
        }

        /**
         * Recursevely populate subtree starting at node.
         * 
         * @param node
         */
        private void populateChildren(final TaxonomyTreeItem node) {

            dispatch.execute(new GetSubclasses(node.getUri()), new AsyncCallback<GetSubclassesResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Error getting subclasses of " + userMetadataField.getUri(), caught);
                }

                @Override
                public void onSuccess(GetSubclassesResult result) {
                    List<NamedThing> children = result.getSubclasses();
                    if (children.size() != 0) {
                        for (NamedThing namedThing : children ) {
                            GWT.log("Adding " + namedThing.getName() + " to " + node.getLabel());
                            final TaxonomyTreeItem newNode = new TaxonomyTreeItem(namedThing.getName(), namedThing.getUri());
                            node.addItem(newNode);
                            populateChildren(newNode);
                        }
                    }
                }
            });
        }

        @Override
        Widget createInputWidget() {
            tree = new Tree();
            tree.setAnimationEnabled(true);
            tree.setWidth("500px");
            return tree;
        }

        @Override
        public String getValue() {
            return tree.getSelectedItem().getText();
        }

        @Override
        public String getUri() {
            return ((TaxonomyTreeItem) tree.getSelectedItem()).getUri();
        }
    }

    class TaxonomyTreeItem extends TreeItem {

        private String uri;
        private String label;

        public TaxonomyTreeItem() {
            super();
        }

        public TaxonomyTreeItem(String label, String uri) {
            super();
            this.label = label;
            this.uri = uri;
            if (label.length() > 20) {
                setText(label.substring(0, 20) + "...");
            } else {
                setText(label);
            }
        }

        public String getUri() {
            return uri;
        }

        public String getLabel() {
            return label;
        }
    }
}
