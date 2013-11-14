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
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A text box that has default text set for when it's empty. The special text to
 * show when it's empty is the watermark.
 * 
 * @author Luigi Marini
 * 
 */
public class WatermarkTextBox extends TextBox implements FocusHandler,
        BlurHandler {

    private String              watermark;
    private HandlerRegistration blurHandler;
    private HandlerRegistration focusHandler;

    /**
     * No watermark set.
     */
    public WatermarkTextBox() {
        super();
        addStyleName("textBoxWatermark");
    }

    /**
     * Set content.
     * 
     * @param text
     *            text in box
     */
    public WatermarkTextBox(String text) {
        this();
        setText(text);
    }

    /**
     * Set both content and watermark.
     * 
     * @param text
     *            text in box
     * @param watermark
     *            watermark for when text is removed
     */
    public WatermarkTextBox(String text, String watermark) {
        this(text);
        setWatermark(watermark);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.gwt.event.dom.client.FocusHandler#onFocus(com.google.gwt.event
     * .dom.client.FocusEvent)
     */
    @Override
    public void onFocus(FocusEvent event) {
        removeStyleName("textBoxWatermark");
        if (getText().equalsIgnoreCase(watermark)) {
            setText("");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event
     * .dom.client.BlurEvent)
     */
    @Override
    public void onBlur(BlurEvent event) {
        showWatermark();
    }

    /**
     * Show the watermark.
     */
    public void showWatermark() {
        if (getText().length() == 0 || getText().equalsIgnoreCase(watermark)) {
            setText(watermark);
            addStyleName("textBoxWatermark");
        }
    }

    /**
     * Set the value of the watermark.
     * 
     * @param watermark
     *            text to use for the watermark
     */
    public void setWatermark(String watermark) {
        this.watermark = watermark;

        if (watermark != null && !watermark.equals("")) {
            blurHandler = addBlurHandler(this);
            focusHandler = addFocusHandler(this);
            showWatermark();
        } else {
            blurHandler.removeHandler();
            focusHandler.removeHandler();
        }
    }

    /**
     * Get the watermark.
     * 
     * @return watermark text
     */
    public String getWatermark() {
        return watermark;
    }

}
