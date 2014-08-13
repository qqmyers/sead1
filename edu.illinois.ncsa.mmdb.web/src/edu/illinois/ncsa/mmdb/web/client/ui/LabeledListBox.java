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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class LabeledListBox extends Composite implements ValueSelectionControl<String> {
    String  selected;
    Label   label;
    ListBox choice;

    public LabeledListBox(String text) {
        HorizontalPanel mainPanel = new HorizontalPanel();

        label = new Label(text);
        label.addStyleName("labeledListBoxTitle");
        mainPanel.add(label);

        choice = new ListBox();
        choice.addStyleName("labeledListBoxPulldown");

        choice.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                String selected = choice.getValue(choice.getSelectedIndex());
                setSelected(selected);
                ValueChangeEvent.fire(LabeledListBox.this, selected);
            }
        });

        mainPanel.add(choice);

        initWidget(mainPanel);
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
        if (selected != null) {
            for (int i = 0; i < choice.getItemCount(); i++ ) {
                if (selected.equals(choice.getValue(i))) {
                    choice.setSelectedIndex(i);
                }
            }
        }
    }

    /** Add a choice */
    public void addItem(String title, String value) {
        choice.addItem(title, value);

        if (getSelected() == null) {
            selected = value;
        }
    }

    public void removeItem(String title) {
        for (int i = 0; i < choice.getItemCount(); i++ ) {
            if (title.equals(choice.getValue(i))) {
                choice.removeItem(i);
                return;
            }
        }
    }

    public void clear() {
        int itemCount = choice.getItemCount();
        for (int i = 0; i < itemCount; i++ ) {
            choice.removeItem(itemCount - 1 - i); //remove from end, or can remove(0) itemCount-1 times.
        }
    }

    public void setText(String text) {
        label.setText(text);
    }

    public String getText() {
        return label.getText();
    }

    public String getTitle() {
        return choice.getItemText(choice.getSelectedIndex());
    }

    public Label getLabel() {
        return label;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
