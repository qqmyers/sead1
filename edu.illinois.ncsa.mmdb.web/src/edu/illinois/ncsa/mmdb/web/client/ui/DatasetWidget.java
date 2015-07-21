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

import java.util.Collections;
import java.util.List;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionsCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ExtractionService;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ExtractionServiceResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetLicense;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadataResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserActions;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUserActionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.LicenseResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Metadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetTitle;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UnpackZip;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UnpackZipResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UserAction;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.PreviewSectionShowEvent;
import edu.illinois.ncsa.mmdb.web.client.ui.preview.PreviewPanel;
import edu.illinois.ncsa.mmdb.web.common.Permission;

/**
 * Show one datasets and related information about it.
 *
 * @author Luigi Marini
 *
 *         TODO replace VerticalPanel and HorizontalPanel with FlowPanel
 */
@SuppressWarnings("nls")
public class DatasetWidget extends Composite {

    private static final String     DOWNLOAD_URL  = "./api/image/download/";

    private final DispatchAsync     service;

    private final FlowPanel         leftColumn;
    private final FlowPanel         rightColumn;

    private String                  uri;

    private InfoWidget              infoPanel;
    private FlexTable               informationTable;
    protected DerivedDatasetsWidget derivedDatasetsWidget;
    private Label                   noExtractedMetadata;

    private final PermissionUtil    rbac;

    private PreviewPanel            previewPanel;

    /** eventbus that is used when a new section is selected */
    protected final HandlerManager  eventBus;

    private static final int        MAX_TEXT_SIZE = 80;

    /**
     *
     * @param dispatchAsync
     */
    public DatasetWidget(DispatchAsync dispatchAsync, HandlerManager eventBus) {
        this.service = dispatchAsync;
        this.eventBus = eventBus;
        rbac = new PermissionUtil(service);

        FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("datasetMainContainer");
        initWidget(mainPanel);

        leftColumn = new FlowPanel();
        leftColumn.addStyleName("datasetMainContainerLeftColumn");
        mainPanel.add(leftColumn);

        rightColumn = new FlowPanel();
        rightColumn.addStyleName("datasetMainContainerRightColumn");
        mainPanel.add(rightColumn);

        // necessary so that the main container wraps around the two columns
        SimplePanel clearFloat = new SimplePanel();
        clearFloat.addStyleName("clearFloat");
        mainPanel.add(clearFloat);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        if (previewPanel != null) {
            previewPanel.unload();
        }
    }

