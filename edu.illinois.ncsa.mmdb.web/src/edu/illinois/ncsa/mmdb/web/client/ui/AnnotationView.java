/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteAnnotation;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteAnnotationResult;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DeletedHandler;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;

/**
 * @author Luigi Marini <lmarini@ncsa.uiuc.edu>
 * 
 */
public class AnnotationView extends Composite {

	private SimplePanel mainPanel = new SimplePanel();

	private FlexTable mainTable = new FlexTable();

	private FlexCellFormatter flexCellFormatter;

	public AnnotationView(final String annotatedThingUri, final AnnotationBean annotationBean) {
		
		initWidget(mainPanel);

		mainPanel.addStyleName("annotationMainPanel");

		mainPanel.add(mainTable);

		mainTable.setBorderWidth(0);

		mainTable.setWidth("100%");

		mainTable.setCellSpacing(10);

		flexCellFormatter = mainTable.getFlexCellFormatter();

		mainTable.setHTML(0, 0, annotationBean.getTitle());

		flexCellFormatter.setColSpan(0, 0, 1);

		flexCellFormatter.addStyleName(0, 0, "annotationTitle");

		Anchor deleteButton = new Anchor("Delete");
		deleteButton.addStyleName("datasetActionLink");
		deleteButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ConfirmDialog confirm = new ConfirmDialog("Delete","Are you sure you want to delete this comment?");
				confirm.addConfirmHandler(new ConfirmHandler() {
					public void onConfirm(ConfirmEvent event) {
						delete(annotatedThingUri, annotationBean.getUri());
					}
				});
			}
		});
		mainTable.setWidget(0,1, deleteButton);
		
		String mediumDate = "";

		String shortTime = "";

		if (annotationBean.getDate() != null) {

			mediumDate = DateTimeFormat.getMediumDateFormat().format(
					annotationBean.getDate());

			shortTime = DateTimeFormat.getShortTimeFormat().format(
					annotationBean.getDate());
		}

		String creator = "Anonymous";
		
		if (annotationBean.getCreator() != null) {
			
			creator = annotationBean.getCreator().getName();
		}
		
		mainTable.setHTML(1, 0, "By " + creator
				+ " on " + mediumDate + " " + shortTime);

		flexCellFormatter.setColSpan(1, 0, 2);

		flexCellFormatter.addStyleName(1, 0, "annotationAttributes");

		String description = annotationBean.getDescription();
		
		description = description.replaceAll("\n", "<br>");
		
		mainTable.setHTML(2, 0, description);

		flexCellFormatter.setColSpan(2, 0, 2);

		flexCellFormatter.addStyleName(2, 0, "annotationBody");
	}
	
	// delete this annotation
	void delete(final String annotatedThingUri, final String annotationUri) {
		MMDB.dispatchAsync.execute(new DeleteAnnotation(annotatedThingUri, annotationUri), new AsyncCallback<DeleteAnnotationResult>() {
			public void onFailure(Throwable caught) {
				GWT.log("Error deleting annotation", caught);
			}
			public void onSuccess(DeleteAnnotationResult result) {
				fireEvent(new DeletedEvent(annotationUri));
				//addStyleName("hidden");
			}
		});
	}
	
	public void addDeletedHandler(DeletedHandler h) {
		addHandler(h, DeletedEvent.TYPE);
	}
}
