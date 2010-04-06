package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
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
    private Image                            image;
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
        Image previewImage = new Image(PREVIEW_URL.get(size) + datasetUri);
        if (size != GetPreviews.LARGE) {
            previewImage.addStyleName("thumbnail");
        }
        if (checkPending) {
            previewImage.addLoadHandler(new LoadHandler() {
                public void onLoad(LoadEvent event) {
                    MMDB.dispatchAsync.execute(new IsPreviewPending(datasetUri, size), new AsyncCallback<IsPreviewPendingResult>() {
                        public void onFailure(Throwable caught) {
                        }

                        public void onSuccess(IsPreviewPendingResult result) {
                            if (result.isPending()) {
                                pending();
                            } else if (!result.isReady()) {
                                grayImage();
                            }
                        }
                    });
                }
            });
        }
        addLink(previewImage);
        contentPanel.clear();
        contentPanel.add(previewImage);
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

    protected void getPreview(final String datasetUri, final String link, final boolean display) {
        MMDB.dispatchAsync.execute(new IsPreviewPending(datasetUri, size), new AsyncCallback<IsPreviewPendingResult>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(IsPreviewPendingResult result) {
                if (result.isReady()) {
                    contentPanel.clear();
                    image = new Image(PREVIEW_URL.get(size) + datasetUri);
                    contentPanel.add(image);
                    addLink(image);
                } else if (!result.isReady()) {
                    retryTimer = new Timer() {
                        @Override
                        public void run() {
                            getPreview(datasetUri, link);
                        }
                    };
                    timeOffset = (timeOffset + 37) % 100;
                    retryTimer.schedule(1000 + timeOffset); // every second or so
                }
            }
        });

    }

    boolean isGrayImage    = false;
    boolean isPendingImage = false;

    protected void grayImage() {
        if (!isGrayImage) {
            contentPanel.clear();
            image = new Image(GRAY_URL.get(size));
            image.addStyleName("thumbnail");
            addLink(image);
            image.addStyleName("imagePreviewShortWidth");
            //image.setWidth(getMaxWidth()+"px");
            contentPanel.add(image);
            isGrayImage = true;
            isPendingImage = false;
        }
    }

    protected void pending() {
        pendingImage();
        getPreview();
    }

    protected void pendingImage() {
        if (!isPendingImage) {
            contentPanel.clear();
            image = new Image(PENDING_URL.get(size));
            if (size.equals(GetPreviews.LARGE)) {
                image.addStyleName("thumbnail");
                image.addStyleName("pendingLarge");
            } else {
                image.addStyleName("pendingSmall");
            }
            addLink(image);
            image.addStyleName("imagePreviewShortWidth");
            //image.setWidth(getMaxWidth()+"px");
            contentPanel.add(image);
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
