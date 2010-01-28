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
 * A text box that has default text set for when it's empty. The special text to
 * show when it's empty is the watermark.
 * 
 * @author Luigi Marini
 * 
 */
public class WatermarkTextBox extends TextBox implements FocusHandler,
		BlurHandler {

	private String watermark;
	private HandlerRegistration blurHandler;
	private HandlerRegistration focusHandler;

	/**
	 * No watermark set.
	 */
	public WatermarkTextBox() {
		super();
		addStyleName("textBoxWatermark");
	}

	/**
	 * Set content.
	 * 
	 * @param text text in box
	 */
	public WatermarkTextBox(String text) {
		this();
		setText(text);
	}

	/**
	 * Set both content and watermark.
	 * 
	 * @param text text in box
	 * @param watermark watermark for when text is removed
	 */
	public WatermarkTextBox(String text, String watermark) {
		this(text);
		setWatermark(watermark);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gwt.event.dom.client.FocusHandler#onFocus(com.google.gwt.event
	 * .dom.client.FocusEvent)
	 */
	@Override
	public void onFocus(FocusEvent event) {
		removeStyleName("textBoxWatermark");
		if (getText().equalsIgnoreCase(watermark)) {
			setText("");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event
	 * .dom.client.BlurEvent)
	 */
	@Override
	public void onBlur(BlurEvent event) {
		showWatermark();
	}

	/**
	 * Show the watermark.
	 */
	private void showWatermark() {
		if (getText().length() == 0 || getText().equalsIgnoreCase(watermark)) {
			setText(watermark);
			addStyleName("textBoxWatermark");
		}
	}

	/**
	 * Set the value of the watermark.
	 * 
	 * @param watermark text to use for the watermark
	 */
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

	/**
	 * Get the watermark.
	 * 
	 * @return watermark text
	 */
	public String getWatermark() {
		return watermark;
	}

}
