package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.client.DispatchAsync;

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

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.IsPreviewPending;
import edu.illinois.ncsa.mmdb.web.client.dispatch.IsPreviewPendingResult;

/**
 * 
 * @author Joe Futrelle
 * @author Luigi Marini
 * 
 */
public class OldPreviewWidget extends Composite implements HasAllMouseHandlers {

    private static final int                MAXREQUEST      = 30;                    // 5 minutes approx

    public static final String              UNKNOWN_TYPE    = "Unknown";             // FIXME make sure this constant isn't "magic-numbered" anywhere else

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
        GRAY_URL.put(GetPreviews.LARGE, "./images/nopreview-100.gif"); // TODO is this correct?
        GRAY_URL.put(GetPreviews.BADGE, "./images/nopreview-100.gif");
        PENDING_URL = new HashMap<String, String>(); // how I yearn for map literals
        PENDING_URL.put(GetPreviews.SMALL, "./images/loading-small.gif");
        PENDING_URL.put(GetPreviews.LARGE, "./images/loading-large.gif");
        PENDING_URL.put(GetPreviews.BADGE, "./images/loading-small.gif");
    }

    static final String                     LOADING_TEXT    = "Loading...";
    static final String                     NO_PREVIEW_TEXT = "No preview available";
    private final SimplePanel               contentPanel;
    private Image                           image;
    private final Image                     grayImage;
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
    final AbsolutePanel                     imagePanel;

    private final DispatchAsync             dispatchAsync;

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
    public OldPreviewWidget(DispatchAsync dispatchAsync, final String datasetUri, String desiredSize,
            final String link) {
        this(dispatchAsync, datasetUri, desiredSize, link, UNKNOWN_TYPE, true);
    }

    public OldPreviewWidget(DispatchAsync dispatchAsync, final String datasetUri, String desiredSize,
            final String link, String type) {
        this(dispatchAsync, datasetUri, desiredSize, link, type, true, true);
    }

    public OldPreviewWidget(DispatchAsync dispatchAsync, final String datasetUri, String desiredSize,
            final String link, String type, final boolean checkPending) {
        this(dispatchAsync, datasetUri, desiredSize, link, type, checkPending, true);
    }

    public static OldPreviewWidget newCollectionBadge(DispatchAsync dispatchAsync, String collectionUri, String link) {
        return new OldPreviewWidget(dispatchAsync, collectionUri, GetPreviews.BADGE, link, UNKNOWN_TYPE, true, false);
    }

    public OldPreviewWidget(DispatchAsync dispatchAsync, final String datasetUri, String desiredSize,
                final String link, String type, final boolean checkPending, boolean initialDisplay) {
        this.dispatchAsync = dispatchAsync;
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
        grayImage = new Image(GRAY_URL.get(size));

        imagePanel = new AbsolutePanel();
        final Image overlay = new Image();

        // add the preview image
        contentPanel = new SimplePanel();
        image = new Image(PREVIEW_URL.get(size) + datasetUri);
        if (initialDisplay && datasetUri != null) {
            gray(false);
        } else {
            gray(true); // image will not be attached to the page, so will not do an HTTP GET on the preview URL
        }

        if (datasetUri == null) {
            imagePanel.addStyleName("imageThumbnailBordered");
        } else if (desiredSize != GetPreviews.LARGE) {
            imagePanel.addStyleName("imageThumbnail");
        }

        gray(true);
        imagePanel.add(grayImage);
        imagePanel.add(image);

        //icons that appear over thumbnail 
        if (!UNKNOWN_TYPE.equals(type) && initialDisplay) {
            overlay.setUrl("images/icons/" + type + "_overlay.png");
            overlay.addStyleName("imageOverlay");
            imagePanel.add(overlay);
        }

        contentPanel.clear();

        contentPanel.add(imagePanel);

        addLink(image);
        addLink(grayImage);
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
        if (size != GetPreviews.LARGE && link != null) {
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

    void gray(boolean gray) {
        if (gray) {
            grayImage.removeStyleName("hidden");
            image.addStyleName("hidden");
        } else {
            image.removeStyleName("hidden");
            grayImage.addStyleName("hidden");
        }
    }

    public void changeImage(String newURL, String mime) {
        String type = ContentCategory.getCategory(mime, dispatchAsync);
        //FIXME a better way to specify whether a filetype will show a thumbnail or not
        if (mime.contains("image") || "Video".equals(type) || mime.contains("pdf") || "Audio".equals(type)) {
            imagePanel.remove(image);
            image = new Image(PREVIEW_URL.get(size) + newURL);
            imagePanel.add(image);
            imagePanel.removeStyleName("imageThumbnailBordered");
            imagePanel.addStyleName("imageThumbnail");
            gray(false);
        } else {
            gray(true);
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
        dispatchAsync.execute(new IsPreviewPending(datasetUri, size), new AsyncCallback<IsPreviewPendingResult>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(IsPreviewPendingResult result) {
                previewTries++;
                if (checkPending) { // do we need to know the pending state?
                    if (result.isReady()) {
                        //GWT.log("Preview is now READY for " + datasetUri);
                        if (wasEverPending) {
                            image.setUrl(PREVIEW_URL.get(size) + "new/" + datasetUri); // workaround firefox bug
                        } else {
                            image.setUrl(PREVIEW_URL.get(size) + datasetUri);
                        }
                        gray(false);
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
            isGrayImage = true;
            isPendingImage = false;
        }
        gray(true);
    }

    protected void pendingImage() {
        if (!isPendingImage) {
            grayImage.setUrl(PENDING_URL.get(size));
            if (size.equals(GetPreviews.LARGE)) {
                grayImage.addStyleName("pendingLarge");
            } else {
                grayImage.addStyleName("pendingSmall");
            }
            isPendingImage = true;
            isGrayImage = false;
        }
        gray(true);
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
