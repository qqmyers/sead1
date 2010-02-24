package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AnnotateResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AnnotateResourceResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAnnotations;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAnnotationsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;

/**
 * Shows a list of comments (annotations) for a particular resource. Allows the
 * user to add a new annotation to the list.
 * 
 * @author Luigi Marini <lmarini@ncsa.uiuc.edu>
 * 
 */
public class CommentsView extends Composite {

	private SimplePanel mainPanel = new SimplePanel();

	private VerticalPanel layoutPanel;

	private NewAnnotationView newAnnotationView;

	private String resource;

	private VerticalPanel commentsPanel;

	private final MyDispatchAsync service;

	/**
	 * Draws the main panel and the widget to input a new annotation. Calls the
	 * refresh method to refresh the list and retrieve all the current
	 * annotations.
	 * 
	 * @param resource
	 */
	public CommentsView(final String resource, final MyDispatchAsync service) {

		this.resource = resource;

		this.service = service;

		DisclosurePanel disclosurePanel = new DisclosurePanel("Comments");
		
		disclosurePanel.addStyleName("datasetDisclosurePanel");
		
		disclosurePanel.setOpen(true);
		
		disclosurePanel.setAnimationEnabled(true);
		
		initWidget(disclosurePanel);

		mainPanel.addStyleName("commentsView");

		layoutPanel = new VerticalPanel();

		mainPanel.add(layoutPanel);
		
		disclosurePanel.setContent(mainPanel);

		commentsPanel = new VerticalPanel();

		commentsPanel.setWidth("100%");

		layoutPanel.add(commentsPanel);

		newAnnotationView = new NewAnnotationView();

		// add new annotation
		newAnnotationView.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent arg0) {

				service.execute(new AnnotateResource(resource,
						newAnnotationView.getAnnotationBean(), MMDB.sessionID),
						new AsyncCallback<AnnotateResourceResult>() {

							@Override
							public void onFailure(Throwable caught) {
								GWT.log("Failed to annotate resource "
										+ resource, caught);
							}

							@Override
							public void onSuccess(AnnotateResourceResult result) {
								newAnnotationView.clear();

								refresh();

							}
						});

			}

		});

		layoutPanel.add(newAnnotationView);

		refresh();
	}

	/**
	 * Retrieves annotations and adds them to the panel.
	 * 
	 */
	private void refresh() {

		commentsPanel.clear();

		service.execute(new GetAnnotations(resource),
				new AsyncCallback<GetAnnotationsResult>() {

					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Error retrieving annotations", caught);
					}

					@Override
					public void onSuccess(GetAnnotationsResult result) {
						
						ArrayList<AnnotationBean> annotations = result
								.getAnnotations();
						
						show(annotations);
					}
				});
	}

	/**
	 * 
	 * @param annotations
	 */
	public void show(ArrayList<AnnotationBean> annotations) {
		commentsPanel.clear();
		if (annotations.size() == 0) {

			commentsPanel.add(new Label("No comments... yet!"));

		} else {

			for (AnnotationBean annotation : annotations) {

				commentsPanel.add(new AnnotationView(annotation));

			}

		}
	}

}
