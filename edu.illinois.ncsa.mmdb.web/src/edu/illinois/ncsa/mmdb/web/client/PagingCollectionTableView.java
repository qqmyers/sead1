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
package edu.illinois.ncsa.mmdb.web.client;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.PagingTablePresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetsInCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetsInCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewCollectionEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.ui.ConfirmDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.DownloadDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;

public class PagingCollectionTableView extends PagingDcThingView<CollectionBean> implements Display<CollectionBean> {
    FlexTable                   table;
    private final DispatchAsync dispatchAsync;

    public PagingCollectionTableView(DispatchAsync dispatchAsync) {
        super();
        this.dispatchAsync = dispatchAsync;
        addStyleName("datasetTable"); // gotta style ourselves like a dataset table
        displayView();
    }

    @Override
    protected HorizontalPanel createPagingPanel(int page, String sortKey,
            String viewType) {
        // TODO Auto-generated method stub
        HorizontalPanel p = super.createPagingPanel(page, sortKey, viewType);
        viewOptions.removeItem("flow");
        return p;
    }

    Map<String, Panel> badgeImages = new HashMap<String, Panel>();

    @Override
    public void addItem(final String uri, CollectionBean item, String type) {
        HorizontalPanel previewPanel = new HorizontalPanel();
        previewPanel.add(new Image("./images/preview-100.gif")); // is this necessary?
        previewPanel.addStyleName("centered");
        badgeImages.put(uri, previewPanel);

        if (getViewType().equals("grid")) {
            addGridItem(uri, item, previewPanel);
        } else if (getViewType().equals("flow")) {
            addFlowItem(uri, item, previewPanel);
        } else {
            addListItem(uri, item, previewPanel);
        }

        addBadge(uri);
    }

    void addFlowItem(String uri, CollectionBean item, Panel previewPanel) {
    }

    String shortenTitle(String title) {
        if (title.length() > 15) {
            return title.substring(0, 15) + "...";
        } else {
            return title;
        }
    }

    int n = 0;

    void addGridItem(String uri, CollectionBean item, Panel previewPanel) {
        previewPanel.setWidth("120px");
        Label t = new Label(shortenTitle(item.getTitle()));
        t.addStyleName("smallText");
        t.setWidth("120px");
        int row = n / 5; // width of table
        int col = n % 5;
        table.setWidget(row * 2, col, previewPanel);
        table.getCellFormatter().addStyleName(row * 2, col, "gridPreviewSmall");
        table.setWidget((row * 2) + 1, col, t);
        table.getCellFormatter().addStyleName((row * 2) + 1, col, "gridLabelSmall");
        n++;
    }

