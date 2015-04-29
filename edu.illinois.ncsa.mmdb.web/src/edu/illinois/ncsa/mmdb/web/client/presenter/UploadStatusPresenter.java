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
package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.event.AllDatasetsUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.ui.DatasetSelectionCheckboxHandler;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * This presenter manages the relationship between DND upload applet callbacks
 * and the display of upload status for a batch of uploads
 *
 * @author futrelle
 *
 */
public class UploadStatusPresenter extends BasePresenter<UploadStatusPresenter.Display> {
    private final DispatchAsync dispatch;

    private int                 nDropped  = 0;
    Set<Integer>                completed = new HashSet<Integer>();

    public UploadStatusPresenter(DispatchAsync dispatch, HandlerManager eventBus, Display display) {
        super(display, dispatch, eventBus);
        this.dispatch = dispatch;
    }

    public interface Display {
        void clear();

        void onDropped(int ix, String filename, String sizeString);

        void onProgress(int ix, int percent);

        HasValue<Boolean> onComplete(int ix, String uri, int total);

        void onPostComplete(int ix, DatasetBean dataset);

        //HasValue<Boolean> getSelectionControl(int ix);
    }

    /**
     * Applet calls back for all files dropped, then for each file in order,
     * <ol>
     * <li>calls back with progress, repeatedly</li>
     * <li>calls back on completion with dataset uri</li>
     * </ol>
     *
     * @param count
     *
     * @param file
     */
    public void onDropped(int count, String filename, String sizeString) {
        if (nDropped == 0) {
            nDropped = 0;
            display.clear();
            // clear the selection
            eventBus.fireEvent(new AllDatasetsUnselectedEvent());
        }
        nDropped++;
        display.onDropped(count, filename, sizeString);
    }

    public void onProgressIndex(int percent, int index) {
        if (!completed.contains(index)) {
            display.onProgress(index, percent);
        }
    }

    public void onComplete(final String uri, final int ix) {
        completed.add(ix);
        final HasValue<Boolean> selectionControl = display.onComplete(ix, uri, nDropped);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            public void execute() {
                selectionControl.addValueChangeHandler(new DatasetSelectionCheckboxHandler(uri, eventBus));
                selectionControl.setValue(true, true); // select, and fire the selection event
                // checkbox should also respond to selection and deselection events coming from elsewhere
                addHandler(DatasetSelectedEvent.TYPE, new DatasetSelectedHandler() {
                    public void onDatasetSelected(DatasetSelectedEvent event) {
                        if (event.getUri().equals(uri)) {
                            selectionControl.setValue(true, false); // select, but don't fire another selection event
                        }
                    }
                });
                addHandler(DatasetUnselectedEvent.TYPE, new DatasetUnselectedHandler() {
                    public void onDatasetUnselected(DatasetUnselectedEvent datasetUnselectedEvent) {
                        if (datasetUnselectedEvent.getUri().equals(uri)) {
                            selectionControl.setValue(false, false); // deselect, but don't fire another deselection event
                        }
                    }
                });
                fetchDataset(ix, uri);
            }
        });
    }

    void fetchDataset(final int ix, final String uri) {
        //Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        //public void execute() {
        dispatch.execute(new GetDataset(uri, MMDB.getUsername()), new AsyncCallback<GetDatasetResult>() {
            public void onFailure(Throwable caught) {
                Window.alert("GetDataset failed for " + uri); // FIXME debug
            }

            public void onSuccess(GetDatasetResult result) {
                GWT.log("fetchDataset " + ix);
                display.onPostComplete(ix, result.getDataset());
            }
        });
        //}
        //});
    }

    @Override
    public void unbind() {
    }
}
