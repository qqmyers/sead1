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

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.presenter.BatchOperationPresenter;
import edu.illinois.ncsa.mmdb.web.client.presenter.DatasetTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.view.BatchOperationView;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicTableView;

/**
 * @author lmarini
 *
 */
public class ListDatasetsPage extends Page {

    private final DispatchAsync  dispatch;
    private final HandlerManager eventbus;

    public ListDatasetsPage(DispatchAsync dispatch, HandlerManager eventBus) {
        super("Datasets", dispatch);
        this.dispatch = dispatch;
        this.eventbus = eventBus;

        HorizontalPanel rightHeader = new HorizontalPanel();
        pageTitle.addEast(rightHeader);

        // batch operations
        BatchOperationView batchOperationView = new BatchOperationView();
        batchOperationView.addStyleName("titlePanelRightElement");
        BatchOperationPresenter batchOperationPresenter = new BatchOperationPresenter(dispatch, eventBus, batchOperationView, false);
        batchOperationPresenter.bind();
        rightHeader.add(batchOperationView);

        //add rss in same place as on collections page
        rightHeader.add(getRssFeed());

        DynamicTableView dynamicTableView = new DynamicTableView();
        final DatasetTablePresenter dynamicTablePresenter = new DatasetTablePresenter(dispatch, eventBus, dynamicTableView);
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
    }

    @Override
    public void layout() {
        // TODO Auto-generated method stub

    }

}
