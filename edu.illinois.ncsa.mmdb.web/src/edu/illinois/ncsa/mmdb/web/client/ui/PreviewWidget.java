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
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviewsResult;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;

/**
 * 
 * @author Joe Futrelle
 * @author Luigi Marini
 * 
 */
public class PreviewWidget extends Composite {

	private static final Map<String, String> PREVIEW_URL;

	static {
		PREVIEW_URL = new HashMap<String, String>();
		PREVIEW_URL.put(GetPreviews.SMALL, "./api/image/preview/small/");
		PREVIEW_URL.put(GetPreviews.LARGE, "./api/image/preview/large/");
	}

	static final int delays[] = new int[] { 5000, 15000, 30000, -1 };
	static final String LOADING_TEXT = "Loading...";
	static final String NO_PREVIEW_TEXT = "No preview available";
	private final SimplePanel contentPanel;
	private Image image;
	private Label noPreview;
	private final String size;
	private int whichDelay = 0;
	private final String datasetUri;
	private final String link;

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
	public PreviewWidget(final String datasetUri, String desiredSize,
			String link) {

		this.datasetUri = datasetUri;
		this.link = link;
		this.size = desiredSize;

		contentPanel = new SimplePanel();
		initWidget(contentPanel);

		// default to small size if desired size is unrecognized
		if (!desiredSize.equals(GetPreviews.SMALL)) {
			statusLabel(LOADING_TEXT);
			getPreview(datasetUri, link);
		} else {
			showThumbnail();
		}
	}

	/**
	 * Try showing the thumbnail. If not available show a no preview label and
	 * schedule a request for later.
	 */
	private void showThumbnail() {
		Image previewImage = new Image(PREVIEW_URL.get(size) + datasetUri);
		previewImage.addStyleName("thumbnail");
		previewImage.addErrorHandler(new ErrorHandler() {
			public void onError(ErrorEvent event) {
				statusLabel(NO_PREVIEW_TEXT);
				getPreview(datasetUri, link);
			}
		});
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

	/**
	 * 
	 * @param <A>
	 * @param <R>
	 * @param action
	 * @param callback
	 */
	protected void getPreview(final String datasetUri, final String link) {
		MMDB.dispatchAsync.execute(new GetPreviews(datasetUri),
				new AsyncCallback<GetPreviewsResult>() {
					public void onFailure(Throwable arg0) {
						GWT.log("Failed retrieving dataset previews", arg0);
					}

					public void onSuccess(GetPreviewsResult arg0) {
						Map<String, PreviewImageBean> previews = arg0
								.getPreviews();
						if (previews.size() == 0) {
							Timer tryAgain = new Timer() {
								@Override
								public void run() {
									getPreview(datasetUri, link);
								}
							};
							if (delays[whichDelay] > 0) {
								tryAgain.schedule(delays[whichDelay++]);
							} else {
								statusLabel(NO_PREVIEW_TEXT);
							}
						} else {
							contentPanel.clear();
							contentPanel.add(createImage(datasetUri, size,
									link, previews));
						}

					}
				});
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
				if (width >= 600) {
					image.addStyleName("imagePreviewShortWidth");
				}
			} else {
				if (height >= 600) {
					image.addStyleName("imagePreviewShortHeight");
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

		addLink(image);

		return image;
	}
}
