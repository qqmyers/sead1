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
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.MMDB;

/**
 * A widget showing links to login and logout and a name
 * if the user is logged in. Currently used in the main
 * menu.
 *
 * @author Luigi Marini
 * @author Rob Kooper
 *         #author myersjd@umich.edu
 *
 */
public class LoginStatusWidget extends Composite {

    private final FlowPanel   mainPanel;
    protected DisclosurePanel logoutPanel;
    protected Label           dropLabel;

    /**
     * Create a main panel and show the appropriate
     * link depending on what the sessionID is.
     */
    public LoginStatusWidget() {
        mainPanel = new FlowPanel();
        mainPanel.addStyleName("loginPanel");
        initWidget(mainPanel);
        if (MMDB.getUsername() != null) {
            loggedIn(MMDB.getUsername());
        } else {
            loggedOut();
        }
    }

    Hyperlink hyperlink(String name, String link) {
        Hyperlink theLink = new Hyperlink(name, link);
        // anchor.addStyleName("navMenuLink");
        return theLink;
    }

    /**
     * Add the name of the user logged in and
     * a link to log out.
     *
     * @param name
     *            user logged in
     */
    public void loggedIn(String name) {
        mainPanel.clear();
        mainPanel.add(hyperlink(name, "account"));
        logoutPanel = new DisclosurePanel();
        FocusPanel fp = new FocusPanel();
        FlowPanel newHeaderFlowPanel = new FlowPanel();
        newHeaderFlowPanel.addStyleName("logoutPanel");
        newHeaderFlowPanel.add(hyperlink("Logout", "logout"));
        dropLabel = new Label('\u25BC' + "");
        dropLabel.addStyleName("droparrow");
        dropLabel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                logoutPanel.setOpen(!logoutPanel.isOpen());
                dropLabel.setText(logoutPanel.isOpen() ? "\u25B2" : "\u25BC");
                event.stopPropagation();

            }
        });

        newHeaderFlowPanel.add(dropLabel);
        fp.add(newHeaderFlowPanel);
        logoutPanel.setHeader(fp);

        logoutPanel.getHeader().unsinkEvents(Event.ONCLICK);
        //Stop panel clicks from opening disclosure panel
        fp.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
            }

        });
        VerticalPanel vp = new VerticalPanel();
        vp.setStyleName("navMenuText");
        Anchor switchLink = new Anchor("Switch User");
        switchLink.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                LoginPage.logout(new Command() {

                    @Override
                    public void execute() {
                        LoginPage.setAutologin(false);
                        History.newItem("login", true);
                    }
                });
            }
        });
        vp.add(switchLink);
        Anchor socialLink = new Anchor(" Social Logout", "http://accounts.google.com/logout");
        socialLink.setTarget("_blank");
        if (!"local".equals(MMDB.getSessionState().getLoginProvider())) {
            socialLink.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    LoginPage.logout(new Command() {
                        @Override
                        public void execute() {
                            LoginPage.setAutologin(false);
                            History.newItem("login", true);
                        }
                    });
                }
            });
            vp.add(socialLink);
        }
        logoutPanel.add(vp);
        mainPanel.add(logoutPanel);
    }

    /**
     * Display a login link.
     */
    public void loggedOut() {
        mainPanel.clear();
        mainPanel.add(hyperlink("Login", "login"));
        mainPanel.add(hyperlink("Sign up", "signup"));
    }
}
