package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

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
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviewsResult;

public class PreviewWidget extends Composite {
	private static final Map<String,String> PREVIEW_URL;
	
	static {
		PREVIEW_URL = new HashMap<String,String>();
		PREVIEW_URL.put(GetPreviews.SMALL, "./api/image/preview/small/");
		PREVIEW_URL.put(GetPreviews.LARGE, "./api/image/preview/large/");
	}
	
	VerticalPanel contentPanel;
	Image image;
	Label noPreview;

	public HasClickHandlers getTarget() {
		if(image != null) {
			return image;
		} else {
			return noPreview;
		}
	}

	protected <A extends Action<R>, R extends Result> void ajax(A action, AsyncCallback<R> callback) {
		MMDB.dispatchAsync.execute(action, callback);
	}
	
	Image createImage(final String datasetUri, final String size, final String link) {
		image = new Image(PREVIEW_URL.get(size) + datasetUri);
		image.addStyleName("thumbnail");
		image.addErrorHandler(new ErrorHandler() {
			public void onError(ErrorEvent arg0) {
				// no preview is available
				contentPanel.clear();
				noPreview = new Label("No preview available");
				noPreview.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						History.newItem(link);
					}
				});
				contentPanel.add(noPreview);
				Timer tryAgain = new Timer() {
					public void run() {
						ajax(new GetPreviews(datasetUri), new AsyncCallback<GetPreviewsResult>() {
							public void onFailure(Throwable arg0) {
								// give up.
							}
							public void onSuccess(GetPreviewsResult arg0) {
								contentPanel.clear();
								contentPanel.add(createImage(datasetUri,size,link));
							}
						});
					}
				};
				if(delays[whichDelay] > 0) {
					tryAgain.schedule(delays[whichDelay++]); // give the extractor a good long time
				}
			}
		});
		image.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				History.newItem(link);
			}
		});
		return image;
	}
	
	int whichDelay = 0;
	static final int delays[] = new int[] { 5000, 15000, 30000, -1 };
	
	public PreviewWidget(final String datasetUri, String desiredSize, String link) {
		final String size;
		if(!desiredSize.equals(GetPreviews.LARGE)) {
			size = GetPreviews.SMALL;
		} else {
			size = desiredSize;
		}
		contentPanel = new VerticalPanel();
		image = createImage(datasetUri, size, link);
		contentPanel.add(image);
		initWidget(contentPanel);
	}
}

