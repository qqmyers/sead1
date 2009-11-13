/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

import edu.uiuc.ncsa.cet.bean.AnnotationBean;

/**
 * @author Luigi Marini <lmarini@ncsa.uiuc.edu>
 * 
 */
public class AnnotationView extends Composite {

	private SimplePanel mainPanel = new SimplePanel();

	private FlexTable mainTable = new FlexTable();

	private FlexCellFormatter flexCellFormatter;

	public AnnotationView(AnnotationBean annotationBean) {

		initWidget(mainPanel);

		mainPanel.addStyleName("annotationMainPanel");

		mainPanel.add(mainTable);

		mainTable.setBorderWidth(0);

		mainTable.setWidth("100%");

		mainTable.setCellSpacing(10);

		flexCellFormatter = mainTable.getFlexCellFormatter();

		mainTable.setHTML(0, 0, annotationBean.getTitle());

		flexCellFormatter.setColSpan(0, 0, 2);

		flexCellFormatter.addStyleName(0, 0, "annotationTitle");

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

		mainTable.setHTML(2, 0, annotationBean.getDescription());

		flexCellFormatter.setColSpan(2, 0, 2);

		flexCellFormatter.addStyleName(2, 0, "annotationBody");
	}
}
