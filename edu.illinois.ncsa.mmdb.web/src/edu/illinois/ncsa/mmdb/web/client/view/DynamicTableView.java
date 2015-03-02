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

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryResult.ListQueryItem.SectionHit;
import edu.illinois.ncsa.mmdb.web.client.presenter.DynamicTablePresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.ui.LabeledListBox;
import edu.illinois.ncsa.mmdb.web.client.ui.PagingWidget;

/**
 * A table widget to enable paging, sorting and ordering of business beans.
 *
 * @author Luigi Marini
 *
 */
public class DynamicTableView extends Composite implements Display {

    public static final String                        LIST_VIEW_TYPE = "list";
    public static final String                        GRID_VIEW_TYPE = "grid";
    public static final String                        FLOW_VIEW_TYPE = "flow";
    public static final String                        PAGE_SIZE_X1   = "default";
    public static final String                        PAGE_SIZE_X2   = "two";
    public static final String                        PAGE_SIZE_X4   = "four";
    public static final LinkedHashMap<String, String> SORTCHOICES;
    public static final LinkedHashMap<String, String> PAGE_VIEW_TYPES;
    public static final LinkedHashMap<String, String> LIST_PAGE_SIZES;
    public static final LinkedHashMap<String, String> GRID_PAGE_SIZES;

    static {
        SORTCHOICES = new LinkedHashMap<String, String>();
        SORTCHOICES.put("date-desc", "Date: newest first");
        SORTCHOICES.put("date-asc", "Date: oldest first");
        SORTCHOICES.put("title-asc", "Title: A-Z");
        SORTCHOICES.put("title-desc", "Title: Z-A");
        SORTCHOICES.put("category-asc", "Category: A-Z");
        SORTCHOICES.put("category-desc", "Category: Z-A");

        PAGE_VIEW_TYPES = new LinkedHashMap<String, String>();
        PAGE_VIEW_TYPES.put(LIST_VIEW_TYPE, "List");
        PAGE_VIEW_TYPES.put(GRID_VIEW_TYPE, "Grid");

        LIST_PAGE_SIZES = new LinkedHashMap<String, String>();
        LIST_PAGE_SIZES.put(PAGE_SIZE_X1, "5");
        LIST_PAGE_SIZES.put(PAGE_SIZE_X2, "10");
        LIST_PAGE_SIZES.put(PAGE_SIZE_X4, "15");

        GRID_PAGE_SIZES = new LinkedHashMap<String, String>();
        GRID_PAGE_SIZES.put(PAGE_SIZE_X1, "24");
        GRID_PAGE_SIZES.put(PAGE_SIZE_X2, "48");
        GRID_PAGE_SIZES.put(PAGE_SIZE_X4, "96");
    }

    private final FlowPanel                           mainPanel;
    private final HorizontalPanel                     topPagingPanel;
    private final VerticalPanel                       middlePanel;
    private final HorizontalPanel                     bottomPagingPanel;
    private final PagingWidget                        pagingWidgetTop;
    private final PagingWidget                        pagingWidgetBottom;
    private final LabeledListBox                      sortOptionsTop;
    private final LabeledListBox                      viewOptionsTop;
    private final LabeledListBox                      sizeOptionsTop;
    private String                                    sortKey;
    private String                                    viewType;
    private String                                    sizeType;
    private final LabeledListBox                      sortOptionsBottom;
    private final LabeledListBox                      viewOptionsBottom;
    private final LabeledListBox                      sizeOptionsBottom;

    public DynamicTableView() {
        mainPanel = new FlowPanel();
        mainPanel.addStyleName("dynamicTable");
        initWidget(mainPanel);
        topPagingPanel = new HorizontalPanel();
        topPagingPanel.addStyleName("dynamicTableHeader");
        topPagingPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        middlePanel = new VerticalPanel();
        middlePanel.addStyleName("content");
        bottomPagingPanel = new HorizontalPanel();
        bottomPagingPanel.addStyleName("dynamicTableHeader");
        bottomPagingPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        mainPanel.add(topPagingPanel);
        mainPanel.add(middlePanel);
        mainPanel.add(bottomPagingPanel);
        pagingWidgetTop = new PagingWidget();
        topPagingPanel.add(pagingWidgetTop);
        topPagingPanel.setCellWidth(pagingWidgetTop, "50%");
        sortOptionsTop = createSortOptions();
        topPagingPanel.add(sortOptionsTop);
        viewOptionsTop = createViewOptions();
        topPagingPanel.add(viewOptionsTop);
        sizeOptionsTop = createSizeOptions();
        topPagingPanel.add(sizeOptionsTop);
        pagingWidgetBottom = new PagingWidget();
        bottomPagingPanel.add(pagingWidgetBottom);
        bottomPagingPanel.setCellWidth(pagingWidgetBottom, "50%");
        sortOptionsBottom = createSortOptions();
        bottomPagingPanel.add(sortOptionsBottom);
        viewOptionsBottom = createViewOptions();
        bottomPagingPanel.add(viewOptionsBottom);
        sizeOptionsBottom = createSizeOptions();
        bottomPagingPanel.add(sizeOptionsBottom);
    }

