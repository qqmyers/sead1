package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class EditableLabel extends Composite implements HasValueChangeHandlers<String> {
	HorizontalPanel panel;
	Label label;
	boolean isEditable = true;
	
	public EditableLabel(String text) {
		super();
		panel = new HorizontalPanel();
		label = new Label(text);
		label.setTitle("Click to edit");
		panel.add(label);
		label.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(isEditable) {
					label.removeStyleName("editHighlight");
					displayEditControls();
				}
			}
		});
		label.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				if(isEditable) {
					label.addStyleName("editHighlight");
				}
			}
		});
		label.addMouseOutHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent event) {
				label.removeStyleName("editHighlight");
			}
		});
		initWidget(panel);
	}
	
	public void displayEditControls() {
		panel.remove(label);
		HorizontalPanel editPanel = new HorizontalPanel();
		editPanel.addStyleName("datasetTitle");
		final TextBox textBox = new TextBox();
		textBox.setText(label.getText());
		textBox.setWidth("20em");
		final Anchor submit = new Anchor("Save");
		submit.addStyleName("anchorButton");
		submit.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ValueChangeEvent.fire(EditableLabel.this, textBox.getText());
			}
		});
		final Anchor cancel = new Anchor("Cancel");
		submit.addStyleName("anchorButton");
		cancel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				cancel();
			}
		});
		editPanel.add(textBox);
		editPanel.add(submit);
		editPanel.add(cancel);
		panel.add(editPanel);
	}
	
	public Label getLabel() {
		return label;
	}
	
	public void cancel() {
		panel.clear();
		panel.add(label);
	}
	
	public void setText(String newValue) {
		label.setText(newValue);
		cancel(); // where "cancel" means "no longer editing"
	}
	
	public String getText() {
		return label.getText();
	}
	
	
	public boolean isEditable() {
		return isEditable;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
}
