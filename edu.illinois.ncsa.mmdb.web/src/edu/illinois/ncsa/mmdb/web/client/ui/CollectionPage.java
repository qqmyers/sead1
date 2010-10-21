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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.PagingDatasetTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.PagingDatasetTableView;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetProperty;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPropertyResult;
import edu.illinois.ncsa.mmdb.web.client.presenter.BatchOperationPresenter;
import edu.illinois.ncsa.mmdb.web.client.view.BatchOperationView;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

/**
 * A widget showing a collection.
 * 
 * @author Luigi Marini
 * 
 */
public class CollectionPage extends Composite {

    private final String                 uri;
    private final MyDispatchAsync        dispatchasync;
    private final PermissionUtil         rbac;
    private final HandlerManager         eventBus;
    private final FlowPanel              mainContent;
    private final String                 PREVIEW_URL = "./api/image/preview/small/";
    private TitlePanel                   pageTitle;
    private Label                        descriptionLabel;
    private Label                        dateLabel;
    private FlowPanel                    infoPanel;
    private Label                        numDatasetsLabel;
    private Label                        authorLabel;
    private final PagingDatasetTableView datasetTableView;

    public CollectionPage(String uri, MyDispatchAsync dispatchasync,
            HandlerManager eventBus) {
        this.uri = uri;
        this.dispatchasync = dispatchasync;
        rbac = new PermissionUtil(dispatchasync);
        this.eventBus = eventBus;
        mainContent = new FlowPanel();
        mainContent.addStyleName("page");
        initWidget(mainContent);

        mainContent.add(createPageTitle());

        mainContent.add(createInfoPanel());

        datasetTableView = new PagingDatasetTableView(uri);
        datasetTableView.addStyleName("datasetTable");

        PagingDatasetTablePresenter datasetTablePresenter =
                new PagingDatasetTablePresenter(datasetTableView, eventBus);
        datasetTablePresenter.bind();

        mainContent.add(datasetTableView);

        mainContent.add(createSocialAnnotationsPanel());

        retrieveCollection();
    }

    /**
     * A panel with comments on the left and tags on the right.
     * 
     * @return the panel
     */
    private Widget createSocialAnnotationsPanel() {
        CommentsView commentsView = new CommentsView(uri, dispatchasync);
        TagsWidget tagsWidget = new TagsWidget(uri, dispatchasync);
        TwoColumnLayout layout = new TwoColumnLayout(commentsView, tagsWidget);
        return layout;
    }

    /**
     * High level information about the dataset.
     * 
     * @return the panel
     */
    private Widget createInfoPanel() {
        // batch operations and logic to unbind
        BatchOperationView batchOperationView = new BatchOperationView();
        final BatchOperationPresenter batchOperationPresenter =
                new BatchOperationPresenter(dispatchasync, eventBus, batchOperationView);

        HorizontalPanel horizontalPanel = new HorizontalPanel() {
            @Override
            protected void onDetach() {
                super.onDetach();
                batchOperationPresenter.unbind();
            }
        };
        horizontalPanel.setWidth("100%");

        infoPanel = new FlowPanel();
        infoPanel.addStyleName("collectionInfo");
        authorLabel = new Label("Author");
        infoPanel.add(authorLabel);
        descriptionLabel = new Label("Description");
        infoPanel.add(descriptionLabel);
        dateLabel = new Label("Creation date unavailable");
        infoPanel.add(dateLabel);
        numDatasetsLabel = new Label("Number of datasets");
        infoPanel.add(numDatasetsLabel);
        horizontalPanel.add(infoPanel);

        // batch operations
        batchOperationView.addStyleName("titlePanelRightElement");
        batchOperationPresenter.bind();
        horizontalPanel.add(batchOperationView);

        return horizontalPanel;
    }

    /**
     * Create the title of the page.
     * 
     * @return title widget
     */
    private Widget createPageTitle() {
        pageTitle = new TitlePanel("Collection");
        return pageTitle;
    }

    /**
     * Request collection from the server.
     */
    private void retrieveCollection() {
        dispatchasync.execute(new GetCollection(uri), new AsyncCallback<GetCollectionResult>() {

            @Override
            public void onFailure(Throwable arg0) {
                GWT.log("Failed to retrieve collection", arg0);
            }

            @Override
            public void onSuccess(GetCollectionResult arg0) {
                showCollection(arg0.getCollection(), arg0.getCollectionSize());
            }
        });
    }

    /**
     * Draw the elements of the collection on the page.
     * 
     * @param collection
     * @param datasets
     */
    protected void showCollection(final CollectionBean collection, int collectionSize) {

        pageTitle.setText(collection.getTitle());

        rbac.doIfAllowed(Permission.EDIT_METADATA, new PermissionCallback() {
            @Override
            public void onAllowed() {
                pageTitle.setEditable(true);
                // collection title is editable
                pageTitle.addValueChangeHandler(new ValueChangeHandler<String>() {
                    public void onValueChange(final ValueChangeEvent<String> event) {
                        dispatchasync.execute(new SetProperty(collection.getUri(), "http://purl.org/dc/elements/1.1/title", event.getValue()),
                                new AsyncCallback<SetPropertyResult>() {
                                    public void onFailure(Throwable caught) {
                                        pageTitle.getEditableLabel().cancel();
                                    }

                                    public void onSuccess(SetPropertyResult result) {
                                        pageTitle.setText(event.getValue());
                                    }
                                });
                    }
                });
            }
        });

        if (collection.getCreator() == null) {
            authorLabel.setText("By Anonymous");
        } else {
            authorLabel.setText(collection.getCreator().getName());
        }
        descriptionLabel.setText(collection.getDescription());
        if (collection.getCreationDate() != null) {
            DateTimeFormat formatter = DateTimeFormat.getFullDateFormat();
            dateLabel.setText(formatter.format(collection.getCreationDate()));
        }
        numDatasetsLabel.setText(collectionSize + " dataset(s)");

    }

}
