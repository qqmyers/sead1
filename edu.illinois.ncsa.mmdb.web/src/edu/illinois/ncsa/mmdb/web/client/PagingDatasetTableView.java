package edu.illinois.ncsa.mmdb.web.client;

import java.util.Date;

import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class PagingDatasetTableView extends PagingDcThingView<DatasetBean> {
	DatasetTableView table;
	
	public PagingDatasetTableView(int page, String sortKey, String viewType) {
		super(page, sortKey, viewType);
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

	public void setTable(DatasetTableView table) {
		middlePanel.clear();
		middlePanel.add(table);
		this.table = table;
	}
}
