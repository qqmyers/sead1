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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetItemsBySet;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetItemsBySetResult;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * View of the selected datasets
 *
 * @author Luis Mendez
 *
 */
public class SelectedItemsPage extends Page {

    private final FlowPanel           selectedPanel;

    private final DispatchAsync       service;

    private final FlowPanel           leftcolumn;
    private final FlowPanel           rightcolumn;

    private final int                 num;
    private final Set<DatasetBean>    datasets;
    private final Set<CollectionBean> collections;
    int                               first;

    private CreateRelationshipsWidget relationshipWidget;

    private final PermissionUtil      rbac;

    /**
     * Create an instance of selected datasets view page.
     *
     * @param dispatchAsync
     */
    public SelectedItemsPage(DispatchAsync dispatchAsync) {

        super("Selected Items", dispatchAsync);

        rbac = new PermissionUtil(dispatchAsync);
        service = dispatchAsync;
        first = 1;

        //user interface
        selectedPanel = new FlowPanel();
        selectedPanel.addStyleName("selectedMainContainer");

        leftcolumn = new FlowPanel();
        leftcolumn.addStyleName("selectedLeftContainer");
        selectedPanel.add(leftcolumn);

        rightcolumn = new FlowPanel();
        rightcolumn.addStyleName("selectedRightContainer");
        selectedPanel.add(rightcolumn);

        SimplePanel clearFloat = new SimplePanel();
        clearFloat.addStyleName("clearFloat");
        selectedPanel.add(clearFloat);

        //view selected datasets
        final HashSet<String> selectedItems = new HashSet<String>(MMDB.getSessionState().getSelectedItems());

        datasets = new HashSet<DatasetBean>();
        collections = new HashSet<CollectionBean>();

        num = MMDB.getSessionState().getSelectedItems().size();
        leftcolumn.add(new Label("Showing " + num + " selected items"));

        //create relationship - widget (if allowed)
        rbac.doIfAllowed(Permission.ADD_RELATIONSHIP, new PermissionCallback() {
            @Override
            public void onAllowed() {
                relationshipWidget = new CreateRelationshipsWidget(selectedItems, service);
                if (selectedItems.size() == 1) {
                    rightcolumn.add(new Label("You cannot create a relationship with 1 item"));
                } else {
                    rightcolumn.add(relationshipWidget);
                }
            }

            @Override
            public void onDenied() {
                Label notAllowed = new Label("You do not have permission to create relationships");
                rightcolumn.add(notAllowed);
            }
        });

        //fetch all datasets via their URI to add to list & relationship widget
        fetchData(selectedItems);

        mainLayoutPanel.add(selectedPanel);

    }

    private void fetchData(HashSet<String> uris) {

        dispatchAsync.execute(new GetItemsBySet(uris, MMDB.getUsername()), new AsyncCallback<GetItemsBySetResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error getting datasetbean from uri");
            }

            @Override
            public void onSuccess(GetItemsBySetResult result) {
                //alphabetically sort list
                ArrayList<CollectionBean> collectionList = new ArrayList<CollectionBean>(result.getCollections());
                Collections.sort(collectionList, new Comparator<CollectionBean>() {

                    public int compare(CollectionBean k1, CollectionBean k2) {
                        return k1.getTitle().toLowerCase().compareTo(k2.getTitle().toLowerCase());
                    }
                });

                //add each one to relationship widget & list to the left
                for (final CollectionBean collection : collectionList ) {
                    leftcolumn.add(new CollectionInfoWidget(collection, true, dispatchAsync));
                    collections.add(collection);
                    //create relationship - widget (if allowed)
                    rbac.doIfAllowed(Permission.ADD_RELATIONSHIP, new PermissionCallback() {
                        @Override
                        public void onAllowed() {
                            relationshipWidget.addToList(shortenTitle(collection.getTitle()), collection.getUri(), true);
                            if (first == 1) {
                                relationshipWidget.thumb1.changeImage(collection.getUri(), "Collection");

                                first = 0;
                            } else if (first == 0) {
                                relationshipWidget.thumb2.changeImage(collection.getUri(), "Collection");
                                first = -1;
                            }
                        }
                    });
                }

                //alphabetically sort list
                ArrayList<DatasetBean> datasetList = new ArrayList<DatasetBean>(result.getDatasets());
                Collections.sort(datasetList, new Comparator<DatasetBean>() {

                    public int compare(DatasetBean k1, DatasetBean k2) {
                        return k1.getTitle().toLowerCase().compareTo(k2.getTitle().toLowerCase());
                    }
                });

                //add each one to relationship widget & list to the left
                for (final DatasetBean dataset : datasetList ) {
                    leftcolumn.add(new DatasetInfoWidget(dataset, true, dispatchAsync));
                    datasets.add(dataset);
                    //create relationship - widget (if allowed)
                    rbac.doIfAllowed(Permission.ADD_RELATIONSHIP, new PermissionCallback() {
                        @Override
                        public void onAllowed() {

                            relationshipWidget.addToList(shortenTitle(dataset.getTitle()), dataset.getUri(), false);
                            if (first == 1) {
                                relationshipWidget.thumb1.changeImage(dataset.getUri(), dataset.getMimeType());

                                first = 0;
                            } else if (first == 0) {
                                relationshipWidget.thumb2.changeImage(dataset.getUri(), dataset.getMimeType());
                                first = -1;
                            }

                        }
                    });
                }
            }
        });
    }

    private String shortenTitle(String title) {
        if (title != null && title.length() > 15) {
            return title.substring(0, 15) + "...";
        } else {
            return title;
        }
    }

    @Override
    public void layout() {
        // TODO Auto-generated method stub

    }
}
