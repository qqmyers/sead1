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

import java.util.Date;
import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasetsResult;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * TODO Add comments
 * 
 * @author Joe Futrelle
 * 
 */
public class PagingDatasetTableView extends PagingDcThingView<DatasetBean> {
    DatasetTableView            table;
    String                      inCollection;
    int                         numberOfPages = 0;
    int                         pageOffset    = 0;
    int                         pageSize;
    private final DispatchAsync dispatchAsync;

    public PagingDatasetTableView(DispatchAsync dispatchAsync) {
        super();
        this.dispatchAsync = dispatchAsync;
    }

    public PagingDatasetTableView(String inCollection, DispatchAsync dispatchAsync) {
        this(dispatchAsync);
        setInCollection(inCollection);
    }

    public String getInCollection() {
        return inCollection;
    }

    public void setInCollection(String inCollection) {
        this.inCollection = inCollection;
    }

    public String getAction() {
        return inCollection == null ? "listDatasets" : "collection";
    }

    protected String getHistoryToken() {
        // TODO Auto-generated method stub
        return super.getHistoryToken() + (inCollection != null ? "&uri=" + inCollection : "");
    }

    protected Map<String, String> parseHistoryToken(String token) {
        Map<String, String> params = super.parseHistoryToken(token);
        String newInCollection = params.get("uri");
        inCollection = newInCollection;
        // FIXME should invalidate the view if inCollection has changed, yet tolerate nulls.
        return params;
    }

    @Override
    public void addItem(String uri, DatasetBean dataset, String type) {
        String title = dataset.getTitle();
        Date date = dataset.getDate();
        String previewUri = "/api/image/preview/small/" + uri;
        String size = TextFormatter.humanBytes(dataset.getSize());
        String authorsId = "Anonymous";
        if (dataset.getCreator() != null) {
            authorsId = dataset.getCreator().getName();
        }
        table.addRow(uri, title, type, date, previewUri, size, authorsId);
    }

    public DatasetTableView getTable() {
        return table;
    }

    protected void displayView() {
        DatasetTableView datasetTableView = null;
        if (getViewType().equals("grid")) {
            datasetTableView = new DatasetTableGridView(dispatchAsync);
        } else if (getViewType().equals("flow")) {
            datasetTableView = new DatasetTableCoverFlowView(dispatchAsync);
        } else {
            datasetTableView = new DatasetTableOneColumnView(dispatchAsync);
        }
        setTable(datasetTableView);
    }

    public void setTable(DatasetTableView table) {
        middlePanel.clear();
        middlePanel.add(table);
        this.table = table;
    }

    String uriForSortKey() {
        if (sortKey.startsWith("title-")) {
            return "http://purl.org/dc/elements/1.1/title";
        } else {
            return "http://purl.org/dc/elements/1.1/date";
        }
    }

    protected void displayPage() {
        // if we know the number of pages, have the view reflect it
        if (numberOfPages > 0) {
            setNumberOfPages(numberOfPages);
        }
        // compute the page size using the table's preferred size
        pageSize = getTable().getPageSize();
        // now compute the current page offset
        pageOffset = (page - 1) * pageSize;

        // we need to adjust the page size, just for flow view
        final int adjustedPageSize = (getViewType().equals("flow") ? 3 : pageSize);

        ListDatasets query = new ListDatasets();
        query.setOrderBy(uriForSortKey());
        query.setDesc(descForSortKey());
        query.setLimit(adjustedPageSize);
        query.setOffset(pageOffset);
        query.setInCollection(inCollection);
        query.setUser(MMDB.getUsername());
        dispatchAsync.execute(query,
                new AsyncCallback<ListDatasetsResult>() {

                    public void onFailure(Throwable caught) {
                        GWT.log("Error retrieving datasets", null);
                        DialogBox dialogBox = new DialogBox();
                        dialogBox.setText("Oops");
                        dialogBox.add(new Label(MMDB.SERVER_ERROR));
                        dialogBox.setAnimationEnabled(true);
                        dialogBox.center();
                        dialogBox.show();
                    }

                    @Override
                    public void onSuccess(ListDatasetsResult result) {
                        table.removeAllRows();
                        for (DatasetBean dataset : result.getDatasets() ) {
                            GWT.log("Sending event add dataset " + dataset.getTitle(), null);
                            AddNewDatasetEvent event = new AddNewDatasetEvent();
                            event.setDataset(dataset);
                            MMDB.eventBus.fireEvent(event);
                        }
                        table.doneAddingRows(); // FIXME here we're using MVP to deliver add rows but not to stop adding rows. encapsulation issues ...
                        int np = (result.getDatasetCount() / pageSize) + (result.getDatasetCount() % pageSize != 0 ? 1 : 0);
                        setNumberOfPages(np); // this just sets the displayed number of pages in the paging controls.
                        numberOfPages = np;
                    }
                });
    }

    @Override
    protected String getViewTypePreference() {
        return MMDB.DATASET_VIEW_TYPE_PREFERENCE;
    }

    @Override
    public void addItem(String uri, DatasetBean item, String type, int position) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addItem(String uri, DatasetBean item, String type, String sectionUri, String sectionLabel, String sectionMarker) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addItem(String uri, DatasetBean item, String type, int position, String sectionUri, String sectionLabel, String sectionMarker) {
        // TODO Auto-generated method stub

    }
}
