/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.History;

/**
 * Search box for text based search using lucene.
 * 
 * @author Luigi Marini
 *
 */
public class SearchBox extends WatermarkTextBox {
	
	private final String watermarkText;

	public SearchBox(String watermark) {
		super("", watermark);
		watermarkText = watermark;
		addStyleName("searchBox");
		addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					String searchString = getText();
					setFocus(false);
					setText("");
					setWatermark(watermarkText);
					History.newItem("search?q=" + searchString);
				}
				
			}
		});
	}
}
