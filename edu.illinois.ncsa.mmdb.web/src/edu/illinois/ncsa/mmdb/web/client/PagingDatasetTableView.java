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
import edu.illinois.ncsa.mmdb.web.client.ui.PagingWidget;
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
	
	public String getAction() {
		return inCollection == null ? "listDatasets" : "collection";
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
		if (viewType.equals("grid")) {
			datasetTableView = new DatasetTableFlowGridView();
		} else if(viewType.equals("flow")) {
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
		} else if(inCollection != null) { // default is creation date
			return "http://purl.org/dc/terms/created";
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

		for(PagingWidget p : pagingControls) {
			p.setPage(page);
		}
		
		// we need to adjust the page size, just for flow view
		final int adjustedPageSize = (viewType.equals("flow") ? 3 : pageSize);
		
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
				int np = (result.getDatasetCount() / pageSize) + (result.getDatasetCount() % pageSize != 0 ? 1 : 0);
				setNumberOfPages(np); // this just sets the displayed number of pages in the paging controls.
				numberOfPages = np;
			}
		});
	}
}
