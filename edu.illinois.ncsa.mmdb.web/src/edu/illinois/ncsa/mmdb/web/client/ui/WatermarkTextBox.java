/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Luigi Marini
 *
 */
public class WatermarkTextBox extends TextBox implements FocusHandler,
		BlurHandler {

	private String watermark;
	private HandlerRegistration blurHandler;
	private HandlerRegistration focusHandler;
	
	public WatermarkTextBox() {
		super();
		addStyleName("textBoxWatermark");
	}
	
	WatermarkTextBox(String text) {
		this();
		setText(text);
	}
	
	WatermarkTextBox(String text, String watermark) {
		this(text);
		setWatermark(watermark);
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.FocusHandler#onFocus(com.google.gwt.event.dom.client.FocusEvent)
	 */
	@Override
	public void onFocus(FocusEvent event) {
		removeStyleName("textBoxWatermark");
		if (getText().equalsIgnoreCase(watermark)) {
			setText("");
		}
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event.dom.client.BlurEvent)
	 */
	@Override
	public void onBlur(BlurEvent event) {
		showWatermark();
	}
	
	private void showWatermark() {
		if (getText().length() == 0 || getText().equalsIgnoreCase(watermark)) {
			setText(watermark);
			addStyleName("textBoxWatermark");
		}
	}

	public void setWatermark(String watermark) {
		this.watermark = watermark;
		
		if (watermark != null && !watermark.equals("")) {
			blurHandler = addBlurHandler(this);
			focusHandler = addFocusHandler(this);
			showWatermark();
		} else {
			blurHandler.removeHandler();
			focusHandler.removeHandler();
		}
	}

	public String getWatermark() {
		return watermark;
	}

}
