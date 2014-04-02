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
package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

import edu.uiuc.ncsa.cet.bean.CETBean;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Triggered when a new dataset is added to the interface.
 * 
 * @author Luigi Marini
 * 
 */
public class AddNewDatasetOrCollectionEvent extends GwtEvent<AddNewDatasetOrCollectionHandler> {

    public static final GwtEvent.Type<AddNewDatasetOrCollectionHandler> TYPE          = new GwtEvent.Type<AddNewDatasetOrCollectionHandler>();

    private CETBean                                                     bean          = new DatasetBean();
    private boolean                                                     isDataset     = true;
    private String                                                      previewUri    = null;

    private int                                                         position      = -1;
    /** Optional section information **/
    private String                                                      sectionUri    = null;
    /** Optional section information **/
    private String                                                      sectionLabel  = null;
    /** Optional section information **/
    private String                                                      sectionMarker = null;

    @Override
    protected void dispatch(AddNewDatasetOrCollectionHandler handler) {
        handler.onAddNewDatasetOrCollection(this);
    }

    @Override
    public GwtEvent.Type<AddNewDatasetOrCollectionHandler> getAssociatedType() {
        return TYPE;
    }

    public void setDatasetOrCollection(CETBean bean) {
        this.bean = bean;
        if (bean instanceof CollectionBean) {
            isDataset = false;
        }
    }

    public boolean isDataset() {
        return isDataset;
    }

    public CETBean getBean() {
        return bean;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
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

    public String getPreviewUri() {
        return previewUri;
    }

    public void setPreviewUri(String previewUri) {
        this.previewUri = previewUri;
    }

}
