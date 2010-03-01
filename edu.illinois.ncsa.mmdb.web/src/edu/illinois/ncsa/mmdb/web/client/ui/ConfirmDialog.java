package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.event.CancelEvent;
import edu.illinois.ncsa.mmdb.web.client.event.CancelHandler;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmHandler;

public class ConfirmDialog extends DialogBox {

	public ConfirmDialog(String title, String message) {
    	setText(title);
    	VerticalPanel panel = new VerticalPanel();
    	panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    	panel.add(new Label(message));
    	
    	HorizontalPanel buttonsPanel = new HorizontalPanel();
    	Button yesButton = new Button("Yes", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireEvent(new ConfirmEvent());
				hide();
			}
		});
    	
    	buttonsPanel.add(yesButton);
    	Button noButton = new Button("No", new ClickHandler() {
			public void onClick(ClickEvent event) {
				fireEvent(new CancelEvent());
				hide();
			}
		});
    	
    	buttonsPanel.add(noButton);
    	panel.add(buttonsPanel);
    	add(panel);
    	center();
	}
	
	public void addCancelHandler(CancelHandler h) {
		addHandler(h, CancelEvent.TYPE);
	}
	
	public void addConfirmHandler(ConfirmHandler h) {
		addHandler(h, ConfirmEvent.TYPE);
	}
}
