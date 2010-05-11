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
package edu.illinois.ncsa.mmdb.web.client.mvp;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Base presenter. Should be extended specific presenters.
 * 
 * @author Luigi Marini
 * 
 */
public class BasePresenter<D> implements Presenter {

    protected D                             display;
    protected final HandlerManager          eventBus;
    private final List<HandlerRegistration> handlerRegistrations = new LinkedList<HandlerRegistration>();

    public BasePresenter(D display, HandlerManager eventBus) {
        this.display = display;
        this.eventBus = eventBus;
    }

    protected <T extends EventHandler> void addHandler(GwtEvent.Type<T> type, T handler) {
        handlerRegistrations.add(eventBus.addHandler(type, handler));
    }

    /* (non-Javadoc)
     * @see edu.illinois.ncsa.mmdb.web.client.mvp.Presenter#bindDisplay(edu.illinois.ncsa.mmdb.web.client.mvp.Display)
     */
    @Override
    public void bind() {
        // TODO Auto-generated method stub
    }

    public void unbind() {
        for (HandlerRegistration registration : handlerRegistrations ) {
            GWT.log("removing " + registration);
            registration.removeHandler();
        }
    }
}
