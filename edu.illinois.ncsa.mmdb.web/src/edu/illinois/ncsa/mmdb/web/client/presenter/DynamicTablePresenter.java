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
package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.Set;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQuery;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryResult.ListQueryItem;
import edu.illinois.ncsa.mmdb.web.client.event.ClearDatasetsEvent;
import edu.illinois.ncsa.mmdb.web.client.event.NoMoreItemsEvent;
import edu.illinois.ncsa.mmdb.web.client.event.RefreshEvent;
import edu.illinois.ncsa.mmdb.web.client.event.RefreshHandler;
import edu.illinois.ncsa.mmdb.web.client.event.ShowItemEvent;
import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicGridView;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicListView;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicTableView;

/**
 * A table widget to allow paging, sorting and ordering of business beans.
 * 
 * @author Luigi Marini
 * 
 * @param <B>
 *            Server side bean to be shown in the table
 */
public abstract class DynamicTablePresenter extends BasePresenter<DynamicTablePresenter.Display> {

    private int                pageSize             = DynamicListView.DEFAULT_PAGE_SIZE;
    protected String           sortKey              = "date-desc";
    protected String           viewTypePreference;
    protected String           viewType             = DynamicTableView.GRID_VIEW_TYPE;
    protected String           sizeType             = DynamicTableView.PAGE_SIZE_X1;
    protected int              numberOfPages;
    protected int              currentPage          = 1;

    private static String      initialSortKey       = "date-desc";
    private static String      initialViewType      = DynamicTableView.GRID_VIEW_TYPE;
    private static String      initialSize          = DynamicTableView.PAGE_SIZE_X1;

    protected static Boolean   showTopLevelDatasets = false;
    protected BasePresenter<?> viewTypePresenter;

    public interface Display {
        void setCurrentPage(int num);

        void setTotalNumPages(int num);

        void addItem(String id, int position);

        Set<HasValueChangeHandlers<Integer>> getPagingWidget();

        Set<HasValueChangeHandlers<String>> getSortListBox();

        Set<HasValueChangeHandlers<String>> getViewListBox();

        Set<HasValueChangeHandlers<String>> getSizeListBox();

        Widget asWidget();

        void removeAllRows();

        void setPage(Integer value);

        void setOrder(String order);

        void setContentView(Widget contentView);

        void setViewType(String viewType);

        void setSizeType(String sizeType);

        void changeGridSizeNumbers();

        void changeListSizeNumbers();
    }

    static public void setInitialKeys(String sort, String view, Boolean showTopLevelData) {
        if (sort != null && !sort.isEmpty()) {
            initialSortKey = sort;
        }
        if (view != null && !view.isEmpty()) {
            initialViewType = view;
        }
        if (showTopLevelData != null) {
            showTopLevelDatasets = showTopLevelData;
        }

    }

    /**
     * Setup a presenter and view for the inner table visualization.
     * 
     * @param dispatch
     * @param eventBus
     * @param display
     */
    public DynamicTablePresenter(DispatchAsync dispatch, HandlerManager eventBus, Display display) {
        super(display, dispatch, eventBus);
        sortKey = MMDB.getSessionPreference(getViewSortPreference(), initialSortKey);
        viewType = MMDB.getSessionPreference(getViewTypePreference(), initialViewType);
        display.setOrder(sortKey);
        setViewType(viewType, MMDB.getSessionPreference(getViewSizeTypePreference(), initialSize));
        addHandler(RefreshEvent.TYPE, new RefreshHandler() {
            @Override
            public void onRefresh(RefreshEvent event) {
                refresh();
            }
        });
    }

    public void refresh() {
        getContent();
    }

    /**
     * Retrieve server side items using {@link getQuery} and adding them to
     * display using {@link addItem}.
     */
    private void getContent() {
        ListQuery query = getQuery();
        service.execute(query,
                new AsyncCallback<ListQueryResult>() {

                    public void onFailure(Throwable caught) {
                        GWT.log("Error retrieving items to show in table", caught);
                        DialogBox dialogBox = new DialogBox();
                        dialogBox.setText("Error retrieving items");
                        dialogBox.add(new Label(MMDB.SERVER_ERROR));
                        dialogBox.setAnimationEnabled(true);
                        dialogBox.center();
                        dialogBox.show();
                    }

                    @Override
                    public void onSuccess(final ListQueryResult result) {
                        eventBus.fireEvent(new ClearDatasetsEvent());
                        int index = 0;
                        for (ListQueryItem item : result.getResults() ) {
                            ShowItemEvent event = new ShowItemEvent();
                            event.setPosition(index);
                            addItem(event, item);
                            index++;
                            eventBus.fireEvent(event);
                        }
                        eventBus.fireEvent(new NoMoreItemsEvent());
                        final int np = (int) Math.ceil((double) result.getTotalCount() / getPageSize());
                        setNumberOfPages(np);
                    }
                });
    }

    /**
     * Query to retrieve items server side.
     * 
     * @return query sent to server to retrieve items
     */
    protected abstract ListQuery getQuery();

    /**
     * Add item to interface.
     * 
     * @param event
     *            event to fire to add item to interface
     * @param index
     *            item retrieved from server side
     */
    protected void addItem(ShowItemEvent event, ListQueryItem item) {
        event.setId(item.getUri());
        if (item.getTitle() != null) {
            event.setTitle(item.getTitle());
        } else {
            event.setTitle(item.getUri());
        }
        if (item.getAuthor() != null) {
            event.setAuthor(item.getAuthor());
        } else {
            event.setAuthor("Unknown Author");
        }
        event.setDate(item.getDate());
        event.setSize(item.getSize());
        event.setType(item.getCategory());

        if ("Collection".equals(item.getCategory())) {
            event.setURL("collection?uri=" + item.getUri());
        } else {
            event.setURL("dataset?id=" + item.getUri());
        }
    }

