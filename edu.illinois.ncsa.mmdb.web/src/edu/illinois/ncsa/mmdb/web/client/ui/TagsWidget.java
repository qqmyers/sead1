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

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTags;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTagsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResourceResult;

/**
 * A widget listing tags and providing a way to add a new one.
 * 
 * @author Luigi Marini
 * 
 */
public class TagsWidget extends Composite {

    private final FlowPanel       mainPanel;
    private final FlexTable       tagsPanel;
    private final String          id;
    private final MyDispatchAsync service;
    private final Label           tagLabel;
    private AddTagWidget          tagWidget;
    final Set<String>             tagsShown;

    /**
     * A widget listing tags and providing a way to add a new one.
     * 
     * @param id
     * @param service
     */
    public TagsWidget(final String id, final MyDispatchAsync service) {
        this(id, service, true);
    }

    public TagsWidget(final String id, final MyDispatchAsync service, boolean withTitle) {
        this.id = id;
        this.service = service;
        tagsShown = new HashSet<String>();

        mainPanel = new FlowPanel();
        mainPanel.addStyleName("datasetRightColSection");
        initWidget(mainPanel);

        if (withTitle) {
            tagLabel = new Label("Tags");
            tagLabel.addStyleName("datasetRightColHeading");
            mainPanel.add(tagLabel);
        } else {
            tagLabel = null;
        }

        tagsPanel = new FlexTable();
        tagsPanel.addStyleName("tagsLinks");
        mainPanel.add(tagsPanel);

        final Anchor addTagAnchor = new Anchor("Add tag(s)");
        addTagAnchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                mainPanel.remove(addTagAnchor);

                tagWidget = new AddTagWidget();

                tagWidget.getSubmitLink().addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        submitTag(tagWidget.getTags());
                        tagWidget.getTagBox().setText("");
                        tagWidget.getTagBox().setFocus(true);
                    }
                });

                tagWidget.getTagBox().addKeyUpHandler(new KeyUpHandler() {
                    @Override
                    public void onKeyUp(KeyUpEvent event) {
                        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                            submitTag(tagWidget.getTags());
                            tagWidget.getTagBox().setText("");
                            tagWidget.getTagBox().setFocus(true);
                        }

                    }
                });

                tagWidget.getCancelLink().addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        mainPanel.remove(tagWidget);
                        mainPanel.add(addTagAnchor);
                    }
                });

                mainPanel.add(tagWidget);

                DeferredCommand.addCommand(new Command() {
                    public void execute() {
                        tagWidget.getTagBox().setFocus(true);
                    }
                });
            }
        });
        mainPanel.add(addTagAnchor);
        //				mainPanel.setCellHorizontalAlignment(tagWidget, HasHorizontalAlignment.ALIGN_RIGHT);

        getTags();
    }

    void addTag(final String tag) {
        if (!tagsShown.contains(tag)) {
            final int row = tagsPanel.getRowCount();
            tagsPanel.setWidget(row, 0, tagHyperlink(tag));
            Anchor delete = new Anchor("delete");
            delete.addStyleName("deleteLink");
            delete.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    deleteTag(tag, row);
                    tagWidget.getTagBox().setFocus(true);
                }
            });
            tagsPanel.setWidget(row, 1, delete);
            tagsShown.add(tag);
        }
    }

    /**
     * Use service to retrieve tags from server.
     */
    private void getTags() {

        service.execute(new GetTags(id), new AsyncCallback<GetTagsResult>() {

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error retrieving tags", caught);
            }

            @Override
            public void onSuccess(GetTagsResult result) {
                for (final String tag : result.getTags().keySet() ) {
                    addTag(tag);
                }
            }
        });

    }

    Set<String> tagSet(String cdl) {
        Set<String> tagSet = new HashSet<String>();
        for (String s : cdl.split(",") ) {
            tagSet.add(s);
        }
        return tagSet;
    }

    private void deleteTag(final String tags, final int toRemove) {
        final Set<String> tagSet = tagSet(tags);

        service.execute(new TagResource(id, tagSet, true), new AsyncCallback<TagResourceResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Failed to remove resource", caught);
            }

            @Override
            public void onSuccess(TagResourceResult result) {
                tagsPanel.getRowFormatter().addStyleName(toRemove, "hidden");
                for (String tag : tagSet ) {
                    tagsShown.remove(tag);
                }
            }
        });
    }

    /**
     * Submit tags to the server.
     * 
     * @param tags
     */
    private void submitTag(final String tags) {

        if (!"".equals(tags)) {
            final Set<String> tagSet = tagSet(tags);

            service.execute(new TagResource(id, tagSet), new AsyncCallback<TagResourceResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Failed tagging resource", caught);
                }

                @Override
                public void onSuccess(TagResourceResult result) {
                    for (String tag : result.getTags() ) {
                        addTag(tag);
                    }
                }
            });
        }
    }

    /**
     * Create a tag hyperlink.
     * 
     * @param tag
     * @return hyperlink
     */
    private Hyperlink tagHyperlink(String tag) {
        Hyperlink link = new Hyperlink(tag, "tag?title=" + tag);
        link.addStyleName("tag");
        return link;
    }

}
