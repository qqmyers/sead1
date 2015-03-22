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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddToCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddToCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRelationshipResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListNamedThingsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListRelationshipTypes;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRelationshipResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetUserMetadata;

/**
 * Widget used in selected datasets page to manually create relationships
 *
 * @author Luis Mendez
 *
 */
public class CreateRelationshipsWidget extends Composite {

    public static final String  RELATES  = "relates";
    public static final String  DESCENDS = "descends";

    private final FlowPanel     mainPanel;
    LabeledListBox              item1;
    LabeledListBox              item2;
    String                      title1;
    String                      title2;
    LabeledListBox              relationshipsList;
    PreviewWidget               thumb1;
    PreviewWidget               thumb2;
    FlexTable                   relationshipsWidget;

    DispatchAsync               service;
    Set<String>                 items    = new HashSet<String>();
    private final Button        submit;
    private Map<String, String> relationshipOptions;

    /**
     * A widget to manually create relationships between data
     *
     * @param datasets
     * @param service
     */

    public CreateRelationshipsWidget(final Set<String> selected, final DispatchAsync service) {
        this.service = service;

        mainPanel = new FlowPanel();
        //commented out for now, until selected
        //mainPanel.addStyleName("CreateRelationshipContainer");
        initWidget(mainPanel);

        Label createRelationships = new Label("Create Relationship");
        createRelationships.addStyleName("datasetRightColHeading");
        mainPanel.add(createRelationships);

        //user interface: thumbnails & relationship Type
        relationshipsWidget = new FlexTable();
        relationshipsWidget.setStyleName("createRelationshipThumbs");
        mainPanel.add(relationshipsWidget);

        thumb1 = new PreviewWidget(null, GetPreviews.SMALL, null, "Unknown", false, false, service);
        thumb1.setWidth("110px");
        thumb1.setHeight("110px");
        thumb1.setStyleName("relationshipThumbnail");
        relationshipsWidget.setWidget(0, 0, thumb1);

        relationshipsList = new LabeledListBox("");
        relationshipsList.choice.setStyleName("relationshipsList");
        relationshipsWidget.setWidget(0, 1, relationshipsList);

        thumb2 = new PreviewWidget(null, GetPreviews.SMALL, null, "Unknown", false, false, service);
        thumb2.setWidth("110px");
        thumb2.setHeight("110px");
        thumb2.setStyleName("relationshipThumbnail");
        relationshipsWidget.setWidget(0, 2, thumb2);

        //user interface: Filename dropdown forms
        item1 = createItemOptions();
        relationshipsWidget.setWidget(1, 0, item1);

        item1.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                fetchItem(event.getValue().substring(1), thumb1);
                updateRelationShipOptions();
            }
        });

        item2 = createItemOptions();
        relationshipsWidget.setWidget(1, 2, item2);

        item2.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                fetchItem(event.getValue().substring(1), thumb2);
                updateRelationShipOptions();
            }
        });

        //user interface: submit button
        submit = new Button("Submit");
        submit.setEnabled(false);
        submit.addStyleName("relationshipSubmit");
        relationshipsWidget.setWidget(2, 1, submit);

        submit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (item1.getSelected().startsWith("D") && item2.getSelected().startsWith("D")) {
                    addDatashipRelationShip();
                } else if (item1.getSelected().startsWith("C") && item2.getSelected().startsWith("C")) {
                    addCollectionRelationShip();
                } else if (item1.getSelected().startsWith("C") && item2.getSelected().startsWith("D")) {
                    addToCollection();
                } else if (item1.getSelected().startsWith("D") && item2.getSelected().startsWith("C")) {
                    addToCollection();
                }
            }
        });

        service.execute(new ListRelationshipTypes(), new AsyncCallback<ListNamedThingsResult>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(ListNamedThingsResult result) {
                relationshipOptions = result.getThingsOrderedByName();
            }
        });
    }

    private void updateRelationShipOptions() {
        // remove old relation shipts
        String selected = relationshipsList.getSelected();
        for (String item : items ) {
            relationshipsList.removeItem(item);
        }
        items.clear();

        // make sure 2 items are not the same
        submit.setEnabled(false);
        if (!item1.getSelected().equals(item2.getSelected())) {

            if (item1.getSelected().startsWith("C") && item2.getSelected().startsWith("C")) {
                // in case of 2 collections show hasPart
                relationshipsList.addItem("Has Subcollection", "http://purl.org/dc/terms/hasPart");
                items.add("http://purl.org/dc/terms/hasPart");

            } else if (item1.getSelected().startsWith("D") && item2.getSelected().startsWith("D")) {
                // in case of 2 datasets show standard relationships
                for (Map.Entry<String, String> entry : relationshipOptions.entrySet() ) {
                    items.add(entry.getKey());
                    relationshipsList.addItem(entry.getValue(), entry.getKey());
                }

            } else if (item1.getSelected().startsWith("D") && item2.getSelected().startsWith("C")) {
                relationshipsList.addItem("Member of", "http://purl.org/dc/terms/hasPart");
                items.add("http://purl.org/dc/terms/hasPart");

            } else if (item1.getSelected().startsWith("C") && item2.getSelected().startsWith("D")) {
                relationshipsList.addItem("Has Member", "http://purl.org/dc/terms/hasPart");
                items.add("http://purl.org/dc/terms/hasPart");
            }

            submit.setEnabled(true);
        }
        if (selected != null) {
            relationshipsList.setSelected(selected);
        }
    }

    private LabeledListBox createItemOptions() {
        LabeledListBox itemOptions = new LabeledListBox("");
        itemOptions.choice.addStyleName("relationshipDatasetPulldown");
        return itemOptions;
    }

    private void fetchItem(String uri, final PreviewWidget pw) {
        pw.changeImage(uri, null, null, null, false, true);
    }

    private void addToCollection() {
        String collection;
        Set<String> dataset = new HashSet<String>();

        if (item1.getSelected().startsWith("D")) {
            collection = item2.getSelected().substring(1);
            dataset.add(item1.getSelected().substring(1));
        } else {
            collection = item1.getSelected().substring(1);
            dataset.add(item2.getSelected().substring(1));
        }

        service.execute(new AddToCollection(collection, dataset), new AsyncCallback<AddToCollectionResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error adding collection relationship.", caught);
            }

            @Override
            public void onSuccess(AddToCollectionResult result) {
                showFeedback();
            }
        });
    }

    private void addCollectionRelationShip() {
        service.execute(new SetUserMetadata(item1.getSelected().substring(1), relationshipsList.getSelected(), item2.getSelected().substring(1), true), new AsyncCallback<EmptyResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error adding collection relationship.", caught);
            }

            @Override
            public void onSuccess(EmptyResult result) {
                showFeedback();
            }
        });
    }

    private void addDatashipRelationShip() {
        //check if relationship already exists
        service.execute(new GetRelationship(item1.getSelected().substring(1), MMDB.getUsername()), new AsyncCallback<GetRelationshipResult>() {
            @Override
            public void onFailure(Throwable arg0) {
                GWT.log("Error Retrieving Relationships of a Dataset", arg0);
            }

            @Override
            public void onSuccess(GetRelationshipResult arg0) {
                //Check if relationship exists
                Map<String, Relationship> relationship = arg0.getRelationship();

                if (relationship.containsKey(relationshipsList.getSelected())) {
                    Relationship check = relationship.get(relationshipsList.getSelected());
                    if (check.uris.contains(item2.getSelected().substring(1))) {
                        ConfirmDialog okay = new ConfirmDialog("Error", "That relationship already exists", false);
                        okay.getOkText().setText("OK");
                    } else {
                        createRelationship();
                    }
                }
                //try creating relationship
                else {
                    createRelationship();
                }

            }
        });
    }

    private void createRelationship() {
        service.execute(new SetRelationship(item1.getSelected().substring(1), relationshipsList.getSelected(), item2.getSelected().substring(1), MMDB.getUsername()),
                new AsyncCallback<SetRelationshipResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error creating relationship", caught);
                    }

                    public void onSuccess(SetRelationshipResult result) {
                        showFeedback();
                    }
                });
    }

    private void showFeedback() {
        HorizontalPanel submitted = new HorizontalPanel();
        submitted.addStyleName("relationshipCreated");

        Label newRelationship1 = new Label("Created:");
        submitted.add(newRelationship1);

        Hyperlink hyperlink1;
        if (item1.getSelected().startsWith("C")) {
            hyperlink1 = new Hyperlink(item1.getTitle(), "collection?uri=" + item1.getSelected().substring(1));
        } else {
            hyperlink1 = new Hyperlink(item1.getTitle(), "dataset?id=" + item1.getSelected().substring(1));
        }
        hyperlink1.addStyleName("relationshipHyperlink");
        submitted.add(hyperlink1);

        Label newRelationship2 = new Label(relationshipsList.getTitle());
        submitted.add(newRelationship2);

        Hyperlink hyperlink2;
        if (item2.getSelected().startsWith("C")) {
            hyperlink2 = new Hyperlink(item2.getTitle(), "collection?uri=" + item2.getSelected().substring(1));
        } else {
            hyperlink2 = new Hyperlink(item2.getTitle(), "dataset?id=" + item2.getSelected().substring(1));
        }
        hyperlink2.addStyleName("relationshipHyperlink");
        submitted.add(hyperlink2);

        mainPanel.add(submitted);
    }

    public void addToList(String name, String value, boolean collection) {
        if (collection) {
            item1.addItem(name, "C" + value);
            item2.addItem(name, "C" + value);
        } else {
            item1.addItem(name, "D" + value);
            item2.addItem(name, "D" + value);
        }
        updateRelationShipOptions();
    }

}
