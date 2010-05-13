/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQuery;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.ShowItemEvent;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Dynamic table presenter for datasets.
 * 
 * @author Luigi Marini
 * 
 */
public class DatasetTablePresenter extends DynamicTablePresenter<DatasetBean> {

    public DatasetTablePresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display) {
        super(dispatch, eventBus, display);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see edu.illinois.ncsa.mmdb.web.client.presenter.DynamicTablePresenter#getQuery()
     */
    @Override
    protected ListQuery<DatasetBean> getQuery() {
        int offset = (currentPage - 1) * getPageSize();
        GWT.log("Getting datasets " + offset + " to " + (offset + getPageSize()));
        ListQueryDatasets query = new ListQueryDatasets();
        query.setOrderBy(uriForSortKey());
        query.setDesc(descForSortKey());
        query.setLimit(getPageSize());
        query.setOffset(offset);
        return query;
    }

    @Override
    protected void addItem(ShowItemEvent event, DatasetBean item) {
        event.setId(item.getUri());
        event.setTitle(item.getTitle());
        event.setAuthor(item.getCreator().getName());
        event.setDate(item.getDate());
        event.setSize(TextFormatter.humanBytes(item.getSize()));
        event.setType(item.getMimeType());
    }

    @Override
    protected String getViewTypePreference() {
        return MMDB.DATASET_VIEW_TYPE_PREFERENCE;
    }
}
