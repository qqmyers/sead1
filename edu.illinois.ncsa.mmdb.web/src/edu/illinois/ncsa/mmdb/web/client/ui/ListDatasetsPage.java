/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;

import edu.illinois.ncsa.mmdb.web.client.PagingDatasetTablePresenter;
import edu.illinois.ncsa.mmdb.web.client.PagingDatasetTableView;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.presenter.BatchOperationPresenter;
import edu.illinois.ncsa.mmdb.web.client.view.BatchOperationView;

/**
 * @author lmarini
 * 
 */
public class ListDatasetsPage extends Page {

    private final MyDispatchAsync dispatchAsync2;
    private final HandlerManager  eventbus;

    public ListDatasetsPage(MyDispatchAsync dispatchAsync, HandlerManager eventbus) {
        super("Datasets", dispatchAsync);
        dispatchAsync2 = dispatchAsync;
        this.eventbus = eventbus;

        HorizontalPanel rightHeader = new HorizontalPanel();
        pageTitle.addEast(rightHeader);

        // batch operations
        BatchOperationView batchOperationView = new BatchOperationView();
        batchOperationView.addStyleName("titlePanelRightElement");
        BatchOperationPresenter batchOperationPresenter = new BatchOperationPresenter(dispatchAsync, eventbus, batchOperationView);
        batchOperationPresenter.bind();
        rightHeader.add(batchOperationView);

        // rss feed
        Anchor rss = new Anchor();
        rss.setHref("rss.xml");
        rss.addStyleName("rssIcon");
        rss.addStyleName("titlePanelRightElement");
        DOM.setElementAttribute(rss.getElement(), "type",
                "application/rss+xml");
        rss.setHTML("<img src='./images/rss_icon.gif' border='0px' class='navMenuLink'>"); // FIXME hack
        rightHeader.add(rss);

        // paging table
        PagingDatasetTableView pagingView = new PagingDatasetTableView();
        pagingView.addStyleName("datasetTable");
        PagingDatasetTablePresenter datasetTablePresenter = new PagingDatasetTablePresenter(
                pagingView, eventbus);
        datasetTablePresenter.bind();
        mainLayoutPanel.add(pagingView.asWidget());
    }

    @Override
    public void layout() {
        // TODO Auto-generated method stub

    }

}
