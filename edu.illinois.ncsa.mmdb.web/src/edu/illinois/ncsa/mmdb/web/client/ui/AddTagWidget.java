/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A simple widget to add a tag to a resource
 * 
 * @author Luigi Marini
 *
 */
public class AddTagWidget extends Composite {
	
	FlowPanel layout;
	private final TextBox tagBox;
	private final Anchor submitLink;
	private final Anchor cancelLink;
	
	public AddTagWidget() {
		
		layout = new FlowPanel();
		initWidget(layout);
		layout.addStyleName("addTags");
		tagBox = new TextBox();
		tagBox.setWidth("100px");
		layout.add(tagBox);
		
		submitLink = new Anchor("Submit");
		
		submitLink.addStyleName("addTagsLink");
		
		layout.add(submitLink);
		
		cancelLink = new Anchor("Cancel");
		
		cancelLink.addStyleName("addTagsLink");
		
		cancelLink.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
			}
		});
		
		layout.add(cancelLink);
	}

	public Anchor getSubmitLink() {
		return submitLink;
	}
	
	public Anchor getCancelLink() {
		return cancelLink;
	}

	public TextBox getTagBox() {
		return tagBox;
	}

	public String getTags() {
		return tagBox.getText();
	}
}
