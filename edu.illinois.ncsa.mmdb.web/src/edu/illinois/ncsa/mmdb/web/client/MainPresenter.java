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

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Main presenter mananges list of datasets and single datasets.
 * 
 * @author Luigi Marini
 * 
 */
// FIXME dead code
public class MainPresenter extends BasePresenter<MainView> {

    private final DatasetTablePresenter tablePresenter;
    private final DatasetPresenter      datasetPresenter;
    private Presenter                   currentPresenter;

    public MainPresenter(MainView display, DispatchAsync dispatchAsync, HandlerManager eventBus,
            DatasetTablePresenter tablePresenter,
            DatasetPresenter datasetPresenter) {
        super(display, dispatchAsync, eventBus);
        this.tablePresenter = tablePresenter;
        this.datasetPresenter = datasetPresenter;

        switchPresenter(tablePresenter);
    }

    interface MainViewInterface extends View {

    }

    @Override
    public void bind() {
        super.bind();
        eventBus.addHandler(DatasetSelectedEvent.TYPE, new DatasetSelectedHandler() {

            @Override
            public void onDatasetSelected(DatasetSelectedEvent event) {
                doShowDataset(event.getDataset());
            }
        });
    }

    protected void doShowDataset(DatasetBean dataset) {
        datasetPresenter.showDataset(dataset);
        switchPresenter(datasetPresenter);
    }

    private void switchPresenter(Presenter presenter) {

        if (this.currentPresenter != null) {
            //			this.currentPresenter.unbind();
            display.removeContent();
        }

        this.currentPresenter = presenter;

        if (presenter != null) {
            //display.addContent(presenter.getView().asWidget());
            this.currentPresenter.bind();
        }

    }

}
