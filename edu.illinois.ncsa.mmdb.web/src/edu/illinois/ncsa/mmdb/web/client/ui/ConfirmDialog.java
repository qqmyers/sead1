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
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.event.CancelEvent;
import edu.illinois.ncsa.mmdb.web.client.event.CancelHandler;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmHandler;

public class ConfirmDialog extends DialogBox {
    String                  message;
    protected VerticalPanel content;
    HasText                 okText;
    HasText                 cancelText;

    public ConfirmDialog(String title) {
        this(title, null);
    }

    public ConfirmDialog(String title, String message) {
        this(title, message, true);
    }

    public ConfirmDialog(String title, String message, boolean includeCancelButton) {
        setText(title);
        init(includeCancelButton);

        this.setWidth("300px");

        if (message != null) {
            content.add(new Label(message));
        }
    }

    void init(boolean includeCancelButton) {
        VerticalPanel panel = new VerticalPanel();
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        content = new VerticalPanel();
        panel.add(content);

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        Button yesButton = new Button("Yes", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fireEvent(new ConfirmEvent());
                hide();
            }
        });
        okText = yesButton;

        buttonsPanel.add(yesButton);

        if (includeCancelButton) {
            Button noButton = new Button("No", new ClickHandler() {
                public void onClick(ClickEvent event) {
                    fireEvent(new CancelEvent());
                    hide();
                }
            });
            cancelText = noButton;

            buttonsPanel.add(noButton);
        }

        panel.add(buttonsPanel);
        add(panel);
        center();
    }

    public HasText getOkText() {
        return okText;
    }

    public HasText getCancelText() {
        return cancelText;
    }

    public void addCancelHandler(CancelHandler h) {
        addHandler(h, CancelEvent.TYPE);
    }

    public void addConfirmHandler(ConfirmHandler h) {
        addHandler(h, ConfirmEvent.TYPE);
    }
}
