package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
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
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviewsResult;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;

/**
 * 
 * @author Joe Futrelle
 * @author Luigi Marini
 * 
 */
public class PreviewWidget extends Composite {
	
	// FIXME use enums
	private static final Map<String, String> PREVIEW_URL;
	private static final Map<String, String> GRAY_URL;
	private static final Map<String, String> PENDING_URL;

	int maxWidth = 600;
	
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

	static final int delays[] = new int[] { 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, -1 };
	static final String LOADING_TEXT = "Loading...";
	static final String NO_PREVIEW_TEXT = "No preview available";
	private final SimplePanel contentPanel;
	private Image image;
	private Label noPreview;
	private String size;
	private int whichDelay = 0;
	private final String datasetUri;
	private final String link;

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
		this.datasetUri = datasetUri;
		this.link = link;
		// default to small size if desired size is unrecognized
		if(desiredSize == GetPreviews.BADGE) {
			size = desiredSize;
		} else if(desiredSize == GetPreviews.LARGE) {
			size = desiredSize;
		} else {
			size = GetPreviews.SMALL;
		}

		contentPanel = new SimplePanel();
		initWidget(contentPanel);

		// add the preview image
		if(size != GetPreviews.LARGE) {
			final Image previewImage = new Image(PREVIEW_URL.get(size) + datasetUri);
			previewImage.addStyleName("thumbnail");
			previewImage.addLoadHandler(new LoadHandler() {
				public void onLoad(LoadEvent event) {
					getPreview(datasetUri, link); // handle pending case.
				}
			});
			previewImage.addErrorHandler(new ErrorHandler() {
				public void onError(ErrorEvent event) {
					pending(datasetUri, size, link);
				}
			});
			addLink(previewImage, link);
			contentPanel.clear();
			contentPanel.add(previewImage);
		} else {
			pending(datasetUri, size, link);
		}
	}

	/**
	 * If link is available for image add a click handler to the image.
	 */
	private void addLink(Image image, final String link) {
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
	Timer retryTimer;
	
	
	@Override
	protected void onDetach() {
		super.onDetach();
		if(retryTimer != null) {
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
	protected void getPreview(final String datasetUri, final String link) {
		getPreview(datasetUri, link, true);
	}
	protected void getPreview(final String datasetUri, final String link, final boolean display) {
		MMDB.dispatchAsync.execute(new GetPreviews(datasetUri),
				new AsyncCallback<GetPreviewsResult>() {
					public void onFailure(Throwable arg0) {
						GWT.log("Failed retrieving dataset previews", arg0);
					}

					public void onSuccess(GetPreviewsResult arg0) {
						Map<String, PreviewImageBean> previews = arg0.getPreviews();
						if(previews.get(size) == null && arg0.isStopAsking()) {
							grayImage(size, link);
						} else if (previews.get(size) == null && !arg0.isStopAsking()) {
							pendingImage(size, link);
							retryTimer = new Timer() {
								@Override
								public void run() {
									getPreview(datasetUri, link);
								}
							};
							timeOffset = (timeOffset + 37) % 100;
							retryTimer.schedule(1000 + timeOffset); // every second or so
						} else if(display && previews.get(size) != null) {
							contentPanel.clear();
							contentPanel.add(createImage(datasetUri, size, link, previews));
						} 
					}
				});
	}

	boolean isGrayImage = false;
	boolean isPendingImage = false;
	
	protected void grayImage(String size, String link) {
		if(!isGrayImage) {
			contentPanel.clear();
			image = new Image(GRAY_URL.get(size));
			image.addStyleName("thumbnail");
			addLink(image, link);
			image.addStyleName("imagePreviewShortWidth");
			//image.setWidth(getMaxWidth()+"px");
			contentPanel.add(image);
			isGrayImage = true;
			isPendingImage = false;
		}
	}

	protected void pending(String datasetUri, String size, String link) {
		pendingImage(size, link);
		getPreview(datasetUri, link);
	}
	
	protected void pendingImage(String size, String link) {
		if(!isPendingImage) {
			contentPanel.clear();
			image = new Image(PENDING_URL.get(size));
			if(size.equals(GetPreviews.LARGE)) {
				image.addStyleName("thumbnail");
				image.addStyleName("pendingLarge");
			} else {
				image.addStyleName("pendingSmall");
			}
			addLink(image, link);
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

	/**
	 * 
	 * @param datasetUri
	 * @param size
	 * @param link
	 * @param previews
	 * @return
	 */
	private Image createImage(final String datasetUri, final String size,
			final String link, Map<String, PreviewImageBean> previews) {

		image = new Image(PREVIEW_URL.get(size) + datasetUri);

		if (size.equals(GetPreviews.LARGE)) {
			image.addStyleName("imagePreviewNoOverflow");

			// keep aspect ratio
			PreviewImageBean previewImageBean = previews.get(GetPreviews.LARGE);
			long width = previewImageBean.getWidth();
			long height = previewImageBean.getHeight();
			if (width >= height) {
				if (width >= maxWidth) {
					image.addStyleName("imagePreviewShortWidth");
					image.setWidth(maxWidth+"px");
				}
			} else {
				if (height >= maxWidth) {
					image.addStyleName("imagePreviewShortHeight");
					image.setHeight(maxWidth+"px");
				}
			}
		} else {
			image.addStyleName("thumbnail");
		}

		image.addErrorHandler(new ErrorHandler() {
			public void onError(ErrorEvent arg0) {

				statusLabel(LOADING_TEXT);

				// wait before trying again
				Timer tryAgain = new Timer() {
					@Override
					public void run() {
						getPreview(datasetUri, link);
					}
				};
				if (delays[whichDelay] > 0) {
					tryAgain.schedule(delays[whichDelay++]);
				}
				statusLabel(NO_PREVIEW_TEXT);
			}
		});

		addLink(image, link);

		return image;
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}
	
	
}
