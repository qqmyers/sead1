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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsers;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetUsersResult;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;

/**
 * Add new annotation widget.
 *
 * @author Luigi Marini
 * @author myersjd@umich.edu
 *
 */
public class NewAnnotationView extends Composite {

    private final SimplePanel              mainPanel    = new SimplePanel();

    private final FlexTable                mainTable    = new FlexTable();

    private final TextBox                  titleTextBox = new TextBox();

    private final TextArea                 descriptionTextArea;

    private final Button                   submitButton;

    private final PopupPanel               colleagues;

    private static SuggestOracle           so           = null;
    private static HashMap<String, String> emailToName  = new HashMap<String, String>();

    /**
     * Add new annotation widget.
     *
     * @param service
     */
    @SuppressWarnings("deprecation")
    public NewAnnotationView(DispatchAsync service) {

        initWidget(mainPanel);

        if (so == null) {
            so = loadOracle(service);
        }

        colleagues = new PopupPanel();
        colleagues.getElement().setId("userspopup");

        final SuggestBox usersBox = new SuggestBox(so);
        usersBox.getElement().setId("suggestUsers");
        colleagues.setWidget(usersBox);
        //Adding keypresshandler to suggestbox give 2 copies of the event (known GWT issue) - this is one way to fix.
        usersBox.getTextBox().addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if ((event.getCharCode() == KeyCodes.KEY_ENTER)
                //     || (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
                ) {
                    event.stopPropagation();
                    String name = getNameFromText(usersBox.getText());
                    if (name == null) {
                        //replace with plain text - no match with a user
                        String currentString = descriptionTextArea.getText().substring(0, descriptionTextArea.getText().length() - 1);
                        descriptionTextArea.setText(currentString + usersBox.getText());
                    } else {
                        descriptionTextArea.setText(descriptionTextArea.getText() + name);
                    }
                    usersBox.setText("");
                    colleagues.hide();
                    descriptionTextArea.setFocus(true);
                }
            }

            private String getNameFromText(String text) {
                if (emailToName.containsKey(text)) {
                    return emailToName.get(text);

                } else if (emailToName.containsValue(text)) {
                    return text;
                }
                return null;
            }
        });

        mainPanel.addStyleName("newAnnotationMainPanel");

        mainPanel.add(mainTable);

        Label header = new Label("Write a Comment");

        header.addStyleName("newCommentHeader");

        mainTable.setWidget(0, 0, header);

        descriptionTextArea = new TextArea();

        descriptionTextArea.addStyleName("newCommentTextArea");

        descriptionTextArea.setWidth("500px");

        descriptionTextArea.setSize("500px", "200px");

        descriptionTextArea.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {

                if ('@' == event.getCharCode() || (event.getNativeEvent().getCharCode() == '@')) {
                    colleagues.setPopupPosition(event.getRelativeElement().getAbsoluteRight() - 250,
                            event.getRelativeElement().getAbsoluteTop() - 20);
                    colleagues.show();
                    usersBox.setFocus(true);

                }

            }
        });

        mainTable.setWidget(1, 0, descriptionTextArea);

        submitButton = new Button("Comment");

        mainTable.setWidget(2, 0, submitButton);
    }

    private SuggestOracle loadOracle(DispatchAsync service) {
        final MultiWordSuggestOracle userSuggestOracle = new MultiWordSuggestOracle();
        emailToName.clear();
        service.execute(new GetUsers(),
                new AsyncCallback<GetUsersResult>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Error getting users", caught);
                    }

                    @Override
                    public void onSuccess(GetUsersResult result) {
                        ArrayList<GetUsersResult.User> users = result.getUsers();
                        for (GetUsersResult.User u : users ) {
                            if ((u.name != null) && (u.name.length() > 0) && (u.email != null) && (u.email.length() > 0)) {
                                userSuggestOracle.add(u.name);
                                userSuggestOracle.add(u.email);
                                emailToName.put(u.email, u.name);

                            }
                        }
                    }
                });

        return userSuggestOracle;
    }

    /**
     * Create an AnnotationBean based on values in widgets.
     *
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
     *
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
