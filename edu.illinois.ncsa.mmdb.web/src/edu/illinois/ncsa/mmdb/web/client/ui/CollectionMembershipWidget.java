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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AddToCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddToCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveFromCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveFromCollectionResult;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

/**
 * Widget showing collections a particular detaset is part of.
 * 
 * @author Luigi Marini
 * 
 */
public class CollectionMembershipWidget extends Composite {

    private final FlowPanel       mainContainer;

    private final Label           titleLabel;

    private final Anchor          addAnchor;

    private AddToCollectionDialog addToCollectionDialog;

    private final DispatchAsync   service;

    private final String          datasetURI;

    private final FlexTable       collectionsPanel;

    /**
     * Create empty widget showing a title and a add to collection link.
     */
    public CollectionMembershipWidget(DispatchAsync dispatch, String datasetURI) {
        this.service = dispatch;
        this.datasetURI = datasetURI;
        mainContainer = new FlowPanel();
        mainContainer.addStyleName("datasetRightColSection");
        initWidget(mainContainer);

        titleLabel = new Label("Collections");
        titleLabel.addStyleName("datasetRightColHeading");
        mainContainer.add(titleLabel);

        collectionsPanel = new FlexTable();
        collectionsPanel.setVisible(false);
        collectionsPanel.addStyleName("tagsLinks");
        mainContainer.add(collectionsPanel);

        // add to collection anchor
        addAnchor = new Anchor("Add to a collection");

        addAnchor.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                showAddToCollectionDialog();
            }
        });
        mainContainer.add(addAnchor);
        loadCollections();
    }

    /**
     * Popup a dialog to select collection.
     */
    protected void showAddToCollectionDialog() {
        addToCollectionDialog = new AddToCollectionDialog(service,
                new AddToCollectionHandler());
        addToCollectionDialog.center();
    }

    /**
     * Add a collection to the list of collections shown.
     * 
     * @param collection
     */
    public void addCollection(CollectionBean collection) {
        if (!collectionsPanel.isVisible()) {
            collectionsPanel.setVisible(true);
        }
        final String uri = collection.getUri();
        String href = "collection?uri=" + uri;

        final Hyperlink link = new Hyperlink(collection.getTitle(), href);

        int row = collectionsPanel.getRowCount();
        final PreviewWidget badge = PreviewWidget.newCollectionBadge(uri, href, service);
        collectionsPanel.setWidget(row++, 0, badge);
        collectionsPanel.setWidget(row, 0, link);

        final Anchor removeButton = new Anchor("Remove");
        removeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                service.execute(new RemoveFromCollection(uri, datasetURI), new AsyncCallback<RemoveFromCollectionResult>() {
                    public void onSuccess(RemoveFromCollectionResult result) {
                        collectionsPanel.remove(badge); // remove badge row
                        collectionsPanel.remove(link); // remove title row
                        collectionsPanel.remove(removeButton); // remove remove link
                    }

                    public void onFailure(Throwable caught) {
                        GWT.log("Error removing dataset from collection", caught);
                    }
                });
            }
        });
        collectionsPanel.setWidget(row, 1, removeButton);
    }

    /**
     * Clear the list of collections.
     */
    public void clear() {
        collectionsPanel.removeAllRows();
    }

    /**
     * Asynchronously load the collections this dataset is part of.
     */
    private void loadCollections() {
        service.execute(new GetCollections(datasetURI),
                new AsyncCallback<GetCollectionsResult>() {

                    @Override
                    public void onFailure(Throwable arg0) {
                        GWT.log("Error loading collections the dataset is part of", arg0);
                    }

                    @Override
                    public void onSuccess(GetCollectionsResult arg0) {
                        ArrayList<CollectionBean> collections = arg0
                                .getCollections();

                        clear();
                        if (collections.size() > 0) {
                            for (CollectionBean collection : collections ) {
                                addCollection(collection);
                            }
                        }
                    }
                });
    }

    class AddToCollectionHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent arg0) {
            String value = addToCollectionDialog.getSelectedValue();
            if (value != null) {
                GWT.log("Adding " + datasetURI + " to collection " + value, null);
                Collection<String> datasets = new HashSet<String>();
                datasets.add(datasetURI);
                service.execute(new AddToCollection(value, datasets),
                        new AsyncCallback<AddToCollectionResult>() {

                            @Override
                            public void onFailure(Throwable arg0) {
                                GWT.log("Error adding dataset to collection", arg0);
                            }

                            @Override
                            public void onSuccess(AddToCollectionResult arg0) {
                                GWT.log("Data successfully added to collection");
                                addToCollectionDialog.hide();
                                loadCollections();
                            }
                        });
            }
        }
    }
}
