package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Page title widget.
 * 
 * @author lmarini
 *
 */
public class TitlePanel extends HorizontalPanel implements HasValueChangeHandlers<String> {
	private String title;
	private final EditableLabel titleLabel;
	
	public TitlePanel() {
		super();
		this.setVerticalAlignment(ALIGN_MIDDLE);
		addStyleName("titlePanel");
		titleLabel = new EditableLabel("");
		titleLabel.setEditable(false);
		titleLabel.getLabel().addStyleName("pageTitle");
		titleLabel.setEditableStyleName("datasetTitle");
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

	public boolean isEditable() {
		return titleLabel.isEditable();
	}
	public void setEditable(boolean editable) {
		titleLabel.setEditable(editable);
	}
	
	public EditableLabel getEditableLabel() { // wagh. encapsulation violation
		return titleLabel;
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {
		return titleLabel.addValueChangeHandler(handler);
	}
}
