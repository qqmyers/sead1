package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewCollectionEvent;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

public class PagingCollectionTableView extends PagingDcThingView<CollectionBean> {
	FlexTable table;
	
	public PagingCollectionTableView() {
		super();
		displayView();
	}

	@Override
	public void addItem(String uri, CollectionBean item) {
		int row = table.getRowCount();
		
		HorizontalPanel panel = new HorizontalPanel();
		panel.add(new Hyperlink(item.getTitle(), "collection?uri="+uri));
		if(item.getCreationDate() != null) {
			panel.add(new Label(item.getCreationDate()+""));
		}
		
		table.setWidget(row, 1, panel);
	}

	String uriForSortKey() {
		if (sortKey.startsWith("title-")) {
			return "http://purl.org/dc/elements/1.1/title";
		} else {
			return "http://purl.org/dc/terms/created";
		}
	}

	protected void displayPage() {
		// for now hardcode the page size
		int pageSize = 15;
		// now compute the current page offset
		int pageOffset = (page - 1) * pageSize;

		// now list the collections
		GetCollections query = new GetCollections();
		query.setSortKey(uriForSortKey());
		query.setDesc(descForSortKey());
		query.setOffset(pageOffset);
		query.setLimit(pageSize);

		MMDB.dispatchAsync.execute(query, new AsyncCallback<GetCollectionsResult>() {
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onSuccess(GetCollectionsResult result) {
				for (CollectionBean collection : result.getCollections()) {
					AddNewCollectionEvent event = new AddNewCollectionEvent(
							collection);
					GWT.log("Firing event add collection "
							+ collection.getTitle(), null);
					MMDB.eventBus.fireEvent(event);
				}
			}
		});
	}

	protected void displayView() {
		middlePanel.clear();
		table = new FlexTable();
		middlePanel.add(table);
	}

	public String getAction() {
		return "listCollections";
	}
}
