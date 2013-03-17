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

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.presenter.DynamicGridPresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;

/**
 * 
 * @author Luigi Marini
 * 
 */
public class DynamicGridView extends FlexTable implements Display {

    private final HashMap<Integer, CheckBox>      checkBoxes;
    private final HashMap<Integer, VerticalPanel> layouts;
    private final static DateTimeFormat           DATE_TIME_FORMAT  = DateTimeFormat.getShortDateTimeFormat();
    public static final String                    UNKNOWN_TYPE      = "Unknown";
    public static final int                       DEFAULT_PAGE_SIZE = 24;
    public static final int                       PAGE_SIZE_X2      = 48;
    public static final int                       PAGE_SIZE_X4      = 96;
    private final int                             ROW_WIDTH         = 6;
    private int                                   numItems          = 0;
    private final DispatchAsync                   dispatchAsync;

    public DynamicGridView(DispatchAsync dispatchAsync) {
        super();
        this.dispatchAsync = dispatchAsync;
        addStyleName("dynamicGrid");
        checkBoxes = new HashMap<Integer, CheckBox>();
        layouts = new HashMap<Integer, VerticalPanel>();
    }

    @Override
    public int insertItem(final String id, String title, String type) {

        final VerticalPanel layoutPanel = new VerticalPanel();
        layoutPanel.addStyleName("dynamicGridElement");
        layoutPanel.setHeight("130px");
        layoutPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        layouts.put(numItems, layoutPanel);

        HorizontalPanel titlePanel = new HorizontalPanel();
        titlePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        FlowPanel images = new FlowPanel();
        images.setStyleName("imageOverlayPanel");

        // preview
        PreviewWidget pre = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id, type, dispatchAsync);
        pre.setWidth("120px");
        pre.setMaxWidth(100);
        images.add(pre);

        //badge type overlay
        if (type != null && !UNKNOWN_TYPE.equals(type)) {
            Image overlay = new Image("images/icons/" + type + ".png");
            overlay.addStyleName("imageOverlay");
            overlay.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    History.newItem("dataset?id=" + id);
                }
            });

            images.add(overlay);
        }

        layoutPanel.add(images);

        // selection checkbox
        CheckBox checkBox = new CheckBox();
        checkBoxes.put(numItems, checkBox);
        titlePanel.add(checkBox);

        // title
        Hyperlink hyperlink = new Hyperlink(shortenTitle(title), "dataset?id=" + id);
        hyperlink.setStyleName("dataLink");
        hyperlink.setTitle(title);
        hyperlink.addStyleName("smallText");
        hyperlink.addStyleName("inline");
        hyperlink.setWidth("100px");
        titlePanel.add(hyperlink);

        layoutPanel.add(titlePanel);
        layoutPanel.setCellHeight(titlePanel, "20px");

        final int row = this.getRowCount();

        setWidget(numItems / ROW_WIDTH, numItems % ROW_WIDTH, layoutPanel);

        numItems++;

        return numItems - 1;
    }

    @Override
    @Deprecated
    public int insertItem(final String id, String name, String type, Date date, String preview, String size, String authorId) {

        final VerticalPanel layoutPanel = new VerticalPanel();
        layoutPanel.addStyleName("dynamicGridElement");
        layoutPanel.setHeight("130px");
        layoutPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        HorizontalPanel titlePanel = new HorizontalPanel();
        titlePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        // preview
        PreviewWidget pre = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id, type, dispatchAsync);
        pre.setWidth("120px");
        pre.setMaxWidth(100);
        layoutPanel.add(pre);

        // selection checkbox
        CheckBox checkBox = new CheckBox();
        checkBoxes.put(numItems, checkBox);
        titlePanel.add(checkBox);

        // title
        Hyperlink hyperlink = new Hyperlink(shortenTitle(name), "dataset?id=" + id);
        hyperlink.setStyleName("dataLink");
        hyperlink.setTitle(name);
        hyperlink.addStyleName("smallText");
        hyperlink.addStyleName("inline");
        hyperlink.setWidth("100px");
        titlePanel.add(hyperlink);

        layoutPanel.add(titlePanel);
        layoutPanel.setCellHeight(titlePanel, "20px");

        final int row = this.getRowCount();

        Timer t = new Timer() {
            public void run() {
                setWidget(numItems / ROW_WIDTH, numItems % ROW_WIDTH, layoutPanel);
            }
        };
        t.schedule(1500);
        numItems++;

        return numItems - 1;
    }

    @Override
    public void removeAllRows() {
        super.removeAllRows();
        numItems = 0;
    }

    private String shortenTitle(String title) {
        if (title != null && title.length() > 15) {
            return title.substring(0, 10) + "...";
        } else {
            return title;
        }
    }

    public String getCheckboxId(CheckBox checkbox) {
        return "";
    }

    @Override
    public Set<HasValue<Boolean>> getCheckBoxes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HasValue<Boolean> getSelected(int location) {
        GWT.log("Grid view: retrieving checkbox at location " + location);
        return checkBoxes.get(location);
    }

    @Override
    public void showSelected(boolean checked, int location) {
        if (checked) {
            layouts.get(location).addStyleName("dynamicGridSelected");
        } else {
            layouts.get(location).removeStyleName("dynamicGridSelected");
        }
    }
}
