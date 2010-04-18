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
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasetsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicListView;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class DynamicTablePresenter implements Presenter {

    private final MyDispatchAsync dispatch;
    private final HandlerManager  eventBus;
    private final Display         display;
    private final int             pageSize    = 5;
    private String                sortKey     = "date-desc";
    private int                   numberOfPages;
    private int                   currentPage = 1;

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

        void setContentView(DynamicListView listView);
    }

    public DynamicTablePresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display) {
        this.dispatch = dispatch;
        this.eventBus = eventBus;
        this.display = display;

        DynamicListView listView = new DynamicListView();
        DynamicListPresenter listPresenter = new DynamicListPresenter(dispatch, eventBus, listView);
        listPresenter.bind();
        display.setContentView(listView);

        getContent();
    }

    /**
     * TODO This should potentially be an abstract method used to get the proper
     * content for the table.
     */
    private void getContent() {
        int offset = (currentPage - 1) * pageSize;
        GWT.log("Getting datasets " + offset + " to " + (offset + pageSize));
        ListDatasets query = new ListDatasets();
        query.setOrderBy(uriForSortKey());
        query.setDesc(descForSortKey());
        query.setLimit(pageSize);
        query.setOffset(offset);
        //        query.setInCollection(""); // FIXME specify collection
        dispatch.execute(query,
                new AsyncCallback<ListDatasetsResult>() {

                    public void onFailure(Throwable caught) {
                        GWT.log("Error retrieving datasets", caught);
                        DialogBox dialogBox = new DialogBox();
                        dialogBox.setText("Error retrieving datasets");
                        dialogBox.add(new Label(MMDB.SERVER_ERROR));
                        dialogBox.setAnimationEnabled(true);
                        dialogBox.center();
                        dialogBox.show();
                    }

                    @Override
                    public void onSuccess(ListDatasetsResult result) {
                        display.removeAllRows();
                        int index = 0;
                        for (DatasetBean dataset : result.getDatasets() ) {
                            AddNewDatasetEvent addNewDatasetEvent = new AddNewDatasetEvent();
                            addNewDatasetEvent.setDataset(dataset);
                            addNewDatasetEvent.setPosition(index);
                            eventBus.fireEvent(addNewDatasetEvent);
                            //                            display.addItem(dataset.getTitle(), index);
                            index++;
                        }
                        int np = (result.getDatasetCount() / pageSize) + (result.getDatasetCount() % pageSize != 0 ? 1 : 0);
                        setNumberOfPages(np);
                    }
                });

    }

    protected void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
        display.setTotalNumPages(numberOfPages);
    }

    private boolean descForSortKey() {
        return !sortKey.endsWith("-asc"); // default is descending
    }

    private String uriForSortKey() {
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
                }
            });
        }
    }

    @Override
    public View getView() {
        // TODO Auto-generated method stub
        return null;
    }

}
