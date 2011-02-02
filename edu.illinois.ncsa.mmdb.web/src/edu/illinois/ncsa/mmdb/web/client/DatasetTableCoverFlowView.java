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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;

public class DatasetTableCoverFlowView extends DatasetTableView {
    int n = 0;

    public DatasetTableCoverFlowView() {
        super();
        setWidth("715px");
    }

    @Override
    public void removeAllRows() {
        super.removeAllRows();
        n = 0;
    }

    @Override
    public void addRow(String id, String title, String mimeType, Date date, String previewUri, String size, String authorsId) {
        VerticalPanel panel = new VerticalPanel();
        PreviewWidget preview = null;
        Label titleLabel = new Label(title);
        if (n++ == 1) {
            preview = new PreviewWidget(id, GetPreviews.LARGE, "dataset?id=" + id);
            preview.setWidth("400px");
            preview.setMaxWidth(400);
            getCellFormatter().addStyleName(0, n, "flowPreviewLarge");
            titleLabel.addStyleName("flowLabelLarge");
        } else {
            preview = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id);
            preview.setMaxWidth(150);
            preview.setWidth("150px");
            getCellFormatter().addStyleName(0, n, "flowPreviewSmall");
            titleLabel.addStyleName("flowLabelSmall");
        }
        panel.add(preview);
        panel.add(titleLabel);
        this.setWidget(0, n, panel);
    }

    public void doneAddingRows() {
    }

    @Override
    public int getPageSize() {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public Widget asWidget() {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public void insertRow(int position, String id, String title, String mimeType, Date date, String previewUri, String size, String authorsId) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getSelectedDatasets() {
        List<String> selectedDataset = new ArrayList<String>();
        return selectedDataset;
    }

    @Override
    public HasClickHandlers getShowSelectedAnchor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertRow(int row, String id, String title, String mimeType, Date date, String previewUri, String size, String authorsId, String sectionUri, String sectionLabel, String sectionMarker) {
        // TODO Auto-generated method stub

    }

}
