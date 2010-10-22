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

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * View of the selected datasets
 * 
 * @author Luis Mendez
 * 
 */
public class SelectedDatasetsPage extends Page {

    private final FlowPanel                 selectedPanel;

    private final FlowPanel                 leftcolumn;
    private final FlowPanel                 rightcolumn;

    private final int                       num;
    private final Set<DatasetBean>          datasets;

    private final CreateRelationshipsWidget relationshipWidget;

    /**
     * Create an instance of selected datasets view page.
     * 
     * @param dispatchAsync
     */
    public SelectedDatasetsPage(MyDispatchAsync dispatchAsync) {

        super("Selected Datasets", dispatchAsync);

        //user interface
        selectedPanel = new FlowPanel();
        selectedPanel.addStyleName("selectedMainContainer");

        leftcolumn = new FlowPanel();
        leftcolumn.addStyleName("selectedLeftContainer");
        selectedPanel.add(leftcolumn);

        rightcolumn = new FlowPanel();
        rightcolumn.addStyleName("selectedRightContainer");
        selectedPanel.add(rightcolumn);

        SimplePanel clearFloat = new SimplePanel();
        clearFloat.addStyleName("clearFloat");
        selectedPanel.add(clearFloat);

        //view selected datasets
        final Set<String> selectedDatasets = new HashSet<String>(MMDB.getSessionState().getSelectedDatasets());
        datasets = new HashSet<DatasetBean>();

        num = MMDB.getSessionState().getSelectedDatasets().size();
        leftcolumn.add(new Label("Showing " + num + " selected datasets"));

        for (String datasetUri : selectedDatasets ) {
            fetchDataset(datasetUri);
        }

        //create relationship - widget
        relationshipWidget = new CreateRelationshipsWidget(datasets, selectedDatasets, dispatchAsync);
        if (selectedDatasets.size() == 1) {
            rightcolumn.add(new Label("You cannot create a relationship with 1 dataset"));
        } else {
            rightcolumn.add(relationshipWidget);
        }

        mainLayoutPanel.add(selectedPanel);

    }

    private void fetchDataset(String uri) {

        dispatchAsync.execute(new GetDataset(uri), new AsyncCallback<GetDatasetResult>() {

            DatasetBean db = new DatasetBean();

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error getting recent activity");
            }

            @Override
            public void onSuccess(GetDatasetResult result) {

                final String value = result.getDataset().getUri();
                db = result.getDataset();
                datasets.add(db);
                leftcolumn.add(new DatasetInfoWidget(result.getDataset(), true));

                relationshipWidget.addToList(shortenTitle(result.getDataset().getTitle()), value);
            }
        });
    }

    private String shortenTitle(String title) {
        if (title != null && title.length() > 22) {
            return title.substring(0, 20) + "...";
        } else {
            return title;
        }
    }

    @Override
    public void layout() {
        // TODO Auto-generated method stub

    }
}