    /**
     * Retrieve a specific dataset given the uri.
     *
     * @param uri
     *            dataset uri
     */
    public void showDataset(String uri, final String section) {
        this.uri = uri;
        leftColumn.clear();
        rightColumn.clear();

        service.execute(new GetDataset(uri, MMDB.getUsername()), new AsyncCallback<GetDatasetResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error getting dataset", null);
            }

            @Override
            public void onSuccess(GetDatasetResult result) {
                drawPage(result);

                if (section != null) {
                    PreviewSectionShowEvent event = new PreviewSectionShowEvent();
                    event.setSection(section);
                    eventBus.fireEvent(event);
                }
            }
        });
    }

    /**
     * Draw the content on the page given a specific dataset.
     *
     * @param dataset
     * @param collection
     */
    private void drawPage(final GetDatasetResult result) {

        // ----------------------------------------------------------------------
        // Create the left side of the page
        // ----------------------------------------------------------------------

        // icon - content category
        HorizontalPanel titlePanel = new HorizontalPanel();
        titlePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        // title
        final EditableLabel titleLabel = new EditableLabel(result.getDataset().getTitle());
        titleLabel.setEditable(false);
        titleLabel.getLabel().addStyleName("datasetTitle");
        titleLabel.setEditableStyleName("datasetTitle");
        titleLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(final ValueChangeEvent<String> event) {
                SetTitle change = new SetTitle(uri, event.getValue());
                service.execute(change, new AsyncCallback<EmptyResult>() {
                    public void onFailure(Throwable caught) {
                        titleLabel.cancel();
                    }

                    public void onSuccess(EmptyResult result) {
                        titleLabel.setText(event.getValue());
                    }
                });
            }
        });

        titlePanel.add(titleLabel);
        leftColumn.add(titlePanel);

        // preview - selection text and preview
        previewPanel = new PreviewPanel(service, eventBus);
        previewPanel.drawPreview(result, leftColumn, uri);
        final float embedPreviewRatio = previewPanel.getSizeRatio();

        // dataset actions
        final FlowPanel actionsPanel = new FlowPanel();
        actionsPanel.addStyleName("datasetActions");
        leftColumn.add(actionsPanel);

        // download original only if allowed
        final FlowPanel downloadWidget = new FlowPanel();
        downloadWidget.addStyleName("inlineBlock");
        actionsPanel.add(downloadWidget);

        // delete dataset
        final FlowPanel deleteWidget = new FlowPanel();
        deleteWidget.addStyleName("inlineBlock");
        actionsPanel.add(deleteWidget);

        // rerun extraction
        final FlowPanel extractWidget = new FlowPanel();
        extractWidget.addStyleName("inlineBlock");
        actionsPanel.add(extractWidget);

        // embed action
        final FlowPanel embedWidget = new FlowPanel();
        embedWidget.addStyleName("inlineBlock");
        actionsPanel.add(embedWidget);
        final FlowPanel embedBox = new FlowPanel();

        // upload derived action
        final FlowPanel uploadWidget = new FlowPanel();
        uploadWidget.addStyleName("inlineBlock");
        actionsPanel.add(uploadWidget);

        // Unpack to Collection action
        final FlowPanel unpackWidget = new FlowPanel();
        unpackWidget.addStyleName("inlineBlock");
        actionsPanel.add(unpackWidget);

        // Export (data + metadata) - requires download permission
        final FlowPanel exportWidget = new FlowPanel();
        exportWidget.addStyleName("inlineBlock");
        actionsPanel.add(exportWidget);

        Anchor embedAnchor = new Anchor("Embed");
        embedAnchor.addStyleName("datasetActionLink");
        embedAnchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (embedBox.getWidgetCount() > 0) {
                    embedBox.clear();
                }
                else {
                    embedBox.add(new EmbedWidget(uri, embedPreviewRatio));
                }

            }
        });
        embedWidget.add(embedAnchor);

        leftColumn.add(embedBox);

        // metadata
        final UserMetadataWidget um = new UserMetadataWidget(uri, service, eventBus);
        um.setWidth("100%");
        leftColumn.add(createMetaDataPanel(um));

        // who viewed document
        leftColumn.add(createUserViewPanel());

        // comments

        leftColumn.add(new AnnotationsWidget(uri, service));

        // ----------------------------------------------------------------------
        // Create the right side of the page
        // ----------------------------------------------------------------------

        // dataset information
        infoPanel = new InfoWidget(result.getDataset(), service);
        rightColumn.add(infoPanel);

        // acccess level widget
        rightColumn.add(new AccessLevelWidget(uri, service));

        // license widget
        final LicenseWidget license = new LicenseWidget(uri, service, true, false, false);
        rightColumn.add(license);

        // social items
        rightColumn.add(new SocialWidget(uri, service));

        // tag widget
        rightColumn.add(new TagsWidget(uri, service));

        // collections
        rightColumn.add(new CollectionMembershipWidget(service, uri));

        // map
        rightColumn.add(new LocationWidget(uri, service));

        // derived datasets
        DerivedDatasetsWidget derivedDatasetsWidget = new DerivedDatasetsWidget(uri, service);
        derivedDatasetsWidget.showDepth(4);
        rightColumn.add(derivedDatasetsWidget);

        // relationships widget
        ShowRelationshipsWidget showRelationshipsWidget = new ShowRelationshipsWidget(uri, service);
        rightColumn.add(showRelationshipsWidget);

        // additional operations based on
        rbac.withPermissions(uri, new PermissionsCallback() {
            @Override
            public void onPermissions(final HasPermissionResult p) {
                if (p.isPermitted(Permission.EDIT_METADATA)) {
                    titleLabel.setEditable(true);
                }
                if (p.isPermitted(Permission.RERUN_EXTRACTION)) {
                    Anchor extractAnchor = new Anchor("Rerun Extraction");
                    extractAnchor.addStyleName("datasetActionLink");
                    extractAnchor.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                            showRerunExtraction();
                        }
                    });
                    extractWidget.add(extractAnchor);
                }
                if (p.isPermitted(Permission.DELETE_DATA)) {
                    Anchor deleteAnchor = new Anchor("Delete");
                    deleteAnchor.addStyleName("datasetActionLink");
                    deleteAnchor.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                            showDeleteDialog();
                        }
                    });
                    deleteWidget.add(deleteAnchor);
                }
                if (p.isPermitted(Permission.UPLOAD_DATA)) {
                    Hyperlink up = new Hyperlink();
                    up.addStyleName("datasetActionLink");
                    up.setText("Upload Derived Data");
                    up.setTargetHistoryToken("upload?id=" + uri);
                    uploadWidget.add(up);

                    String mimetype = result.getDataset().getMimeType();
                    if (mimetype.equals("application/zip") || mimetype.equals("application/x-zip-compressed")) {
                        Anchor unpackAnchor = new Anchor("Unpack To Collection");
                        unpackAnchor.addStyleName("datasetActionLink");
                        unpackAnchor.addClickHandler(new ClickHandler() {
                            public void onClick(ClickEvent event) {
                                unpackZip(result.getDataset().getUri(), result.getDataset().getTitle(), MMDB.getUsername());
                            }
                        });
                        unpackWidget.add(unpackAnchor);

                    }
                }
                um.showTableFields(p.isPermitted(Permission.EDIT_USER_METADATA));
                //
                // add download link, set editability of license widget
                service.execute(new GetLicense(uri), new AsyncCallback<LicenseResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error checking for download privileges", caught);
                    }

                    public void onSuccess(LicenseResult result) {
                        String rights = result.getRights().toLowerCase();
                        String rightsHolder = result.getRightsHolderUri();

                        boolean isRightsHolder = MMDB.getUsername().equals(rightsHolder);
                        if (p.isPermitted(Permission.DOWNLOAD) || isRightsHolder || rights.equals("pddl") || "cc-by".equals(rights) || "cc-by-sa".equals(rights) || "cc-by-nd".equals(rights) || "cc-by-nc".equals(rights) || "cc-by-nc-sa".equals(rights) || "cc-by-nc-nd".equals(rights) || result.isAllowDownload()) {
                            Anchor downloadAnchor = new Anchor();
                            downloadAnchor.setHref(DOWNLOAD_URL + uri);
                            downloadAnchor.setText("Download");
                            downloadAnchor.setTarget("_blank");
                            downloadAnchor.addStyleName("datasetActionLink");
                            downloadWidget.add(downloadAnchor);
                            //Add export anchor as well:
                            Anchor exportAnchor = new Anchor();
                            exportAnchor.setHref("./resteasy/datasets/" + URL.encode(uri) + "/export");
                            exportAnchor.setText("Export");
                            exportAnchor.setTarget("_blank");
                            exportAnchor.addStyleName("exportActionLink");
                            exportWidget.add(exportAnchor);
                        }
                        license.setEditable(isRightsHolder || p.isPermitted(Permission.CHANGE_LICENSE));
                    }
                });
            }
        }, Permission.EDIT_METADATA,
                Permission.CHANGE_LICENSE,
                Permission.RERUN_EXTRACTION,
                Permission.DELETE_DATA,
                Permission.EDIT_USER_METADATA,
                Permission.DOWNLOAD,
                Permission.UPLOAD_DATA);
        // FIXME allow owner to do stuff
    }

    protected void showRerunExtraction() {
        ConfirmDialog dialog = new ConfirmDialog("Rerun Extraction", "Are you sure you want to rerun the extraction on this dataset? Results can take a few seconds to minutes to show up.");

        dialog.addConfirmHandler(new ConfirmHandler() {
            public void onConfirm(ConfirmEvent event) {
                service.execute(new ExtractionService(uri, true), new AsyncCallback<ExtractionServiceResult>() {
                    public void onFailure(Throwable caught)
                    {
                        GWT.log("Error submitting extraction job", caught);
                    }

                    public void onSuccess(ExtractionServiceResult result)
                    {
                        GWT.log("Success submitting extraction job " + result.getJobid(), null);
                        ConfirmDialog dialog = new ConfirmDialog("Refresh Page", "Extraction resubmitted, should page be refreshed now, or later? (it can take a few minutes before results show up)");
                        dialog.getOkText().setText("Now");
                        dialog.getCancelText().setText("Later");
                        dialog.addConfirmHandler(new ConfirmHandler() {
                            public void onConfirm(ConfirmEvent event) {
                                showDataset(uri, null);
                            }
                        });
                        dialog.show();
                    }
                });
            }
        });

        dialog.show();
    }

    /**
     * Confirm the user wants to delete the dataset.
     */
    protected void showDeleteDialog() {
        ConfirmDialog dialog = new ConfirmDialog("Delete", "Are you sure you want to delete this dataset?");

        dialog.addConfirmHandler(new ConfirmHandler() {
            public void onConfirm(ConfirmEvent event) {
                service.execute(new DeleteDataset(uri, MMDB.getUsername()), new AsyncCallback<DeleteDatasetResult>() {
                    public void onFailure(Throwable caught) {
                        GWT.log("Error deleting dataset", caught);
                    }

                    public void onSuccess(DeleteDatasetResult result) {
                        MMDB.eventBus.fireEvent(new DatasetDeletedEvent(uri));
                        MMDB.eventBus.fireEvent(new DatasetUnselectedEvent(uri));
                        History.back();
                    }
                });
            }
        });

        dialog.show();
    }

    Anchor        collectionContextLink = null;
    VerticalPanel collectionContextLinksPanel;

    private Composite createUserViewPanel() {
        final DisclosurePanel userViewPanel = new DisclosurePanel("User Views");
        userViewPanel.addStyleName("datasetDisclosurePanel");
        userViewPanel.setOpen(false);

        if (uri != null) {
            rbac.withPermissions(uri, new PermissionsCallback() {
                @Override
                public void onPermissions(final HasPermissionResult p) {
                    if (p.isPermitted(Permission.VIEW_ACTIVITY)) {
                        service.execute(new GetUserActions(uri), new AsyncCallback<GetUserActionsResult>() {
                            @Override
                            public void onFailure(Throwable arg0) {
                                GWT.log("Error retrieving metadata about dataset " + uri, null);
                                userViewPanel.add(new Label("Error retrieving metadata about dataset " + uri));
                            }

                            @SuppressWarnings("deprecation")
                            @Override
                            public void onSuccess(GetUserActionsResult arg0) {
                                FlexTable userViewTable = new FlexTable();
                                userViewTable.addStyleName("metadataTable");
                                userViewTable.setWidth("100%");
                                userViewPanel.add(userViewTable);

                                List<UserAction> userActions = arg0.getUserActions();
                                Collections.sort(userActions, Collections.reverseOrder());
                                for (UserAction ua : userActions ) {
                                    int row = userViewTable.getRowCount();
                                    userViewTable.setText(row, 0, ua.getWhen().toLocaleString());
                                    userViewTable.setText(row, 1, ua.getName());
                                    userViewTable.setText(row, 2, ua.getAction());

                                    // formatting
                                    userViewTable.getFlexCellFormatter().addStyleName(row, 0, "metadataTableCell");
                                    userViewTable.getFlexCellFormatter().addStyleName(row, 1, "metadataTableCell");
                                    userViewTable.getFlexCellFormatter().addStyleName(row, 2, "metadataTableCell");
                                    if (row % 2 == 0) {
                                        userViewTable.getRowFormatter().addStyleName(row, "metadataTableEvenRow");
                                    } else {
                                        userViewTable.getRowFormatter().addStyleName(row, "metadataTableOddRow");
                                    }
                                }
                            }
                        });
                    } else {
                        userViewPanel.add(new Label("No permission to view activity. Please contact an admin if you think this is an error."));
                    }
                }
            }, Permission.VIEW_ACTIVITY);
        }

        return userViewPanel;
    }

    private Composite createMetaDataPanel(UserMetadataWidget um) {

        //User Specified Metadata

        DisclosurePanel userInformationPanel = new DisclosurePanel("User Specified Metadata");
        userInformationPanel.addStyleName("datasetDisclosurePanel");
        userInformationPanel.setOpen(true);
        userInformationPanel.setAnimationEnabled(true);

        VerticalPanel userPanel = new VerticalPanel();
        userPanel.addStyleName("userSpecifiedBody");
        userInformationPanel.add(userPanel);
        userPanel.add(um);

        leftColumn.add(userInformationPanel);

        //Extracted Metadata

        informationTable = new FlexTable();
        informationTable.addStyleName("metadataTable");
        informationTable.setWidth("100%");

        DisclosurePanel additionalInformationPanel = new DisclosurePanel("Extracted Information");
        additionalInformationPanel.addStyleName("datasetDisclosurePanel");
        additionalInformationPanel.setOpen(false);
        additionalInformationPanel.setAnimationEnabled(true);

        VerticalPanel verticalPanel = new VerticalPanel();
        additionalInformationPanel.add(verticalPanel);

        noExtractedMetadata = new Label("No extracted metadata");
        noExtractedMetadata.addStyleName("noMetadata");
        noExtractedMetadata.addStyleName("hidden");
        verticalPanel.add(noExtractedMetadata);
        verticalPanel.add(informationTable);

        if (uri != null) {
            service.execute(new GetMetadata(uri), new AsyncCallback<GetMetadataResult>() {
                @Override
                public void onFailure(Throwable arg0) {
                    GWT.log("Error retrieving metadata about dataset " + uri, null);
                }

                @Override
                public void onSuccess(GetMetadataResult arg0) {
                    List<Metadata> metadata = arg0.getMetadata();
                    Collections.sort(metadata);
                    String category = "";
                    if (metadata.size() == 0) {
                        noExtractedMetadata.removeStyleName("hidden");
                    }
                    for (Metadata tuple : metadata ) {
                        if (!category.equals(tuple.getCategory())) {
                            int row = informationTable.getRowCount() + 1;
                            informationTable.setHTML(row, 0, "<b>" + tuple.getCategory() + "</b>");
                            informationTable.setText(row, 1, ""); //$NON-NLS-1$
                            informationTable.getFlexCellFormatter().addStyleName(row, 0, "metadataTableCell");
                            category = tuple.getCategory();
                        }
                        int row = informationTable.getRowCount();
                        informationTable.setText(row, 0, tuple.getLabel());
                        if (tuple.getValue().startsWith("http")) {
                            String text = "<a href=\"" + tuple.getValue() + "\">";
                            if (tuple.getValue().length() > MAX_TEXT_SIZE) {
                                text += tuple.getValue().substring(0, MAX_TEXT_SIZE - 3) + "...";
                            } else {
                                text += tuple.getValue();
                            }
                            text += "</a>";
                            informationTable.setHTML(row, 1, text);
                            if (tuple.getValue().length() > MAX_TEXT_SIZE) {
                                informationTable.getFlexCellFormatter().getElement(row, 1).setAttribute("title", tuple.getValue());
                            }
                        } else {
                            if (tuple.getValue().length() > MAX_TEXT_SIZE) {
                                informationTable.setText(row, 1, tuple.getValue().substring(0, MAX_TEXT_SIZE - 3) + "...");
                                informationTable.getFlexCellFormatter().getElement(row, 1).setAttribute("title", tuple.getValue());
                            } else {
                                informationTable.setText(row, 1, tuple.getValue());
                            }
                        }

                        // formatting
                        informationTable.getFlexCellFormatter().addStyleName(row, 0, "metadataTableCell");
                        informationTable.getFlexCellFormatter().addStyleName(row, 1, "metadataTableCell");
                        if (row % 2 == 0) {
                            informationTable.getRowFormatter().addStyleName(row, "metadataTableEvenRow");
                        } else {
                            informationTable.getRowFormatter().addStyleName(row, "metadataTableOddRow");
                        }

                        // extra metadata to display in info
                        if ("Extractor".equals(tuple.getCategory()) && "Image Size".equals(tuple.getLabel())) {
                            Label lbl = new Label(tuple.getLabel() + " : " + tuple.getValue());
                            lbl.addStyleName("datasetRightColText");
                            infoPanel.add(lbl);
                        }
                        if ("FFMPEG".equals(tuple.getCategory()) && "Video Duration".equals(tuple.getLabel())) {
                            Label lbl = new Label(tuple.getLabel() + " : " + tuple.getValue());
                            lbl.addStyleName("datasetRightColText");
                            infoPanel.add(lbl);
                        }
                        if ("FFMPEG".equals(tuple.getCategory()) && "Video FPS".equals(tuple.getLabel())) {
                            Label lbl = new Label(tuple.getLabel() + " : " + tuple.getValue());
                            lbl.addStyleName("datasetRightColText");
                            infoPanel.add(lbl);
                        }
                        if ("FFMPEG".equals(tuple.getCategory()) && "Video Size".equals(tuple.getLabel())) {
                            Label lbl = new Label(tuple.getLabel() + " : " + tuple.getValue());
                            lbl.addStyleName("datasetRightColText");
                            infoPanel.add(lbl);
                        }
                        if ("VA".equals(tuple.getCategory()) && "DOI".equals(tuple.getLabel())) {
                            Label lbl = new Label(tuple.getLabel() + " : " + tuple.getValue());
                            lbl.addStyleName("datasetRightColText");
                            infoPanel.add(lbl);
                        }
                    }
                }
            });
        }

        return additionalInformationPanel;
    }

    private void unpackZip(String datasetURI, String datasetName, String user) {

        UnpackZip uz = new UnpackZip();
        uz.setUri(datasetURI);
        uz.setUser(user);
        uz.setName(datasetName);
        service.execute(uz,
                new AsyncCallback<UnpackZipResult>() {

                    @Override
                    public void onFailure(Throwable arg0) {
                        GWT.log("Error unzipping to collection: ", arg0);
                        Window.alert("Unzip was unsuccessful: " + arg0.getLocalizedMessage());
                    }

                    @Override
                    public void onSuccess(UnpackZipResult arg0) {
                        History.newItem("collection?uri=" + arg0.getUri(), true);
                    }
                });
    }
}
