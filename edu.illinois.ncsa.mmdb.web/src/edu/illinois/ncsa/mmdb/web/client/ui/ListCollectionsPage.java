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

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.presenter.BatchOperationPresenter;
import edu.illinois.ncsa.mmdb.web.client.presenter.CollectionTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.view.BatchOperationView;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicTableView;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

/**
 * List all collections in system.
 * 
 * @author Luigi Marini
 * 
 */
public class ListCollectionsPage extends Page {

    private final DispatchAsync            dispatch;
    private final HandlerManager           eventbus;
    private final FlowPanel                addCollectionWidget;
    private final Label                    statusLabel;
    private final CollectionTablePresenter dynamicTablePresenter;

    public ListCollectionsPage(DispatchAsync dispatch, HandlerManager eventBus) {
        super("Collections", dispatch);
        this.dispatch = dispatch;
        this.eventbus = eventBus;

        HorizontalPanel rightHeader = new HorizontalPanel();
        pageTitle.addEast(rightHeader);

        // batch operations
        BatchOperationView batchOperationView = new BatchOperationView();
        batchOperationView.addStyleName("titlePanelRightElement");
        BatchOperationPresenter batchOperationPresenter = new BatchOperationPresenter(dispatch, eventBus, batchOperationView, true);
        batchOperationPresenter.bind();
        rightHeader.add(batchOperationView);

        // rss feed
        Anchor rss = new Anchor();
        rss.setHref("rss.xml");
        rss.addStyleName("rssIcon");
        DOM.setElementAttribute(rss.getElement(), "type", "application/rss+xml");
        rss.setHTML("<img src='./images/rss_icon.gif' border='0px' id='rssIcon' class='navMenuLink'>"); // FIXME hack
        rightHeader.add(rss);

        DynamicTableView dynamicTableView = new DynamicTableView();
        dynamicTablePresenter = new CollectionTablePresenter(dispatch, eventBus, dynamicTableView);
        dynamicTablePresenter.bind();

        VerticalPanel vp = new VerticalPanel() {
            @Override
            protected void onDetach() {
                dynamicTablePresenter.unbind();
            }
        };

        // add collection widget
        addCollectionWidget = createAddCollectionWidget();
        mainLayoutPanel.add(addCollectionWidget);

        statusLabel = new Label("");
        mainLayoutPanel.add(statusLabel);

        vp.add(dynamicTableView.asWidget());
        vp.addStyleName("tableCenter");
        mainLayoutPanel.add(vp);
    }

    /**
     * Widget to create a new collection.
     * 
     * @return
     */
    private FlowPanel createAddCollectionWidget() {
        final FlowPanel addCollectionPanel = new FlowPanel();
        PermissionUtil rbac = new PermissionUtil(dispatch);
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
                        addCollectionBox.setText("");
                        addCollectionBox.showWatermark();
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
                String msg = "You do not have permission to create collections";
                GWT.log(msg);
                statusLabel.setText(msg);
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

        dispatch.execute(new AddCollection(collection, MMDB.getUsername()),
                new AsyncCallback<AddCollectionResult>() {

                    @Override
                    public void onFailure(Throwable arg0) {
                        statusLabel.setText("Error: could not add collection");
                        GWT.log("Failed creating new collection", arg0);
                    }

                    @Override
                    public void onSuccess(AddCollectionResult arg0) {
                        dynamicTablePresenter.refresh();
                    }
                });
    }

    @Override
    public void layout() {
        // TODO Auto-generated method stub

    }

}
