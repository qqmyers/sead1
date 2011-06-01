/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.uiuc.edu/
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

import java.util.HashSet;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DownloadDialog extends DialogBox {

    public DownloadDialog(String title, HashSet<String> uris) {
        super();

        setAnimationEnabled(true);
        setGlassEnabled(true);
        setText(title);

        VerticalPanel vertical = new VerticalPanel();
        vertical.setWidth("300px");
        vertical.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        HorizontalPanel horizontal = new HorizontalPanel();
        horizontal.addStyleName("downloadDialog");
        HorizontalPanel buttons = new HorizontalPanel();

        //Compile string of URIs to send through invisible text field
        StringBuffer postData = new StringBuffer();
        for (String uri : uris ) {
            postData.append(URL.encode(uri));
            postData.append("&");
        }

        //Form options
        final FormPanel form = new FormPanel();
        form.setAction("BatchDownload");
        form.setEncoding(FormPanel.ENCODING_URLENCODED);
        form.setMethod(FormPanel.METHOD_POST);

        //Set file name
        Label titleLabel = new Label("Filename:");
        final TextBox titleText = new TextBox();
        titleText.setName("filename");
        titleText.setText("medici");
        titleText.setHeight("24px");
        titleText.setMaxLength(32);

        Label extLabel = new Label(".zip");

        //Invisible list of URIs
        TextArea text = new TextArea();
        text.setName("uri");
        text.setVisible(false);
        text.setText(postData.toString());

        horizontal.add(titleLabel);
        horizontal.add(titleText);
        horizontal.add(extLabel);
        horizontal.add(text);

        //Submit Form
        Button submit = new Button("Download");
        submit.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                form.submit();
            }
        });
        buttons.add(submit);

        Button cancel = new Button("Cancel");
        cancel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                DownloadDialog.this.hide();
            }
        });
        buttons.add(cancel);
        cancel.addStyleName("confirmDialogButtonNo");

        //Default filename
        form.addSubmitHandler(new SubmitHandler() {

            @Override
            public void onSubmit(SubmitEvent event) {
                if (titleText.getText().isEmpty()) {
                    titleText.setText("medici");
                }

            }
        });

        form.add(horizontal);
        vertical.add(form);
        vertical.add(buttons);

        add(vertical);

        center();
    }
}
