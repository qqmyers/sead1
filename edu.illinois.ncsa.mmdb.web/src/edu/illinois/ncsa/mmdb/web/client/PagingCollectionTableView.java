package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import edu.uiuc.ncsa.cet.bean.CollectionBean;

public class PagingCollectionTableView extends PagingDcThingView<CollectionBean> {
	FlexTable table;
	
	public PagingCollectionTableView(int page, String sortKey, String viewType) {
		super(page, sortKey, viewType);
		table = new FlexTable();
		middlePanel.add(table);
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
}
