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
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetLikeDislike;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetLikeDislikeResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetViewCount;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetViewCountResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetLikeDislike.LikeDislike;

/**
 * Show social information about the dataset
 * 
 * @author Rob Kooper
 * 
 */
public class SocialWidget extends Composite {
    private final FlowPanel mainPanel;

    /**
     * A widget listing tags and providing a way to add a new one.
     * 
     * @param id
     * @param service
     */
    public SocialWidget(String uri, MyDispatchAsync service) {
        this(uri, service, true);
    }

    public SocialWidget(final String uri, final MyDispatchAsync service, boolean withTitle) {
        // mainpanel
        mainPanel = new FlowPanel();
        mainPanel.addStyleName("datasetRightColSection");
        initWidget(mainPanel);

        if (withTitle) {
            Label mapHeader = new Label("Social");
            mapHeader.addStyleName("datasetRightColHeading");
            mainPanel.add(mapHeader);
        }

        // get view count (including updating count)
        final Label viewLabel = new Label("Viewed: N/A");
        viewLabel.addStyleName("datasetRightColText");
        service.execute(new GetViewCount(uri, MMDB.getUsername()), new AsyncCallback<GetViewCountResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error getting view count", caught);
            }

            public void onSuccess(GetViewCountResult result) {
                String count = NumberFormat.getDecimalFormat().format(result.getCount());
                viewLabel.setText("Viewed by " + count + " people");
            }
        });
        mainPanel.add(viewLabel);

        // like/dislike
        final Label likeCount = new Label();
        likeCount.addStyleName("datasetRightColText");
        mainPanel.add(likeCount);

        final Anchor likeAnchor = new Anchor("Like");
        likeAnchor.addStyleName("datasetRightColText");
        mainPanel.add(likeAnchor);

        final Anchor dislikeAnchor = new Anchor("Dislike");
        dislikeAnchor.addStyleName("datasetRightColText");
        dislikeAnchor.addStyleName("multiAnchor");
        mainPanel.add(dislikeAnchor);

        // get like/dislike count
        service.execute(new GetLikeDislike(uri, MMDB.getUsername()), new AsyncCallback<GetLikeDislikeResult>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Error getting view count", caught);
            }

            public void onSuccess(GetLikeDislikeResult result) {
                String likes = NumberFormat.getDecimalFormat().format(result.getLikeCount());
                String dislikes = NumberFormat.getDecimalFormat().format(result.getDislikeCount());
                likeCount.setText(likes + " likes and " + dislikes + " dislikes");

                if (result.getState() == LikeDislike.LIKE) {
                    likeAnchor.setText("Unlike");
                } else {
                    likeAnchor.setText("Like");
                }
                if (result.getState() == LikeDislike.DISLIKE) {
                    dislikeAnchor.setText("Undislike");
                } else {
                    dislikeAnchor.setText("Dislike");
                }
            }
        });

        // clicks
        likeAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GetLikeDislike ld = new GetLikeDislike(uri, MMDB.getUsername());
                if (likeAnchor.getText().equals("Like")) {
                    ld.setState(LikeDislike.LIKE);
                } else {
                    ld.setState(LikeDislike.NONE);
                }
                // get like/dislike count
                service.execute(ld, new AsyncCallback<GetLikeDislikeResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error getting like/dislike count", caught);
                    }

                    public void onSuccess(GetLikeDislikeResult result) {
                        String likes = NumberFormat.getDecimalFormat().format(result.getLikeCount());
                        String dislikes = NumberFormat.getDecimalFormat().format(result.getDislikeCount());
                        likeCount.setText(likes + " likes and " + dislikes + " dislikes");

                        if (result.getState() == LikeDislike.LIKE) {
                            likeAnchor.setText("Unlike");
                        } else {
                            likeAnchor.setText("Like");
                        }
                        if (result.getState() == LikeDislike.DISLIKE) {
                            dislikeAnchor.setText("Undislike");
                        } else {
                            dislikeAnchor.setText("Dislike");
                        }
                    }
                });
            }
        });

        dislikeAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GetLikeDislike ld = new GetLikeDislike(uri, MMDB.getUsername());
                if (dislikeAnchor.getText().equals("Dislike")) {
                    ld.setState(LikeDislike.DISLIKE);
                } else {
                    ld.setState(LikeDislike.NONE);
                }
                // get like/dislike count
                service.execute(ld, new AsyncCallback<GetLikeDislikeResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error getting like/dislike count", caught);
                    }

                    public void onSuccess(GetLikeDislikeResult result) {
                        String likes = NumberFormat.getDecimalFormat().format(result.getLikeCount());
                        String dislikes = NumberFormat.getDecimalFormat().format(result.getDislikeCount());
                        likeCount.setText(likes + " likes and " + dislikes + " dislikes");

                        if (result.getState() == LikeDislike.LIKE) {
                            likeAnchor.setText("Unlike");
                        } else {
                            likeAnchor.setText("Like");
                        }
                        if (result.getState() == LikeDislike.DISLIKE) {
                            dislikeAnchor.setText("Undislike");
                        } else {
                            dislikeAnchor.setText("Dislike");
                        }
                    }
                });
            }
        });
    }
}
