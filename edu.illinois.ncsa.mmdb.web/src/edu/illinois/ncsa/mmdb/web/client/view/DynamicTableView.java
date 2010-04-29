package edu.illinois.ncsa.mmdb.web.client.view;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

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

    public static final String    LIST_VIEW_TYPE = "list";
    public static final String    GRID_VIEW_TYPE = "grid";
    public static final String    FLOW_VIEW_TYPE = "flow";
    private final FlowPanel       mainPanel;
    private final HorizontalPanel topPagingPanel;
    private final VerticalPanel   middlePanel;
    private final HorizontalPanel bottomPagingPanel;
    private final PagingWidget    pagingWidgetTop;
    private final PagingWidget    pagingWidgetBottom;
    private final LabeledListBox  sortOptionsTop;
    private final LabeledListBox  viewOptionsTop;
    private String                sortKey;
    private String                viewType;
    private final LabeledListBox  sortOptionsBottom;
    private final LabeledListBox  viewOptionsBottom;

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
        pagingWidgetBottom = new PagingWidget();
        bottomPagingPanel.add(pagingWidgetBottom);
        bottomPagingPanel.setCellWidth(pagingWidgetBottom, "50%");
        sortOptionsBottom = createSortOptions();
        bottomPagingPanel.add(sortOptionsBottom);
        viewOptionsBottom = createViewOptions();
        bottomPagingPanel.add(viewOptionsBottom);
    }

    private LabeledListBox createSortOptions() {
        LabeledListBox sortOptions = new LabeledListBox("Sort by: ");
        sortOptions.addStyleName("pagingLabel");
        sortOptions.addItem("Date: newest first", "date-desc");
        sortOptions.addItem("Date: oldest first", "date-asc");
        sortOptions.addItem("Title: A-Z", "title-asc");
        sortOptions.addItem("Title: Z-A", "title-desc");
        sortOptions.setSelected(sortKey);
        return sortOptions;
    }

    private LabeledListBox createViewOptions() {
        LabeledListBox viewOptions = new LabeledListBox("View:");
        viewOptions.addStyleName("pagingLabel");
        viewOptions.addItem("List", LIST_VIEW_TYPE);
        viewOptions.addItem("Grid", GRID_VIEW_TYPE);
        viewOptions.addItem("Flow", FLOW_VIEW_TYPE);
        viewOptions.setSelected(viewType);
        return viewOptions;
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
}
