/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.MMDB;

/**
 * A widget showing links to login and logout and a name
 * if the user is logged in. Currently used in the main
 * menu.
 * 
 * @author Luigi Marini
 *
 */
public class LoginStatusWidget extends Composite {

	private final FlowPanel mainPanel;
	private final Anchor loginAnchor;
	private final Anchor logoutAnchor;
	
	/**
	 * Create a main panel and show the appropriate
	 * link depending on what the sessionID is.
	 */
	public LoginStatusWidget() {
		mainPanel = new  FlowPanel();
		mainPanel.addStyleName("loginMenu");
		initWidget(mainPanel);
		
		// login anchor
		loginAnchor = new Anchor("Login");
		loginAnchor.addStyleName("loginMenuLink");
		loginAnchor.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("login");
			}
		});
		
		// logout anchor
		logoutAnchor = new Anchor("Logout");
		logoutAnchor.addStyleName("loginMenuLink");
		logoutAnchor.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				LoginPage.logout();
				logout();
				History.newItem("");
			}
		});
		
		if (MMDB.sessionID == null) {
			mainPanel.add(loginAnchor);
		} else {
			mainPanel.add(logoutAnchor);
		}
	}
	
	/**
	 * Add the name of the user logged in and
	 * a link to log out.
	 * 
	 * @param name user logged in
	 */
	public void login(String name) {
		mainPanel.clear();
		mainPanel.add(new Label(name + " | "));
		mainPanel.add(logoutAnchor);
	}
	
	/**
	 * Display a login link.
	 */
	public void logout() {
		mainPanel.clear();
		mainPanel.add(loginAnchor);
	}
}
