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

import java.util.Iterator;
import java.util.TreeMap;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAllTags;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTagsResult;

/**
 * A page listing all tags in the system.
 *
 * @author Luigi Marini
 *
 */
public class TagsPage extends Page {

    private FlowPanel tagsPanel;

    /**
     * Build the page and retrieve all the tags in the system.
     *
     * @param dispatchAsync
     *            dispatch service
     */
    public TagsPage(DispatchAsync dispatchAsync) {
        super("Tags", dispatchAsync);
        getTags();
    }

    /**
     * Get tags from server and add them to the tag panel. Shows both the tag
     * name and tag count.
     */
    private void getTags() {
        dispatchAsync.execute(new GetAllTags(),
                new AsyncCallback<GetTagsResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error getting tags", caught);
                    }

                    @Override
                    public void onSuccess(GetTagsResult result) {
                        TreeMap<String, Integer> tags = result.getTags();
                        Iterator<String> iterator = tags.keySet().iterator();
                        while (iterator.hasNext()) {
                            FlowPanel tagPanel = new FlowPanel();
                            String tag = iterator.next();
                            if (!tag.equals("")) {
                                String linkText = tag;
                                if (linkText.length() > 10) {
                                    linkText = linkText.substring(0, 10) + "...";
                                }
                                Hyperlink link = new Hyperlink(linkText, "tag?title=" + URL.encodeQueryString(tag));
                                link.addStyleName("tagLink");
                                link.setTitle(tag);
                                tagPanel.add(link);
                                Label tagCount = new Label(" (" + tags.get(tag) + ") ");
                                tagCount.addStyleName("tagCount");
                                tagPanel.add(tagCount);
                                tagPanel.addStyleName("tagInPanel");
                                tagsPanel.add(tagPanel);
                            }
                        }
                    }

                });
    }

    @Override
    public void layout() {
        tagsPanel = new FlowPanel();
        tagsPanel.addStyleName("tagsPanel");
        mainLayoutPanel.add(tagsPanel);
    }
}
