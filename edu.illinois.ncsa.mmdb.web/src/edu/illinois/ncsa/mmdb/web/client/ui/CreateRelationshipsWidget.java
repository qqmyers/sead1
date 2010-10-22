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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetRelationshipResult;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Widget used in selected datasets page to manually create relationships
 * 
 * @author Luis Mendez
 * 
 */
public class CreateRelationshipsWidget extends Composite {

    public static final String RELATED  = "related";
    public static final String DESCENDS = "descendant";
    private final FlowPanel    mainPanel;
    LabeledListBox             dataset1;
    LabeledListBox             dataset2;
    LabeledListBox             relationships;
    PreviewWidget              pre1;
    PreviewWidget              pre2;
    HorizontalPanel            thumbs;

    MyDispatchAsync            service;
    Set<String>                selected;
    DatasetBean                current;

    /**
     * A widget to manually create relationships between data
     * 
     * @param datasets
     * @param service
     */

    public CreateRelationshipsWidget(final Set<DatasetBean> datasets, final Set<String> selected, final MyDispatchAsync service) {

        this.service = service;
        this.selected = selected;
        current = new DatasetBean();

        mainPanel = new FlowPanel();
        initWidget(mainPanel);

        Label createRelationships = new Label("Create Relationship");
        createRelationships.addStyleName("datasetRightColHeading");
        mainPanel.add(createRelationships);

        //user interface: thumbnails & relationship Type
        thumbs = new HorizontalPanel();
        pre1 = new PreviewWidget(null, GetPreviews.SMALL, null, "Unknown", false, false);
        pre1.setWidth("100px");
        thumbs.add(pre1);

        relationships = createRelationshipOptions();
        thumbs.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        thumbs.add(relationships);

        pre2 = new PreviewWidget(null, GetPreviews.SMALL, null, "Unknown", false, false);
        pre2.setMaxWidth(100);
        thumbs.add(pre2);

        mainPanel.add(thumbs);

        //user interface: Filename dropdown forms
        HorizontalPanel forms = new HorizontalPanel();
        dataset1 = createDatasetOptions();
        dataset1.addStyleName("relationshipDatasets");
        forms.add(dataset1);

        dataset1.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {
                fetchDataset(event.getValue(), pre1);
            }
        });

        dataset2 = createDatasetOptions();
        forms.add(dataset2);

        dataset2.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {
                fetchDataset(event.getValue(), pre2);
            }
        });

        mainPanel.add(forms);

        //user interface: submit button
        HorizontalPanel finalize = new HorizontalPanel();
        finalize.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        finalize.setStyleName("relationshipStyle");
        Button submit = new Button("Submit");

        submit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                CreateRelationship();
            }
        });

        finalize.add(submit);

        mainPanel.add(finalize);

    }

    private LabeledListBox createRelationshipOptions() {
        LabeledListBox relationshipOptions = new LabeledListBox("");
        relationshipOptions.addStyleName("createRelationshipType");
        relationshipOptions.addItem("Relates To", RELATED);
        relationshipOptions.addItem("Descends From", DESCENDS);
        relationshipOptions.setSelected(RELATED);
        return relationshipOptions;
    }

    private LabeledListBox createDatasetOptions() {
        LabeledListBox datasetOptions = new LabeledListBox("");
        datasetOptions.choice.addStyleName("relationshipDatasetPulldown");
        return datasetOptions;
    }

    private void fetchDataset(String uri, final PreviewWidget pw) {

        service.execute(new GetDataset(uri), new AsyncCallback<GetDatasetResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error getting recent activity");
            }

            @Override
            public void onSuccess(GetDatasetResult result) {
                current = result.getDataset();
                pw.changeImage(result.getDataset().getUri(), result.getDataset().getMimeType());
            }
        });
    }

    private void CreateRelationship() {
        //error handling - dataset cannot be related to itself
        if (dataset1.getSelected().equals(dataset2.getSelected())) {
            ConfirmDialog okay = new ConfirmDialog("Error", "Please select two different datasets", false);
            okay.getOkText().setText("OK");

        } else {
            //try creating relationship
            service.execute(new SetRelationship(dataset1.getSelected(), relationships.getSelected(), dataset2.getSelected()),
                    new AsyncCallback<SetRelationshipResult>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            GWT.log("Error creating relationship", caught);
                        }

                        public void onSuccess(SetRelationshipResult result) {
                            //show feedback, bottom is temporary
                            ConfirmDialog olay = new ConfirmDialog("Relationship Created", "test - implementation in progress", false);
                            olay.okText.setText("ok");
                            //createdFeedback(dataset1.getSelected(), relationships.getSelected(), dataset2.getSelected());
                        }
                    });

        }
    }

    /*private void createdFeedback(String uri1, String type, String uri2) {
        HorizontalPanel submitted = new HorizontalPanel();
        Label newRelationship = new Label(uri1 + " has relationship " + type + " with " + uri2);
        submitted.add(newRelationship);
        mainPanel.add(submitted);
    }
    */

    public void addToList(String name, String value) {
        dataset1.addItem(name, value);
        dataset2.addItem(name, value);
    }

}