    private LabeledListBox createSortOptions() {
        LabeledListBox sortOptions = new LabeledListBox("Sort by: ");
        sortOptions.addStyleName("pagingLabel");
        for (Map.Entry<String, String> entry : DynamicTableView.SORTCHOICES.entrySet() ) {
            sortOptions.addItem(entry.getValue(), entry.getKey());
        }
        sortOptions.setSelected(sortKey);
        return sortOptions;
    }

    private LabeledListBox createViewOptions() {
        LabeledListBox viewOptions = new LabeledListBox("View:");
        viewOptions.addStyleName("pagingLabel");
        for (Map.Entry<String, String> entry : DynamicTableView.PAGE_VIEW_TYPES.entrySet() ) {
            viewOptions.addItem(entry.getValue(), entry.getKey());
        }
        viewOptions.setSelected(viewType);
        return viewOptions;
    }

    private LabeledListBox createSizeOptions() {
        LabeledListBox sizeOptions = new LabeledListBox("Page Size:");
        sizeOptions.addStyleName("pagingLabel");
        for (Map.Entry<String, String> entry : DynamicTableView.LIST_PAGE_SIZES.entrySet() ) {
            sizeOptions.addItem(entry.getValue(), entry.getKey());
        }
        sizeOptions.setSelected(sizeType);
        return sizeOptions;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setCurrentPage(int num) {
        pagingWidgetTop.setPage(num);
        pagingWidgetBottom.setPage(num);
    }

    @Override
    public void setTotalNumPages(int num) {
        pagingWidgetTop.setNumberOfPages(num);
        pagingWidgetBottom.setNumberOfPages(num);
    }

    @Override
    public void addItem(String id, int position) {
        //        middlePanel.insert(new Label(id), position);
    }

    @Override
    public void removeAllRows() {
        //        middlePanel.clear();
    }

    @Override
    public void changeGridSizeNumbers() {

        sizeOptionsTop.clear();
        sizeOptionsBottom.clear();
        for (Map.Entry<String, String> entry : DynamicTableView.GRID_PAGE_SIZES.entrySet() ) {
            sizeOptionsTop.addItem(entry.getValue(), entry.getKey());
            sizeOptionsBottom.addItem(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public void changeListSizeNumbers() {

        sizeOptionsTop.clear();
        sizeOptionsBottom.clear();

        for (Map.Entry<String, String> entry : DynamicTableView.LIST_PAGE_SIZES.entrySet() ) {
            sizeOptionsTop.addItem(entry.getValue(), entry.getKey());
            sizeOptionsBottom.addItem(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public Set<HasValueChangeHandlers<String>> getSortListBox() {
        Set<HasValueChangeHandlers<String>> set = new HashSet<HasValueChangeHandlers<String>>();
        set.add(sortOptionsTop);
        set.add(sortOptionsBottom);
        return set;
    }

    @Override
    public Set<HasValueChangeHandlers<String>> getViewListBox() {
        Set<HasValueChangeHandlers<String>> set = new HashSet<HasValueChangeHandlers<String>>();
        set.add(viewOptionsTop);
        set.add(viewOptionsBottom);
        return set;
    }

    @Override
    public Set<HasValueChangeHandlers<String>> getSizeListBox() {
        Set<HasValueChangeHandlers<String>> set = new HashSet<HasValueChangeHandlers<String>>();
        set.add(sizeOptionsTop);
        set.add(sizeOptionsBottom);
        return set;
    }

    @Override
    public Set<HasValueChangeHandlers<Integer>> getPagingWidget() {
        Set<HasValueChangeHandlers<Integer>> set = new HashSet<HasValueChangeHandlers<Integer>>();
        set.add(pagingWidgetTop);
        set.add(pagingWidgetBottom);
        return set;
    }

    @Override
    public void setPage(Integer value) {
        pagingWidgetTop.setPage(value, false);
        pagingWidgetBottom.setPage(value, false);
    }

    @Override
    public void setOrder(String order) {
        sortOptionsTop.setSelected(order);
        sortOptionsBottom.setSelected(order);
    }

    @Override
    public void setContentView(Widget contentView) {
        middlePanel.clear();
        middlePanel.add(contentView);

    }

    @Override
    public void setViewType(String viewType) {
        viewOptionsTop.setSelected(viewType);
        viewOptionsBottom.setSelected(viewType);
    }

    @Override
    public void setSizeType(String sizeType) {
        sizeOptionsTop.setSelected(sizeType);
        sizeOptionsBottom.setSelected(sizeType);
    }

    //Compare alphabetically by sectionLabel and then numerically by sectionMarker
    protected static class SectionHitComparator implements Comparator<SectionHit> {

        @Override
        public int compare(SectionHit sh1, SectionHit sh2) {
            int result = sh1.getSectionLabel().compareTo(sh2.getSectionLabel());
            if (result == 0) {
                try {
                    int mark1 = Integer.parseInt(sh1.getSectionMarker());
                    int mark2 = Integer.parseInt(sh2.getSectionMarker());
                    if (mark1 < mark2) {
                        result = -1;
                    } else if (mark1 == mark2) {
                        result = 0;
                    } else {
                        result = 1;
                    }
                } catch (NumberFormatException nfe) {
                    result = sh1.getSectionMarker().compareTo(sh2.getSectionMarker());
                }
            }
            return result;
        }

    }
}
