/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.presenter.BatchOperationPresenter;
import edu.illinois.ncsa.mmdb.web.client.presenter.DatasetTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.view.BatchOperationView;
import edu.illinois.ncsa.mmdb.web.client.view.DynamicTableView;

/**
 * @author lmarini
 * 
 */
public class ListDatasetsPage extends Page {

    private final MyDispatchAsync dispatch;
    private final HandlerManager  eventbus;

    public ListDatasetsPage(MyDispatchAsync dispatch, HandlerManager eventBus) {
        super("Datasets", dispatch);
        this.dispatch = dispatch;
        this.eventbus = eventBus;

        HorizontalPanel rightHeader = new HorizontalPanel();
        pageTitle.addEast(rightHeader);

        // batch operations
        BatchOperationView batchOperationView = new BatchOperationView();
        batchOperationView.addStyleName("titlePanelRightElement");
        BatchOperationPresenter batchOperationPresenter = new BatchOperationPresenter(dispatch, eventBus, batchOperationView);
        batchOperationPresenter.bind();
        rightHeader.add(batchOperationView);

        // rss feed
        Anchor rss = new Anchor();
        rss.setHref("rss.xml");
        rss.addStyleName("rssIcon");
        rss.addStyleName("titlePanelRightElement");
        DOM.setElementAttribute(rss.getElement(), "type", "application/rss+xml");
        rss.setHTML("<img src='./images/rss_icon.gif' border='0px' id='rssIcon' class='navMenuLink'>"); // FIXME hack
        rightHeader.add(rss);

        // paging table
        //                PagingDatasetTableView pagingView = new PagingDatasetTableView();
        //                pagingView.addStyleName("datasetTable");
        //                PagingDatasetTablePresenter datasetTablePresenter = new PagingDatasetTablePresenter(
        //                        pagingView, eventBus);
        //                datasetTablePresenter.bind();
        //                mainLayoutPanel.add(pagingView.asWidget());

        DynamicTableView dynamicTableView = new DynamicTableView();
        final DatasetTablePresenter dynamicTablePresenter = new DatasetTablePresenter(dispatch, eventBus, dynamicTableView);
        dynamicTablePresenter.bind();

        VerticalPanel vp = new VerticalPanel() {
            @Override
            protected void onDetach() {
                dynamicTablePresenter.unbind();
            }
        };
        vp.add(dynamicTableView.asWidget());
        mainLayoutPanel.add(vp);

        dynamicTablePresenter.refresh();
    }

    @Override
    public void layout() {
        // TODO Auto-generated method stub

    }

}
