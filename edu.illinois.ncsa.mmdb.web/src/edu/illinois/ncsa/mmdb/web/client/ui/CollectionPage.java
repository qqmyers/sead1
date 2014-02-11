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

import java.util.Map.Entry;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ConfigurationResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetConfiguration;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetTitle;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetUserMetadata;
import edu.illinois.ncsa.mmdb.web.client.presenter.BatchOperationPresenter;
import edu.illinois.ncsa.mmdb.web.client.presenter.DatasetTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.ui.preview.PreviewGeoPointBean;
import edu.illinois.ncsa.mmdb.web.client.ui.preview.PreviewPanel;
import edu.illinois.ncsa.mmdb.web.client.view.BatchOperationView;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicTableView;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;

/**
 * A widget showing a collection.
 * 
 * @author Luigi Marini
 * 
 */
public class CollectionPage extends Composite {

    private final String                uri;
    private final DispatchAsync         service;
    private final PermissionUtil        rbac;
    private final HandlerManager        eventBus;
    private final FlowPanel             mainContent;
    private final String                PREVIEW_URL = "./api/image/preview/small/";
    private TitlePanel                  pageTitle;
    private Label                       descriptionLabel;
    private Label                       dateLabel;
    private FlowPanel                   infoPanel;
    private FlowPanel                   previewFlowPanel;
    private Label                       numDatasetsLabel;
    private Label                       authorLabel;
    private Anchor                      doiAnchor;
    private AddToCollectionDialog       addToCollectionDialog;
    private PreviewPanel                previewPanel;
    private final DatasetTablePresenter dynamicTablePresenter;

    public CollectionPage(String uri, DispatchAsync dispatchasync,
            HandlerManager eventBus) {
        this.uri = uri;
        this.service = dispatchasync;
        rbac = new PermissionUtil(dispatchasync);
        this.eventBus = eventBus;
        mainContent = new FlowPanel();
        mainContent.addStyleName("page");
        initWidget(mainContent);

        mainContent.add(createPageTitle());

        mainContent.add(createInfoPanel());

        mainContent.add(createPreviewPanel());

        DynamicTableView dynamicTableView = new DynamicTableView();
        dynamicTablePresenter = new DatasetTablePresenter(dispatchasync, eventBus, dynamicTableView, uri);
        dynamicTablePresenter.bind();

        VerticalPanel vp = new VerticalPanel() {
            @Override
            protected void onDetach() {
                dynamicTablePresenter.unbind();
            }
        };
        vp.add(dynamicTableView.asWidget());
        vp.addStyleName("tableCenter");
        mainContent.add(vp);

        retrieveCollection();

        final UserMetadataWidget um = new UserMetadataWidget(uri, dispatchasync, eventBus);
        um.setWidth("100%");

        //mainContent.add(createSubcollectionsPanel(um));
        mainContent.add(createCollectionContextPanel(um));
        mainContent.add(createMetadataPanel(um));

        mainContent.add(createSocialAnnotationsPanel());

        rbac.doIfAllowed(Permission.EDIT_METADATA, uri, new PermissionCallback() {
            @Override
            public void onAllowed() {

                um.showTableFields(true);

            }
        });
    }

    private Widget createCollectionContextPanel(UserMetadataWidget um) {
        DisclosurePanel userInformationPanel = new DisclosurePanel("Collection Context");
        userInformationPanel.addStyleName("datasetDisclosurePanel");
        userInformationPanel.setOpen(true);
        userInformationPanel.setAnimationEnabled(true);

        VerticalPanel userPanel = new VerticalPanel();
        userPanel.addStyleName("userSpecifiedBody");
        userInformationPanel.add(userPanel);
        collectionContextLink = new Anchor();
        userPanel.add(collectionContextLink);

        userPanel.add(um);

        return userInformationPanel;
    }

    Anchor               collectionContextLink  = null;

    Anchor               subCollectionLink      = null;
    VerticalPanel        subCollectionLinksPanel;
    private final String subcollectionPredicate = "http://purl.org/dc/terms/hasPart";

