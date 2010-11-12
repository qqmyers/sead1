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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteRelationshipResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRelationshipResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class ShowRelationshipsWidget extends Composite {
    private final FlowPanel       mainContainer;
    private final List<String>    rTypes;
    private final MyDispatchAsync service;
    private final String          currenturi;

    public ShowRelationshipsWidget(String uri, MyDispatchAsync service) {
        this(uri, service, true);
    }

    public ShowRelationshipsWidget(final String uri, final MyDispatchAsync service, boolean withTitle) {

        currenturi = uri;
        this.service = service;

        //holds unique types
        rTypes = new LinkedList<String>();

        mainContainer = new FlowPanel();
        mainContainer.addStyleName("datasetRightColSection");
        mainContainer.setVisible(false);
        initWidget(mainContainer);

        if (withTitle) {
            Label titleLabel = new Label("Relationships");
            titleLabel.addStyleName("datasetRightColHeading");
            mainContainer.add(titleLabel);
        }

        service.execute(new GetRelationship(uri), new AsyncCallback<GetRelationshipResult>() {
            @Override
            public void onFailure(Throwable arg0) {
                GWT.log("Error Retrieving Relationships of a Dataset");
            }

            @Override
            public void onSuccess(GetRelationshipResult arg0) {
                //Map<String, Relationship> dataset = new HashMap<String, Relationship>();
                Map<String, Relationship> dataset = arg0.getRelationship();
                //List<String> types = arg0.getTypes();

                //for every unique type
                for (Map.Entry<String, Relationship> entry : dataset.entrySet() ) {

                    Relationship relationship = entry.getValue();

                    String t = entry.getKey();
                    //if (!rTypes.contains(t)) {
                    //initialize panel
                    //rTypes.add(t);
                    RelationshipPanel panel = new RelationshipPanel();
                    mainContainer.add(panel.disclosurePanel);
                    panel.type = relationship.typeLabel;

                    //add datasets
                    for (DatasetBean d : relationship.datasets ) {
                        addDataset(d, t, panel);
                    }

                    panel.disclosurePanel.getHeaderTextAccessor().setText(panel.type + " (" + panel.count + ")");
                    mainContainer.setVisible(panel.previews.getRowCount() > 0);
                    //}
                }
            }
        });

    }

    private void addDataset(final DatasetBean ds, final String type, final RelationshipPanel panel) {

        //initialize preview with image and title
        String url = "dataset?id=" + ds.getUri();
        final PreviewWidget pw = new PreviewWidget(ds.getUri(), GetPreviews.SMALL, url);
        String title = ds.getTitle();
        title = title.length() > 15 ? title.substring(0, 15) + "..." : title;
        Hyperlink link = new Hyperlink(title, url);
        int n = panel.previews.getRowCount();

        //add delete handler
        final int rowToDelete = n;
        Anchor removeButton = new Anchor("Remove");

        removeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                service.execute(new DeleteRelationship(currenturi, type, ds.getUri(), MMDB.getUsername()), new AsyncCallback<DeleteRelationshipResult>() {
                    public void onSuccess(DeleteRelationshipResult result) {

                        deleteRelationship(rowToDelete, panel);
                        pw.setVisible(false); //IE fix: bug where thumbnail remains

                    }

                    public void onFailure(Throwable caught) {
                        GWT.log("Error deleting relationship");
                    }
                });
            }
        });

        //update widget
        panel.count++;
        panel.previews.setWidget(n++, 0, pw);
        panel.previews.setWidget(n, 0, link);
        panel.previews.setWidget(n, 1, removeButton);
    }

    private void deleteRelationship(final int toDelete, RelationshipPanel panel) {

        //hide deleted datasets
        panel.previews.getRowFormatter().addStyleName(toDelete, "relationshipDelete");
        panel.previews.getRowFormatter().addStyleName(toDelete + 1, "relationshipDelete");

        panel.count--;

        //hide disclosure panel if necessary and update header
        if (panel.count == 0) {
            panel.disclosurePanel.setVisible(false);
        } else {
            panel.disclosurePanel.getHeaderTextAccessor().setText(panel.type + " (" + panel.count + ")");
        }
    }
}
