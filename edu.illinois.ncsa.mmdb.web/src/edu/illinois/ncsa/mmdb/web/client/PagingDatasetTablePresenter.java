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

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetHandler;
import edu.illinois.ncsa.mmdb.web.client.ui.ContentCategory;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class PagingDatasetTablePresenter extends PagingTablePresenter<DatasetBean> {

    public PagingDatasetTablePresenter(Display<DatasetBean> display, DispatchAsync service, HandlerManager eventBus) {
        super(display, service, eventBus);
    }

    @Override
    public void bind() {

        super.bind();

        eventBus.addHandler(AddNewDatasetEvent.TYPE,
                new AddNewDatasetHandler() {
                    @Override
                    public void onAddNewDataset(AddNewDatasetEvent event) {
                        DatasetBean dataset = event.getDataset();
                        String id = dataset.getUri();
                        String type = ContentCategory.getCategory(dataset.getMimeType(), service);
                        if (event.getPosition() == -1) {
                            if (event.getSectionUri() == null) {
                                display.addItem(id, dataset, type);
                            } else {
                                display.addItem(id, dataset, type, event.getSectionUri(), event.getSectionLabel(), event.getSectionMarker());
                            }
                        } else {
                            if (event.getSectionUri() == null) {
                                display.addItem(id, dataset, type, event.getPosition());
                            } else {
                                display.addItem(id, dataset, type, event.getPosition(), event.getSectionUri(), event.getSectionLabel(), event.getSectionMarker());
                            }
                        }
                    }
                });
    }
}