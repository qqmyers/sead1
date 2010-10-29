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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionsCallback;
import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
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
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.LicenseResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Metadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetTitle;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;

/**
 * Show one datasets and related information about it.
 * 
 * @author Luigi Marini
 * 
 *         TODO replace VerticalPanel and HorizontalPanel with FlowPanel
 */
@SuppressWarnings("nls")
public class DatasetWidget extends Composite {

    private static final String     DOWNLOAD_URL = "./api/image/download/";

    private final MyDispatchAsync   service;

    private final FlowPanel         leftColumn;
    private final FlowPanel         rightColumn;

    private String                  uri;

    private Panel                   infoPanel;
    private FlexTable               informationTable;
    protected DerivedDatasetsWidget derivedDatasetsWidget;

    private final PermissionUtil    rbac;

    private PreviewPanel            previewPanel;

    /**
     * 
     * @param dispatchAsync
     */
    public DatasetWidget(MyDispatchAsync dispatchAsync) {
        this.service = dispatchAsync;
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
        previewPanel.unload();
    }

    /**
     * Retrieve a specific dataset given the uri.
     * 
     * @param uri
     *            dataset uri
     */
    public void showDataset(String uri) {
        this.uri = uri;
        leftColumn.clear();
        rightColumn.clear();

        service.execute(new GetDataset(uri), new AsyncCallback<GetDatasetResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error getting dataset", null);
            }

            @Override
            public void onSuccess(GetDatasetResult result) {
                drawPage(result);
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
        titlePanel.addStyleName("datasetTitleIcon");
        final Image image = new Image();

        image.setUrl("images/icons/" + ContentCategory.getCategory(result.getDataset().getMimeType()) + ".png");
        image.setTitle(ContentCategory.getCategory(result.getDataset().getMimeType()) + " File");
        titlePanel.add(image);

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
        previewPanel = new PreviewPanel();
        previewPanel.drawPreview(result, leftColumn, uri);

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

        // metadata        
        final UserMetadataWidget um = new UserMetadataWidget(uri, service);
        um.setWidth("100%");
        leftColumn.add(createMetaDataPanel(um));

        // comments
        leftColumn.add(new AnnotationsWidget(uri, service));

        // ----------------------------------------------------------------------
        // Create the right side of the page
        // ----------------------------------------------------------------------

        // dataset information
        infoPanel = createInfoPanel(result.getDataset());
        rightColumn.add(infoPanel);

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

        // license widget
        final LicenseWidget license = new LicenseWidget(uri, service, true, false, false);
        rightColumn.add(license);

        // social items
        rightColumn.add(new SocialWidget(uri, service));

        rbac.withPermissions(uri, new PermissionsCallback() {
            @Override
            public void onPermissions(final HasPermissionResult p) {
                if (p.isPermitted(Permission.EDIT_METADATA)) {
                    titleLabel.setEditable(true);
                }
                if (p.isPermitted(Permission.CHANGE_LICENSE)) {
                    license.setEditable(true);
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
                um.showFields(p.isPermitted(Permission.EDIT_USER_METADATA));
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
                        }
                        license.setEditable(rightsHolder == null || isRightsHolder || p.isPermitted(Permission.CHANGE_LICENSE));
                    }
                });
            }
        }, Permission.EDIT_METADATA,
                Permission.CHANGE_LICENSE,
                Permission.RERUN_EXTRACTION,
                Permission.DELETE_DATA,
                Permission.EDIT_USER_METADATA,
                Permission.DOWNLOAD);
        // FIXME allow owner to do stuff
    }

    void addInfo(String name, String value, Panel panel) {
        if (value != null && !value.equals("")) {
            Label lbl = new Label(name + ": " + value);
            lbl.addStyleName("datasetRightColText");
            panel.add(lbl);
        }
    }

    /**
     * Create the panel containing the information about the dataset.
     * 
     * @return panel with information about the dataset.
     */
    protected Panel createInfoPanel(DatasetBean data) {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName("datasetRightColSection");
        Label lbl = new Label("Info");
        lbl.addStyleName("datasetRightColHeading");
        panel.add(lbl);

        lbl = new Label("Contributor: ");
        lbl.addStyleName("datasetRightColText");
        PersonBean creator = data.getCreator();
        if (creator != null) {
            lbl.setTitle(creator.getEmail());
            lbl.setText("Contributor: " + creator.getName());
        }
        panel.add(lbl);

        String filename = data.getFilename();
        addInfo("Filename", filename, panel);

        String size = TextFormatter.humanBytes(data.getSize());
        addInfo("Size", size, panel);

        String cat = ContentCategory.getCategory(data.getMimeType());
        addInfo("Category", cat, panel);

        String type = data.getMimeType();
        addInfo("MIME Type", type, panel);

        String date = "";
        if (data.getDate() != null) {
            date += DateTimeFormat.getShortDateTimeFormat().format(data.getDate());
        }
        addInfo("Uploaded", date, panel);

        return panel;
    }

    protected void showRerunExtraction() {
        ConfirmDialog dialog = new ConfirmDialog("Rerun Extraction", "Are you sure you want to rerun the extraction on this dataset? Results can take a few seconds to minutes to show up.");

        dialog.addConfirmHandler(new ConfirmHandler() {
            public void onConfirm(ConfirmEvent event) {
                service.execute(new ExtractionService(uri), new AsyncCallback<ExtractionServiceResult>() {
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
                                    showDataset(uri);
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
                service.execute(new DeleteDataset(uri), new AsyncCallback<DeleteDatasetResult>() {
                    public void onFailure(Throwable caught) {
                        GWT.log("Error deleting dataset", caught);
                    }

                    public void onSuccess(DeleteDatasetResult result) {
                        MMDB.eventBus.fireEvent(new DatasetDeletedEvent(uri));
                        History.back();
                    }
                });
            }
        });

        dialog.show();
    }

    private Composite createMetaDataPanel(UserMetadataWidget um) {
        informationTable = new FlexTable();
        informationTable.addStyleName("metadataTable");
        informationTable.setWidth("100%");

        DisclosurePanel additionalInformationPanel = new DisclosurePanel("Additional Information");
        additionalInformationPanel.addStyleName("datasetDisclosurePanel");
        additionalInformationPanel.setOpen(false);
        additionalInformationPanel.setAnimationEnabled(true);

        VerticalPanel verticalPanel = new VerticalPanel();
        additionalInformationPanel.add(verticalPanel);

        verticalPanel.add(new HTML("<b>User Specified</b>"));
        verticalPanel.add(um);

        verticalPanel.add(new HTML("<b>Extracted</b>"));
        verticalPanel.add(informationTable);

        leftColumn.add(additionalInformationPanel);

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
                        informationTable.setText(row, 1, tuple.getValue());

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
                    }
                }
            });
        }

        return additionalInformationPanel;
    }
}
