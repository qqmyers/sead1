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

import java.util.Date;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import edu.uiuc.ncsa.cet.bean.AnnotationBean;

/**
 * Add new annotation widget.
 * 
 * @author Luigi Marini
 * 
 */
public class NewAnnotationView extends Composite {

	private SimplePanel mainPanel = new SimplePanel();

	private FlexTable mainTable = new FlexTable();

	private TextBox titleTextBox = new TextBox();

	private TextArea descriptionTextArea;

	private Button submitButton;

	/**
	 *  Add new annotation widget.
	 */
	public NewAnnotationView() {

		initWidget(mainPanel);

		mainPanel.addStyleName("newAnnotationMainPanel");

		mainPanel.add(mainTable);

		Label header = new Label("Write a Comment");

		header.addStyleName("newCommentHeader");

		mainTable.setWidget(0, 0, header);

		descriptionTextArea = new TextArea();
		
		descriptionTextArea.addStyleName("newCommentTextArea");

		descriptionTextArea.setWidth("500px");

		descriptionTextArea.setSize("500px", "200px");

		mainTable.setWidget(1, 0, descriptionTextArea);

		submitButton = new Button("Comment");

		mainTable.setWidget(2, 0, submitButton);
	}

	/**
	 * Create an AnnotationBean based on values in widgets.
	 * @return
	 */
	public AnnotationBean getAnnotationBean() {

		AnnotationBean annotation = new AnnotationBean();

		annotation.setTitle(titleTextBox.getText());

		annotation.setDescription(descriptionTextArea.getText());

		annotation.setDate(new Date());

		return annotation;
	}

	/**
	 * Add a click handler to the submit button.
	 * @param clickHandler
	 */
	public void addClickHandler(ClickHandler clickHandler) {
		submitButton.addClickHandler(clickHandler);
	}

	public void clear() {

		titleTextBox.setText("");

		descriptionTextArea.setText("");

	}

}