    private Widget createSubcollectionsPanel(UserMetadataWidget um) {
        DisclosurePanel userInformationPanel = new DisclosurePanel("Sub-Collections");
        userInformationPanel.addStyleName("datasetDisclosurePanel");
        userInformationPanel.setOpen(true);
        userInformationPanel.setAnimationEnabled(true);

        subCollectionLinksPanel = new VerticalPanel();
        subCollectionLinksPanel.addStyleName("userSpecifiedBody");
        userInformationPanel.add(subCollectionLinksPanel);

        return userInformationPanel;
    }

    private Widget createMetadataPanel(UserMetadataWidget um) {
        DisclosurePanel userInformationPanel = new DisclosurePanel("User Specified Information");
        userInformationPanel.addStyleName("datasetDisclosurePanel");
        userInformationPanel.setOpen(true);
        userInformationPanel.setAnimationEnabled(true);

        VerticalPanel userPanel = new VerticalPanel();
        userPanel.addStyleName("userSpecifiedBody");
        userInformationPanel.add(userPanel);

        userPanel.add(um);

        return userInformationPanel;
    }

    /**
     * A panel with comments on the left and tags on the right.
     * 
     * @return the panel
     */
    private Widget createSocialAnnotationsPanel() {
        CommentsView commentsView = new CommentsView(uri, service);
        TagsWidget tagsWidget = new TagsWidget(uri, service);
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
                new BatchOperationPresenter(service, eventBus, batchOperationView, false);

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
        doiAnchor = new Anchor("DOI");
        infoPanel.add(doiAnchor);
        descriptionLabel = new Label("Description");
        infoPanel.add(descriptionLabel);
        dateLabel = new Label("Creation date unavailable");
        infoPanel.add(dateLabel);
        numDatasetsLabel = new Label("Number of datasets");
        infoPanel.add(numDatasetsLabel);

        // add subcollection link
        PermissionUtil rbac = new PermissionUtil(service);
        rbac.doIfAllowed(Permission.EDIT_COLLECTION, new PermissionCallback() {
            @Override
            public void onAllowed() {
                Panel createSubcollectionPanel = createSubcollectionPanel();
                infoPanel.add(createSubcollectionPanel);
            }
        });

        horizontalPanel.add(infoPanel);

        // batch operations
        batchOperationView.addStyleName("titlePanelRightElement");
        batchOperationPresenter.bind();
        horizontalPanel.add(batchOperationView);

        return horizontalPanel;
    }

