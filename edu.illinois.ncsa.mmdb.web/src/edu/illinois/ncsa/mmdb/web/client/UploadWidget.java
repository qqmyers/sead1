package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

public class UploadWidget extends Composite {
	public UploadWidget() {
		// multiple uploads
		VerticalPanel uploadStackPanel = new VerticalPanel();
		// each upload
		FlowPanel uploadPanel = new FlowPanel();
		// has a file upload form
		final FormPanel uploadForm = new FormPanel();
		uploadForm.setAction("UploadBlob"); // FIXME hardcoded servlet URL
		uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		uploadForm.setMethod(FormPanel.METHOD_POST);
		FileUpload fu = new FileUpload();
		fu.setName("f1");
		uploadForm.add(fu);
		// form behavior:
		uploadForm.addSubmitHandler(new SubmitHandler() {
			public void onSubmit(SubmitEvent event) {
			}
		});
		uploadForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				History.back();
			}
		});
		// and a submit button
		Button submit = new Button("Submit", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				GWT.log("upload form submitted",null);
				uploadForm.submit();
			}
		});
		uploadPanel.add(uploadForm);
		uploadPanel.add(submit);
		uploadStackPanel.add(uploadPanel);
		initWidget(uploadStackPanel);
	}
}
