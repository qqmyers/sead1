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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.presenter.BatchOperationPresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.ui.LabeledListBox;

public class BatchOperationListBoxView extends Composite implements Display {
    LabeledListBox       menu;
    Map<String, Command> commands = new HashMap<String, Command>();

    public BatchOperationListBoxView() {
        menu = new LabeledListBox("0 datasets selected");
        menu.getLabel().addStyleName("batchOperationCount");
        menu.addItem("Actions", "Actions");
        menu.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String commandName = event.getValue();
                if (commandName != null && !commandName.equals("Actions")) {
                    Command command = commands.get(commandName);
                    if (command != null) {
                        command.execute();
                    }
                    menu.setSelected("Actions");
                }
            }
        });
        initWidget(menu);
    }

    @Override
    public void addMenuAction(String name, Command command) {
        menu.addItem(name, name);
        commands.put(name, command);
    }

    @Override
    public void addMenuSeparator() {

    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setNumSelected(int num) {
        menu.setText(num + " dataset" + (num == 1 ? "" : "s") + " selected");
    }

}
