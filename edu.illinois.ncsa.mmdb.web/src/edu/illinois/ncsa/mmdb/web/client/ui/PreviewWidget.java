package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PreviewWidget extends Composite {
	public static final int SMALL = 0;
	public static final int LARGE = 1;
	
	private final String PREVIEW_URL[] = new String[] {
			"./api/image/preview/small/",
			"./api/image/preview/large/"
	};
	
	Image image;
	Label noPreview;

	public HasClickHandlers getTarget() {
		if(image != null) {
			return image;
		} else {
			return noPreview;
		}
	}
	
	public PreviewWidget(String datasetUri, int size) {
		if(size != SMALL && size != LARGE) { size = SMALL; }
		final VerticalPanel contentPanel = new VerticalPanel();
		image = new Image(PREVIEW_URL[size] + datasetUri);
		image.addErrorHandler(new ErrorHandler() {
			public void onError(ErrorEvent arg0) {
				contentPanel.remove(0);
				noPreview = new Label("No preview available");
				contentPanel.add(noPreview);
			}
		});
		image.addStyleName("thumbnail");
		contentPanel.add(image);
		initWidget(contentPanel);
	}
}

