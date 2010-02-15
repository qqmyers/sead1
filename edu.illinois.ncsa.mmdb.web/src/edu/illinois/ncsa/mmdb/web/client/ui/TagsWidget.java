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
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
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

	private final FlowPanel mainPanel;
	private final FlexTable tagsPanel;
	private final String id;
	private final MyDispatchAsync service;
	private final Label tagLabel;
	private final Anchor addTag;
	
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
		mainPanel.addStyleName("datasetRightColSection");
		initWidget(mainPanel);
		
		tagLabel = new Label("Tags");
		tagLabel.addStyleName("datasetRightColHeading");
		mainPanel.add(tagLabel);
		
		tagsPanel = new FlexTable();
		tagsPanel.addStyleName("tagsLinks");
		mainPanel.add(tagsPanel);
		
		addTag = new Anchor("Add a tag");
		mainPanel.add(addTag);
		
		addTag.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				mainPanel.remove(addTag);
				
				final AddTagWidget tagWidget = new AddTagWidget();
				
				tagWidget.getSubmitLink().addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						submitTag(tagWidget.getTags());
						mainPanel.remove(tagWidget);
						mainPanel.add(addTag);
					}
				});
				
				tagWidget.getTagBox().addKeyUpHandler(new KeyUpHandler() {
					
					@Override
					public void onKeyUp(KeyUpEvent event) {
						if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
							submitTag(tagWidget.getTags());
							mainPanel.remove(tagWidget);
							mainPanel.add(addTag);
						}
						
					}
				});
				
				tagWidget.getCancelLink().addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						mainPanel.remove(tagWidget);
						mainPanel.add(addTag);
					}
				});
				
				mainPanel.add(tagWidget);
//				mainPanel.setCellHorizontalAlignment(tagWidget, HasHorizontalAlignment.ALIGN_RIGHT);
				
				DeferredCommand.addCommand(new Command() {
					@Override
					public void execute() {
						tagWidget.getTagBox().setFocus(true);
					}
				});
				
			}
		});
		getTags();
		
	}
	
	void addTag(final String tag) {
		final int row = tagsPanel.getRowCount();
		tagsPanel.setWidget(row,0,tagHyperlink(tag));
		Anchor delete = new Anchor("delete");
		delete.addStyleName("deleteLink");
		delete.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				deleteTag(tag, row);
			}
		});
		tagsPanel.setWidget(row,1,delete);
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
				for (final String tag : result.getTags()) {
					addTag(tag);
				}
			}
		});

	}

	Set<String> tagSet(String cdl) {
		Set<String> tagSet = new HashSet<String>();
		for (String s : cdl.split(",")) {
			tagSet.add(s);
		}
		return tagSet;
	}
	
	private void deleteTag(final String tags, final int toRemove) {
		final Set<String> tagSet = tagSet(tags);
		
		service.execute(new TagResource(id, tagSet, true), new AsyncCallback<TagResourceResult>() {
			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Failed to remove resource", caught);	
			}
			@Override
			public void onSuccess(TagResourceResult result) {
				tagsPanel.getRowFormatter().addStyleName(toRemove,"hidden");
			}
		});
	}
	
	/**
	 * Submit tags to the server.
	 * @param tags
	 */
	private void submitTag(final String tags) {
		
		final Set<String> tagSet = tagSet(tags);
		
		service.execute(new TagResource(id, tagSet), new AsyncCallback<TagResourceResult>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Failed tagging resource", caught);	
			}

			@Override
			public void onSuccess(TagResourceResult result) {
				for (String tag : tagSet) {
					addTag(tag);
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
		Hyperlink link = new Hyperlink(tag, "tag?title=" + tag);
		link.addStyleName("tag");
		return link;
	}

}
