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
package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResourceResult;
import edu.illinois.ncsa.mmdb.web.client.event.BatchCompletedEvent;

/**
 * @author Luigi Marini
 * 
 */
public class TagDialogPresenter extends TextDialogPresenter {

    private final MyDispatchAsync dispatch;
    private final HandlerManager  eventBus;
    private final Display         display;
    private Set<String>           selectedResources;
    private final boolean         delete;           // should we delete the tags instead?

    public TagDialogPresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display) {
        this(dispatch, eventBus, display, false);
    }

    public TagDialogPresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display, boolean delete) {
        this.dispatch = dispatch;
        this.eventBus = eventBus;
        this.display = display;
        this.selectedResources = new HashSet<String>();
        this.delete = delete;
    }

    @Override
    public void bind() {
        display.getSubmitButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                tag();
                display.hide();
            }
        });

        display.getCancelButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                display.hide();
            }
        });

        display.getTextBox().addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    tag();
                    display.hide();
                }
            }
        });
    }

    /**
     * Tag resources if tag is not empty.
     */
    protected void tag() {
        Set<String> tagSet = new HashSet<String>();
        for (String s : display.getTextString().getText().split(",") ) {
            if (!s.equals("")) {
                tagSet.add(s);
            }
        }
        if (!tagSet.isEmpty()) {
            String actionVerb = delete ? "untagged" : "tagged";
            final BatchCompletedEvent done = new BatchCompletedEvent(selectedResources.size(), actionVerb);
            for (final String id : selectedResources ) {

                dispatch.execute(new TagResource(id, tagSet, delete), new AsyncCallback<TagResourceResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Failed tagging resource", caught);
                        done.setFailure(id, "failed: " + caught.getMessage());
                        if (done.readyToFire()) {
                            eventBus.fireEvent(done);
                        }
                    }

                    @Override
                    public void onSuccess(TagResourceResult result) {
                        GWT.log("Resource successfully tagged", null);
                        done.addSuccess(id);
                        if (done.readyToFire()) {
                            eventBus.fireEvent(done);
                        }
                    }
                });
            }
        }
    }

    public void setSelectedResources(Set<String> selectedResources) {
        this.selectedResources = selectedResources;
    }

    public Set<String> getSelectedResources() {
        return selectedResources;
    }
}
