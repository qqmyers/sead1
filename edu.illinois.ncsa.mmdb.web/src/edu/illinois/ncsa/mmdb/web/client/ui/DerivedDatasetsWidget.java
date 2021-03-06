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

import java.util.List;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDerivedFrom;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDerivedFromResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class DerivedDatasetsWidget extends Composite {
    private final FlowPanel     mainContainer;
    private final FlexTable     previews;

    private final String        uri;
    private final DispatchAsync service;

    public DerivedDatasetsWidget(String uri, DispatchAsync service) {
        this(uri, true, service);
    }

    public DerivedDatasetsWidget(final String uri, boolean withTitle, DispatchAsync service) {
        this.service = service;
        this.uri = uri;

        mainContainer = new FlowPanel();
        mainContainer.addStyleName("datasetRightColSection");
        mainContainer.setVisible(false);
        initWidget(mainContainer);

        if (withTitle) {
            Label titleLabel = new Label("Derived from");
            titleLabel.addStyleName("datasetRightColHeading");
            mainContainer.add(titleLabel);
        }

        previews = new FlexTable();
        previews.setWidth("150px");
        mainContainer.add(previews);
    }

    public void showDepth(int depth) {
        previews.removeAllRows();
        showLevel(uri, depth);
    }

    private void showLevel(final String uri, final int level) {
        service.execute(new GetDerivedFrom(uri), new AsyncCallback<GetDerivedFromResult>() {
            @Override
            public void onFailure(Throwable arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onSuccess(GetDerivedFromResult arg0) {
                List<DatasetBean> df = arg0.getDerivedFrom();
                for (DatasetBean d : df ) {
                    addDataset(d);
                    if (level > 0) {
                        showLevel(d.getUri(), level - 1);
                    }
                }
                mainContainer.setVisible(previews.getRowCount() > 0);
            }
        });
    }

    private void addDataset(DatasetBean ds) {
        String url = "dataset?id=" + ds.getUri();
        PreviewWidget pw = new PreviewWidget(ds.getUri(), GetPreviews.SMALL, url, service);
        String title = ds.getTitle();
        title = title.length() > 15 ? title.substring(0, 15) + "..." : title;
        Hyperlink link = new Hyperlink(title, url);
        link.setStyleName("dataLink");
        link.setTitle(ds.getTitle());
        int n = previews.getRowCount();
        if (n > 0) {
            previews.setWidget(n++, 0, new Label("which was derived from:"));
        }
        previews.setWidget(n++, 0, pw);
        previews.setWidget(n++, 0, link);
    }
}
