/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Luigi Marini
 *
 */
public abstract class Page extends Composite {

	protected final FlowPanel mainLayoutPanel;
	protected final TitlePanel pageTitle;
	protected DispatchAsync dispatchAsync;
	private final SimplePanel feedbackPanel;

	/**
	 * 
	 */
	public Page() {
		mainLayoutPanel = new FlowPanel();
		mainLayoutPanel.addStyleName("page");
		initWidget(mainLayoutPanel);
		
		// page title
		pageTitle =  new TitlePanel("Page");
		mainLayoutPanel.add(pageTitle);
		
		// feedback panel
		feedbackPanel = new SimplePanel();
		mainLayoutPanel.add(feedbackPanel);
		
		layout();
	}
	
	public Page(DispatchAsync dispatchAsync) {
		this();
		this.dispatchAsync = dispatchAsync;
	}
	
	/**
	 * 
	 * @param title
	 */
	public Page(String title, DispatchAsync dispatchAsync) {
		this(dispatchAsync);
		setPageTitle(title);
	}
	
	/**
	 * 
	 * @param title
	 */
	public void setPageTitle(String title) {
		pageTitle.setText(title);
	}
	
	/**
	 * 
	 */
	public void clear() {
		mainLayoutPanel.clear();
		mainLayoutPanel.add(pageTitle);
		mainLayoutPanel.add(feedbackPanel);
	}
	
	/**
	 * 
	 */
	public void refresh() {
		layout();
	}

	/**
	 * 
	 */
	public abstract void layout();
	
	/**
	 * 
	 * @param message
	 */
	public void showFeedbackMessage(String message) {
		Label messageLabel = new Label(message);
		messageLabel.addStyleName("feedbackMessage");
		feedbackPanel.clear();
		feedbackPanel.add(messageLabel);
	}
}
