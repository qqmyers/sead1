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

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.DatasetTableFourColumnView;
import edu.illinois.ncsa.mmdb.web.client.DatasetTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTag;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * @author lmarini
 *
 */
public class TagPage extends Composite {

	private final FlowPanel mainPanel;
	private TitlePanel pageTitle;
	private final String uri;
	private final MyDispatchAsync dispatchAsync;
	private final HandlerManager eventBus;
	
	public TagPage(String uri, MyDispatchAsync dispatchAsync, HandlerManager eventBus) {
		this.uri = uri;
		this.dispatchAsync = dispatchAsync;
		this.eventBus = eventBus;
		
		mainPanel = new FlowPanel();
		mainPanel.addStyleName("page");
		initWidget(mainPanel);
		
		// page title
		mainPanel.add(createPageTitle());
		
		mainPanel.add(createTagInformation());
		
		// datasets
		retrieveDatasets();
	}
	
	private Widget createTagInformation() {
		return new HTML("Datasets tagged with '<b>" + uri + "</b>'");
	}

	private void retrieveDatasets() {
		
		DatasetTableFourColumnView datasetTableWidget = new DatasetTableFourColumnView();
		DatasetTablePresenter datasetTablePresenter = new DatasetTablePresenter(
				datasetTableWidget, eventBus);
		datasetTablePresenter.bind();
		
		mainPanel.add(datasetTableWidget.asWidget());
		
		dispatchAsync.execute(new GetTag(uri), new AsyncCallback<GetDatasetsResult>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error retrieving datasets", null);
				DialogBox dialogBox = new DialogBox();
				dialogBox.setText("Oops");
				dialogBox.add(new Label("Error retrieving datasets " + caught));
				dialogBox.setAnimationEnabled(true);
				dialogBox.center();
				dialogBox.show();
			}

			@Override
			public void onSuccess(GetDatasetsResult result) {
				SortedSet<DatasetBean> orderedResult = new TreeSet<DatasetBean>(new Comparator<DatasetBean>() {
					public int compare(DatasetBean arg0, DatasetBean arg1) {
						if(arg0 == arg1) { return 0; }
						String t0 = arg0.getTitle();
						String t1 = arg1.getTitle();
						if(t0 == null && t1 != null) {
							return 1;
						}
						if(t0 != null && t1 == null) {
							return -1;
						}
						if(t0.equals(t1)) {
							return arg0.hashCode() < arg1.hashCode() ? -1 : 1;
							// we already know they're not the same object
							// and they came from a hashset so hash collisions
							// are already a problem before this point
						}
						return t0.compareTo(t1);
					}
				});
				orderedResult.addAll(result.getDatasets());
				for (DatasetBean dataset : orderedResult) {
					GWT.log("Sending event add dataset " + dataset.getTitle(),
							null);
					AddNewDatasetEvent event = new AddNewDatasetEvent();
					event.setDataset(dataset);
					eventBus.fireEvent(event);
				}
			}
		});
	}

	/**
	 * 
	 * @return
	 */
	private Widget createPageTitle() {
		pageTitle = new TitlePanel("Tag");
		return pageTitle;
	}
}
