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

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

/**
 * @author lmarini
 * 
 */
public class AddToCollectionDialog extends DialogBox {

    private final ListBox list;
    private final Button  submitButton;
    final TextBox         newCollectionBox;

    public AddToCollectionDialog(DispatchAsync service) {
        this(service, "Add to collection");
    }

    public AddToCollectionDialog(DispatchAsync service, String text) {
        this(service, text, false);
    }

    public AddToCollectionDialog(DispatchAsync service, String text, Boolean hideNewCollectionBox) {

        super();
        setAnimationEnabled(true);
        setGlassEnabled(true);
        setText(text);

        FlowPanel mainContainer = new FlowPanel();
        mainContainer.addStyleName("addToCollectionDialog");
        mainContainer.setSize("400px", "300px");
        setWidget(mainContainer);

        mainContainer.add(new HTML("<b>Select collection</b>"));

        list = new ListBox();
        list.setVisibleItemCount(5);
        list.setWidth("350px");
        mainContainer.add(list);

        // retrieve collections
        service.execute(new GetCollections(),
                new AsyncCallback<GetCollectionsResult>() {

                    @Override
                    public void onFailure(Throwable arg0) {
                        GWT.log("Error retrieving collections", arg0);
                    }

                    @Override
                    public void onSuccess(GetCollectionsResult arg0) {
                        for (CollectionBean collection : arg0.getCollections() ) {
                            list.addItem(collection.getTitle(), collection
                                    .getUri());
                        }
                    }
                });

        // new collection
        newCollectionBox = new TextBox();
        newCollectionBox.setWidth("350px");
        if (!hideNewCollectionBox) {
            mainContainer.add(new HTML("<b>Create new collection</b>"));
            mainContainer.add(newCollectionBox);
        }

        // buttons
        FlowPanel buttonsPanels = new FlowPanel();
        mainContainer.add(buttonsPanels);

        // submit button
        submitButton = new Button("Submit");
        buttonsPanels.add(submitButton);

        // close button
        Button closeButton = new Button("Cancel", new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                hide();
            }
        });
        buttonsPanels.add(closeButton);

        // selection listeners
        list.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                newCollectionBox.setText("");
            }
        });
        newCollectionBox.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (list.getSelectedIndex() != -1) {
                    list.setItemSelected(list.getSelectedIndex(), false);
                }
            }
        });

        center();
    }

    public AddToCollectionDialog(DispatchAsync service, ClickHandler clickHandler) {
        this(service);
        addClickHandler(clickHandler);
    }

    public String getSelectedValue() {
        if (list.getSelectedIndex() == -1) {
            return null;
        } else {
            return list.getValue(list.getSelectedIndex());
        }
    }

    public String getNewCollectionValue() {
        return newCollectionBox.getValue();
    }

    public void addClickHandler(ClickHandler ch) {
        submitButton.addClickHandler(ch);
    }
}
