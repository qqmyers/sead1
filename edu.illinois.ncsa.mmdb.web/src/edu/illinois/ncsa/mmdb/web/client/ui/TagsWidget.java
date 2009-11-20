/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTags;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTagsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.TagResourceResult;

/**
 * A widget listing tags and providing a way to add a new one.
 * 
 * @author Luigi Marini
 *
 */
public class TagsWidget extends Composite {

	private FlowPanel mainPanel;
	private FlowPanel tagsPanel;
	private final String id;
	private Button tagButton;
	private final MyDispatchAsync service;
	private Label tagLabel;
	
	/**
	 * A widget listing tags and providing a way to add a new one.
	 * 
	 * @param id
	 * @param service
	 */
	public TagsWidget(final String id, final MyDispatchAsync service) {
		
		this.id = id;
		this.service = service;
		
		mainPanel = new FlowPanel();
		mainPanel.addStyleName("tagsView");
		tagsPanel = new FlowPanel();
		tagsPanel.addStyleName("tagsLinks");
		initWidget(mainPanel);
		
		tagLabel = new Label("Tags:");
		tagLabel.addStyleName("tagsHeading");
		
		mainPanel.add(tagLabel);
		mainPanel.add(tagsPanel);
		
		tagButton = new Button("Tag");
		tagButton.addStyleName("tagsButton");
		mainPanel.add(tagButton);
		tagButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				final TagDialogBox tagDialogBox = new TagDialogBox(id, service);
				
				tagDialogBox.getSubmitButton().addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						submitTag(tagDialogBox.getTags());
						tagDialogBox.hide();
					}
				});
				
				tagDialogBox.center();
				tagDialogBox.show();
				tagDialogBox.getTagBox().addKeyUpHandler(new KeyUpHandler() {
					
					@Override
					public void onKeyUp(KeyUpEvent event) {
						if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
							submitTag(tagDialogBox.getTags());
							tagDialogBox.hide();
						}
						
					}
				});
				
			}
		});
		
		getTags();
		
	}
	
	/**
	 * Use service to retrieve tags from server.
	 */
	private void getTags() {
		
		service.execute(new GetTags(id), new AsyncCallback<GetTagsResult>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error retrieving tags", caught);
			}

			@Override
			public void onSuccess(GetTagsResult result) {
				for (String tag : result.getTags()) {
					tagsPanel.add(tagHyperlink(tag));
				}
			}
		});

	}

	/**
	 * Submit tags to the server.
	 * @param tags
	 */
	private void submitTag(final String tags) {
		
		final Set<String> tagSet = new HashSet<String>();
		for (String s : tags.split(",")) {
			tagSet.add(s);
		}
		
		service.execute(new TagResource(id, tagSet), new AsyncCallback<TagResourceResult>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Failed tagging resource", caught);	
			}

			@Override
			public void onSuccess(TagResourceResult result) {
				for (String tag : tagSet) {
					tagsPanel.add(tagHyperlink(tag));
				}
			}
		});
	}
	
	/**
	 * Create a tag hyperlink.
	 * 
	 * @param tag
	 * @return hyperlink
	 */
	private Hyperlink tagHyperlink(String tag) {
		Hyperlink link = new Hyperlink(tag, History.getToken());
		link.addStyleName("tag");
		return link;
	}

}
