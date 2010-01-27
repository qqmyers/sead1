package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class LabeledListBox extends Composite implements HasValueChangeHandlers<String> {
	String selected;
	ListBox choice;
	
	public LabeledListBox(String label) {
		HorizontalPanel mainPanel = new HorizontalPanel();
		
		Label l = new Label(label);
		l.addStyleName("pagingLabel");
		mainPanel.add(l);

		choice = new ListBox();
		choice.addStyleName("pagingLabel");
		
		choice.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				String selected = choice.getValue(choice.getSelectedIndex());
				setSelected(selected);
				ValueChangeEvent.fire(LabeledListBox.this, selected);
			}
		});
		
		mainPanel.add(choice);
		
		initWidget(mainPanel);
		
		setSelected(selected);
	}

	public String getSelected() {
		return selected;
	}

	public void setSelected(String selected) {
		this.selected = selected;
		if(selected != null) {
			for (int i = 0; i < choice.getItemCount(); i++) {
				if (selected.equals(choice.getValue(i))) {
					choice.setSelectedIndex(i);
				}
			}
		}
	}

	/** Add a choice */
	public void addItem(String title, String value) {
		choice.addItem(title, value);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
}
