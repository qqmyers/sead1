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


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TextBox;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;

/**
 * Popup to tag a resource. Submit button doesn't have a default
 * click handler attached to it.
 * 
 * @author Luigi Marini
 *
 */
public class TagDialogBox extends DialogBox {

	private FlowPanel layout;
	private TextBox tagBox;
	private Button submitButton;
	private Button cancelButton;
	private final MyDispatchAsync service;
	private final String id;
	
	/**
	 * A simple dialog box to annotate a resource
	 * 
	 * TODO automatically put cursor in text field
	 * 
	 * @param id
	 * @param service
	 */
	public TagDialogBox(String id, MyDispatchAsync service) {
		this.id = id;
		this.service = service;
		
		setText("Tag");
		
		layout = new FlowPanel();
		tagBox = new TextBox();
		tagBox.setWidth("300px");
		layout.add(tagBox);
		
		submitButton = new Button("Submit");
		
		layout.add(submitButton);
		
		cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		layout.add(cancelButton);
		
		setWidget(layout);
		center();
		show();
	}

	public Button getSubmitButton() {
		return submitButton;
	}

	public String getTags() {
		return tagBox.getText();
	}

	public FocusWidget getTagBox() {
		return tagBox;
	}

	public MyDispatchAsync getService() {
		return service;
	}

	public String getId() {
		return id;
	}
}
