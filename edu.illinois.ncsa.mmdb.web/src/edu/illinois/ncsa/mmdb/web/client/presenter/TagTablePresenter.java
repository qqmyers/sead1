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
/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQuery;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.ShowItemEvent;
import edu.illinois.ncsa.mmdb.web.client.ui.ContentCategory;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * @author lmarini
 * 
 */
public class TagTablePresenter extends DynamicTablePresenter<DatasetBean> {

    private String tagName;

    public TagTablePresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display, String tagName) {
        super(dispatch, eventBus, display);
        setTagName(tagName);
    }

    @Override
    protected void addItem(ShowItemEvent event, DatasetBean item) {
        event.setId(item.getUri());
        event.setTitle(item.getTitle());
        event.setAuthor(item.getCreator().getName());
        event.setDate(item.getDate());
        event.setSize(TextFormatter.humanBytes(item.getSize()));
        event.setType(ContentCategory.getCategory(item.getMimeType()));
    }

    @Override
    protected ListQuery<DatasetBean> getQuery() {
        int offset = (currentPage - 1) * getPageSize();
        GWT.log("Getting datasets " + offset + " to " + (offset + getPageSize()) + " with tag " + tagName);
        ListQueryDatasets query = new ListQueryDatasets();
        query.setOrderBy(uriForSortKey());
        query.setDesc(descForSortKey());
        query.setLimit(getPageSize());
        query.setOffset(offset);
        query.setWithTag(tagName);
        return query;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

    @Override
    protected String getViewTypePreference() {
        return MMDB.DATASET_VIEW_TYPE_PREFERENCE;
    }

    @Override
    protected String getViewSizeTypePreference() {
        return MMDB.DATASET_VIEWSIZE_TYPE_PREFERENCE;
    }

    @Override
    public String getPageKey() {
        return null; // do not remember what page we're on
    }
}
