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

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;

public abstract class EditableUserMetadataPresenter extends UserMetadataPresenter {
    Display                  editDisplay;
    protected HandlerManager eventBus;

    public EditableUserMetadataPresenter(DispatchAsync dispatch, HandlerManager eventBus, Display display) {
        super(dispatch, display);
        editDisplay = display;
        this.eventBus = eventBus;
    }

    public void bind() {
        super.bind();

        if (editDisplay.getSubmitControl() != null) {
            editDisplay.getSubmitControl().addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    String predicate = editDisplay.getSelectedField();
                    String value = editDisplay.getValue();
                    onSetMetadataField(predicate, value);
                }
            });
        }
    }

    public interface Display extends UserMetadataPresenter.Display {
        /**
         * Called by the presenter to get the uri of the currently-selected
         * predicate.
         */
        String getSelectedField();

        /**
         * Called by the presenter to get the value that the user wants to set
         * the currently-selected predicate to
         */
        String getValue();

        /**
         * Called by the presenter to get the control that sets a field/value.
         */
        HasClickHandlers getSubmitControl();

        /** Called by the presenter when the edit operation succeeds */
        void onSuccess();

        /** Called by the presenter when the edit operation fails */
        void onFailure();
    }

    /**
     * Implement this to handle a user edit action.
     * 
     * @param predicate
     *            the predicate being set
     * @param value
     *            the value to to set it to (null to clear the value)
     */
    protected abstract void onSetMetadataField(String predicate, String value);
}
