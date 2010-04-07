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

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedHandler;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;

/**
 * List datasets in repository using a youtube-like list. A one column
 * table that makes it easier to read attributes of each element.
 * 
 * @author Luigi Marini
 */
public class DatasetTableOneColumnView extends DatasetTableView {

    private final static DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getShortDateTimeFormat();

    private final ArrayList<Hyperlink>  datasetLinks     = new ArrayList<Hyperlink>();

    private final String                BLOB_URL         = "./api/image/";
    private final String                PREVIEW_URL      = "./api/image/preview/small/";

    public DatasetTableOneColumnView() {
        super();
        addStyleName("datasetTable");
    }

    public int getPageSize() {
        return 10;
    }

    @Override
    public void addRow(final String id, String name, String type, Date date, String preview, String size, String authorsId) {

        final int row = this.getRowCount();

        GWT.log("Adding dataset " + name + " to row " + row, null);

        PreviewWidget pre = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id);
        pre.setMaxWidth(100);
        setWidget(row, 0, pre);

        VerticalPanel verticalPanel = new VerticalPanel();

        verticalPanel.setSpacing(5);

        setWidget(row, 1, verticalPanel);

        // title
        Hyperlink hyperlink = new Hyperlink(name, "dataset?id=" + id);
        verticalPanel.add(hyperlink);

        // date
        verticalPanel.add(new Label(DATE_TIME_FORMAT.format(date)));

        // size
        verticalPanel.add(new Label(size));

        // author
        verticalPanel.add(new Label(authorsId));

        // type
        verticalPanel.add(new Label(type));

        // FIXME debug
        /*
        Anchor zoomLink = new Anchor("zoom", GWT.getHostPageBaseURL()+"pyramid/uri="+id);
        verticalPanel.add(zoomLink);
        */
        // FIXME end debug

        getCellFormatter().addStyleName(row, 0, "leftCell");
        getCellFormatter().addStyleName(row, 1, "rightCell");
        getCellFormatter().setVerticalAlignment(row, 1, HasVerticalAlignment.ALIGN_TOP); // FIXME move to CSS
        getRowFormatter().addStyleName(row, "oddRow");
    }

    public void doneAddingRows() {
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    public void addDatasetDeletedHandler(DatasetDeletedHandler handler) {
        this.addHandler(handler, DatasetDeletedEvent.TYPE);
    }

    @Override
    public void insertRow(int row, String id, String title, String mimeType, Date date, String previewUri, String size, String authorsId) {

        GWT.log("Inserting dataset " + title + " to row " + row, null);

        PreviewWidget pre = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id);
        pre.setMaxWidth(100);
        setWidget(row, 0, pre);

        VerticalPanel verticalPanel = new VerticalPanel();

        verticalPanel.setSpacing(5);

        setWidget(row, 1, verticalPanel);

        // title
        Hyperlink hyperlink = new Hyperlink(title, "dataset?id=" + id);
        verticalPanel.add(hyperlink);

        // date
        verticalPanel.add(new Label(DATE_TIME_FORMAT.format(date)));

        // size
        verticalPanel.add(new Label(size));

        // author
        verticalPanel.add(new Label(authorsId));

        // type
        verticalPanel.add(new Label(mimeType));

        // FIXME debug
        /*
        Anchor zoomLink = new Anchor("zoom", GWT.getHostPageBaseURL()+"pyramid/uri="+id);
        verticalPanel.add(zoomLink);
        */
        // FIXME end debug

        getCellFormatter().addStyleName(row, 0, "leftCell");
        getCellFormatter().addStyleName(row, 1, "rightCell");
        getCellFormatter().setVerticalAlignment(row, 1, HasVerticalAlignment.ALIGN_TOP); // FIXME move to CSS
        getRowFormatter().addStyleName(row, "oddRow");
    }
}
