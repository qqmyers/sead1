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

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PersonBean;

/**
 * Create the panel containing the information about the dataset.
 * 
 * @return panel with information about the dataset.
 */
public class InfoWidget extends Composite {

    private final FlowPanel panel;

    public InfoWidget(DatasetBean data, DispatchAsync service) {
        panel = new FlowPanel();
        panel.addStyleName("datasetRightColSection");
        Label lbl = new Label("Info");
        lbl.addStyleName("datasetRightColHeading");
        panel.add(lbl);

        lbl = new Label("Contributor: ");
        lbl.addStyleName("datasetRightColText");
        PersonBean creator = data.getCreator();
        if (creator != null) {
            lbl.setTitle(creator.getEmail());
            lbl.setText("Contributor: " + creator.getName());
        }
        panel.add(lbl);

        String filename = data.getFilename();
        addInfo("Filename", filename, panel);

        String size = TextFormatter.humanBytes(data.getSize());
        addInfo("Size", size, panel);

        String cat = ContentCategory.getCategory(data.getMimeType(), service);
        addInfo("Category", cat, panel);

        String type = data.getMimeType();
        addInfo("MIME Type", type, panel);

        String date = "";
        if (data.getDate() != null) {
            date += DateTimeFormat.getShortDateTimeFormat().format(data.getDate());
        }
        addInfo("Uploaded", date, panel);

        initWidget(panel);

    }

    void addInfo(String name, String value, Panel panel) {
        if (value != null && !value.equals("")) {
            Label lbl = new Label(name + ": " + value);
            lbl.addStyleName("datasetRightColText");
            panel.add(lbl);
        }
    }

    public void add(Label lbl) {
        panel.add(lbl);

    }
}