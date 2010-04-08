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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.IsPreviewPending;
import edu.illinois.ncsa.mmdb.web.client.dispatch.IsPreviewPendingResult;

/**
 * 
 * @author Joe Futrelle
 * @author Luigi Marini
 * 
 */
public class PreviewWidget extends Composite {

    // FIXME use enums
    private static final Map<String, String> PREVIEW_URL;
    public static final Map<String, String>  GRAY_URL;
    private static final Map<String, String> PENDING_URL;

    int                                      maxWidth        = 600;

    static {
        PREVIEW_URL = new HashMap<String, String>();
        PREVIEW_URL.put(GetPreviews.SMALL, "./api/image/preview/small/");
        PREVIEW_URL.put(GetPreviews.LARGE, "./api/image/preview/large/");
        PREVIEW_URL.put(GetPreviews.BADGE, "./api/collection/preview/");
        GRAY_URL = new HashMap<String, String>(); // how I yearn for map literals
        GRAY_URL.put(GetPreviews.SMALL, "./images/preview-100.gif");
        GRAY_URL.put(GetPreviews.LARGE, "./images/preview-500.gif");
        GRAY_URL.put(GetPreviews.BADGE, "./images/preview-100.gif");
        PENDING_URL = new HashMap<String, String>(); // how I yearn for map literals
        PENDING_URL.put(GetPreviews.SMALL, "./images/loading-small.gif");
        PENDING_URL.put(GetPreviews.LARGE, "./images/loading-large.gif");
        PENDING_URL.put(GetPreviews.BADGE, "./images/loading-small.gif");
    }

    static final int                         delays[]        = new int[] { 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, -1 };
    static final String                      LOADING_TEXT    = "Loading...";
    static final String                      NO_PREVIEW_TEXT = "No preview available";
    private final SimplePanel                contentPanel;
    private final Image                      image;
    private Label                            noPreview;
    private String                           size;
    private final int                        whichDelay      = 0;
    private final String                     datasetUri;
    private final String                     link;
    private boolean                          checkPending    = true;

    /**
     * Create a preview. If the desired size is small (thumbnail) try showing
     * the thumbnail, if thumbnail notavailable show a no preview label. If the
     * desired size is large (preview) ask the server for the size of the
     * preview and then properly size the image keeping the correct aspect
     * ratio.
     * 
     * @param datasetUri
     * @param desiredSize
     * @param link
     */
    public PreviewWidget(final String datasetUri, String desiredSize,
            final String link) {
        this(datasetUri, desiredSize, link, true);
    }

    public PreviewWidget(final String datasetUri, String desiredSize,
            final String link, boolean checkPending) {
        this.checkPending = checkPending;
        this.datasetUri = datasetUri;
        this.link = link;
        // default to small size if desired size is unrecognized
        if (desiredSize == GetPreviews.BADGE) {
            size = desiredSize;
        } else if (desiredSize == GetPreviews.LARGE) {
            size = desiredSize;
        } else {
            size = GetPreviews.SMALL;
        }

        contentPanel = new SimplePanel();
        initWidget(contentPanel);

        // add the preview image
        image = new Image(PREVIEW_URL.get(size) + datasetUri);
        if (size != GetPreviews.LARGE) {
            image.addStyleName("thumbnail");
        }
        if (checkPending) {
            image.addErrorHandler(new ErrorHandler() {
                public void onError(ErrorEvent event) {
                    wasEverPending = true;
                }
            });
            Timer timer = new Timer() {
                public void run() {
                    getPreview();
                }
            };
            timer.schedule(10); // run almost immediately
        }
        addLink(image);
        contentPanel.clear();
        contentPanel.add(image);
    }

    /**
     * If link is available for image add a click handler to the image.
     */
    private void addLink(Image image) {
        if (link != null) {
            image.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    History.newItem(link);
                }
            });
        }
    }

    /**
     * 
     * @return
     */
    public HasClickHandlers getTarget() {
        if (image != null) {
            return image;
        } else {
            return noPreview;
        }
    }

    static int timeOffset;
    Timer      retryTimer;

    @Override
    protected void onDetach() {
        super.onDetach();
        if (retryTimer != null) {
            retryTimer.cancel();
        }
    }

    /**
     * 
     * @param <A>
     * @param <R>
     * @param action
     * @param callback
     */
    protected void getPreview() {
        getPreview(datasetUri, link);
    }

    protected void getPreview(final String datasetUri, final String link) {
        getPreview(datasetUri, link, true);
    }

    boolean wasEverPending = false;

    protected void getPreview(final String datasetUri, final String link, final boolean display) {
        MMDB.dispatchAsync.execute(new IsPreviewPending(datasetUri, size), new AsyncCallback<IsPreviewPendingResult>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(IsPreviewPendingResult result) {
                if (result.isReady()) {
                    GWT.log("Preview is now READY for " + datasetUri);
                    if (wasEverPending) {
                        image.setUrl(PREVIEW_URL.get(size) + "new/" + datasetUri);
                    }
                } else if (result.isPending()) {
                    GWT.log("Preview is PENDING for " + datasetUri);
                    if (!wasEverPending) {
                        wasEverPending = true;
                        pendingImage();
                    }
                    retryTimer = new Timer() {
                        public void run() {
                            getPreview(datasetUri, link);
                        }
                    };
                    timeOffset = (timeOffset + 37) % 100;
                    retryTimer.schedule(1000 + timeOffset); // every second or so
                } else {
                    GWT.log("Preview is NOT READY, NOT PENDING for " + datasetUri);
                }
            }
        });

    }

    boolean isGrayImage    = false;
    boolean isPendingImage = false;

    protected void grayImage() {
        if (isPendingImage) {
            image.removeStyleName("pendingLarge");
            image.removeStyleName("pendingSmall");
        }
        if (!isGrayImage) {
            image.setUrl(GRAY_URL.get(size));
            isGrayImage = true;
            isPendingImage = false;
        }
    }

    protected void pendingImage() {
        if (!isPendingImage) {
            image.setUrl(PENDING_URL.get(size));
            if (size.equals(GetPreviews.LARGE)) {
                image.addStyleName("pendingLarge");
            } else {
                image.addStyleName("pendingSmall");
            }
            isPendingImage = true;
            isGrayImage = false;
        }
    }

    /**
	 *
	 */
    protected void statusLabel(String text) {
        // no preview is available
        contentPanel.clear();
        noPreview = new Label(text);
        if (size == GetPreviews.LARGE) {
            noPreview.setHeight("300px");
        } else {
            noPreview.setHeight("75px");
        }
        contentPanel.add(noPreview);
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

}
