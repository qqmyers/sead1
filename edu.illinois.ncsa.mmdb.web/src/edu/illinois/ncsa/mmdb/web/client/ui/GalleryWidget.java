/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.ArrayList;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * @author lmarini
 *
 */
public class GalleryWidget extends Composite {

	private final FlowPanel mainPanel;
	private final ArrayList<String> uris;
	private final FlowPanel imagePanel;
	private int pageNum;
	private final int pageSize = 3;
	private final String PREVIEW_URL = "./api/image/preview/small/";
	
	public GalleryWidget(ArrayList<String> uris) {
		this.uris = uris;
		this.pageNum = 1;
		mainPanel = new FlowPanel();
		initWidget(mainPanel);
		
		imagePanel = new FlowPanel();
		mainPanel.add(imagePanel);
		
		PagingWidget pager = new PagingWidget(pageNum);
		pager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			public void onValueChange(ValueChangeEvent<Integer> event) {
				changePage(event.getValue());
			}
		});
		mainPanel.add(pager);
		
		showImages();
		
	}

	private void changePage(int value) {
		pageNum = value;
		showImages();
	}

	private void showImages() {
		imagePanel.clear();
		for (int i=0; i<pageSize; i++) {
			imagePanel.add(new Image(PREVIEW_URL + uris.get((pageNum - 1) * pageSize + i)));
		}
	}
	
	
}
