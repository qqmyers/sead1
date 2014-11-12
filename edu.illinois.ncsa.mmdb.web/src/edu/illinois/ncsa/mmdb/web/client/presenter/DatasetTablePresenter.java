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

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQuery;

/**
 * Dynamic table presenter for datasets.
 * 
 * @author Luigi Marini
 * 
 */
public class DatasetTablePresenter extends DynamicTablePresenter {

    private String collectionURI = null;

    public DatasetTablePresenter(DispatchAsync dispatch, HandlerManager eventBus, Display display) {
        super(dispatch, eventBus, display);
        // TODO Auto-generated constructor stub
    }

    public DatasetTablePresenter(DispatchAsync dispatch, HandlerManager eventBus, Display display, String collectionURI) {
        super(dispatch, eventBus, display);

        this.collectionURI = collectionURI;
    }

    /* (non-Javadoc)
     * @see edu.illinois.ncsa.mmdb.web.client.presenter.DynamicTablePresenter#getQuery()
     */
    @Override
    protected ListQuery getQuery() {
        int offset = (currentPage - 1) * getPageSize();
        GWT.log("Getting datasets " + offset + " to " + (offset + getPageSize()));
        ListQuery query = new ListQuery();
        query.setOrderBy(sortKey);
        query.setLimit(getPageSize());
        query.setOffset(offset);
        if (collectionURI != null) {
            query.setCollection(collectionURI);
        } else {
            query.setBean("http://cet.ncsa.uiuc.edu/2007/Dataset");
        }
        query.setUser(MMDB.getUsername());
        query.setShowDataLevel(showTopLevelDatasets);
        return query;
    }

    @Override
    protected boolean rememberPageNumber() {
        return true;
    }
}
