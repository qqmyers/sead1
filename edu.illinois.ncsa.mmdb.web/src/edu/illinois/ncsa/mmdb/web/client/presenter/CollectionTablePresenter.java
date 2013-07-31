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

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQuery;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryCollections;
import edu.illinois.ncsa.mmdb.web.client.event.ShowItemEvent;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicTableView;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;

/**
 * Dynamic table presenter for datasets.
 * 
 * @author Luigi Marini
 * 
 */
public class CollectionTablePresenter extends DynamicTablePresenter<CollectionBean> {

    public CollectionTablePresenter(DispatchAsync dispatch, HandlerManager eventBus, Display display) {
        super(dispatch, eventBus, display, DynamicTableView.GRID_VIEW_TYPE, DynamicTableView.PAGE_SIZE_X1);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see edu.illinois.ncsa.mmdb.web.client.presenter.DynamicTablePresenter#getQuery()
     */
    @Override
    protected ListQuery<CollectionBean> getQuery() {
        int offset = (currentPage - 1) * getPageSize();
        GWT.log("Getting collections " + offset + " to " + (offset + getPageSize()));
        ListQueryCollections query = new ListQueryCollections();
        query.setOrderBy(sortKey);
        query.setLimit(getPageSize());
        query.setOffset(offset);
        return query;
    }

    @Override
    protected void addItem(ShowItemEvent event, CollectionBean item) {
        event.setId(item.getUri());
        if (item.getTitle() != null) {
            event.setTitle(item.getTitle());
        } else {
            event.setTitle(item.getUri());
        }
        PersonBean creator = item.getCreator();
        if (creator != null) {
            event.setAuthor(item.getCreator().getName());
        }
        event.setDate(item.getCreationDate());
        event.setSize(Integer.toString(item.getMemberCount()));
        event.setType("Collection");
    }

    @Override
    protected boolean rememberPageNumber() {
        return true;
    }
}
