/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010 , NCSA.  All rights reserved.
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
import java.util.Iterator;
import java.util.List;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.place.PlaceService;
import edu.illinois.ncsa.mmdb.web.client.presenter.BatchOperationPresenter;
import edu.illinois.ncsa.mmdb.web.client.presenter.SearchTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.view.BatchOperationView;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicTableView;

/**
 * @author lmarini
 * @author myersjd@umich.edu
 *
 */
public class ListSearchResultsPage extends Page {

    private final DispatchAsync  dispatch;
    private final HandlerManager eventbus;
    private final static String  HTTPSTRING = "http://";

    public ListSearchResultsPage(DispatchAsync dispatch, HandlerManager eventBus) {
        super("Search Results", dispatch);
        this.dispatch = dispatch;
        this.eventbus = eventBus;

        mainLayoutPanel.addStyleName("searchpage");
        HorizontalPanel rightHeader = new HorizontalPanel();
        pageTitle.addEast(rightHeader);

        String query = PlaceService.getParams().get("q");

        String filter = PlaceService.getParams().get("f");

        addSearchInformation(mainLayoutPanel, filter, query);

        // batch operations
        BatchOperationView batchOperationView = new BatchOperationView();
        batchOperationView.addStyleName("titlePanelRightElement");
        BatchOperationPresenter batchOperationPresenter = new BatchOperationPresenter(dispatch, eventBus, batchOperationView, false);
        batchOperationPresenter.bind();
        rightHeader.add(batchOperationView);

        DynamicTableView dynamicTableView = new DynamicTableView();
        final SearchTablePresenter dynamicTablePresenter = new SearchTablePresenter(dispatch, eventBus, dynamicTableView, filter, query);
        dynamicTablePresenter.bind();

        VerticalPanel vp = new VerticalPanel() {
            @Override
            protected void onDetach() {
                dynamicTablePresenter.unbind();
            }
        };
        vp.add(dynamicTableView.asWidget());
        vp.addStyleName("tableCenter");
        mainLayoutPanel.add(vp);
        mainLayoutPanel.addStyleName("search-page");
    }

    private void addSearchInformation(FlowPanel basePanel, final String filter, final String query) {
        String queryText = "";
        final HTML searchResultsHtml = new HTML();
        basePanel.add(searchResultsHtml);

        List<String> tags = new ArrayList<String>();
        List<String> textTerms = new ArrayList<String>();

        if (filter == null) {
            for (String s : query.split("\\s+") ) {
                if (s.startsWith("tag:")) {
                    tags.add(s.substring(4));
                } else {
                    textTerms.add(s);
                }
                queryText = "Search results for ";
                Iterator<String> it = tags.iterator();
                while (it.hasNext()) {
                    queryText += "tag=<b>" + it.next() + "</b>";
                    if (it.hasNext() || !textTerms.isEmpty()) {
                        queryText += " AND ";
                    }
                }
                Iterator<String> it2 = textTerms.iterator();
                if (it2.hasNext()) {
                    queryText += "text term" + ((textTerms.size() > 1) ? "s: <b>" : ": <b>");

                    while (it2.hasNext()) {
                        queryText += it2.next();
                        if (it2.hasNext()) {
                            queryText += ", ";
                        }
                    }
                    queryText += "</b>";
                }
            }
            searchResultsHtml.setHTML(queryText);
        } else {
            UserMetadataWidget.initReadableFields(dispatch, new AsyncCallback<ListUserMetadataFieldsResult>() {

                @Override
                public void onSuccess(ListUserMetadataFieldsResult result) {
                    // TODO Auto-generated method stub
                    String queryText = "Search results for: <b>" + UserMetadataWidget.getFieldLabel(filter);
                    queryText += " = ";
                    if (query.contains(MMDB._vivoIdentifierUri)) {
                        //VIVO URLs are of the form '<URI> : <name>'
                        int sep = query.indexOf(" : ");
                        queryText += "<a href=\"" + query.substring(sep + 3) + "\">" + query.substring(0, sep) + "</a></b>";
                    } else {
                        queryText += query + "</b>";
                    }
                    searchResultsHtml.setHTML(queryText);
                }

                @Override
                public void onFailure(Throwable caught) {
                    // TODO Auto-generated method stub

                }
            });
        }
    }

    @Override
    public void layout() {
        // TODO Auto-generated method stub

    }

}
