package edu.illinois.ncsa.mmdb.web.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

import edu.illinois.ncsa.mmdb.web.client.PagingCollectionTablePresenter.CollectionDisplay;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewCollectionEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddPreviewEvent;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

public class PagingCollectionTableView extends PagingDcThingView<CollectionBean> implements CollectionDisplay {
	FlexTable table;
	
	public PagingCollectionTableView() {
		super();
		displayView();
	}
 
	Map<String,Panel> badgeImages = new HashMap<String,Panel>();
	
	@Override
	public void addItem(String uri, CollectionBean item) {
		int row = table.getRowCount();
		
		HorizontalPanel panel = new HorizontalPanel();
		
		HorizontalPanel previewPanel = new HorizontalPanel();
		previewPanel.add(new Image("./images/preview-100.gif"));
		badgeImages.put(uri, previewPanel);
		panel.add(previewPanel);
		
		panel.add(new Hyperlink(item.getTitle(), "collection?uri="+uri));
		if(item.getCreationDate() != null) {
			panel.add(new Label(item.getCreationDate()+""));
		}
		
		table.setWidget(row, 1, panel);
	}

	@Override
	public void addBadge(String collectionUri, String badgeUri) {
		Panel p = badgeImages.get(collectionUri);
		if(p != null) {
			p.clear();
			p.add(new PreviewWidget(badgeUri, GetPreviews.SMALL, "collection?uri="+collectionUri));
		}
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

		badgeImages.clear();
		
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
				int i = 0;
				for(String badge : result.getBadges()) {
					if(badge != null) {
						String collectionUri = result.getCollections().get(i++).getUri();
						AddPreviewEvent event = new AddPreviewEvent(collectionUri, badge);
						GWT.log("firing add badge "+collectionUri+" badge="+badge ,null);
						MMDB.eventBus.fireEvent(event);
					}
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
