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

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PagingCollectionTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.PagingCollectionTableView;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;

/**
 * List all collections in system.
 * 
 * @author Luigi Marini
 * 
 */
public class ListCollectionsPage extends Composite {

    private final DispatchAsync       dispatchasync;
    private final HandlerManager      eventBus;
    private final FlowPanel           mainContainer;
    private final Label               statusLabel;
    private final FlowPanel           addCollectionWidget;
    private TitlePanel                pageTitle;
    private PagingCollectionTableView view;

    public ListCollectionsPage(DispatchAsync dispatchasync,
            HandlerManager eventBus) {
        this.dispatchasync = dispatchasync;
        this.eventBus = eventBus;
        mainContainer = new FlowPanel();
        mainContainer.addStyleName("page");
        initWidget(mainContainer);

        mainContainer.add(createPageTitle());

        // add collection widget
        addCollectionWidget = createAddCollectionWidget();
        mainContainer.add(addCollectionWidget);

        statusLabel = new Label("Loading...");
        mainContainer.add(statusLabel);

        retrieveCollections();
    }

    /**
     * 
     * @return
     */
    private Widget createPageTitle() {
        pageTitle = new TitlePanel("Collections");
        return pageTitle;
    }

    /**
     * Widget to create a new collection.
     * 
     * @return
     */
    private FlowPanel createAddCollectionWidget() {
        final FlowPanel addCollectionPanel = new FlowPanel();

        PermissionUtil rbac = new PermissionUtil(dispatchasync);
        rbac.doIfAllowed(Permission.ADD_COLLECTION, new PermissionCallback() {
            @Override
            public void onAllowed() {
                Label createLabel = new Label("Create new collection: ");
                createLabel.addStyleName("inline");
                addCollectionPanel.add(createLabel);
                final WatermarkTextBox addCollectionBox = new WatermarkTextBox("",
                        "Collection name");
                addCollectionBox.addStyleName("inline");
                addCollectionPanel.add(addCollectionBox);
                Button addButton = new Button("Add", new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent arg0) {
                        createNewCollection(addCollectionBox.getText());
                    }
                });
                addButton.addStyleName("inline");
                addCollectionPanel.add(addButton);
                SimplePanel clearBoth = new SimplePanel();
                clearBoth.addStyleName("clearBoth");
                addCollectionPanel.add(clearBoth);
            }

            @Override
            public void onDenied() {
                //Label notAllowed = new Label("You do not have permission to create collections");
            }
        });

        return addCollectionPanel;
    }

    /**
     * Create new collection on the server.
     * 
     * @param text
     *            name of collection
     */
    protected void createNewCollection(String text) {

        CollectionBean collection = new CollectionBean();
        collection.setTitle(text);

        dispatchasync.execute(new AddCollection(collection, MMDB.getUsername()),
                new AsyncCallback<AddCollectionResult>() {

                    @Override
                    public void onFailure(Throwable arg0) {
                        statusLabel.setText("Error: could not load collections");
                        GWT.log("Failed creating new collection", arg0);
                    }

                    @Override
                    public void onSuccess(AddCollectionResult arg0) {
                        retrieveCollections();
                    }
                });
    }

    /**
     * Retrieve collections from server.
     */
    private void retrieveCollections() {
        dispatchasync.execute(new GetCollections(),
                new AsyncCallback<GetCollectionsResult>() {

                    @Override
                    public void onFailure(Throwable arg0) {
                        GWT.log("Error getting collections", arg0);

                    }

                    @Override
                    public void onSuccess(GetCollectionsResult arg0) {
                        if (arg0.getCount() > 0) {
                            statusLabel.addStyleName("hidden");
                            showCollections(arg0.getCollections());
                        } else {
                            statusLabel.setText("No collections found.");
                        }
                    }
                });
    }

    /**
     * Draw table with list of collections.
     * 
     * @param collections
     */
    protected void showCollections(ArrayList<CollectionBean> collections) {
        if (view != null) {
            mainContainer.remove(view);
        }

        view = new PagingCollectionTableView(dispatchasync);
        view.addStyleName("datasetTable");
        PagingCollectionTablePresenter presenter = new PagingCollectionTablePresenter(
                view, dispatchasync, eventBus);
        presenter.bind();

        view.setNumberOfPages(0);

        mainContainer.add(view.asWidget());
    }

}
