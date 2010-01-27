/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * For users that haven't been enabled by an admin yet.
 * 
 * @author Luigi Marini
 *
 */
public class NotEnabledPage extends Composite {

	private final FlowPanel mainPanel;
	private Object pageTitle;

	public NotEnabledPage() {
		mainPanel = new FlowPanel();
		mainPanel.addStyleName("page");
		initWidget(mainPanel);

		// page title
		mainPanel.add(new TitlePanel("Account Not Enabled"));
		
		HTML message = new HTML("You are account has not been authorized by an administrator yet.");
		// TODO change to more appropriate style
		message.addStyleName("loginForm");
		mainPanel.add(message);
	}
}
