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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Small info widget for a dataset.
 * 
 * @author Luigi Marini
 * 
 */
public class DatasetInfoWidget extends Composite {

    private final FlowPanel     mainPanel;
    private final DispatchAsync service;

    public DatasetInfoWidget(DatasetBean dataset, DispatchAsync service) {
        this(dataset, false, service);
    }

    public DatasetInfoWidget(DatasetBean dataset, boolean shortenTitle, DispatchAsync service) {
        this.service = service;
        mainPanel = new FlowPanel();
        mainPanel.addStyleName("datasetInfoWidget");
        initWidget(mainPanel);

        PreviewWidget thumbnail = new PreviewWidget(dataset.getUri(), GetPreviews.SMALL, "dataset?id=" + dataset.getUri(), ContentCategory.getCategory(dataset.getMimeType(), service), service);
        thumbnail.setMaxWidth(100);
        SimplePanel previewPanel = new SimplePanel();
        previewPanel.addStyleName("datasetInfoThumbnail");
        previewPanel.add(thumbnail);
        mainPanel.add(previewPanel);

        FlowPanel descriptionPanel = new FlowPanel();
        descriptionPanel.addStyleName("datasetInfoDescription");
        HorizontalPanel anchorPanel = new HorizontalPanel();
        Hyperlink hyperlink;
        if (!shortenTitle) {
            hyperlink = new Hyperlink(shortenTitle(dataset.getTitle()), "dataset?id=" + dataset.getUri());
        } else {
            hyperlink = new Hyperlink(dataset.getTitle(), "dataset?id=" + dataset.getUri());
        }
        hyperlink.setStyleName("dataLink");
        hyperlink.setTitle(dataset.getTitle());
        anchorPanel.add(hyperlink);
        //so whitespace next to title won't get hyperlinked
        anchorPanel.add(new Label(""));

        descriptionPanel.add(anchorPanel);
        if (dataset.getCreator().getName() != null) {
            descriptionPanel.add(new Label(dataset.getCreator().getName()));
        } else {
            descriptionPanel.add(new Label("Contributor unknown"));
        }
        descriptionPanel.add(new Label(DateTimeFormat.getLongDateFormat().format(dataset.getDate())));
        descriptionPanel.add(new Label(TextFormatter.humanBytes(dataset.getSize())));
        descriptionPanel.add(new Label(ContentCategory.getCategory(dataset.getMimeType(), service)));
        mainPanel.add(descriptionPanel);

        Label clearLabel = new Label();
        clearLabel.addStyleName("clearFloat");
        mainPanel.add(clearLabel);
    }

    private String shortenTitle(String title) {
        if (title != null && title.length() > 10) {
            return title.substring(0, 10) + "...";
        } else {
            return title;
        }
    }

}
