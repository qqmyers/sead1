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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ExtractionService;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ExtractionServiceResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetLicense;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMetadataResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.LicenseResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Metadata;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetProperty;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPropertyResult;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;
import edu.uiuc.ncsa.cet.bean.PreviewPyramidBean;
import edu.uiuc.ncsa.cet.bean.PreviewVideoBean;

/**
 * Show one datasets and related information about it.
 * 
 * @author Luigi Marini
 * 
 *         TODO replace VerticalPanel and HorizontalPanel with FlowPanel
 */
@SuppressWarnings("nls")
public class DatasetWidget extends Composite {
    /** maximum width of a preview image */
    private static final long       MAX_WIDTH    = 600;
    /** maximum height of a preview image */
    private static final long       MAX_HEIGHT   = 600;

    private static final String     BLOB_URL     = "./api/image/";
    private static final String     DOWNLOAD_URL = "./api/image/download/";
    private static final String     PYRAMID_URL  = "./pyramid/";

    private final MyDispatchAsync   service;

    private final FlowPanel         leftColumn;
    private final FlowPanel         rightColumn;

    private String                  uri;

    private Panel                   infoPanel;
    private FlexTable               informationTable;
    protected DerivedDatasetsWidget derivedDatasetsWidget;
    private AbsolutePanel           previewPanel;
    private PreviewBean             currentPreview;

