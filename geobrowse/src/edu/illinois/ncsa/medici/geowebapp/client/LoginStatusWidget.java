/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
/**
 * 
 */
package edu.illinois.ncsa.medici.geowebapp.client;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

/**
 * A widget showing links to login and logout and a name if the user is logged
 * in. Currently used in the main menu.
 * 
 * @author Luigi Marini
 * @author Rob Kooper
 * 
 */
public class LoginStatusWidget extends Composite {

	private final HorizontalPanel mainPanel;
	private final String signupURL;

	/**
	 * Create a main panel and show the appropriate link depending on what the
	 * sessionID is.
	 */
	public LoginStatusWidget() {
		mainPanel = new HorizontalPanel();
		mainPanel.addStyleName("navMenu");
		initWidget(mainPanel);
		signupURL = Geo_webapp.getMediciUrl() + "/#signup";
	}

	Hyperlink hyperlink(String name, String link) {
		Hyperlink hlink = new Hyperlink(name, link);
		hlink.addStyleName("navMenuLink");
		return hlink;
	}

	Anchor anchor(String name, String link) {
		Anchor anchor = new Anchor(name, link);
		anchor.addStyleName("navMenuLink");
		return anchor;
	}

	
	/**
	 * Add the name of the user logged in and a link to log out.
	 * 
	 * @param name
	 *            user logged in
	 */
	public void loggedIn(String name) {
		mainPanel.clear();
		Label label = new Label(name);
		label.setStyleName("navMenuText");
		mainPanel.add(label);
		mainPanel.add(hyperlink("(Logout)", "logout"));
	}

	/**
	 * Display a login link.
	 */
	public void loggedOut() {
		mainPanel.clear();
		mainPanel.add(hyperlink("Login", "login"));
		mainPanel.add(anchor("Sign up", signupURL));
	}
}
