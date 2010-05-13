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
 * @author lmarini
 * 
 */
public class TagTablePresenter extends DynamicTablePresenter<DatasetBean> {

    private String tagName;

    public TagTablePresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display) {
        super(dispatch, eventBus, display);
        // TODO Auto-generated constructor stub
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
    protected ListQuery<DatasetBean> getQuery() {
        int offset = (currentPage - 1) * getPageSize();
        GWT.log("Getting datasets " + offset + " to " + (offset + getPageSize()) + " with tag " + tagName);
        ListQueryDatasets query = new ListQueryDatasets();
        query.setOrderBy(uriForSortKey());
        query.setDesc(descForSortKey());
        query.setLimit(getPageSize());
        query.setOffset(offset);
        query.setWithTag(tagName);
        return query;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

    @Override
    protected String getViewTypePreference() {
        return MMDB.DATASET_VIEW_TYPE_PREFERENCE;
    }

}
