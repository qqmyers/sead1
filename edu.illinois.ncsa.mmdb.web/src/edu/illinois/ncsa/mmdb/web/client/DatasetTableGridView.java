package edu.illinois.ncsa.mmdb.web.client;

import java.util.Date;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedHandler;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;

public class DatasetTableGridView extends DatasetTableView {

	int n = 0;
	final int WIDTH = 5;
	
	public DatasetTableGridView() {
		super();
		addStyleName("datasetTable");
	}

	
	@Override
	public void removeAllRows() {
		super.removeAllRows();
		n = 0;
	}


	public int getPageSize() {
		return 35;
	}
	
	@Override
	public void addRow(String id, String title, String mimeType, Date date,
			String previewUri) {
		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("150px");
		vp.add(new PreviewWidget(id, GetPreviews.SMALL, "dataset?id="+id));
		Label t = new Label(title);
		t.setWidth("150px");
		t.addStyleName("smallText");
		vp.add(t);
		setWidget(n / WIDTH, n++ % WIDTH, vp);
	}

	@Override
	public Widget asWidget() {
		// TODO Auto-generated method stub
		return this;
	} 

	public void addDatasetDeletedHandler(DatasetDeletedHandler handler) {
		this.addHandler(handler, DatasetDeletedEvent.TYPE);
	}
}
