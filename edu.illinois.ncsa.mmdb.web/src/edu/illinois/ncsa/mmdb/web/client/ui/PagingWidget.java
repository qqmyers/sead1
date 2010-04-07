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


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * TODO Add comments
 * 
 * @author Luigi Marini
 *
 */
public class PagingWidget extends Composite implements ClickHandler, HasValueChangeHandlers<Integer> {
	Image firstButton;
	Image previousButton;
	Image nextButton;
	Image lastButton;
	HorizontalPanel pagePanel;
	Label pageLabel;

	// paging model
	int page = 1;
	int nPages = -1;

	public void setPage(int p) {
		setPage(p,true);
	}
	public void setPage(int p, boolean fire) {
		page = p >= 1 ? p : 1;
		setPageLabel(page,nPages);
		if(fire) {
			ValueChangeEvent.fire(this, page);
		}
	}
	public void setNumberOfPages(int p) {
		nPages = p;
		setPageLabel(page,nPages);
	}
	public int getNumberOfPages() {
		return nPages;
	}
	Anchor pageNumberAnchor(final int p, boolean clickable) {
		Anchor anchor = new Anchor(p+"");
		if(clickable) {
			anchor.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					ValueChangeEvent.fire(PagingWidget.this,p);
				}
			});
			anchor.addStyleName("pageAnchor");
		} else {
			anchor.addStyleName("thisPageAnchor");
		}
		return anchor;
	}
	final int pad = 3;
	void setPageLabel(int p, int np) {
		pagePanel.clear();
		if(p > pad+1) {
			pagePanel.add(pageNumberAnchor(1,true));
			if(p > pad+2) {
				pagePanel.add(new Label("..."));
			}
		}
		for(int i = Math.max(1,p-pad); i < p; i++) {
			pagePanel.add(pageNumberAnchor(i,true));
		}
		pagePanel.add(pageNumberAnchor(p,false));
		for(int i = p+1; i <= Math.min(p+pad,np); i++) {
			pagePanel.add(pageNumberAnchor(i,true));
		}
		if(p < np-pad) {
			if(p < np-pad-1) {
				pagePanel.add(new Label("..."));
			}
			pagePanel.add(pageNumberAnchor(np,true));
		}
	}
	public int getPage() { return page; }
	
	public PagingWidget() {
		this(1);
	}
	public PagingWidget(int p) {
		page = p;
		HorizontalPanel thePanel = new HorizontalPanel();
		thePanel.addStyleName("pagingWidget");
		firstButton = new Image("images/go-first.png");
		previousButton = new Image("images/go-previous.png");
		nextButton = new Image("images/go-next.png");
		lastButton = new Image("images/go-last.png");
		pagePanel = new HorizontalPanel();
		//pageLabel = new Label();
		setPageLabel(page,nPages);
		for(Widget element : new Widget[] { /*firstButton,*/ previousButton, pagePanel, nextButton/*, lastButton*/ }) {
			thePanel.add(element);
			element.addStyleName("pagingButton");
			if(element instanceof Image) {
				((Image)element).addClickHandler(this);
			}
		}
		initWidget(thePanel);
	}
	
	public void onClick(ClickEvent event) {
		Widget w = (Widget) event.getSource();
		if(w == firstButton) {
			setPage(1);
		} else if(w == previousButton) {
			if(getPage() > 1) {
				setPage(getPage()-1);
			}
		} else if(w == nextButton) {
			if(getNumberOfPages() < 1 || getPage() < getNumberOfPages()) {
				setPage(getPage()+1);
			}
		} else if(w == lastButton) {
			if(getPage() < getNumberOfPages()) {
				setPage(getNumberOfPages());
			}
		}
	}

	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
}