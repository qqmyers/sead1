package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.Set;

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
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
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
public abstract class DynamicTablePresenter<B> extends BasePresenter<DynamicTablePresenter.Display> {

    protected final MyDispatchAsync dispatch;
    private int                     pageSize    = DynamicListView.DEFAULT_PAGE_SIZE;
    protected String                sortKey     = "date-desc";
    protected String                viewTypePreference;
    protected String                viewType    = DynamicTableView.LIST_VIEW_TYPE;
    protected int                   numberOfPages;
    protected int                   currentPage = 1;
    protected BasePresenter<?>      viewTypePresenter;

    public interface Display {
        void setCurrentPage(int num);

        void setTotalNumPages(int num);

        void addItem(String id, int position);

        Set<HasValueChangeHandlers<Integer>> getPagingWidget();

        Set<HasValueChangeHandlers<String>> getSortListBox();

        Set<HasValueChangeHandlers<String>> getViewListBox();

        Widget asWidget();

        void removeAllRows();

        void setPage(Integer value);

        void setOrder(String order);

        void setContentView(Widget contentView);

        void setViewType(String viewType);
    }

    /**
     * Setup a presenter and view for the inner table visualization.
     * 
     * @param dispatch
     * @param eventBus
     * @param display
     */
    public DynamicTablePresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display) {
        super(display, eventBus);
        this.dispatch = dispatch;

        changeViewType(MMDB.getSessionPreference(getViewTypePreference(), DynamicTableView.LIST_VIEW_TYPE));

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
        ListQuery<B> query = getQuery();
        dispatch.execute(query,
                new AsyncCallback<ListQueryResult<B>>() {

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
                    public void onSuccess(final ListQueryResult<B> result) {
                        eventBus.fireEvent(new ClearDatasetsEvent());
                        int index = 0;
                        for (B item : result.getResults() ) {
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
    protected abstract ListQuery<B> getQuery();

    /**
     * Add item to interface. Implement based on the specific type of item.
     * 
     * @param event
     *            event to fire to add item to interface
     * @param index
     *            item retrieved from server side
     */
    protected abstract void addItem(ShowItemEvent event, B item);

    /**
     * Set the total number of pages.
     * 
     * @param numberOfPages
     */
    protected void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
        display.setTotalNumPages(numberOfPages);
    }

    /**
     * Set sorting descending.
     * 
     * @return true is sorting is descending
     */
    protected boolean descForSortKey() {
        return !sortKey.endsWith("-asc"); // default is descending
    }

    /**
     * URI used for the sort key.
     * 
     * @return uri for the sort key
     */
    protected String uriForSortKey() {
        if (sortKey.startsWith("title-")) {
            return "http://purl.org/dc/elements/1.1/title";
        } else {
            return "http://purl.org/dc/elements/1.1/date";
        }
    }

    @Override
    public void bind() {
        for (HasValueChangeHandlers<Integer> handler : display.getPagingWidget() ) {
            handler.addValueChangeHandler(new ValueChangeHandler<Integer>() {

                @Override
                public void onValueChange(ValueChangeEvent<Integer> event) {
                    GWT.log("Paging changed " + event.getValue());
                    display.setPage(event.getValue());
                    currentPage = event.getValue();
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
                    getContent();
                }
            });
        }

        for (HasValueChangeHandlers<String> handler : display.getViewListBox() ) {
            handler.addValueChangeHandler(new ValueChangeHandler<String>() {

                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    GWT.log("View list box clicked " + event.getValue());
                    changeViewType(event.getValue());
                    getContent();
                }
            });
        }
        getContent();
    }

    /**
     * 
     * @param viewType
     */
    protected void changeViewType(String viewType) {
        this.viewType = viewType;
        display.setViewType(viewType);
        MMDB.setSessionPreference(getViewTypePreference(), viewType);
        // unbind the existing presenter if any
        if (viewTypePresenter != null) {
            viewTypePresenter.unbind();
        }
        if (viewType.equals(DynamicTableView.LIST_VIEW_TYPE)) {
            DynamicListView listView = new DynamicListView();
            setPageSize(DynamicListView.DEFAULT_PAGE_SIZE);
            DynamicListPresenter listPresenter = new DynamicListPresenter(dispatch, eventBus, listView);
            listPresenter.bind();
            viewTypePresenter = listPresenter;
            display.setContentView(listView);
        } else if (viewType.equals(DynamicTableView.GRID_VIEW_TYPE)) {
            DynamicGridView gridView = new DynamicGridView();
            setPageSize(DynamicGridView.DEFAULT_PAGE_SIZE);
            DynamicGridPresenter gridPresenter = new DynamicGridPresenter(dispatch, eventBus, gridView);
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
    protected abstract String getViewTypePreference();
}
