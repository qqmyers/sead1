package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.PasswordTextBox;

/**
 * FIXME: need to figure a way to switch to a TextBox when showing the watermark.
 * This strategy probably doesn't work for PasswordTextBox.
 * 
 * @author Luigi Marini
 *
 */
public class WatermarkPasswordTextBox extends PasswordTextBox implements FocusHandler,
		BlurHandler {

	private String watermark;
	private HandlerRegistration blurHandler;
	private HandlerRegistration focusHandler;
	
	public WatermarkPasswordTextBox() {
		super();
		addStyleName("textBoxWatermark");
	}
	
	public WatermarkPasswordTextBox(String text) {
		this();
		setText(text);
	}
	
	public WatermarkPasswordTextBox(String text, String watermark) {
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
