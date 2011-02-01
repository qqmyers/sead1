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
import java.util.HashSet;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;

/**
 * Return information about a hit from a search.
 * 
 * @author Luigi Marini <lmarini@ncsa.illinois.edu>
 * 
 */
public class GetSearchHitResult implements Result {

    private static final long       serialVersionUID = -86488013616325220L;

    private DatasetBean             dataset;

    private Collection<PreviewBean> previews;

    private String                  sectionUri;

    private String                  sectionLabel;

    private String                  sectionMarker;

    public GetSearchHitResult() {
    }

    public GetSearchHitResult(DatasetBean datasetBean, Collection<PreviewBean> previews) {
        setDataset(datasetBean);
        setPreviews(previews);
    }

    public void setDataset(DatasetBean dataset) {
        this.dataset = dataset;
    }

    public DatasetBean getDataset() {
        return dataset;
    }

    public void setPreviews(Collection<PreviewBean> previews) {
        this.previews = previews;
    }

    public Collection<PreviewBean> getPreviews() {
        if (previews == null) {
            return new HashSet<PreviewBean>();
        }
        return previews;
    }

    public void setSectionUri(String sectionUri) {
        this.sectionUri = sectionUri;
    }

    public String getSectionUri() {
        return sectionUri;
    }

    public void setSectionLabel(String sectionLabel) {
        this.sectionLabel = sectionLabel;
    }

    public String getSectionLabel() {
        return sectionLabel;
    }

    public void setSectionMarker(String sectionMarker) {
        this.sectionMarker = sectionMarker;
    }

    public String getSectionMarker() {
        return sectionMarker;
    }
}
