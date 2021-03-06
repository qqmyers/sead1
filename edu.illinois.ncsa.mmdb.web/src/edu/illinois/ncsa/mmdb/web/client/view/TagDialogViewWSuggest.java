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
package edu.illinois.ncsa.mmdb.web.client.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.presenter.TextDialogPresenter.Display;

/**
 * Popup dialog box to tag a set of resources with autocomplete.
 * 
 * @author Ashwini Vaidya
 * 
 */
public class TagDialogViewWSuggest extends DialogBox implements Display {

    private final FlowPanel  layout;
    private final SuggestBox textBox;
    private final Button     submitButton;
    private final Button     cancelButton;

    /**
     * A simple dialog box to annotate a resource
     * 
     * @param id
     * @param service
     */
    public TagDialogViewWSuggest(MultiWordSuggestOracle oracle) {
        this("Tag", oracle);
    }

    public TagDialogViewWSuggest(String title, MultiWordSuggestOracle oracle) {
        setText(title);
        layout = new FlowPanel();
        textBox = new SuggestBox(oracle);
        textBox.setWidth("300px");
        layout.add(textBox);

        submitButton = new Button("Submit");

        layout.add(submitButton);

        cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        layout.add(cancelButton);

        setWidget(layout);
        center();
        show();

        DeferredCommand.addCommand(new Command() {
            public void execute() {
                textBox.setFocus(true);
            }
        });
    }

    @Override
    public HasClickHandlers getSubmitButton() {
        return submitButton;
    }

    @Override
    public Widget asWidget() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HasText getTextString() {
        return textBox;
    }

    @Override
    public HasClickHandlers getCancelButton() {
        return cancelButton;
    }

    @Override
    public HasKeyUpHandlers getTextBox() {
        return textBox;
    }
}