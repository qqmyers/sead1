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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class EditableLabel extends Composite implements HasValueChangeHandlers<String> {
    HorizontalPanel panel;
    Label           label;
    boolean         isEditable = true;
    String          editableStyleName;
    Image           pencilIcon;

    public EditableLabel(String text) {
        super();
        panel = new HorizontalPanel();
        label = new Label(text);
        label.setTitle("Click to edit");
        panel.add(label);
        label.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (isEditable) {
                    label.removeStyleName("editHighlight");
                    displayEditControls();
                }
            }
        });
        label.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                if (isEditable) {
                    label.addStyleName("editHighlight");
                }
            }
        });
        label.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                label.removeStyleName("editHighlight");
            }
        });
        pencilIcon = new Image("images/accessories-text-editor.png"); // FIXME magic number antipattern
        panel.add(pencilIcon);
        pencilIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                displayEditControls();
            }
        });
        initWidget(panel);
    }

    public void displayEditControls() {
        panel.remove(label);
        panel.remove(pencilIcon);
        HorizontalPanel editPanel = new HorizontalPanel();
        if (editableStyleName != null) {
            editPanel.addStyleName(editableStyleName);
        }
        final TextBox textBox = new TextBox();
        textBox.setText(label.getText());
        textBox.setWidth("20em");
        final Anchor submit = new Anchor("Save");
        submit.addStyleName("anchorButton");
        submit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                ValueChangeEvent.fire(EditableLabel.this, textBox.getText());
            }
        });
        final Anchor cancel = new Anchor("Cancel");
        submit.addStyleName("anchorButton");
        cancel.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                cancel();
            }
        });
        textBox.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    ValueChangeEvent.fire(EditableLabel.this, textBox.getText());
                }
            }
        });
        editPanel.add(textBox);
        editPanel.add(submit);
        editPanel.add(cancel);
        panel.add(editPanel);
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                textBox.setFocus(true);
                textBox.selectAll();
            }
        });
    }

    public Label getLabel() {
        return label;
    }

    public void cancel() {
        panel.clear();
        panel.add(label);
        if (isEditable) {
            panel.add(pencilIcon);
        }
    }

    public void setText(String newValue) {
        label.setText(newValue);
        cancel(); // where "cancel" means "no longer editing"
    }

    public String getText() {
        return label.getText();
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean isEditable) {
        if (this.isEditable == isEditable) {
            return;
        }
        if (!isEditable) {
            label.setTitle("");
            GWT.log("'" + label.getText() + "' is not editable, removing pencil icon");
            panel.remove(pencilIcon);
        } else {
            panel.add(pencilIcon);
        }
        this.isEditable = isEditable;
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public String getEditableStyleName() {
        return editableStyleName;
    }

    public void setEditableStyleName(String editableStyleName) {
        this.editableStyleName = editableStyleName;
    }
}
