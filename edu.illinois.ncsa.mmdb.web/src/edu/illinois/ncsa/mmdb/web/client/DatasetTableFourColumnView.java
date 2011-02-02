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
package edu.illinois.ncsa.mmdb.web.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * List datasets in repository.
 * 
 * @author Luigi Marini
 */
public class DatasetTableFourColumnView extends DatasetTableView {

    private final static DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getShortDateTimeFormat();

    private final ArrayList<Hyperlink>  datasetLinks     = new ArrayList<Hyperlink>();

    private final String                BLOB_URL         = "./api/image/";
    private final String                PREVIEW_URL      = "./api/image/preview/small/";

    public DatasetTableFourColumnView() {
        super();
        this.setWidget(0, 0, new Label("Name"));
        this.setWidget(0, 1, new Label("Type"));
        this.setWidget(0, 2, new Label("Date"));
        this.setWidget(0, 3, new Label(""));
        addStyleName("datasetTable");
        getRowFormatter().addStyleName(0, "topRow");
    }

    public int getPageSize() {
        return 10;
    }

    @Override
    public void addRow(String id, String name, String type, Date date, String preview, String size, String authorsId) {
        GWT.log("Adding dataset " + name, null);
        int row = this.getRowCount();
        Hyperlink hyperlink = new Hyperlink(name, "dataset?id=" + id);
        datasetLinks.add(hyperlink);
        this.setWidget(row, 0, hyperlink);

        this.setWidget(row, 1, new Label(type));
        this.setWidget(row, 2, new Label(DATE_TIME_FORMAT.format(date)));
        this.setWidget(row, 3, new Image(PREVIEW_URL + id));

        /*
        if (preview != null) {
        	this.setWidget(row, 3, new Image(BLOB_URL + preview));
        } else {
        	this.setWidget(row, 3, new SimplePanel());
        }
        */

        for (int col = 0; col < 4; col++ ) {
            getCellFormatter().addStyleName(row, col, "cell");
        }

        if (row % 2 == 0) {
            getRowFormatter().addStyleName(row, "evenRow");
        } else {
            getRowFormatter().addStyleName(row, "oddRow");
        }
    }

    public void doneAddingRows() {
    }

    @Override
    public Widget asWidget() {
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
