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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.event.AllOnPageSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AllOnPageSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
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

    private final Map<CheckBox, String> checkBoxes;

    private final DispatchAsync         dispatchAsync;

    /**
     * TODO dispatch is currently required because PreviewWidget does not have a
     * presenter
     * 
     * @param dispatchAsync
     */
    public DatasetTableOneColumnView(DispatchAsync dispatchAsync) {
        super();
        this.dispatchAsync = dispatchAsync;
        addStyleName("datasetTable");
        checkBoxes = new HashMap<CheckBox, String>();
    }

    public int getPageSize() {
        return 10;
    }

    @Override
    public void addRow(final String id, String name, String type, Date date, String preview, String size, String authorsId) {

        final int row = this.getRowCount();

        GWT.log("Adding dataset " + name + " to row " + row, null);

        // selection checkbox
        final CheckBox checkBox = new CheckBox();
        checkBoxes.put(checkBox, id);
        setWidget(row, 0, checkBox);

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
        if (sessionState.getSelectedItems().contains(id)) {
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

        MMDB.eventBus.addHandler(AllOnPageSelectedEvent.TYPE, new AllOnPageSelectedHandler() {

            @Override
            public void onAllOnPageSelected(AllOnPageSelectedEvent event) {
                DatasetSelectedEvent ue = new DatasetSelectedEvent();
                ue.setUri(id);
                MMDB.eventBus.fireEvent(ue);
                checkBox.setValue(true);
            }

        });

        // preview
        PreviewWidget pre = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id, type, dispatchAsync);
        pre.setMaxWidth(100);
        setWidget(row, 1, pre);

        // generic information
        VerticalPanel verticalPanel = new VerticalPanel();

        verticalPanel.setSpacing(5);

        setWidget(row, 2, verticalPanel);

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

        getCellFormatter().addStyleName(row, 0, "rightCell");
        getCellFormatter().addStyleName(row, 1, "rightCell");
        getCellFormatter().addStyleName(row, 2, "rightCell");
        //        getCellFormatter().setVerticalAlignment(row, 1, HasVerticalAlignment.ALIGN_TOP); // FIXME move to CSS
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

        PreviewWidget pre = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id, mimeType, dispatchAsync);
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

    /**
     * Adds information about a section of a dataset and the dataset itself.
     * 
     * @param uri
     * @param title
     * @param type
     * @param date
     * @param previewUri
     * @param size
     * @param authorsId
     * @param sectionUri
     * @param sectionLabel
     * @param sectionMarker
     */
    @Override
    public void insertRow(int row, String id, String title, String mimeType, Date date, String previewUri, String size, String authorsId, String sectionUri, String sectionLabel, String sectionMarker) {

        GWT.log("Inserting dataset " + title + " to row " + row, null);

        PreviewWidget pre = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id, mimeType, dispatchAsync);
        pre.setMaxWidth(100);
        setWidget(row, 0, pre);

        VerticalPanel verticalPanel = new VerticalPanel();

        verticalPanel.setSpacing(5);

        setWidget(row, 1, verticalPanel);

        // page link
        Hyperlink pagelink = new Hyperlink(sectionLabel + " " + sectionMarker, URL.encode("dataset?id=" + id + "&section=" + sectionLabel + " " + sectionMarker));
        verticalPanel.add(pagelink);

        // title
        Hyperlink datasetHyperlink = new Hyperlink(title, "dataset?id=" + id);
        verticalPanel.add(datasetHyperlink);

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

    @Override
    public List<String> getSelectedDatasets() {
        List<String> selectedDataset = new ArrayList<String>();
        for (CheckBox checkBox : checkBoxes.keySet() ) {
            if (checkBox.getValue()) {
                selectedDataset.add(checkBoxes.get(checkBox));
            }
        }
        return selectedDataset;
    }

    @Override
    public HasClickHandlers getShowSelectedAnchor() {
        // TODO Auto-generated method stub
        return null;
    }
}
