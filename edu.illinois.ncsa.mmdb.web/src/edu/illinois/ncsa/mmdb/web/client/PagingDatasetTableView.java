package edu.illinois.ncsa.mmdb.web.client;

import java.util.Date;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListDatasetsResult;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class PagingDatasetTableView extends PagingDcThingView<DatasetBean> {
	DatasetTableView table;
	String inCollection;
	int numberOfPages = 0;
	int pageOffset = 0;
	int pageSize;
	
	public PagingDatasetTableView() {
		super();
	}
	
	public PagingDatasetTableView(String inCollection) {
		this();
		setInCollection(inCollection);
	}
	
	public String getInCollection() {
		return inCollection;
	}

	public void setInCollection(String inCollection) {
		this.inCollection = inCollection;
	}

	public String getAction() {
		return inCollection == null ? "listDatasets" : "collection";
	}
	
	protected String getHistoryToken() {
		// TODO Auto-generated method stub
		return super.getHistoryToken() + (inCollection != null ? "&uri=" + inCollection : "");
	}

	protected Map<String,String> parseHistoryToken(String token) {
		Map<String,String> params = super.parseHistoryToken(token);
		String newInCollection = params.get("uri");
		inCollection = newInCollection;
		// FIXME should invalidate the view if inCollection has changed, yet tolerate nulls.
		return params;
	}
	
	@Override
	public void addItem(String uri, DatasetBean dataset) {
		String title = dataset.getTitle();
		String type = dataset.getMimeType();
		Date date = dataset.getDate();
		String previewUri = "/api/image/preview/small/"+uri;
		table.addRow(uri, title, type, date, previewUri);
	}

	public DatasetTableView getTable() {
		return table;
	}

	protected void displayView() {
		DatasetTableView datasetTableView = null;
		if (getViewType().equals("grid")) {
			datasetTableView = new DatasetTableGridView();
		} else if(getViewType().equals("flow")) {
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
		
		ListDatasets query = new ListDatasets();
		query.setOrderBy(uriForSortKey());
		query.setDesc(descForSortKey());
		query.setLimit(adjustedPageSize);
		query.setOffset(pageOffset);
		query.setInCollection(inCollection);
		MMDB.dispatchAsync.execute(query,
				new AsyncCallback<ListDatasetsResult>() {

			public void onFailure(Throwable caught) {
				GWT.log("Error retrieving datasets", null);
				DialogBox dialogBox = new DialogBox();
				dialogBox.setText("Oops");
				dialogBox.add(new Label(MMDB.SERVER_ERROR));
				dialogBox.setAnimationEnabled(true);
				dialogBox.center();
				dialogBox.show();
			}
			
			@Override
			public void onSuccess(ListDatasetsResult result) {
				table.removeAllRows();
				for (DatasetBean dataset : result.getDatasets()) {
					GWT.log("Sending event add dataset " + dataset.getTitle(), null);
					AddNewDatasetEvent event = new AddNewDatasetEvent();
					event.setDataset(dataset);
					MMDB.eventBus.fireEvent(event);
				}
				table.doneAddingRows(); // FIXME here we're using MVP to deliver add rows but not to stop adding rows. encapsulation issues ...
				int np = (result.getDatasetCount() / pageSize) + (result.getDatasetCount() % pageSize != 0 ? 1 : 0);
				setNumberOfPages(np); // this just sets the displayed number of pages in the paging controls.
				numberOfPages = np;
			}
		});
	}
}
