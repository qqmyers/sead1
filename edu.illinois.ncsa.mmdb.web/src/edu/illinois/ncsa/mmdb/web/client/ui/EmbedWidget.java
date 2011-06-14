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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EmbedWidget extends Composite {
    private final VerticalPanel mainContainer;

    public EmbedWidget(final String uri, final float ratio) {
        mainContainer = new VerticalPanel();
        mainContainer.addStyleName("embeddedWidget");
        mainContainer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        //Width and Height Text Boxes
        HorizontalPanel textBoxes = new HorizontalPanel();

        //Embed widget has extra padding height of 52
        int width = (int) (448 * ratio) + 2;

        int height = 500;

        final TextBox widthBox = new TextBox();
        widthBox.setStyleName("embedSizeBoxes");
        widthBox.setMaxLength(4);
        widthBox.setWidth("2.7em");
        widthBox.setText(Integer.toString(width));
        textBoxes.add(new Label("Width:"));
        textBoxes.add(widthBox);
        Label pixels = new Label("px");
        pixels.addStyleName("embedBoxSpacer");
        textBoxes.add(pixels);

        final TextBox heightBox = new TextBox();
        heightBox.setStyleName("embedSizeBoxes");
        heightBox.setMaxLength(4);
        heightBox.setWidth("2.7em");
        heightBox.setText(Integer.toString(height));
        textBoxes.add(new Label("Height:"));
        textBoxes.add(heightBox);
        textBoxes.add(new Label("px"));

        //Text box to copy code from
        final TextArea iframe = new TextArea();
        iframe.addStyleName("embedTextArea");
        iframe.setText(iframeText(uri, widthBox, heightBox));
        iframe.setWidth("95%");
        iframe.setReadOnly(true);
        iframe.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                iframe.selectAll();
            }
        });

        //Change width and height of iframe dynamically
        widthBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (!widthBox.getText().isEmpty()) {
                    int width_box = Integer.parseInt(widthBox.getText());
                    heightBox.setText(Integer.toString((int) (((width_box - 2) / ratio) + 52)));
                    iframe.setText(iframeText(uri, widthBox, heightBox));
                }
            }
        });

        //Change width and height of iframe dynamically
        heightBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (!heightBox.getText().isEmpty()) {
                    int height_box = Integer.parseInt(heightBox.getText());
                    widthBox.setText(Integer.toString((int) (((height_box - 52) * ratio)) + 2));
                    iframe.setText(iframeText(uri, widthBox, heightBox));
                }

            }
        });

        mainContainer.add(iframe);
        mainContainer.add(textBoxes);

        Button preview = new Button("Preview");
        preview.addStyleName("embedPreviewButton");
        preview.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                EmbedPreviewDialog embedPreview = new EmbedPreviewDialog(iframe.getText());
                embedPreview.show();
            }
        });
        mainContainer.add(preview);

        initWidget(mainContainer);
    }

    private String iframeText(String uri, TextBox width, TextBox height) {
        return "<iframe width=\"" + width.getText() + "px\" height=\"" + height.getText() + "px\" " +
                "src=\"" + GWT.getHostPageBaseURL() + "embed.html#d?id=" + uri + "\" frameborder=\"0\">";

    }

}
