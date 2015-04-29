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

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;

/**
 * @author Luigi Marini
 *
 */
public abstract class Page extends Composite {

    protected final FlowPanel  mainLayoutPanel;
    protected final TitlePanel pageTitle;
    protected DispatchAsync    dispatchAsync;
    protected HandlerManager   eventBus;
    private final SimplePanel  feedbackPanel;

    /**
	 *
	 */
    public Page() {
        this(null, null, null, false);
    }

    public Page(DispatchAsync dispatchAsync) {
        this(null, dispatchAsync, null, false);
    }

    /**
     *
     * @param title
     */
    public Page(String title, DispatchAsync dispatchAsync) {
        this(title, dispatchAsync, null, false);
    }

    public Page(String title, DispatchAsync dispatchAsync, HandlerManager eventBus) {
        this(title, dispatchAsync, eventBus, false);
    }

    /**
     *
     * @param title
     */
    public Page(String title, DispatchAsync dispatchAsync, HandlerManager eventBus, boolean delegateLayout) {
        this.dispatchAsync = dispatchAsync;
        this.eventBus = eventBus;

        mainLayoutPanel = new FlowPanel();
        mainLayoutPanel.addStyleName("page");
        initWidget(mainLayoutPanel);

        // page title
        pageTitle = new TitlePanel("Page");
        if (title != null) {
            pageTitle.setText(title);
        }
        mainLayoutPanel.add(pageTitle);

        // feedback panel
        feedbackPanel = new SimplePanel();
        mainLayoutPanel.add(feedbackPanel);
        if (!delegateLayout) {
            layout();
        }
    }

    /**
     *
     * @param title
     */
    public void setPageTitle(String title) {
        pageTitle.setText(title);
    }

    /**
	 *
	 */
    public void clear() {
        mainLayoutPanel.clear();
        mainLayoutPanel.add(pageTitle);
        mainLayoutPanel.add(feedbackPanel);
    }

    /**
	 *
	 */
    public void refresh() {
        layout();
    }

    /**
	 *
	 */
    public abstract void layout();

    /**
     *
     * @param message
     */
    public void showFeedbackMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.addStyleName("feedbackMessage");
        feedbackPanel.clear();
        feedbackPanel.add(messageLabel);
    }

    /**
     * Get RSS feed link
     *
     */
    protected Anchor getRssFeed() {
        // rss feed
        Anchor rss = new Anchor();
        String linkString = "rss.xml";

        if (!MMDB.getSessionState().isAnonymous() && (MMDB.getSessionState().getToken() != null)) {
            linkString += "?user=" + MMDB.getUsername() + "&token=" + MMDB.getSessionState().getToken();
            rss.setTitle("This feed URL has your access permissions. Do not share it.");
        }
        rss.setHref(linkString);
        rss.addStyleName("rssIcon");
        DOM.setElementAttribute(rss.getElement(), "type", "application/rss+xml");
        rss.setHTML("<img src='./images/rss_icon.gif' border='0px' id='rssIcon' class='navMenuLink'>"); // FIXME hack

        return rss;
    }
}
