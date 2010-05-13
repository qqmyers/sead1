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
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

public class ListDatasets implements Action<ListDatasetsResult> {
    /**
	 * 
	 */
    private static final long serialVersionUID = 2922437353231426177L;

    public ListDatasets() {
    }

    private String  orderBy;
    private boolean desc;
    private int     limit;
    private int     offset;
    private String  inCollection;
    private String  withTag;

    public ListDatasets(String orderBy, boolean desc, int limit, int offset, String inCollection) {
        this.orderBy = orderBy;
        this.desc = desc;
        this.limit = limit;
        this.offset = offset;
        this.inCollection = inCollection;
    }

    public ListDatasets(String orderBy, boolean desc, int limit, int offset, String inCollection, String withTag) {
        this.orderBy = orderBy;
        this.desc = desc;
        this.limit = limit;
        this.offset = offset;
        this.inCollection = inCollection;
        this.withTag = withTag;
    }

    public void setOrderBy(String s) {
        orderBy = s;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setDesc(boolean b) {
        desc = b;
    }

    public boolean getDesc() {
        return desc;
    }

    public void setLimit(int i) {
        limit = i;
    }

    public int getLimit() {
        return limit;
    }

    public void setOffset(int i) {
        offset = i;
    }

    public int getOffset() {
        return offset;
    }

    public void setInCollection(String s) {
        inCollection = s;
    }

    public String getInCollection() {
        return inCollection;
    }

    public String getWithTag() {
        return withTag;
    }

    public void setWithTag(String withTag) {
        this.withTag = withTag;
    }
}
