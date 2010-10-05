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

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;

/**
 * View of the selected datasets
 * 
 * @author Luigi Marini
 * 
 */
public class SelectedDatasetsPage extends Page {

    private final FlowPanel selectedPanel;

    private final int       num;

    /**
     * Create an instance of selected datasets view page.
     * 
     * @param dispatchAsync
     */
    public SelectedDatasetsPage(DispatchAsync dispatchAsync) {

        super("Selected Datasets", dispatchAsync);
        selectedPanel = new FlowPanel();
        final Set<String> selectedDatasets = new HashSet<String>(MMDB.getSessionState().getSelectedDatasets());

        num = MMDB.getSessionState().getSelectedDatasets().size();
        selectedPanel.add(new Label("Showing " + num + " selected datasets"));

        for (String datasetUri : selectedDatasets ) {
            fetchDataset(datasetUri);
        }

        mainLayoutPanel.add(selectedPanel);

    }

    private void fetchDataset(String uri) {

        dispatchAsync.execute(new GetDataset(uri), new AsyncCallback<GetDatasetResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error getting recent activity");
            }

            @Override
            public void onSuccess(GetDatasetResult result) {

                selectedPanel.add(new DatasetInfoWidget(result.getDataset()));

            }
        });

    }

    @Override
    public void layout() {
        // TODO Auto-generated method stub

    }
}
