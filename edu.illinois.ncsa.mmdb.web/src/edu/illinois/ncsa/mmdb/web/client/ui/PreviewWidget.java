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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
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
public class PreviewWidget extends Composite implements HasAllMouseHandlers {

    private static final int                MAXREQUEST      = 30;                    // 5 minutes approx

    // FIXME use enums
    public static final Map<String, String> PREVIEW_URL;
    public static final Map<String, String> GRAY_URL;
    public static final Map<String, String> PENDING_URL;

    int                                     maxWidth        = 600;

    static {
        PREVIEW_URL = new HashMap<String, String>();
        PREVIEW_URL.put(GetPreviews.SMALL, "./api/image/preview/small/");
        PREVIEW_URL.put(GetPreviews.LARGE, "./api/image/preview/large/");
        PREVIEW_URL.put(GetPreviews.BADGE, "./api/collection/preview/");
        GRAY_URL = new HashMap<String, String>(); // how I yearn for map literals
        GRAY_URL.put(GetPreviews.SMALL, "./images/nopreview-100.gif");
        GRAY_URL.put(GetPreviews.LARGE, "./images/nopreview-100.gif");
        GRAY_URL.put(GetPreviews.BADGE, "./images/nopreview-100.gif");
        PENDING_URL = new HashMap<String, String>(); // how I yearn for map literals
        PENDING_URL.put(GetPreviews.SMALL, "./images/loading-small.gif");
        PENDING_URL.put(GetPreviews.LARGE, "./images/loading-large.gif");
        PENDING_URL.put(GetPreviews.BADGE, "./images/loading-small.gif");
    }

    static final String                     LOADING_TEXT    = "Loading...";
    static final String                     NO_PREVIEW_TEXT = "No preview available";
    private final SimplePanel               contentPanel;
    private final Image                     image;
    private Label                           noPreview;
    private String                          size;
    private final String                    datasetUri;
    private final String                    link;
    private boolean                         checkPending    = true;
    boolean                                 wasEverPending  = false;
    boolean                                 checkingPending = false;
    Timer                                   retryTimer;
    Timer                                   safariForceTimer;
    private int                             previewTries;

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
        this(datasetUri, desiredSize, link, "Unknown", true);
    }

    public PreviewWidget(final String datasetUri, String desiredSize,
            final String link, String type) {
        this(datasetUri, desiredSize, link, type, true, true);
    }

    public PreviewWidget(final String datasetUri, String desiredSize,
            final String link, String type, final boolean checkPending) {
        this(datasetUri, desiredSize, link, type, checkPending, true);
    }

    public PreviewWidget(final String datasetUri, String desiredSize,
                final String link, String type, final boolean checkPending, boolean initialDisplay) {
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

        final AbsolutePanel imagePanel = new AbsolutePanel();
        final Image overlay = new Image();

        //Icons that appear over thumbnail 
        if (type != "Image") {
            overlay.setUrl("images/icons/" + type + "_overlay.png");
        }

        contentPanel = new SimplePanel();
        // add the preview image
        if (initialDisplay) {
            image = new Image(PREVIEW_URL.get(size) + datasetUri);
        } else {
            image = new Image(GRAY_URL.get(size));
        }

        overlay.addStyleName("imageOverlay");
        image.addStyleName("imageThumbnail");

        imagePanel.add(image);
        if (type != "Unknown") {
            imagePanel.add(overlay);
        }

        contentPanel.clear();

        contentPanel.add(imagePanel);

        addLink(image);
        addLink(overlay);

        //
        safariForceTimer = new Timer() { // MMDB-620
            public void run() {
                contentPanel.clear();
                contentPanel.add(imagePanel);
                addLink(image);
                addLink(overlay);
            }
        };
        safariForceTimer.schedule(50); // right away
        safariForceTimer.schedule(60); // and again
        safariForceTimer.schedule(70); // rapid fire
        safariForceTimer.schedule(100); // take that!
        safariForceTimer.schedule(250); // will this work?
        safariForceTimer.schedule(1000); // somewhat later
        safariForceTimer.schedule(2000); // last attempt
        //
        if (size != GetPreviews.LARGE) {
            image.addStyleName("thumbnail");
        } else {
            //image.setWidth(getMaxWidth() + "px");
        }
        if (checkPending) {
            image.addErrorHandler(new ErrorHandler() {
                public void onError(ErrorEvent event) {
                    wasEverPending = false;
                    grayImage();
                    getPreview(datasetUri, link);
                }
            });
            getPreview(datasetUri, link);
        }

        initWidget(contentPanel);
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

    @Override
    protected void onDetach() {
        super.onDetach();
        if (retryTimer != null) {
            retryTimer.cancel();
        }
        if (safariForceTimer != null) {
            safariForceTimer.cancel();
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

    protected void getPreview(final String datasetUri, final String link, final boolean display) {
        checkingPending = true;
        MMDB.dispatchAsync.execute(new IsPreviewPending(datasetUri, size), new AsyncCallback<IsPreviewPendingResult>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(IsPreviewPendingResult result) {
                previewTries++;
                if (checkPending) { // do we need to know the pending state?
                    if (result.isReady()) {
                        //GWT.log("Preview is now READY for " + datasetUri);
                        if (wasEverPending) {
                            image.setUrl(PREVIEW_URL.get(size) + "new/" + datasetUri); // workaround firefox bug
                        }
                        if (size.equals(GetPreviews.LARGE)) {
                            image.setWidth(getMaxWidth() + "px");
                        }
                        if (retryTimer != null) {
                            retryTimer.cancel();
                        }
                        checkingPending = false;
                        checkPending = false;
                    } else if (result.isPending() && (previewTries < MAXREQUEST)) {
                        //GWT.log("Preview is PENDING for " + datasetUri);
                        if (!wasEverPending) {
                            wasEverPending = true;
                            pendingImage();
                        }
                        if (retryTimer == null) {
                            previewTries = 0;
                            retryTimer = new Timer() {
                                public void run() {
                                    getPreview(datasetUri, link);
                                }
                            };
                            retryTimer.scheduleRepeating(1000); // every 1s
                        }
                    } else {
                        //GWT.log("Preview is NOT READY, NOT PENDING for " + datasetUri);
                        if (retryTimer != null) {
                            retryTimer.cancel();
                        }
                        if (wasEverPending) {
                            grayImage();
                        }
                        checkingPending = false;
                        checkPending = false;
                    }
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

    @Override
    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
        return addDomHandler(handler, MouseMoveEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return addDomHandler(handler, MouseOutEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return addDomHandler(handler, MouseOverEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
        return addDomHandler(handler, MouseUpEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
        return addDomHandler(handler, MouseWheelEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return addDomHandler(handler, MouseDownEvent.getType());
    }

}
