package edu.illinois.ncsa.mmdb.web.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.PagingCollectionTablePresenter.CollectionDisplay;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewCollectionEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddPreviewEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;
import edu.uiuc.ncsa.cet.bean.CollectionBean;

public class PagingCollectionTableView extends PagingDcThingView<CollectionBean> implements CollectionDisplay {
	FlexTable table;
	
	public PagingCollectionTableView() {
		super();
		addStyleName("datasetTable"); // gotta style ourselves like a dataset table
		displayView();
	}

	
	@Override
	protected HorizontalPanel createPagingPanel(int page, String sortKey,
			String viewType) {
		// TODO Auto-generated method stub
		HorizontalPanel p = super.createPagingPanel(page, sortKey, viewType);
		viewOptions.removeItem("flow");
		return p;
	}


	Map<String,Panel> badgeImages = new HashMap<String,Panel>();
	
	@Override
	public void addItem(final String uri, CollectionBean item) {
		HorizontalPanel previewPanel = new HorizontalPanel();
		previewPanel.add(new Image("./images/preview-100.gif"));
		previewPanel.addStyleName("centered");
		badgeImages.put(uri, previewPanel);

		if(getViewType().equals("grid")) {
			addGridItem(uri, item, previewPanel);
		} else if(getViewType().equals("flow")) {
			addFlowItem(uri, item, previewPanel);
		} else {
			addListItem(uri, item, previewPanel);
		}
	}

	void addFlowItem(String uri, CollectionBean item, Panel previewPanel) {
	}

	String shortenTitle(String title) {
		if(title.length()>15) {
			return title.substring(0,15)+"...";
		} else {
			return title;
		}
	}

	int n = 0;
	void addGridItem(String uri, CollectionBean item, Panel previewPanel) {
		previewPanel.setWidth("120px");
		Label t = new Label(shortenTitle(item.getTitle()));
		t.addStyleName("smallText");
		t.setWidth("120px");
		int row = n / 5; // width of table
		int col = n % 5;
		table.setWidget(row*2, col, previewPanel);
		table.getCellFormatter().addStyleName(row*2, col, "gridPreviewSmall");
		table.setWidget((row*2)+1, col, t);
		table.getCellFormatter().addStyleName((row*2)+1, col, "gridLabelSmall");
		n++;
	}
	
	void addListItem(final String uri, CollectionBean item, Panel previewPanel) {
		VerticalPanel infoPanel = new VerticalPanel();
		infoPanel.add(new Hyperlink(item.getTitle(), "collection?uri="+uri));
		
		if(item.getCreationDate() != null) {
			infoPanel.add(new Label(item.getCreationDate()+""));
		} else {
			infoPanel.add(new Label(""));
		}
		
		int count = item.getMemberCount();
		if(count > 0) {
			infoPanel.add(new Label(count+" item"+(count > 1 ? "s" :"")));
		}
		
		final int row = table.getRowCount();
		
		Button deleteButton = new Button("Delete");
		deleteButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				MMDB.dispatchAsync.execute(new DeleteDataset(uri), new AsyncCallback<DeleteDatasetResult>() {
					public void onFailure(Throwable caught) {
					}
					public void onSuccess(DeleteDatasetResult result) {
						table.getRowFormatter().addStyleName(row, "hidden");
						MMDB.eventBus.fireEvent(new DatasetDeletedEvent(uri));
					}
				});
			}
		});
		infoPanel.add(deleteButton);
		
		// yoinked from DatasetTableOneColumnView
		table.setWidget(row, 0, previewPanel);
		table.setWidget(row, 1, infoPanel);
		table.getCellFormatter().addStyleName(row, 0, "leftCell");
		table.getCellFormatter().addStyleName(row, 1, "rightCell");
		table.getCellFormatter().setVerticalAlignment(row, 1, HasVerticalAlignment.ALIGN_TOP);
		table.getRowFormatter().addStyleName(row, "oddRow");
	}

	@Override
	public void addBadge(String collectionUri, String badgeUri) {
		Panel p = badgeImages.get(collectionUri);
		if(p != null) {
			p.clear();
			PreviewWidget pw = new PreviewWidget(badgeUri, GetPreviews.SMALL, "collection?uri="+collectionUri);
			pw.setMaxWidth(100);
			p.add(pw);
		}
	}

	String uriForSortKey() {
		if (sortKey.startsWith("title-")) {
			return "http://purl.org/dc/elements/1.1/title";
		} else {
			return "http://purl.org/dc/terms/created";
		}
	}

	int pageSize() {
		if(getViewType().equals("grid")) {
			return 35;
		} else if(getViewType().equals("flow")) {
			return 3;
		} else {
			return 10;
		}
	}
	protected void displayPage() {
		table.removeAllRows();
		badgeImages.clear();
		
		final int pageSize = pageSize();
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
				int np = result.getCount() / pageSize + (result.getCount() % pageSize != 0 ? 1 : 0);
				setNumberOfPages(np);
				n = 0;
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
						String collectionUri = result.getCollections().get(i).getUri();
						AddPreviewEvent event = new AddPreviewEvent(collectionUri, badge);
						GWT.log("firing add badge "+collectionUri+" badge="+badge ,null);
						MMDB.eventBus.fireEvent(event);
					}
					i++;
				}
			}
		});
	}

	protected void displayView() {
		middlePanel.clear();
		table = new FlexTable();
		table.addStyleName("datasetTable"); // inner table needs style too
		middlePanel.add(table);
	}

	public String getAction() {
		return "listCollections";
	}
}
