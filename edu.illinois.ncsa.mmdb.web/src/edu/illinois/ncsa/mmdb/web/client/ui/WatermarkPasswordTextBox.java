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
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.PasswordTextBox;

/**
 * FIXME: need to figure a way to switch to a TextBox when showing the watermark.
 * This strategy probably doesn't work for PasswordTextBox.
 * 
 * @author Luigi Marini
 *
 */
public class WatermarkPasswordTextBox extends PasswordTextBox implements FocusHandler,
		BlurHandler {

	private String watermark;
	private HandlerRegistration blurHandler;
	private HandlerRegistration focusHandler;
	
	public WatermarkPasswordTextBox() {
		super();
		addStyleName("textBoxWatermark");
	}
	
	public WatermarkPasswordTextBox(String text) {
		this();
		setText(text);
	}
	
	public WatermarkPasswordTextBox(String text, String watermark) {
		this(text);
		setWatermark(watermark);
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.FocusHandler#onFocus(com.google.gwt.event.dom.client.FocusEvent)
	 */
	@Override
	public void onFocus(FocusEvent event) {
		removeStyleName("textBoxWatermark");
		if (getText().equalsIgnoreCase(watermark)) {
			setText("");
		}
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event.dom.client.BlurEvent)
	 */
	@Override
	public void onBlur(BlurEvent event) {
		showWatermark();
	}
	
	private void showWatermark() {
		if (getText().length() == 0 || getText().equalsIgnoreCase(watermark)) {
			setText(watermark);
			addStyleName("textBoxWatermark");
		}
	}

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

	public String getWatermark() {
		return watermark;
	}

}
