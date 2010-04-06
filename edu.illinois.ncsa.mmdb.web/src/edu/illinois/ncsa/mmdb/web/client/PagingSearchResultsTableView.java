package edu.illinois.ncsa.mmdb.web.client;

import java.util.Date;
import java.util.Map;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

import edu.illinois.ncsa.mmdb.web.client.ui.LabeledListBox;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * TODO Add comments
 * 
 * @author Joe Futrelle
 * 
 */
public class PagingSearchResultsTableView extends PagingDcThingView<DatasetBean> {
    DatasetTableView table;
    int              numberOfPages = 0;
    int              pageOffset    = 0;
    int              pageSize;
    private String   queryString;

    public PagingSearchResultsTableView() {
        super();
    }

    public PagingSearchResultsTableView(String inCollection) {
        this();
    }

    protected Map<String, String> parseHistoryToken(String token) {
        Map<String, String> params = super.parseHistoryToken(token);
        queryString = params.get("q");
        // FIXME should invalidate the view if queryString has changed, yet tolerate nulls.
        return params;
    }

    @Override
    protected HorizontalPanel createPagingPanel(int page, String sortKey,
            String viewType) {
        HorizontalPanel panel = createPagingPanel(page);
        panel.addStyleName("redBorder");
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        viewOptions = new LabeledListBox("View:");
        viewOptions.addStyleName("pagingLabel");
        viewOptions.addItem("List", "list");
        viewOptions.addItem("Grid", "grid");
        viewOptions.addItem("Flow", "flow");
        viewOptions.setSelected(viewType);
        addViewTypeControl(viewOptions);
        panel.add(viewOptions);

        addViewTypeChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                viewOptions.setSelected(event.getValue());
            }
        });
        return panel;
    }

    @Override
    public void addItem(String uri, DatasetBean dataset) {
        String title = dataset.getTitle();
        String type = dataset.getMimeType();
        Date date = dataset.getDate();
        String previewUri = "/api/image/preview/small/" + uri;
        String size = TextFormatter.humanBytes(dataset.getSize());
        String authorsId = "Anonymous";
        if (dataset.getCreator() != null) {
            authorsId = dataset.getCreator().getName();
        }
        table.addRow(uri, title, type, date, previewUri, size, authorsId);
    }

    public DatasetTableView getTable() {
        return table;
    }

    protected void displayView() {
        DatasetTableView datasetTableView = null;
        if (getViewType().equals("grid")) {
            datasetTableView = new DatasetTableGridView();
        } else if (getViewType().equals("flow")) {
            datasetTableView = new DatasetTableCoverFlowView();
        } else {
            datasetTableView = new DatasetTableOneColumnView();
        }
        setTable(datasetTableView);
    }

    public void setTable(DatasetTableView table) {
        middlePanel.clear();
        middlePanel.add(table);
        this.table = table;
    }

    String uriForSortKey() {
        if (sortKey.startsWith("title-")) {
            return "http://purl.org/dc/elements/1.1/title";
        } else {
            return "http://purl.org/dc/elements/1.1/date";
        }
    }

    protected void displayPage() {
        // if we know the number of pages, have the view reflect it
        if (numberOfPages > 0) {
            setNumberOfPages(numberOfPages);
        }
        // compute the page size using the table's preferred size
        pageSize = getTable().getPageSize();
        // now compute the current page offset
        pageOffset = (page - 1) * pageSize;

        // we need to adjust the page size, just for flow view
        final int adjustedPageSize = (getViewType().equals("flow") ? 3 : pageSize);
    }

    protected String getHistoryToken() {
        return super.getHistoryToken() + (queryString != null ? "&q=" + queryString : "");
    }

    @Override
    public String getAction() {
        return "search";
    }

    @Override
    public void addItem(String uri, DatasetBean dataset, int position) {
        String title = dataset.getTitle();
        String type = dataset.getMimeType();
        Date date = dataset.getDate();
        String previewUri = "/api/image/preview/small/" + uri;
        String size = TextFormatter.humanBytes(dataset.getSize());
        String authorsId = "Anonymous";
        if (dataset.getCreator() != null) {
            authorsId = dataset.getCreator().getName();
        }
        table.insertRow(position, uri, title, type, date, previewUri, size, authorsId);
    }
}
