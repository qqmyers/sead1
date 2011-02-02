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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
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
        return 25;
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
    public void addRow(final String id, String title, String mimeType, Date date,
            String previewUri, String size, String authorsId) {
        PreviewWidget pw = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id, mimeType);
        pw.setWidth("120px");
        pw.setMaxWidth(100);
        // selection checkbox
        final CheckBox checkBox = new CheckBox();
        checkBox.addStyleName("inline");
        // HACK since this view doesn't have it's own presenter
        checkBox.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (checkBox.getValue()) {
                    DatasetSelectedEvent datasetSelected = new DatasetSelectedEvent();
                    datasetSelected.setUri(id);
                    MMDB.eventBus.fireEvent(datasetSelected);
                } else {
                    DatasetUnselectedEvent datasetUnselected = new DatasetUnselectedEvent();
                    datasetUnselected.setUri(id);
                    MMDB.eventBus.fireEvent(datasetUnselected);
                }

            }
        });

        UserSessionState sessionState = MMDB.getSessionState();
        if (sessionState.getSelectedDatasets().contains(id)) {
            checkBox.setValue(true);
        } else {
            checkBox.setValue(false);
        }

        MMDB.eventBus.addHandler(DatasetUnselectedEvent.TYPE, new DatasetUnselectedHandler() {

            @Override
            public void onDatasetUnselected(DatasetUnselectedEvent datasetUnselectedEvent) {
                if (id.equals(datasetUnselectedEvent.getUri())) {
                    checkBox.setValue(false);
                }

            }

        });
        // title label
        Label titleLabel = new Label(shortenTitle(title));
        titleLabel.addStyleName("smallText");
        titleLabel.addStyleName("inline");
        titleLabel.setWidth("120px");
        int row = n / WIDTH;
        int col = n % WIDTH;
        setWidget(row * 2, col, pw);
        getCellFormatter().addStyleName(row * 2, col, "gridPreviewSmall");
        // title panel
        FlowPanel titlePanel = new FlowPanel();
        titlePanel.addStyleName("dynamicGridElementTitle");
        titlePanel.add(checkBox);
        titlePanel.add(titleLabel);
        // clear both for IE
        SimplePanel clear = new SimplePanel();
        clear.addStyleName("clearFloat");
        titlePanel.add(clear);
        setWidget((row * 2) + 1, col, titlePanel);
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

    @Override
    public void insertRow(int row, String id, String title, String mimeType, Date date, String previewUri, String size, String authorsId, String sectionUri, String sectionLabel, String sectionMarker) {
        // TODO Auto-generated method stub

    }
}
