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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteAnnotation;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteAnnotationResult;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DeletedHandler;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;

/**
 * @author Luigi Marini <lmarini@ncsa.uiuc.edu>
 * 
 */
public class AnnotationView extends Composite {

	private SimplePanel mainPanel = new SimplePanel();

	private FlexTable mainTable = new FlexTable();

	private FlexCellFormatter flexCellFormatter;

	public AnnotationView(final String annotatedThingUri, final AnnotationBean annotationBean) {
		
		initWidget(mainPanel);

		mainPanel.addStyleName("annotationMainPanel");

		mainPanel.add(mainTable);

		mainTable.setBorderWidth(0);

		mainTable.setWidth("100%");

		mainTable.setCellSpacing(10);

		flexCellFormatter = mainTable.getFlexCellFormatter();

		Anchor deleteButton = new Anchor("Delete");
		deleteButton.addStyleName("datasetActionLink");
		deleteButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ConfirmDialog confirm = new ConfirmDialog("Delete","Are you sure you want to delete this comment?");
				confirm.addConfirmHandler(new ConfirmHandler() {
					public void onConfirm(ConfirmEvent event) {
						delete(annotatedThingUri, annotationBean.getUri());
					}
				});
			}
		});
		
		String mediumDate = "";

		String shortTime = "";

		if (annotationBean.getDate() != null) {

			mediumDate = DateTimeFormat.getMediumDateFormat().format(
					annotationBean.getDate());

			shortTime = DateTimeFormat.getShortTimeFormat().format(
					annotationBean.getDate());
		}

		String creator = "Anonymous";
		
		if (annotationBean.getCreator() != null) {
			
			creator = annotationBean.getCreator().getName();
		}
		
		mainTable.setHTML(0, 0, "By " + creator
				+ " on " + mediumDate + " " + shortTime);

		flexCellFormatter.setColSpan(0, 0, 2);

		flexCellFormatter.addStyleName(0, 0, "annotationAttributes");
		
		mainTable.setWidget(0, 1, deleteButton);

		String description = annotationBean.getDescription();
		
		description = description.replaceAll("\n", "<br>");
		
		mainTable.setHTML(1, 0, description);

		flexCellFormatter.setColSpan(1, 0, 2);

		flexCellFormatter.addStyleName(1, 0, "annotationBody");
	}
	
	// delete this annotation
	void delete(final String annotatedThingUri, final String annotationUri) {
		MMDB.dispatchAsync.execute(new DeleteAnnotation(annotatedThingUri, annotationUri), new AsyncCallback<DeleteAnnotationResult>() {
			public void onFailure(Throwable caught) {
				GWT.log("Error deleting annotation", caught);
			}
			public void onSuccess(DeleteAnnotationResult result) {
				fireEvent(new DeletedEvent(annotationUri));
				//addStyleName("hidden");
			}
		});
	}
	
	public void addDeletedHandler(DeletedHandler h) {
		addHandler(h, DeletedEvent.TYPE);
	}
}
