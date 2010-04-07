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
package edu.illinois.ncsa.mmdb.web.client;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.PagingTablePresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.ui.PagingWidget;

public abstract class PagingTableView<T> extends Composite implements Display<T> {
	VerticalPanel mainPanel;
	
	List<PagingWidget> pagingControls;
	
	protected HorizontalPanel topPagingPanel;
	protected VerticalPanel middlePanel;
	protected HorizontalPanel bottomPagingPanel;
	
	protected PagingTableView() {
		//
		mainPanel = new VerticalPanel(); 
		topPagingPanel = new HorizontalPanel();
		middlePanel = new VerticalPanel();
		bottomPagingPanel = new HorizontalPanel();
		mainPanel.add(topPagingPanel);
		mainPanel.add(middlePanel);
		mainPanel.add(bottomPagingPanel);
		//
		pagingControls = new LinkedList<PagingWidget>();
		
		initWidget(mainPanel);
	}
	
	public PagingTableView(int page) {
		this();
		topPagingPanel.add(createPagingPanel(page));
		bottomPagingPanel.add(createPagingPanel(page));
	}

	protected void addPagingControl(PagingWidget control) {
		pagingControls.add(control);
	}
	
	protected HorizontalPanel createPagingPanel(int page) {
		HorizontalPanel panel = new HorizontalPanel();
		panel.addStyleName("datasetsPager");
		panel.addStyleName("centered"); // special IE-friendly centering style

		PagingWidget pagingWidget = new PagingWidget();
		pagingWidget.setPage(page);
		addPagingControl(pagingWidget);
		panel.add(pagingWidget);
		return panel;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	public void addPageChangeHandler(ValueChangeHandler<Integer> handler) {
		for(HasValueChangeHandlers<Integer> control : pagingControls) {
			control.addValueChangeHandler(handler);
		}
	}
	
	public void setNumberOfPages(int p) {
		for(PagingWidget control : pagingControls) {
			control.setNumberOfPages(p);
		}
	}
}
