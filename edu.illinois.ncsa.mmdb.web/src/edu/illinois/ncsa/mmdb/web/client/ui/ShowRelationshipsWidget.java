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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRelationship;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRelationshipResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class ShowRelationshipsWidget extends Composite {
    private final FlowPanel mainContainer;
    private final FlexTable previews;
    int                     count;
    DisclosurePanel         disclosurePanel;

    public ShowRelationshipsWidget(String uri, MyDispatchAsync service) {
        this(uri, service, true);
    }

    public ShowRelationshipsWidget(final String uri, final MyDispatchAsync service, boolean withTitle) {

        count = 0;

        mainContainer = new FlowPanel();
        mainContainer.addStyleName("datasetRightColSection");
        mainContainer.setVisible(false);
        initWidget(mainContainer);

        if (withTitle) {
            Label titleLabel = new Label("Relationships");
            titleLabel.addStyleName("datasetRightColHeading");
            mainContainer.add(titleLabel);
        }

        disclosurePanel = new DisclosurePanel("Relates To (" + count + ")");
        disclosurePanel.setVisible(true);
        disclosurePanel.setAnimationEnabled(false);

        previews = new FlexTable();
        previews.setWidth("150px");

        disclosurePanel.add(previews);

        mainContainer.add(disclosurePanel);

        service.execute(new GetRelationship(uri), new AsyncCallback<GetRelationshipResult>() {
            @Override
            public void onFailure(Throwable arg0) {
                // TODO Auto-generated method stub
                GWT.log("ERROR SHOWING RELATIONSHIPS");
            }

            @Override
            public void onSuccess(GetRelationshipResult arg0) {
                HashMap<DatasetBean, String> rt = arg0.getRelationships();
                for (Map.Entry<DatasetBean, String> db : rt.entrySet() ) {
                    DatasetBean uri = db.getKey();
                    //String type = db.getValue();
                    addDataset(uri);
                }

                count = rt.size();

                disclosurePanel.getHeaderTextAccessor().setText("Relates To (" + count + ")");
                mainContainer.setVisible(previews.getRowCount() > 0);

            }
        });

    }

    private void addDataset(DatasetBean ds) {
        String url = "dataset?id=" + ds.getUri();
        PreviewWidget pw = new PreviewWidget(ds.getUri(), GetPreviews.SMALL, url);
        String title = ds.getTitle();
        title = title.length() > 15 ? title.substring(0, 15) + "..." : title;
        Hyperlink link = new Hyperlink(title, url);
        int n = previews.getRowCount();

        previews.setWidget(n++, 0, pw);
        previews.setWidget(n++, 0, link);
    }

    //Disclosure panels created dynamically based on relationship type
    @SuppressWarnings("unused")
    private void createPanel(String type) {
        //types.add(type);
        DisclosurePanel disclosurePanel = new DisclosurePanel(type);
        disclosurePanel.setAnimationEnabled(true);
        mainContainer.add(disclosurePanel);
    }

}
