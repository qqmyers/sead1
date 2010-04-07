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

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Page title widget.
 * 
 * @author lmarini
 *
 */
public class TitlePanel extends HorizontalPanel implements HasValueChangeHandlers<String> {
	private String title;
	private final EditableLabel titleLabel;
	
	public TitlePanel() {
		super();
		this.setVerticalAlignment(ALIGN_MIDDLE);
		addStyleName("titlePanel");
		titleLabel = new EditableLabel("");
		titleLabel.setEditable(false);
		titleLabel.getLabel().addStyleName("pageTitle");
		titleLabel.setEditableStyleName("datasetTitle");
		add(titleLabel);
	}
	
	public TitlePanel(String title) {
		this();
		setText(title);
	}
	
	public void setText(String t) {
		title = t;
		titleLabel.setText(title);
	}
	public String getText() {
		return title;
	}
	
	public void addEast(Widget w) {
		HorizontalAlignmentConstant h = getHorizontalAlignment();
		setHorizontalAlignment(ALIGN_RIGHT);
		add(w);
		setHorizontalAlignment(h);
	}

	public boolean isEditable() {
		return titleLabel.isEditable();
	}
	public void setEditable(boolean editable) {
		titleLabel.setEditable(editable);
	}
	
	public EditableLabel getEditableLabel() { // wagh. encapsulation violation
		return titleLabel;
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {
		return titleLabel.addValueChangeHandler(handler);
	}
}
