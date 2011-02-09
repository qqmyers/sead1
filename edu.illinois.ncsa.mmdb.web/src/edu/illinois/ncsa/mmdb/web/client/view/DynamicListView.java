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
import java.util.Set;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.presenter.DynamicListPresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.presenter.DynamicTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;

/**
 * Show contents of a {@link DynamicTablePresenter} as a list. One item per row.
 * 
 * @author Luigi Marini
 * 
 */
public class DynamicListView extends FlexTable implements Display {

    private final static DateTimeFormat DATE_TIME_FORMAT  = DateTimeFormat.getShortDateTimeFormat();
    public static final int             DEFAULT_PAGE_SIZE = 5;
    public static final int             PAGE_SIZE_X2      = 10;
    public static final int             PAGE_SIZE_X4      = 20;
    private final DispatchAsync         dispatchAsync;

    public DynamicListView(DispatchAsync dispatchAsync) {
        super();
        this.dispatchAsync = dispatchAsync;
        addStyleName("dynamicTableList");
    }

    @Override
    @Deprecated
    public int insertItem(final String id, String name, String type, Date date, String preview, String size, String authorId) {

        final int row = this.getRowCount();

        // selection checkbox
        CheckBox checkBox = new CheckBox();
        setWidget(row, 0, checkBox);

        PreviewWidget pre = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id, type, dispatchAsync);
        pre.setMaxWidth(100);
        setWidget(row, 1, pre);

        VerticalPanel verticalPanel = new VerticalPanel();

        verticalPanel.setSpacing(5);

        setWidget(row, 2, verticalPanel);

        // title
        Hyperlink hyperlink = new Hyperlink(name, "dataset?id=" + id);
        verticalPanel.add(hyperlink);

        // date
        verticalPanel.add(new Label(DATE_TIME_FORMAT.format(date)));

        // size
        verticalPanel.add(new Label(size));

        // author
        verticalPanel.add(new Label(authorId));

        // type
        verticalPanel.add(new Label(type));

        getFlexCellFormatter().addStyleName(row, 0, "dynamicTableListCheckbox");
        getFlexCellFormatter().addStyleName(row, 1, "dynamicTableListPreview");
        getFlexCellFormatter().addStyleName(row, 2, "dynamicTableListCell");

        return row;
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
        return (CheckBox) getWidget(location, 0);
    }

    @Override
    public int insertItem(String id, String type) {

        final int row = this.getRowCount();

        // selection checkbox
        CheckBox checkBox = new CheckBox();
        setWidget(row, 0, checkBox);

        PreviewWidget pre = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id, type, dispatchAsync);
        pre.setMaxWidth(100);
        setWidget(row, 1, pre);

        FlexTable informationPanel = new FlexTable();
        informationPanel.addStyleName("dynamicTableListInformation");
        informationPanel.getFlexCellFormatter().setColSpan(0, 0, 2);
        setWidget(row, 2, informationPanel);

        getFlexCellFormatter().addStyleName(row, 0, "dynamicTableListCheckbox");
        getFlexCellFormatter().addStyleName(row, 1, "dynamicTableListPreview");
        getFlexCellFormatter().addStyleName(row, 2, "dynamicTableListCell");

        return row;
    }

    @Override
    public void setTitle(int id, String title, String uri) {
        FlexTable panel = (FlexTable) getWidget(id, 2);
        HorizontalPanel anchorPanel = new HorizontalPanel();
        Hyperlink hyperlink = new Hyperlink(title, "dataset?id=" + uri);
        anchorPanel.add(hyperlink);
        anchorPanel.add(new Label("")); //FIXME hack so entire row won't be linked
        panel.setWidget(0, 0, anchorPanel);

    }

    @Override
    public void setAuthor(int row, String author) {
        FlexTable panel = (FlexTable) getWidget(row, 2);
        panel.setWidget(1, 0, new Label(author));
    }

    @Override
    public void setDate(int row, Date date) {
        FlexTable panel = (FlexTable) getWidget(row, 2);
        panel.setWidget(2, 0, new Label(DateTimeFormat.getMediumDateTimeFormat().format(date)));
    }

    @Override
    public void setSize(int row, String size) {
        FlexTable panel = (FlexTable) getWidget(row, 2);
        panel.setWidget(1, 1, new Label(size));
    }

    @Override
    public void setType(int row, String type) {
        FlexTable panel = (FlexTable) getWidget(row, 2);
        panel.setWidget(2, 1, new Label(type));
    }
}