    /**
     * Set the total number of pages.
     * 
     * @param numberOfPages
     */
    protected void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
        display.setTotalNumPages(numberOfPages);
    }

    protected boolean rememberPageNumber() {
        return false;
    }

    protected String getPageKey() {
        return this.getClass().getName();
    }

    private void setPage(int page) {
        currentPage = page;
        display.setPage(page);
        if (rememberPageNumber()) {
            GWT.log("Remembering that we're on page " + page);
            MMDB.getSessionState().setPage(getPageKey(), page); // remember page number in session
        }
    }

    @Override
    public void bind() {
        for (HasValueChangeHandlers<Integer> handler : display.getPagingWidget() ) {
            handler.addValueChangeHandler(new ValueChangeHandler<Integer>() {

                @Override
                public void onValueChange(ValueChangeEvent<Integer> event) {
                    int page = event.getValue();
                    GWT.log("User changed page to " + page);
                    setPage(page);
                    getContent();
                }
            });
        }

        for (HasValueChangeHandlers<String> handler : display.getSortListBox() ) {
            handler.addValueChangeHandler(new ValueChangeHandler<String>() {

                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    GWT.log("Sort list box clicked " + event.getValue());
                    sortKey = event.getValue();
                    display.setOrder(sortKey);
                    setPage(1); // FIXME stay on same page?
                    MMDB.setSessionPreference(getViewSortPreference(), sortKey);
                    getContent();
                }
            });
        }

        for (HasValueChangeHandlers<String> handler : display.getViewListBox() ) {
            handler.addValueChangeHandler(new ValueChangeHandler<String>() {

                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    GWT.log("View list box clicked " + event.getValue());
                    changeViewType(event.getValue(), sizeType);
                    getContent();
                }
            });
        }

        for (HasValueChangeHandlers<String> handler : display.getSizeListBox() ) {
            handler.addValueChangeHandler(new ValueChangeHandler<String>() {

                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    GWT.log("View page size box clicked " + event.getValue());
                    changeViewType(viewType, event.getValue());
                    getContent();
                }
            });
        }

        if (rememberPageNumber()) {
            int rememberedPage = MMDB.getSessionState().getPage(getPageKey());
            GWT.log("Setting page to remembered page " + rememberedPage);
            setPage(rememberedPage);
        }

        getContent();
    }

    int computeNewPage(int oldPage, int oldPageSize) {
        return (((oldPage - 1) * oldPageSize) / pageSize) + 1;
    }

    /**
     * Change the view type. Adjust the page according to the current page on
     * the previous view type.
     * 
     * @param viewType
     * @param sizeType
     */
    protected void changeViewType(String viewType, String sizeType) {
        int oldPage = currentPage;
        int oldPageSize = pageSize;
        setViewType(viewType, sizeType);
        setPage(computeNewPage(oldPage, oldPageSize));
    }

    /**
     * Set the view type. Do not adjust the page according to any existing
     * currentPage value.
     * 
     * @param viewType
     */
    protected void setViewType(String viewType, String sizeType) {
        this.viewType = viewType;
        display.setViewType(viewType);
        MMDB.setSessionPreference(getViewTypePreference(), viewType);
        MMDB.setSessionPreference(getViewSizeTypePreference(), sizeType);
        // unbind the existing presenter if any
        if (viewTypePresenter != null) {
            viewTypePresenter.unbind();
        }
        if (viewType.equals(DynamicTableView.LIST_VIEW_TYPE)) {
            DynamicListView listView = new DynamicListView(service);
            display.changeListSizeNumbers();
            display.setSizeType(sizeType);
            if (sizeType.equals(DynamicTableView.PAGE_SIZE_X2)) {
                setPageSize(DynamicListView.PAGE_SIZE_X2);
            } else if (sizeType.equals(DynamicTableView.PAGE_SIZE_X4)) {
                setPageSize(DynamicListView.PAGE_SIZE_X4);
            } else {
                setPageSize(DynamicListView.DEFAULT_PAGE_SIZE);
            }
            DynamicListPresenter listPresenter = new DynamicListPresenter(service, eventBus, listView);
            listPresenter.bind();
            viewTypePresenter = listPresenter;
            display.setContentView(listView);
        } else if (viewType.equals(DynamicTableView.GRID_VIEW_TYPE)) {
            display.changeGridSizeNumbers();
            display.setSizeType(sizeType);
            DynamicGridView gridView = new DynamicGridView(service);

            if (sizeType.equals(DynamicTableView.PAGE_SIZE_X2)) {
                setPageSize(DynamicGridView.PAGE_SIZE_X2);
            } else if (sizeType.equals(DynamicTableView.PAGE_SIZE_X4)) {
                setPageSize(DynamicGridView.PAGE_SIZE_X4);
            } else {
                setPageSize(DynamicGridView.DEFAULT_PAGE_SIZE);
            }

            DynamicGridPresenter gridPresenter = new DynamicGridPresenter(service, eventBus, gridView);
            gridPresenter.bind();
            viewTypePresenter = gridPresenter;
            display.setContentView(gridView);
        }
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public void unbind() {
        super.unbind();
        if (viewTypePresenter != null) {
            viewTypePresenter.unbind();
        }
    }

    /** Override to return the view type preference key for this kind of table */
    protected String getViewTypePreference() {
        return MMDB.DATASET_VIEW_TYPE_PREFERENCE;
    }

    protected String getViewSizeTypePreference() {
        return MMDB.DATASET_VIEWSIZE_TYPE_PREFERENCE;
    }

    protected String getViewSortPreference() {
        return MMDB.DATASET_VIEW_SORT_PREFERENCE;
    }
}
