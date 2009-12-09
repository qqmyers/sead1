/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import edu.uiuc.ncsa.cet.bean.AnnotationBean;

/**
 * Add new annotation widget.
 * 
 * @author Luigi Marini
 * 
 */
public class NewAnnotationView extends Composite {

	private SimplePanel mainPanel = new SimplePanel();

	private FlexTable mainTable = new FlexTable();

	private TextBox titleTextBox;

	private TextArea descriptionTextArea;

	private Button submitButton;

	/**
	 *  Add new annotation widget.
	 */
	public NewAnnotationView() {

		initWidget(mainPanel);

		mainPanel.addStyleName("newAnnotationMainPanel");

		mainPanel.add(mainTable);

		Label header = new Label("New Comment");

		header.addStyleName("newCommentHeader");

		mainTable.setWidget(0, 0, header);

		mainTable.setHTML(1, 0, "Title: ");

		titleTextBox = new TextBox();

		titleTextBox.setWidth("500px");

		mainTable.setWidget(1, 1, titleTextBox);

		mainTable.setHTML(2, 0, "Description: ");

		descriptionTextArea = new TextArea();
		
		descriptionTextArea.addStyleName("newCommentTextArea");

		descriptionTextArea.setWidth("500px");

		descriptionTextArea.setSize("500px", "200px");

		mainTable.setWidget(2, 1, descriptionTextArea);

		submitButton = new Button("Submit");

		mainTable.setWidget(3, 1, submitButton);
	}

	/**
	 * Create an AnnotationBean based on values in widgets.
	 * @return
	 */
	public AnnotationBean getAnnotationBean() {

		AnnotationBean annotation = new AnnotationBean();

		annotation.setTitle(titleTextBox.getText());

		annotation.setDescription(descriptionTextArea.getText());

		annotation.setDate(new Date());

		return annotation;
	}

	/**
	 * Add a click handler to the submit button.
	 * @param clickHandler
	 */
	public void addClickHandler(ClickHandler clickHandler) {
		submitButton.addClickHandler(clickHandler);
	}

	public void clear() {

		titleTextBox.setText("");

		descriptionTextArea.setText("");

	}

}