    /**
     * 
     * @param dispatchAsync
     */
    public DatasetWidget(MyDispatchAsync dispatchAsync) {
        this.service = dispatchAsync;

        FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("datasetMainContainer");
        initWidget(mainPanel);

        leftColumn = new FlowPanel();
        leftColumn.addStyleName("datasetMainContainerLeftColumn");
        mainPanel.add(leftColumn);

        rightColumn = new FlowPanel();
        rightColumn.addStyleName("datasetMainContainerRightColumn");
        mainPanel.add(rightColumn);

        // necessary so that the main conteinar wraps around the two columns
        SimplePanel clearFloat = new SimplePanel();
        clearFloat.addStyleName("clearFloat");
        mainPanel.add(clearFloat);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        hideSeadragon();
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
        // find best preview bean, add others
        // best image preview is that that is closest to width of column
        int maxwidth = leftColumn.getOffsetWidth();
        List<PreviewBean> previews = new ArrayList<PreviewBean>();
        // FIXME use a map to handle all previews
        PreviewImageBean bestImage = null;
        PreviewVideoBean bestVideo = null;
        for (PreviewBean pb : result.getPreviews() ) {
            if (pb instanceof PreviewImageBean) {
                PreviewImageBean pib = (PreviewImageBean) pb;
                if (bestImage == null) {
                    bestImage = pib;
                } else if (Math.abs(maxwidth - pib.getWidth()) < Math.abs(maxwidth - bestImage.getWidth())) {
                    bestImage = pib;
                }
            } else if (pb instanceof PreviewVideoBean) {
                PreviewVideoBean pvb = (PreviewVideoBean) pb;
                if (bestVideo == null) {
                    bestVideo = pvb;
                } else if (Math.abs(maxwidth - pvb.getWidth()) < Math.abs(maxwidth - bestVideo.getWidth())) {
                    bestVideo = pvb;
                }
            } else {
                previews.add(pb);
            }
        }
        if (bestImage != null) {
            previews.add(bestImage);
        }
        if (bestVideo != null) {
            previews.add(bestVideo);
        }

        // sort beans, image, zoom, video, rest
        Collections.sort(previews, new Comparator<PreviewBean>() {
            @Override
            public int compare(PreviewBean o1, PreviewBean o2)
                {
                    // sort by type
                    if (o1.getClass() != o2.getClass()) {
                        if (o1 instanceof PreviewImageBean) {
                            return -1;
                        }
                        if (o2 instanceof PreviewImageBean) {
                            return +1;
                        }
                        if (o1 instanceof PreviewPyramidBean) {
                            return -1;
                        }
                        if (o2 instanceof PreviewPyramidBean) {
                            return +1;
                        }
                        if (o1 instanceof PreviewVideoBean) {
                            return -1;
                        }
                        if (o2 instanceof PreviewVideoBean) {
                            return +1;
                        }
                    }
                    // don't care at this point
                    return 0;
                }
        });

        // ----------------------------------------------------------------------
        // Create the left side of the page
        // ----------------------------------------------------------------------

        // title
        final EditableLabel titleLabel = new EditableLabel(result.getDataset().getTitle());
        titleLabel.setEditable(false);
        titleLabel.getLabel().addStyleName("datasetTitle");
        titleLabel.setEditableStyleName("datasetTitle");
        titleLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(final ValueChangeEvent<String> event) {
                SetProperty change = new SetProperty(uri, "http://purl.org/dc/elements/1.1/title", event.getValue());
                service.execute(change, new AsyncCallback<SetPropertyResult>() {
                    public void onFailure(Throwable caught) {
                        titleLabel.cancel();
                    }

                    public void onSuccess(SetPropertyResult result) {
                        titleLabel.setText(event.getValue());
                    }
                });
            }
        });
        leftColumn.add(titleLabel);

        // preview options
        final FlowPanel previewsPanel = new FlowPanel();
        previewsPanel.addStyleName("datasetActions");
        for (PreviewBean pb : previews ) {
            String label;
            if (pb instanceof PreviewImageBean) {
                label = "Preview";

            } else if (pb instanceof PreviewPyramidBean) {
                label = "Zoom in";
                if (MMDB.getUsername().contains("joefutrelle@gmail.com") || MMDB.getUsername().contains("acraig@ncsa.uiuc.edu")) {
                    label = "Mega-Zoom™";
                }

            } else if (pb instanceof PreviewVideoBean) {
                label = "Play video";

            } else {
                label = "Unknown"; // FIXME maybe "other" would be more user-friendly?
                GWT.log("Unknown preview bean " + pb);
            }

            final PreviewBean finalpb = pb;
            final Anchor anchor = new Anchor(label);
            anchor.addStyleName("previewActionLink");
            anchor.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    for (int i = 0; i < previewsPanel.getWidgetCount(); i++ ) {
                        previewsPanel.getWidget(i).removeStyleName("deadlink");
                    }
                    anchor.addStyleName("deadlink");
                    showPreview(finalpb);
                }
            });
            if (bestVideo == finalpb) {
                anchor.addStyleName("deadlink");
            } else if (bestImage == finalpb) {
                anchor.addStyleName("deadlink");
            }
            previewsPanel.add(anchor);
        }
        leftColumn.add(previewsPanel);

        // space for the preview/video/zoom
        currentPreview = null;
        previewPanel = new AbsolutePanel();
        previewPanel.addStyleName("previewPanel");
        leftColumn.add(previewPanel);

        // dataset actions
        final FlowPanel actionsPanel = new FlowPanel();
        actionsPanel.addStyleName("datasetActions");
        leftColumn.add(actionsPanel);

        // download original only if allowed
        final FlowPanel downloadWidget = new FlowPanel();
        actionsPanel.add(downloadWidget);

        // delete dataset
        final FlowPanel deleteWidget = new FlowPanel();
        actionsPanel.add(deleteWidget);

        // rerun extraction
        final FlowPanel extractWidget = new FlowPanel();
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

        // license widget
        final LicenseWidget license = new LicenseWidget(uri, service, true, false, false);
        rightColumn.add(license);

        // social items
        rightColumn.add(new SocialWidget(uri, service));

        // ----------------------------------------------------------------------
        // Initialize some more pieces of the UI
        // ----------------------------------------------------------------------

        // show right preview
        if (bestVideo != null) {
            showPreview(bestVideo);
        } else if (bestImage != null) {
            showPreview(bestImage);
        } else {
            previewPanel.add(new PreviewWidget(uri, GetPreviews.LARGE, null));
        }

        // add download link
        service.execute(new GetLicense(uri), new AsyncCallback<LicenseResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error checking for download privileges", caught);
            }

            public void onSuccess(LicenseResult result) {
                String rights = result.getRights().toLowerCase();
                if (MMDB.getUsername().equals(result.getRightsHolderUri()) || rights.equals("pddl") || "cc-by".equals(rights) || "cc-by-sa".equals(rights) || "cc-by-nd".equals(rights) || "cc-by-nc".equals(rights) || "cc-by-nc-sa".equals(rights) || "cc-by-nc-nd".equals(rights) || result.isAllowDownload()) {
                    Anchor downloadAnchor = new Anchor();
                    downloadAnchor.setHref(DOWNLOAD_URL + uri);
                    downloadAnchor.setText("Download");
                    downloadAnchor.setTarget("_blank");
                    downloadAnchor.addStyleName("datasetActionLink");
                    downloadWidget.add(downloadAnchor);
                }
            }
        });

        // get permissions
        service.execute(new HasPermission(MMDB.getUsername(), Permission.VIEW_ADMIN_PAGES), new AsyncCallback<HasPermissionResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error checking for admin privileges", caught);
                addWidgets(false, result.getDataset().getCreator().getUri().equals(MMDB.getUsername()));
            }

            @Override
            public void onSuccess(HasPermissionResult permresult) {
                addWidgets(permresult.isPermitted(), result.getDataset().getCreator().getUri().equals(MMDB.getUsername()));
            }

            private void addWidgets(boolean isAdmin, boolean isCreator) {
                // admins and owners can update some fields
                if (isAdmin || isCreator) {
                    // delete
                    Anchor deleteAnchor = new Anchor("Delete");
                    deleteAnchor.addStyleName("datasetActionLink");
                    deleteAnchor.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                            showDeleteDialog();
                        }
                    });
                    deleteWidget.add(deleteAnchor);

                    // license
                    license.setEditable(true);

                    // title
                    titleLabel.setEditable(true);

                    // user specified metadata
                    um.showFields(true);
                } else {
                    // license
                    license.setEditable(false);

                    // user specified metadata
                    um.showFields(false);
                }

                // only admins can rerun extraction
                if (isAdmin) {
                    Anchor extractAnchor = new Anchor("Rerun Extraction");
                    extractAnchor.addStyleName("datasetActionLink");
                    extractAnchor.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {
                            showRerunExtraction();
                        }
                    });
                    extractWidget.add(extractAnchor);
                }
            }
        });
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

        lbl = new Label("Size: " + TextFormatter.humanBytes(data.getSize()));
        lbl.addStyleName("datasetRightColText");
        panel.add(lbl);

        lbl = new Label("Type: " + data.getMimeType());
        lbl.addStyleName("datasetRightColText");
        panel.add(lbl);

        String date = "";
        if (data.getDate() != null) {
            date += DateTimeFormat.getShortDateTimeFormat().format(data.getDate());
        }
        lbl = new Label("Uploaded: " + date);
        lbl.addStyleName("datasetRightColText");
        panel.add(lbl);

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

    // ----------------------------------------------------------------------
    // preview section
    // ----------------------------------------------------------------------

    private void showPreview(PreviewBean pb) {
        // check to make sure this is not already showing
        if (currentPreview == pb) {
            return;
        }

        // if not same as current preview hide old preview type
        if (currentPreview == null) {
            previewPanel.clear();
        } else if (currentPreview.getClass() != pb.getClass()) {
            if (currentPreview instanceof PreviewPyramidBean) {
                hideSeadragon();
            }

            previewPanel.clear();
            currentPreview = null;
        }

        // if now preview type create a new one
        if (currentPreview == null) {
            if (pb instanceof PreviewImageBean) {
                Image image = new Image();
                //image.addStyleName( "seadragon" );
                image.getElement().setId("preview");
                previewPanel.add(image);

            } else if (pb instanceof PreviewPyramidBean) {
                Label container = new Label();
                container.addStyleName("seadragon");
                container.getElement().setId("preview");
                previewPanel.add(container);

            } else if (pb instanceof PreviewVideoBean) {
                Label container = new Label();
                container.getElement().setId("preview");
                previewPanel.add(container);
            }
        }

        // replace content (either same type or new created)
        if (pb instanceof PreviewImageBean) {
            PreviewImageBean pib = (PreviewImageBean) pb;
            long w = pib.getWidth();
            long h = pib.getHeight();
            if (pib.getWidth() > pib.getHeight()) {
                if (pib.getWidth() > MAX_WIDTH) {
                    h = (long) (h * (double) MAX_WIDTH / w);
                    w = MAX_WIDTH;
                }
            } else {
                if (pib.getHeight() > MAX_HEIGHT) {
                    w = (long) (w * (double) MAX_HEIGHT / h);
                    h = MAX_HEIGHT;
                }
            }
            showImage(BLOB_URL + pb.getUri(), Long.toString(w), Long.toString(h));

        } else if (pb instanceof PreviewPyramidBean) {
            showSeadragon(PYRAMID_URL + pb.getUri() + "/xml");

        } else if (pb instanceof PreviewVideoBean) {
            PreviewVideoBean pvb = (PreviewVideoBean) pb;
            String preview = null;
            if (pvb.getPreviewImage() != null) {
                preview = BLOB_URL + pvb.getPreviewImage().getUri();
            }
            showFlash(BLOB_URL + pb.getUri(), preview, "video", Long.toString(pvb.getWidth()), Long.toString(pvb.getHeight()));
        }

        currentPreview = pb;
    }

    // ----------------------------------------------------------------------
    // javascript
    // ----------------------------------------------------------------------

    public final native void showImage(String url, String w, String h) /*-{
        img = $doc.getElementById("preview");
        img.src=url;
        img.width=w;
        img.height=h;
    }-*/;

    public final native void showFlash(String url, String preview, String type, String w, String h) /*-{
        if (url != null) {
        $wnd.player = new $wnd.SWFObject('player.swf', 'player', w, h, '9');
        $wnd.player.addParam('allowfullscreen','true');
        $wnd.player.addParam('allowscriptaccess','always');
        $wnd.player.addParam('wmode','opaque');
        $wnd.player.addVariable('file',url);
        $wnd.player.addVariable('autostart','false');
        if (preview != null) {
        $wnd.player.addVariable('image',preview);
        }            
        //            $wnd.player.addVariable('author','Joe');
        //            $wnd.player.addVariable('description','Bob');
        //            $wnd.player.addVariable('title','title');
        //            $wnd.player.addVariable('debug','console');
        $wnd.player.addVariable('provider',type);
        $wnd.player.write('preview');
        }
    }-*/;

    public final native void showSeadragon(String url) /*-{
        $wnd.Seadragon.Config.debug = true;
        $wnd.Seadragon.Config.imagePath = "img/";
        $wnd.Seadragon.Config.autoHideControls = true;

        // close existing viewer
        if ($wnd.viewer) {
        $wnd.viewer.setFullPage(false);
        $wnd.viewer.setVisible(false);
        $wnd.viewer.close();
        $wnd.viewer = null;            
        }

        // open with new url
        if (url != null) {
        $wnd.viewer = new $wnd.Seadragon.Viewer("preview");
        $wnd.viewer.openDzi(url);
        }
    }-*/;

    public final native void hideSeadragon() /*-{
        // hide the current viewer if open
        if ($wnd.viewer) {
        $wnd.viewer.setFullPage(false);
        $wnd.viewer.setVisible(false);
        $wnd.viewer.close();
        $wnd.viewer = null;            
        }
    }-*/;

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
