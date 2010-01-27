package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Page title widget.
 * 
 * @author lmarini
 *
 */
public class TitlePanel extends HorizontalPanel {
	private String title;
	private final Label titleLabel;
	
	public TitlePanel() {
		super();
		this.setVerticalAlignment(ALIGN_MIDDLE);
		addStyleName("titlePanel");
		titleLabel = new Label();
		titleLabel.addStyleName("pageTitle");
		add(titleLabel);
	}
	
	public TitlePanel(String title) {
		this();
		setText(title);
	}
	
	public void setText(String t) {
		title = t;
		titleLabel.setText(title);
	}
	public String getText() {
		return title;
	}
	
	public void addEast(Widget w) {
		HorizontalAlignmentConstant h = getHorizontalAlignment();
		setHorizontalAlignment(ALIGN_RIGHT);
		add(w);
		setHorizontalAlignment(h);
	}
}
