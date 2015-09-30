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
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;

/**
 * @author lmarini
 *
 */
@SuppressWarnings("serial")
public class GetCollectionResult implements Result {

    private CollectionBean          collection;
    private int                     collectionSize;
    private Collection<PreviewBean> previews;
    private String[]                pids;
    private Map<String, String>     parents;

    public GetCollectionResult() {
    }

    public GetCollectionResult(CollectionBean collection, int collectionSize) {
        this.collection = collection;
        this.collectionSize = collectionSize;
    }

    /**
     * @return the collection
     */
    public CollectionBean getCollection() {
        return collection;
    }

    public int getCollectionSize() {
        return collectionSize;
    }

    public void setCollectionSize(int collectionSize) {
        this.collectionSize = collectionSize;
    }

    public Collection<PreviewBean> getPreviews() {
        return this.previews;
    }

    public void setPreviews(Collection<PreviewBean> previews) {
        this.previews = previews;
    }

    public String[] getPIDs() {
        return pids;
    }

    public void setPIDs(String[] pids) {
        this.pids = pids;
    }

    public Map<String, String> getParents() {
        return parents;
    }

    public void setParents(Map<String, String> parents) {
        this.parents = parents;
    }
}
