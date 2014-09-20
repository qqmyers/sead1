/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010 , NCSA.  All rights reserved.
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
package edu.illinois.ncsa.mmdb.web.client.view;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetTitle;
import edu.illinois.ncsa.mmdb.web.client.presenter.UploadStatusPresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.ui.ContentCategory;
import edu.illinois.ncsa.mmdb.web.client.ui.EditableLabel;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;
import edu.illinois.ncsa.mmdb.web.client.ui.ProgressBar;
import edu.illinois.ncsa.mmdb.web.client.ui.TagsWidget;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class UploadStatusView extends Composite implements Display {
    VerticalPanel               thePanel;
    HorizontalPanel             progressPanel;
    FlexTable                   statusTable;
    Map<Integer, CheckBox>      selectionCheckboxes;
    private final DispatchAsync dispatchAsync;

    public UploadStatusView(DispatchAsync dispatchAsync) {
        this.dispatchAsync = dispatchAsync;
        thePanel = new VerticalPanel();
        progressPanel = new HorizontalPanel();
        thePanel.add(progressPanel);
        statusTable = new FlexTable();
        selectionCheckboxes = new HashMap<Integer, CheckBox>();
        thePanel.add(statusTable);
        initWidget(thePanel);
    }

    @Override
    public void clear() {
        statusTable.removeAllRows();
    }

    /*
    @Override
    public HasValue<Boolean> getSelectionControl(int ix) {
        if (selectionCheckboxes.get(ix) == null) {
            GWT.log("error: uploadstatus presenter asked for a nonexistent selection control " + ix);
            return null;
        }
        return selectionCheckboxes.get(ix);
    }
    */

    @Override
    public HasValue<Boolean> onComplete(int ix, String uri, int total) {
        GWT.log("onComplete " + ix + " " + uri);
        Anchor anchor = new Anchor("View", "#dataset?id=" + uri);
        anchor.setTarget("_blank");
        CheckBox selectionCheckbox = new CheckBox();
        selectionCheckboxes.put(ix, selectionCheckbox);
        statusTable.setWidget(ix, 0, selectionCheckbox);
        GWT.log("created selection checkbox " + ix); // FIXME debug
        statusTable.setWidget(ix, 1, anchor);
        statusTable.setWidget(ix, 3, new Label("Complete"));
        //
        if (total > 0) {
            int n = ix + 1;
            progressPanel.clear();
            progressPanel.add(new ProgressBar((int) ((float) (n * 100) / (float) total)));
            progressPanel.add(new Label((ix + 1) + " of " + total + " file(s) uploaded"));
            progressPanel.setSpacing(20);
        }
        return selectionCheckbox;
    }

    @Override
    public void onPostComplete(final int ix, final DatasetBean dataset) {
        GWT.log("onPostComplete " + ix + " " + dataset.getFilename());
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            public void execute() {
                // check pending, but don't initially display
                PreviewWidget preview = new PreviewWidget(dataset.getUri(), GetPreviews.SMALL, null/*"dataset?id=" + dataset.getUri()*/, ContentCategory.getCategory(dataset.getMimeType(), dispatchAsync), true, false, dispatchAsync);
                statusTable.setWidget(ix, 1, preview);
                //
                statusTable.setWidget(ix, 2, editableDatasetInfo(dataset));
                TagsWidget tags = new TagsWidget(dataset.getUri(), dispatchAsync, false);
                statusTable.setWidget(ix, 3, tags);
                Anchor anchor = new Anchor("View", "#dataset?id=" + dataset.getUri());
                anchor.setTarget("_blank");
                statusTable.setWidget(ix, 4, anchor);
                statusTable.getCellFormatter().addStyleName(ix, 4, "uploadStatusColumn");
                Anchor hideAnchor = new Anchor("Hide");
                hideAnchor.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        statusTable.getRowFormatter().addStyleName(ix, "hidden");
                    }
                });
                statusTable.setWidget(ix, 5, hideAnchor);
                statusTable.getCellFormatter().addStyleName(ix, 5, "uploadStatusColumn");
            }
        });
        GWT.log("onPostComplete " + ix);
    }

    @Override
    public void onDropped(int ix, String filename, String sizeString) {
        Image image = new Image(PreviewWidget.GRAY_URL.get(GetPreviews.SMALL));
        statusTable.setWidget(ix, 1, image);
        int size = -1;
        try {
            size = Integer.parseInt(sizeString);
        } catch (NumberFormatException x) {
            // FIXME we don't have support for longs, so now what?
        }
        statusTable.setWidget(ix, 2, new Label("Uploading \"" + filename + "\" (" + TextFormatter.humanBytes(size) + ") ..."));
    }

    @Override
    public void onProgress(int ix, int percent) {
        GWT.log("onProgress " + ix + " " + percent + "%");
        statusTable.setWidget(ix, 3, new ProgressBar(percent));
    }

    public Widget editableDatasetInfo(final DatasetBean ds) {
        FlexTable layout = new FlexTable();
        int row = 0;
        layout.setWidget(row, 0, new Label("Title:"));
        final EditableLabel titleLabel = new EditableLabel(ds.getTitle());
        // FIXME this contains dispatching logic, and so should be moved to a presenter
        titleLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(final ValueChangeEvent<String> event) {
                SetTitle change = new SetTitle(ds.getUri(), event.getValue());
                dispatchAsync.execute(change, new AsyncCallback<EmptyResult>() {
                    public void onFailure(Throwable caught) {
                        titleLabel.cancel();
                    }

                    public void onSuccess(EmptyResult result) {
                        titleLabel.setText(event.getValue());
                    }
                });
            }
        });
        layout.setWidget(row, 1, titleLabel);
        row++;
        layout.setWidget(row, 0, new Label("Size:"));
        layout.setWidget(row, 1, new Label(TextFormatter.humanBytes(ds.getSize())));
        row++;
        layout.setWidget(row, 0, new Label("Type:"));
        layout.setWidget(row, 1, new Label(ds.getMimeType()));
        row++;
        layout.setWidget(row, 0, new Label("Date:"));
        layout.setWidget(row, 1, new Label(ds.getDate() + ""));
        return layout;
    }

}
