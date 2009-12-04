/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;

/**
 * A simple widget to add a tag to a resource
 * 
 * @author Luigi Marini
 *
 */
public class AddTagWidget extends Composite {
	
	HorizontalPanel layout;
	private final String resouce;
	private final MyDispatchAsync service;
	private TextBox tagBox;
	private Button submitButton;
	private Button cancelButton;
	
	public AddTagWidget(String resouce, MyDispatchAsync service) {
		
		this.resouce = resouce;
		this.service = service;
		
		layout = new HorizontalPanel();
		initWidget(layout);
		layout.addStyleName("addTags");
		layout.setSpacing(5);
		tagBox = new TextBox();
		tagBox.setWidth("100px");
		layout.add(tagBox);
		
		submitButton = new Button("Submit");
		
		layout.add(submitButton);
		
		cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
			}
		});
		
		layout.add(cancelButton);
	}

	public Button getSubmitButton() {
		return submitButton;
	}
	
	public Button getCancelButton() {
		return cancelButton;
	}

	public TextBox getTagBox() {
		return tagBox;
	}

	public String getTags() {
		return tagBox.getText();
	}
}