    private void addListItem(final String uri, final CollectionBean item, Panel previewPanel) {

        final VerticalPanel infoPanel = new VerticalPanel();

        infoPanel.setSpacing(5);

        infoPanel.add(new Hyperlink(item.getTitle(), "collection?uri=" + uri));

        if (item.getCreationDate() != null) {
            infoPanel.add(new Label(item.getCreationDate() + ""));
        } else {
            infoPanel.add(new Label(""));
        }

        int count = item.getMemberCount();
        if (count > 0) {
            infoPanel.add(new Label(count + " item" + (count > 1 ? "s" : "")));
        }

        final int row = table.getRowCount();

        PermissionUtil rbac = new PermissionUtil(dispatchAsync);

        final Anchor download = new Anchor();
        rbac.doIfAllowed(Permission.DOWNLOAD, new PermissionCallback() {
            @Override
            public void onAllowed() {
                downloadCollection(uri, item.getTitle(), download);
            }
        });
        infoPanel.add(download);

        rbac.doIfAllowed(Permission.DELETE_COLLECTION, new PermissionCallback() {
            @Override
            public void onAllowed() {
                Anchor deleteAnchor = new Anchor("Delete");
                deleteAnchor.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        dispatchAsync.execute(new DeleteDataset(uri), new AsyncCallback<DeleteDatasetResult>() {
                            public void onFailure(Throwable caught) {
                            }

                            public void onSuccess(DeleteDatasetResult result) {
                                table.clearCell(row, 0);
                                table.clearCell(row, 1);
                                table.getRowFormatter().addStyleName(row, "hidden");
                                MMDB.eventBus.fireEvent(new DatasetDeletedEvent(uri));
                            }
                        });
                    }
                });
                infoPanel.add(deleteAnchor);
            }
        });

        // yoinked from DatasetTableOneColumnView
        table.setWidget(row, 0, previewPanel);
        table.setWidget(row, 1, infoPanel);
        table.getCellFormatter().addStyleName(row, 0, "leftCell");
        table.getCellFormatter().addStyleName(row, 1, "rightCell");
        table.getCellFormatter().setVerticalAlignment(row, 1, HasVerticalAlignment.ALIGN_TOP);
        table.getRowFormatter().addStyleName(row, "oddRow");
    }

    private void downloadCollection(final String collectionURI, final String name, Anchor download) {

        download.setText("Download");
        download.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                dispatchAsync.execute(new GetDatasetsInCollection(collectionURI), new AsyncCallback<GetDatasetsInCollectionResult>() {
                    public void onFailure(Throwable caught) {
                        GWT.log("Failed retrieving datasets in collection");
                    }

                    public void onSuccess(GetDatasetsInCollectionResult result) {
                        if (result.getDatasets().size() > 0) {
                            new DownloadDialog("Download Collection", result.getDatasets(), name);
                        }
                        else {
                            ConfirmDialog okay = new ConfirmDialog("Error", "No datasets in collection", false);
                            okay.getOkText().setText("OK");
                        }

                    }
                });
            }
        });
    }

    public void addBadge(String collectionUri) {
        Panel p = badgeImages.get(collectionUri);
        if (p != null) {
            p.clear();
            PreviewWidget pw = PreviewWidget.newCollectionBadge(collectionUri, "collection?uri=" + collectionUri, dispatchAsync);
            pw.setMaxWidth(100);
            p.add(pw);
        }
    }

    String uriForSortKey() {
        if (sortKey.startsWith("title-")) {
            return "http://purl.org/dc/elements/1.1/title";
        } else {
            return "http://purl.org/dc/terms/created";
        }
    }

    int pageSize() {
        if (getViewType().equals("grid")) {
            return 35;
        } else if (getViewType().equals("flow")) {
            return 3;
        } else {
            return 10;
        }
    }

    protected void displayPage() {
        table.removeAllRows();
        badgeImages.clear();

        final int pageSize = pageSize();
        // now compute the current page offset
        int pageOffset = (page - 1) * pageSize;

        // now list the collections
        GetCollections query = new GetCollections();
        query.setSortKey(uriForSortKey());
        query.setDesc(descForSortKey());
        query.setOffset(pageOffset);
        query.setLimit(pageSize);

        dispatchAsync.execute(query, new AsyncCallback<GetCollectionsResult>() {
            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onSuccess(GetCollectionsResult result) {
                int np = result.getCount() / pageSize + (result.getCount() % pageSize != 0 ? 1 : 0);
                setNumberOfPages(np);
                n = 0;
                for (CollectionBean collection : result.getCollections() ) {
                    AddNewCollectionEvent event = new AddNewCollectionEvent(
                            collection);
                    GWT.log("Firing event add collection "
                            + collection.getTitle(), null);
                    MMDB.eventBus.fireEvent(event);
                }
            }
        });
    }

    protected void displayView() {
        middlePanel.clear();
        table = new FlexTable();
        table.addStyleName("datasetTable"); // inner table needs style too
        middlePanel.add(table);
    }

    public String getAction() {
        return "listCollections";
    }

    @Override
    public String getViewTypePreference() {
        return MMDB.COLLECTION_VIEW_TYPE_PREFERENCE;
    }

    @Override
    public void addItem(String uri, CollectionBean item, String type, int position) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addItem(String uri, CollectionBean item, String type, String sectionUri, String sectionLabel, String sectionMarker) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addItem(String uri, CollectionBean item, String type, int position, String sectionUri, String sectionLabel, String sectionMarker) {
        // TODO Auto-generated method stub

    }
}
