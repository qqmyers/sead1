/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010 , NCSA.  All rights reserved.
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
package edu.illinois.ncsa.mmdb.web.client.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

import edu.illinois.ncsa.mmdb.web.client.presenter.EditableUserMetadataPresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.ui.LabeledListBox;

public class BatchAddMetadataView extends DialogBox implements Display {
    HorizontalPanel      thePanel;
    final LabeledListBox selector;
    final TextBox        valueBox;
    final Button         submitButton;
    final Button         cancelButton;

    public BatchAddMetadataView(String title) {
        setText(title);
        thePanel = new HorizontalPanel();
        add(thePanel);
        selector = new LabeledListBox("Set field:");
        thePanel.add(selector);
        valueBox = new TextBox();
        thePanel.add(valueBox);
        submitButton = new Button("Submit");
        thePanel.add(submitButton);
        cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        thePanel.add(cancelButton);
        center();
    }

    @Override
    public String getSelectedField() {
        // TODO Auto-generated method stub
        return selector.getSelected();
    }

    @Override
    public HasClickHandlers getSubmitControl() {
        // TODO Auto-generated method stub
        return submitButton;
    }

    @Override
    public String getValue() {
        // TODO Auto-generated method stub
        return valueBox.getText();
    }

    @Override
    public void addMetadataField(String uri, String name) {
        selector.addItem(name, uri);
        center();
    }

    @Override
    public void addMetadataValue(String uri, String value) {
        // do nothing, this view doesn't display values
    }

    @Override
    public void onFailure() {
        // FIXME warn the user
    }

    @Override
    public void onSuccess() {
        hide();
    }
}
