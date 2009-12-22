/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;

/**
 * @author lmarini
 *
 */
public class GalleryWidget extends Composite {

	private final FlowPanel mainPanel;
	private final ArrayList<String> uris;
	private final HorizontalPanel imagePanel;
	private int pageNum;
	private final int pageSize = 3;
	private final String PREVIEW_URL = "./api/image/preview/small/";
	
	public GalleryWidget(ArrayList<String> uris) {
		this.uris = uris;
		this.pageNum = 1;
		mainPanel = new FlowPanel();
		mainPanel.addStyleName("gallery");
		initWidget(mainPanel);
		
		imagePanel = new HorizontalPanel();
		imagePanel.addStyleName("galleryImages");
		mainPanel.add(imagePanel);
		
		PagingWidget pager = new PagingWidget(pageNum);
		pager.addStyleName("galleryPager");
		pager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			public void onValueChange(ValueChangeEvent<Integer> event) {
				changePage(event.getValue());
			}
		});
		mainPanel.add(pager);
		
		showImages();
		
	}

	/**
	 * 
	 * @param value
	 */
	private void changePage(int value) {
		pageNum = value;
		showImages();
	}

	/**
	 * 
	 */
	private void showImages() {
		imagePanel.clear();
		if (uris.size() > pageSize) {
			for (int i=0; i<pageSize; i++) {
				final String uri = uris.get((pageNum - 1) * pageSize + i);
				PreviewWidget preview = new PreviewWidget(uri, GetPreviews.SMALL, "dataset?id="+uri);
				imagePanel.add(preview);
			}
		} else {
			for (int i=0; i<uris.size(); i++) {
				final String uri = uris.get(i);
				PreviewWidget preview = new PreviewWidget(uri, GetPreviews.SMALL, "dataset?id="+uri);
				imagePanel.add(preview);
			}
		}
	}
	
	
}
