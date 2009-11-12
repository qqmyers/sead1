/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;


import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.TextBox;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResourceResult;

/**
 * @author lmarini
 *
 */
public class TagDialogBox extends DialogBox {

	private FlowPanel layout;
	private TextBox tagBox;
	private Button submitButton;
	private Button cancelButton;
	private final MyDispatchAsync service;
	private final String id;
	
	/**
	 * A simple dialog box to annotate a resource
	 * 
	 * TODO automatically put cursor in text field
	 * 
	 * @param id
	 * @param service
	 */
	public TagDialogBox(String id, MyDispatchAsync service) {
		this.id = id;
		this.service = service;
		
		setText("Tag");
		
		layout = new FlowPanel();
		tagBox = new TextBox();
		tagBox.setWidth("300px");
		layout.add(tagBox);
		
		submitButton = new Button("Submit");
		
		layout.add(submitButton);
		
		cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		layout.add(cancelButton);
		
		setWidget(layout);
		center();
		show();
	}

	public Button getSubmitButton() {
		return submitButton;
	}


	public String getTags() {
		return tagBox.getText();
	}

	public FocusWidget getTagBox() {
		return tagBox;
	}
}