    /**
     * 
     * @return
     */
    private Panel createSubcollectionPanel() {
        SimplePanel panel = new SimplePanel();
        Anchor link = new Anchor("Add subcollection");
        panel.add(link);
        link.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                addToCollectionDialog = new AddToCollectionDialog(service,
                        new AddToCollectionHandler());
                addToCollectionDialog.center();

            }
        });

        return panel;
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

    private Widget createPreviewPanel() {
        previewPanel = new PreviewPanel(service, eventBus, true);
        previewFlowPanel = new FlowPanel();

        return previewFlowPanel;
    }

    /**
     * Request collection from the server.
     */
    private void retrieveCollection() {
        GWT.log("The collection uri is " + uri);

        service.execute(new GetCollection(uri, MMDB.getUsername()), new AsyncCallback<GetCollectionResult>() {

            @Override
            public void onFailure(Throwable arg0) {
                GWT.log("Failed to retrieve collection", arg0);
            }

            @Override
            public void onSuccess(GetCollectionResult result) {
                showCollection(result.getCollection(), result.getCollectionSize());
                for (PreviewBean bean : result.getPreviews() ) {
                    if (bean instanceof PreviewGeoPointBean) {
                        initializePreviewPanel(result);
                        return;
                    }
                }

                previewPanel.drawPreview(result, previewFlowPanel, result.getCollection().getUri());

                //DOI
                String doi = result.getDOI();
                if (doi == null) {
                    doi = "";
                }
                doiAnchor.setText(doi);
                doiAnchor.setHref(doi);
                doiAnchor.setTarget("_blank");

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

        rbac.doIfAllowed(Permission.EDIT_METADATA, collection.getUri(), new PermissionCallback() {
            @Override
            public void onAllowed() {
                pageTitle.setEditable(true);
                // collection title is editable
                pageTitle.addValueChangeHandler(new ValueChangeHandler<String>() {
                    public void onValueChange(final ValueChangeEvent<String> event) {
                        service.execute(new SetTitle(collection.getUri(), event.getValue()),
                                new AsyncCallback<EmptyResult>() {
                                    public void onFailure(Throwable caught) {
                                        pageTitle.getEditableLabel().cancel();
                                    }

                                    public void onSuccess(EmptyResult result) {
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

        service.execute(new GetConfiguration(null, ConfigurationKey.DiscoveryURL), new AsyncCallback<ConfigurationResult>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(ConfigurationResult configresult) {
                String discoveryURL = null;
                for (Entry<ConfigurationKey, String> entry : configresult.getConfiguration().entrySet() ) {
                    switch (entry.getKey()) {
                        case DiscoveryURL:
                            discoveryURL = entry.getValue();
                            if (!discoveryURL.equals("")) {
                                discoveryURL = discoveryURL.endsWith("/") ? discoveryURL : discoveryURL + "/";

                                try {
                                    String collectionContextURI = discoveryURL + "contents?i=" + collection.getUri() + "&t=" + collection.getTitle();
                                    String collectionContextText = "View Collection Context in Discovery interface";
                                    collectionContextLink.setHref(collectionContextURI);
                                    collectionContextLink.setTarget("_blank");
                                    collectionContextLink.setText(collectionContextText);
                                } catch (Exception ex) {
                                    //Handle exception
                                    String exc = ex.getMessage();
                                    System.out.println(exc);
                                }
                            }
                            break;
                        default:
                    }
                }
            }
        });
    }

    private void initializePreviewPanel(final GetCollectionResult result) {
        rbac.doIfAllowed(Permission.VIEW_LOCATION, new PermissionCallback() {

            @Override
            public void onAllowed() {
                service.execute(new GetConfiguration(MMDB.getUsername(), ConfigurationKey.GoogleMapKey), new AsyncCallback<ConfigurationResult>() {

                    @Override
                    public void onFailure(Throwable arg0) {
                        loadMapsApi("", result);
                    }

                    @Override
                    public void onSuccess(ConfigurationResult configResult) {
                        loadMapsApi(configResult.getConfiguration(ConfigurationKey.GoogleMapKey), result);
                    }
                });
            }

        });
    }

    private void loadMapsApi(String key, final GetCollectionResult result) {
        Maps.loadMapsApi(key, "2", false, new Runnable() {

            @Override
            public void run() {
                previewPanel.drawPreview(result, previewFlowPanel, result.getCollection().getUri());
            }

        });
    }

    class AddToCollectionHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent arg0) {
            String existingCollection = addToCollectionDialog.getSelectedValue();
            String newCollection = addToCollectionDialog.getNewCollectionValue();
            if (!newCollection.isEmpty()) {
                // create new collection
                final CollectionBean collection = new CollectionBean();
                collection.setTitle(newCollection);
                service.execute(new AddCollection(collection, MMDB.getUsername()),
                        new AsyncCallback<AddCollectionResult>() {

                            @Override
                            public void onFailure(Throwable arg0) {
                                GWT.log("Failed creating new collection", arg0);
                            }

                            @Override
                            public void onSuccess(AddCollectionResult arg0) {
                                service.execute(new SetUserMetadata(uri, subcollectionPredicate, collection.getUri(), true), new AsyncCallback<EmptyResult>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        GWT.log("Error adding collection relationship.", caught);
                                    }

                                    @Override
                                    public void onSuccess(EmptyResult result) {
                                        GWT.log("Successfully added subcollection.");
                                        addToCollectionDialog.hide();
                                        dynamicTablePresenter.refresh();
                                    }
                                });

                            }
                        });
            } else if (existingCollection != null && !existingCollection.equals(uri)) {
                GWT.log("Adding collection " + existingCollection + " to " + uri);
                service.execute(new SetUserMetadata(uri, subcollectionPredicate, existingCollection, true), new AsyncCallback<EmptyResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error adding collection relationship.", caught);
                    }

                    @Override
                    public void onSuccess(EmptyResult result) {
                        GWT.log("Successfully added subcollection.");
                        addToCollectionDialog.hide();
                        dynamicTablePresenter.refresh();
                    }
                });
            }
        }
    }
}
