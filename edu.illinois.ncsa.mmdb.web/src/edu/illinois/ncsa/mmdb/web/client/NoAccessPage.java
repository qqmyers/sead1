/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

import edu.illinois.ncsa.mmdb.web.client.ui.TitlePanel;

/**
 * @author lmarini
 *
 */
public class NoAccessPage extends Composite {

	private final FlowPanel mainPanel;

	public NoAccessPage() {
		mainPanel = new FlowPanel();
		mainPanel.addStyleName("page");
		initWidget(mainPanel);

		// page title
		mainPanel.add(new TitlePanel("Access Denied"));
		
		HTML message = new HTML("You do not have access to this page.");
		// TODO change to more appropriate style
		message.addStyleName("loginForm");
		mainPanel.add(message);
	}
	
}
