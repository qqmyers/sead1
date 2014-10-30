package edu.illinois.ncsa.mmdb.web.client.geo;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Page title widget.
 * 
 * @author Jim Myers
 * 
 */
public class TitlePanel extends HorizontalPanel {
	private final Label titleLabel;

	public TitlePanel() {
		this("");
	}

	public TitlePanel(String title) {
		super();
		this.setVerticalAlignment(ALIGN_MIDDLE);
		addStyleName("titlePanel");
		titleLabel = new Label(title);
		titleLabel.addStyleName("pageTitle");
		add(titleLabel);
	}
}
