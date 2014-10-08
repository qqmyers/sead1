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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasValue;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.UserSessionState;
import edu.illinois.ncsa.mmdb.web.client.event.AllOnPageSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AllOnPageSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.ClearDatasetsEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ClearDatasetsHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetsDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetsDeletedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.RefreshEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ShowItemEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ShowItemEventHandler;
import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.ui.DatasetSelectionCheckboxHandler;

/**
 * Show contents of a {@link DynamicTablePresenter} as a grid.
 * 
 * @author Luigi Marini
 * 
 */
public class DynamicGridPresenter extends BasePresenter<DynamicGridPresenter.Display> {

    /** Map from uri to location in view **/
    private final Map<String, Integer> items;

    public interface Display {
        HasValue<Boolean> getSelected(int location);

        Set<HasValue<Boolean>> getCheckBoxes();

        void removeAllRows();

        int insertItem(String id, String title, String type, String author, Date date);

        void showSelected(boolean checked, int location);
    }

    public DynamicGridPresenter(DispatchAsync dispatch, HandlerManager eventBus, Display display) {
        super(display, dispatch, eventBus);
        this.items = new HashMap<String, Integer>();
    }

    @Override
    public void bind() {
        // FIXME this binding logic is duplicated as a tile in DynamicListPresenter ... needs refactoring

        addHandler(DatasetUnselectedEvent.TYPE, new DatasetUnselectedHandler() {

            @Override
            public void onDatasetUnselected(DatasetUnselectedEvent datasetUnselectedEvent) {
                String uri = datasetUnselectedEvent.getUri();
                if (items.containsKey(uri)) {
                    HasValue<Boolean> selected = display.getSelected(items.get(uri));
                    display.showSelected(false, items.get(uri));
                    selected.setValue(false);
                }
            }
        });

        addHandler(DatasetSelectedEvent.TYPE, new DatasetSelectedHandler() {

            @Override
            public void onDatasetSelected(DatasetSelectedEvent datasetSelectedEvent) {
                String uri = datasetSelectedEvent.getUri();
                if (items.containsKey(uri)) {
                    HasValue<Boolean> selected = display.getSelected(items.get(uri));
                    display.showSelected(true, items.get(uri));
                    selected.setValue(true);
                }
            }
        });

        addHandler(AllOnPageSelectedEvent.TYPE, new AllOnPageSelectedHandler() {
            @Override
            public void onAllOnPageSelected(AllOnPageSelectedEvent event) {
                for (String uri : items.keySet() ) {
                    DatasetSelectedEvent se = new DatasetSelectedEvent();
                    se.setUri(uri);
                    eventBus.fireEvent(se);
                }
            }
        });

        addHandler(ShowItemEvent.TYPE, new ShowItemEventHandler() {

            @Override
            public void onShowItem(ShowItemEvent showItemEvent) {
                GWT.log("Adding item to grid " + showItemEvent.getId());
                addItem(showItemEvent);
            }

        });

        addHandler(ClearDatasetsEvent.TYPE, new ClearDatasetsHandler() {

            @Override
            public void onClearDatasets(ClearDatasetsEvent event) {
                display.removeAllRows();
                items.clear();
            }
        });

        addHandler(DatasetDeletedEvent.TYPE, new DatasetDeletedHandler() {
            @Override
            public void onDeleteDataset(DatasetDeletedEvent event) {
                if (items.containsKey(event.getDatasetUri())) {
                    eventBus.fireEvent(new RefreshEvent());
                }
            }
        });

        addHandler(DatasetsDeletedEvent.TYPE, new DatasetsDeletedHandler() {
            @Override
            public void onDatasetsDeleted(DatasetsDeletedEvent event) {
                for (String datasetUri : event.getUris() ) {
                    if (items.containsKey(datasetUri)) {
                        eventBus.fireEvent(new RefreshEvent());
                        return;
                    }
                }
            }
        });
    }

    public void addItem(final ShowItemEvent showItemEvent) {
        String id = showItemEvent.getId();
        int location = display.insertItem(id, showItemEvent.getTitle(), showItemEvent.getType(), showItemEvent.getAuthor(), showItemEvent.getDate());
        items.put(id, location);
        final HasValue<Boolean> selected = display.getSelected(location);
        selected.addValueChangeHandler(new DatasetSelectionCheckboxHandler(id, eventBus));
        UserSessionState sessionState = MMDB.getSessionState();
        if (sessionState.getSelectedItems().contains(showItemEvent.getId())) {
            selected.setValue(true);
            display.showSelected(true, location);
        } else {
            selected.setValue(false);
        }
    }
}
