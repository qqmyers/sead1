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

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PagingDatasetTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.PagingSearchResultsTableView;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Search;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SearchResult;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.place.PlaceService;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Page to handle search results.
 * 
 * @author Luigi Marini
 * 
 */
public class SearchResultsPage extends Page {

    private static final String          TITLE = "Search Results";
    private PagingSearchResultsTableView datasetTableView;
    private final HandlerManager         eventbus;
    private HTML                         queryText;
    private final String                 query;
    private PagingDatasetTablePresenter  datasetTablePresenter;

    public SearchResultsPage(MyDispatchAsync dispatchasync, HandlerManager eventbus) {
        super(TITLE, dispatchasync);
        this.eventbus = eventbus;
        query = PlaceService.getParams().get("q");
        if (query != null) {
            queryText = new HTML("Your search for <b>" + query
                    + "</b> returned the following results:");
            mainLayoutPanel.add(queryText);
            queryServer(query);
        }
    }

    private void queryServer(String query) {
        dispatchAsync.execute(new Search(query), new AsyncCallback<SearchResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error executing search", caught);
            }

            @Override
            public void onSuccess(SearchResult result) {
                if (result.getHits().size() == 0) {
                    noResults();
                } else {
                    showResults(result);
                }
            }
        });
    }

    /**
     * Show message when no results are found for a particular query.
     */
    protected void noResults() {
        if (queryText != null) {
            mainLayoutPanel.remove(queryText);
        }
        if (datasetTableView != null) {
            mainLayoutPanel.remove(datasetTableView);
        }
        queryText = new HTML("Your search for <b>" + query
                + "</b> returned no results. Please try a different search.");
        mainLayoutPanel.add(queryText);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.illinois.ncsa.mmdb.web.client.ui.Page#layout()
     */
    @Override
    public void layout() {

    }

    protected void showResults(SearchResult result) {
        // paged table of datasets
        datasetTableView = new PagingSearchResultsTableView();
        datasetTableView.addStyleName("datasetTable");
        datasetTablePresenter =
                new PagingDatasetTablePresenter(datasetTableView, eventbus);
        datasetTablePresenter.bind();
        mainLayoutPanel.add(datasetTableView);

        final List<String> hits = result.getHits();
        for (final String hit : hits ) {
            MMDB.dispatchAsync.execute(new GetDataset(hit),
                    new AsyncCallback<GetDatasetResult>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            GWT.log("Error getting dataset", caught);
                        }

                        @Override
                        public void onSuccess(GetDatasetResult result) {
                            DatasetBean dataset = result.getDataset();
                            AddNewDatasetEvent event = new AddNewDatasetEvent();
                            event.setDataset(dataset);
                            int position = hits.indexOf(hit);
                            event.setPosition(position);
                            MMDB.eventBus.fireEvent(event);
                            GWT.log("Event add dataset " + dataset.getTitle() +
                                    " with position " + position + " sent", null);
                        }
                    });
        }
    }
}
