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
/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.view;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.presenter.BatchOperationPresenter;

/**
 * @author Luigi Marini
 * 
 */
public class BatchOperationView extends Composite implements BatchOperationPresenter.Display {

    private final FlowPanel mainLayout;
    private final Label     numSelectedLabel;
    private final MenuBar   actionsMenu;

    public BatchOperationView() {
        mainLayout = new FlowPanel();
        mainLayout.addStyleName("batchOperationPanel");
        initWidget(mainLayout);
        MenuBar actionsBar = new MenuBar();
        actionsBar.setWidth("80px");
        actionsBar.addStyleName("batchOperationMenu");
        actionsMenu = new MenuBar(true);
        actionsBar.addItem("Actions", actionsMenu);

        numSelectedLabel = new Label("0 selected datasets");
        numSelectedLabel.addStyleName("batchOperationCount");
        mainLayout.add(numSelectedLabel);
        mainLayout.add(actionsBar);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setNumSelected(int num) {
        numSelectedLabel.setText(num + " selected datasets");
    }

    @Override
    public void addMenuAction(String name, Command command) {
        MenuItem menuItem = new MenuItem(name, command);
        actionsMenu.addItem(menuItem);
    }
}
