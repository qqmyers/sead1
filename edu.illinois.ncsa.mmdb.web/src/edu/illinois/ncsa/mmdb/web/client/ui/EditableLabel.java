package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class EditableLabel extends Composite implements HasValueChangeHandlers<String> {
	HorizontalPanel panel;
	Label label;
	
	public EditableLabel(String text) {
		super();
		panel = new HorizontalPanel();
		label = new Label(text);
		label.setTitle("Click to edit");
		panel.add(label);
		label.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				displayEditControls();
			}
		});
		initWidget(panel);
	}
	
	public void displayEditControls() {
		panel.remove(label);
		panel.setStyleName(label.getStyleName());
		final TextBox textBox = new TextBox();
		textBox.setText(label.getText());
		textBox.setWidth("20em");
		final Button submit = new Button("Save");
		submit.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ValueChangeEvent.fire(EditableLabel.this, textBox.getText());
			}
		});
		final Button cancel = new Button("Cancel");
		cancel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				displayValue();
			}
		});
		panel.add(textBox);
		panel.add(submit);
		panel.add(cancel);
	}
	
	public Label getLabel() {
		return label;
	}
	
	public void displayValue() {
		panel.clear();
		panel.add(label);
	}
	
	public void displayNewValue(String newValue) {
		label.setText(newValue);
		displayValue();
	}
	
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
}
