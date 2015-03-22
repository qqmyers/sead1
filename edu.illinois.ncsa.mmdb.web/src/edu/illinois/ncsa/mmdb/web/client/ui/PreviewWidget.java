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

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
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

    private static final int                MAXREQUEST   = 30;         // 5 minutes approx

    public static final String              UNKNOWN_TYPE = "Unknown";

    public static final Map<String, String> PREVIEW_URL;
    public static final Map<String, String> GRAY_URL;
    public static final Map<String, String> PENDING_URL;
    public Map<String, String>              ALT_GRAY_URL;

    int                                     maxWidth     = 600;

    final AbsolutePanel                     imagePanel;
    Image                                   currentImage;
    Image                                   preview;
    Image                                   overlay;
    Image                                   pending;
    Image                                   noPreview;
    String                                  size;
    Timer                                   retryTimer;
    int                                     retriesLeft  = 10;
    State                                   state        = State.BLANK;

    private final DispatchAsync             dispatchAsync;

    // possible state transitions
    // initially displayed -> failed -> no preview
    // initially displayed -> failed -> pending
    // initially displayed -> failed -> pending -> preview
    // initially display -> preview
    // blank -> preview
    // blank -> no preview
    // blank -> pending
    // blank -> pending -> preview
    private enum State {
        BLANK,
        PREVIEW,
        INITIALLY_DISPLAYED,
        FAILED,
        PENDING,
        NO_PREVIEW
    }

    static {
        PREVIEW_URL = new HashMap<String, String>();
        PREVIEW_URL.put(GetPreviews.SMALL, "./api/image/preview/small/");
        PREVIEW_URL.put(GetPreviews.LARGE, "./api/image/preview/large/");
        PREVIEW_URL.put(GetPreviews.BADGE, "./api/collection/preview/");
        GRAY_URL = new HashMap<String, String>(); // how I yearn for map literals
        GRAY_URL.put(GetPreviews.SMALL, "./images/nopreview-200.gif");
        GRAY_URL.put(GetPreviews.LARGE, "./images/nopreview-200.gif");
        GRAY_URL.put(GetPreviews.BADGE, "./images/nopreview-200.gif");
        PENDING_URL = new HashMap<String, String>(); // how I yearn for map literals
        PENDING_URL.put(GetPreviews.SMALL, "./images/loading-small.gif");
        PENDING_URL.put(GetPreviews.LARGE, "./images/loading-large.gif");
        PENDING_URL.put(GetPreviews.BADGE, "./images/loading-small.gif");
    }

    /**
     * Create a preview. If the desired size is small (thumbnail) try showing
     * the thumbnail, if thumbnail not available show a no preview label. If the
     * desired size is large (preview) ask the server for the size of the
     * preview and then properly size the image keeping the correct aspect
     * ratio.
     *
     * @param datasetUri
     * @param desiredSize
     * @param link
     */
    public PreviewWidget(String datasetUri, String desiredSize, final String link, DispatchAsync dispatchAsync) {
        this(datasetUri, desiredSize, link, UNKNOWN_TYPE, dispatchAsync);
    }

    public PreviewWidget(String datasetUri, String desiredSize, String link, String type, DispatchAsync dispatchAsync) {
        this(datasetUri, desiredSize, link, type, true, dispatchAsync);
    }

    public PreviewWidget(String datasetUri, String desiredSize, String link, String type, final boolean checkPending, DispatchAsync dispatchAsync) {
        this(datasetUri, desiredSize, link, type, checkPending, true, dispatchAsync);
    }

    public static PreviewWidget newCollectionBadge(String collectionUri, String link, DispatchAsync dispatchAsync) {
        return new PreviewWidget(collectionUri, GetPreviews.BADGE, link, "Collection", true, false, dispatchAsync);
    }

    public PreviewWidget(String uri, String desiredSize, String link, String type, boolean checkPending, boolean initialDisplay, DispatchAsync dispatchAsync) {
        this.dispatchAsync = dispatchAsync;
        state = State.BLANK; // nothing shown, yet
        size = getSize(desiredSize); // use desired size or default
        // set up panel
        imagePanel = createImagePanel(uri, size);
        //
        changeImage(uri, size, link, type, checkPending, initialDisplay);
        //
        createDefaultNoPreviewImages(type);
        initWidget(imagePanel);
    }

    protected void createDefaultNoPreviewImages(String type) {
        if (type != null && !UNKNOWN_TYPE.equals(type)) {
            String defaultSmallImageFileName = "./images/defaultpreviews/" + type + "-200.png";
            String defaultLargeImageFileName = "./images/defaultpreviews/" + type + "-500.png";

            ALT_GRAY_URL = new HashMap<String, String>();
            ALT_GRAY_URL.put(GetPreviews.SMALL, defaultSmallImageFileName);
            ALT_GRAY_URL.put(GetPreviews.LARGE, defaultSmallImageFileName); // To be consistent with previously released versions, NOPREVIEW SHOULD ALWAYS BE SMALL
            ALT_GRAY_URL.put(GetPreviews.BADGE, defaultSmallImageFileName);
        }

    }

    public void changeImage(String uri, String sz, String link, String type, boolean checkPending, boolean initialDisplay) {
        killTimer();
        // are we supposed to show the image immediately?
        if (initialDisplay) {
            state = State.BLANK;

            // figure out if we need to show the overlay
            showPreview(uri, size, link, 0, 0);
            preview.addErrorHandler(new ErrorHandler() {
                @Override
                public void onError(ErrorEvent event) {
                    state = State.FAILED;
                }
            });
            preview.addLoadHandler(new LoadHandler() {
                @Override
                public void onLoad(LoadEvent event) {
                    state = State.PREVIEW;
                }
            });
            state = State.INITIALLY_DISPLAYED;
        }
        // check pending?
        if (checkPending) {
            checkPending(uri, size, link);
        }
    }

    AbsolutePanel createImagePanel(String uri, String sz) {
        AbsolutePanel ip = new AbsolutePanel();
        if (uri == null) {
            ip.addStyleName("imageThumbnailBordered");
        } else if (!GetPreviews.LARGE.equals(sz)) {
            ip.addStyleName("imageThumbnail");
            ip.addStyleName("thumbnail");
        }
        return ip;
    }

    void checkPending(final String uri, final String sz, final String link) {
        retryTimer = new Timer() {
            public void run() {
                if (retriesLeft-- > 0) {
                    dispatchAsync.execute(new IsPreviewPending(uri, sz), new AsyncCallback<IsPreviewPendingResult>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            GWT.log("Error getting preview status", caught);
                        }

                        @Override
                        public void onSuccess(IsPreviewPendingResult result) {
                            if (result.isReady()) {
                                // the result is ready, which means either:
                                // 1. it was pending at some point, which means we need to show the preview, or
                                // 2. it was initially displayed, and was never pending, in which case we don't need to show the preview
                                // 3. it was initially displayed, but the REST servlet returned 404 for it because it was pending when it was initially displayed, so we do need to show it
                                //GWT.log("Showing PREVIEW for " + uri);
                                showPreview(uri, sz, link, result.getWidth(), result.getHeight());
                                retriesLeft = 0;
                            } else if (result.isPending() && retriesLeft > 0) {
                                //GWT.log("Showing PENDING for " + uri);
                                showPending(sz);
                                state = State.PENDING;
                            } else {
                                //GWT.log("Showing NO PREVIEW for " + uri);
                                showNoPreview(sz, link);
                                state = State.NO_PREVIEW;
                                retriesLeft = 0;
                            }
                        }
                    });
                }
            }
        };
        retryTimer.schedule(25); // pretty darn soon
        retryTimer.scheduleRepeating(1000);
    }

    void retryIn(long ms) {
    }

    void addLink(Image i, final String link) {
        if (i != null && link != null) {
            i.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    History.newItem(link);
                }
            });
        }
    }

    boolean isPreview() {
        return state == State.PREVIEW;
    }

    // show the preview with appropriate link and style
    void showPreview(String uri, String sz, final String link, long width, long height) {
        if (uri != null && state != State.PREVIEW) {
            String newPrefix = (state == State.PENDING || state == State.FAILED) ? "new/" : ""; // workaround for MMDB-1048
            preview = new Image(PREVIEW_URL.get(sz) + newPrefix + uri);
            addLink(preview, link);
            if (!GetPreviews.LARGE.equals(sz)) {
                preview.setWidth("100%");
                preview.setHeight("100%");
                //preview.addStyleName("thumbnail");
            } else {
                if (width > 0) {
                    if (width < getMaxWidth()) {
                        preview.setWidth(width + "px");
                        preview.setHeight(height + "px");
                    } else {
                        preview.setWidth(getMaxWidth() + "px");
                    }
                }
            }
            setImage(preview);
        }
        state = State.PREVIEW;
    }

    boolean isPending() {
        return state == State.PENDING;
    }

    void showPending(String sz) {
        if (!isPending()) {
            pending = new Image(PENDING_URL.get(size));
            if (GetPreviews.LARGE.equals(sz)) {
                pending.addStyleName("pendingLarge");
            } else {
                pending.addStyleName("pendingSmall");
            }
            setImage(pending);
        }
    }

    boolean isNoPreview() {
        return state == State.NO_PREVIEW;
    }

    void showNoPreview(String sz, String link) {
        if (!isNoPreview()) {
            if (ALT_GRAY_URL != null) {
                noPreview = new Image(ALT_GRAY_URL.get(sz));
                final String siz = sz;
                //revert back to the GRAY_URL if the nopreview file for this type is not found!
                noPreview.addErrorHandler(new ErrorHandler() {
                    @Override
                    public void onError(ErrorEvent event) {
                        noPreview.setUrl(GRAY_URL.get(siz));
                    }
                });
            } else {
                noPreview = new Image(GRAY_URL.get(sz));
            }
            if (!GetPreviews.LARGE.equals(sz)) {
                noPreview.setHeight("100%");
                noPreview.setWidth("100%");
            }
            addLink(noPreview, link);
            setImage(noPreview);
        }
    }

    /**
     * Implements a default size of {@link GetPreviews#SMALL}.
     *
     * @param size
     *            the size
     * @return the size, unless it's null or unknown, in which case return
     *         {@link GetPreviews#SMALL}.
     */
    String getSize(String size) {
        if (GetPreviews.BADGE.equals(size)) {
            return size;
        } else if (GetPreviews.LARGE.equals(size)) {
            return size;
        } else {
            return GetPreviews.SMALL;
        }
    }

    void removeImage(Image i) {
        if (i != null) {
            imagePanel.remove(i);
        }
    }

    void addImage(Image i) {
        imagePanel.add(i);
    }

    void setImage(Image i) {
        removeImage(currentImage);
        addImage(i);
        currentImage = i;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    /** For backwards compatibility with the old preview widget */
    public void changeImage(String datasetUri, String mimeType) {
        state = State.BLANK;
        String type = ContentCategory.getCategory(mimeType, dispatchAsync);
        changeImage(datasetUri, size, null, type, false, true);
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

    /**
     *
     * @return
     */
    public HasClickHandlers getTarget() {
        if (preview != null) {
            return preview;
        } else {
            return noPreview;
        }
    }

    void killTimer() {
        if (retryTimer != null) {
            retriesLeft = 0;
            retryTimer.cancel();
            retryTimer = null;
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        killTimer();
    }

}
