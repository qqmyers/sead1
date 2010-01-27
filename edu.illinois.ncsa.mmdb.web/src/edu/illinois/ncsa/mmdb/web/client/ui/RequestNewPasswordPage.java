/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RequestNewPassword;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RequestNewPasswordResult;

/**
 * @author Luigi Marini
 *
 */
public class RequestNewPasswordPage extends Composite {

	private final MyDispatchAsync dispatchAsync;
	private final FlowPanel mainPanel;
	private final TitlePanel pageTitle;
	private final Widget newPasswordForm;
	private TextBox textBox;
	private SimplePanel feedbackPanel;

	public RequestNewPasswordPage(MyDispatchAsync dispatchAsync) {
		this.dispatchAsync = dispatchAsync;
		mainPanel = new FlowPanel();
		mainPanel.addStyleName("page");
		initWidget(mainPanel);

		// page title
		pageTitle =  new TitlePanel("Request New Password");
		mainPanel.add(pageTitle);
		
		newPasswordForm = newPasswordForm();
		mainPanel.add(newPasswordForm);
	}

	private Widget newPasswordForm() {
		FlexTable table = new FlexTable();
		
		table.addStyleName("pageForm");
		
		feedbackPanel = new SimplePanel();

		table.setWidget(0, 0, feedbackPanel);

		table.getFlexCellFormatter().setColSpan(0, 0, 2);

		table.getFlexCellFormatter().setHorizontalAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_CENTER);
		
		table.setWidget(1, 0, new Label("Email address:"));
		
		textBox = new TextBox();
		
		table.setWidget(1, 1, textBox);
		
		Button requestButton = new Button("Request", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				request();
			}
		});
		
		table.setWidget(2, 1, requestButton);
		
		return table;
	}

	protected void showFeedbackMessage(String message) {
		Label messageLabel = new Label(message);
		messageLabel.addStyleName("feedbackMessage");
		feedbackPanel.clear();
		feedbackPanel.add(messageLabel);
	}
	
	protected void request() {
		dispatchAsync.execute(new RequestNewPassword(textBox.getValue()), new AsyncCallback<RequestNewPasswordResult>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Error requesting new password", caught);
			}

			@Override
			public void onSuccess(RequestNewPasswordResult result) {
				showFeedbackMessage(result.getMessage());
			}
		});
	}
}
