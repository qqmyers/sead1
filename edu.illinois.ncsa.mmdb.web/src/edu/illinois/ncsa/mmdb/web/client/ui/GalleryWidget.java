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
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.ArrayList;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;

/**
 * @author lmarini
 * 
 */
public class GalleryWidget extends Composite {

    private final FlowPanel         mainPanel;
    private final ArrayList<String> uris;
    private final HorizontalPanel   imagePanel;
    private int                     pageNum;
    private final int               pageSize    = 3;
    private final String            PREVIEW_URL = "./api/image/preview/small/";
    private final DispatchAsync     dispatchAsync;

    public GalleryWidget(DispatchAsync dispatchAsync, ArrayList<String> uris) {
        this.dispatchAsync = dispatchAsync;
        this.uris = uris;
        this.pageNum = 1;
        mainPanel = new FlowPanel();
        mainPanel.addStyleName("gallery");
        initWidget(mainPanel);

        imagePanel = new HorizontalPanel();
        imagePanel.addStyleName("galleryImages");
        mainPanel.add(imagePanel);

        PagingWidget pager = new PagingWidget(pageNum);
        pager.addStyleName("galleryPager");
        pager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            public void onValueChange(ValueChangeEvent<Integer> event) {
                changePage(event.getValue());
            }
        });
        mainPanel.add(pager);

        showImages();

    }

    /**
     * 
     * @param value
     */
    private void changePage(int value) {
        pageNum = value;
        showImages();
    }

    /**
	 * 
	 */
    private void showImages() {
        imagePanel.clear();
        if (uris.size() > pageSize) {
            for (int i = 0; i < pageSize; i++ ) {
                final String uri = uris.get((pageNum - 1) * pageSize + i);
                PreviewWidget preview = new PreviewWidget(uri, GetPreviews.SMALL, "dataset?id=" + uri, dispatchAsync);
                imagePanel.add(preview);
            }
        } else {
            for (int i = 0; i < uris.size(); i++ ) {
                final String uri = uris.get(i);
                PreviewWidget preview = new PreviewWidget(uri, GetPreviews.SMALL, "dataset?id=" + uri, dispatchAsync);
                imagePanel.add(preview);
            }
        }
    }

}
