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
import com.google.gwt.user.client.ui.HasVerticalAlignment;
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
    Image                firstButton;
    Image                previousButton;
    Image                nextButton;
    Image                lastButton;
    HorizontalPanel      pagePanel;
    Label                pageLabel;
    final int            pad    = 3;
    private final Anchor previousAnchor;
    private final Anchor nextAnchor;
    // paging model
    int                  page   = 1;
    int                  nPages = -1;

    public void setPage(int p) {
        setPage(p, true);
    }

    public void setPage(int p, boolean fire) {
        page = p >= 1 ? p : 1;
        setPageLabel(page, nPages);
        if (fire) {
            ValueChangeEvent.fire(this, page);
        }
    }

    public void setNumberOfPages(int p) {
        nPages = p;
        setPageLabel(page, nPages);
    }

    public int getNumberOfPages() {
        return nPages;
    }

    Anchor pageNumberAnchor(int p) {
        return pageNumberAnchor(p, true);
    }

    Anchor pageNumberAnchor(final int p, boolean clickable) {
        Anchor anchor = new Anchor(p + "");
        if (clickable) {
            anchor.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    ValueChangeEvent.fire(PagingWidget.this, p);
                }
            });
            anchor.addStyleName("pageAnchor");
        } else {
            anchor.addStyleName("thisPageAnchor");
        }
        return anchor;
    }

    void setPageLabel(int p, int np) {
        pagePanel.clear();
        if (np < pad * 2 + 5) {
            for (int i = 1; i <= np; i++ ) {
                pagePanel.add(pageNumberAnchor(i, i != p));
            }
        } else if (p - pad <= 3) {
            for (int i = 1; i <= pad * 2 + 3; i++ ) {
                pagePanel.add(pageNumberAnchor(i, i != p));
            }
            Label lbl = new Label("...");
            lbl.addStyleName("arrowCursor");
            pagePanel.add(lbl);
            pagePanel.add(pageNumberAnchor(np, np != p));
        } else if (p + pad > np - 3) {
            pagePanel.add(pageNumberAnchor(1, 1 != p));
            Label lbl = new Label("...");
            lbl.addStyleName("arrowCursor");
            pagePanel.add(lbl);
            for (int i = np - (pad * 2 + 2); i <= np; i++ ) {
                pagePanel.add(pageNumberAnchor(i, i != p));
            }
        } else {
            pagePanel.add(pageNumberAnchor(1, 1 != p));
            pagePanel.add(new Label("..."));
            for (int i = p - pad; i <= p + pad; i++ ) {
                pagePanel.add(pageNumberAnchor(i, i != p));
            }
            pagePanel.add(new Label("..."));
            pagePanel.add(pageNumberAnchor(np, np != p));
        }
    }

    public int getPage() {
        return page;
    }

    public PagingWidget() {
        this(1);
    }

    public PagingWidget(int p) {
        page = p;
        HorizontalPanel thePanel = new HorizontalPanel();
        thePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        thePanel.setSpacing(1);
        thePanel.addStyleName("pagingWidget");
        firstButton = new Image("images/go-first.png");
        previousButton = new Image("images/go-previous.png");
        nextButton = new Image("images/go-next.png");
        lastButton = new Image("images/go-last.png");
        pagePanel = new HorizontalPanel();
        pagePanel.addStyleName("pagingList");
        //pageLabel = new Label();
        setPageLabel(page, nPages);

        // previous anchor
        previousAnchor = new Anchor("Previous");
        previousAnchor.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (getPage() > 1) {
                    setPage(getPage() - 1);
                }
            }
        });
        // next anchor
        nextAnchor = new Anchor("Next");
        nextAnchor.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (getNumberOfPages() < 1 || getPage() < getNumberOfPages()) {
                    setPage(getPage() + 1);
                }
            }
        });

        // add and configure widgets
        thePanel.add(previousAnchor);
        for (Widget element : new Widget[] { /*firstButton, previousButton,*/pagePanel /*, nextButton, lastButton*/} ) {
            thePanel.add(element);
            element.addStyleName("pagingButton");
            if (element instanceof Image) {
                ((Image) element).addClickHandler(this);
            }
        }
        thePanel.add(nextAnchor);
        initWidget(thePanel);
    }

    public void onClick(ClickEvent event) {
        Widget w = (Widget) event.getSource();
        if (w == firstButton) {
            setPage(1);
        } else if (w == previousButton) {
            if (getPage() > 1) {
                setPage(getPage() - 1);
            }
        } else if (w == nextButton) {
            if (getNumberOfPages() < 1 || getPage() < getNumberOfPages()) {
                setPage(getPage() + 1);
            }
        } else if (w == lastButton) {
            if (getPage() < getNumberOfPages()) {
                setPage(getNumberOfPages());
            }
        }
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}