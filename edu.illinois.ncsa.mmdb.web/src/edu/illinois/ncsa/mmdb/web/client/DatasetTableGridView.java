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
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedHandler;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;

public class DatasetTableGridView extends DatasetTableView {

    int       n     = 0;
    final int WIDTH = 5;

    public DatasetTableGridView() {
        super();
        addStyleName("datasetTable");
    }

    @Override
    public void removeAllRows() {
        super.removeAllRows();
        n = 0;
    }

    public int getPageSize() {
        return 35;
    }

    String shortenTitle(String title) {
        if (title.length() > 15) {
            return title.substring(0, 15) + "...";
        } else {
            return title;
        }
    }

    // misnomer here, when we get an "addRow" we're really adding a next
    // cell in a top-to-bottom, left-to-right traversal of the table.
    @Override
    public void addRow(String id, String title, String mimeType, Date date,
            String previewUri, String size, String authorsId) {
        PreviewWidget pw = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id);
        pw.setWidth("120px");
        pw.setMaxWidth(100);
        Label t = new Label(shortenTitle(title));
        t.addStyleName("smallText");
        t.setWidth("120px");
        int row = n / WIDTH;
        int col = n % WIDTH;
        setWidget(row * 2, col, pw);
        getCellFormatter().addStyleName(row * 2, col, "gridPreviewSmall");
        setWidget((row * 2) + 1, col, t);
        getCellFormatter().addStyleName((row * 2) + 1, col, "gridLabelSmall");
        n++;
    }

    public void doneAddingRows() {
        for (int i = n; i < getPageSize(); i++ ) {
            int row = i / WIDTH;
            int col = i % WIDTH;
            try {
                clearCell(row * 2, col);
                clearCell((row * 2) + 1, col);
            } catch (IndexOutOfBoundsException x) {
                // this is normal and means there are no more cells to clear.
                return;
            }
        }
    }

    @Override
    public Widget asWidget() {
        // TODO Auto-generated method stub
        return this;
    }

    public void addDatasetDeletedHandler(DatasetDeletedHandler handler) {
        this.addHandler(handler, DatasetDeletedEvent.TYPE);
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
}
