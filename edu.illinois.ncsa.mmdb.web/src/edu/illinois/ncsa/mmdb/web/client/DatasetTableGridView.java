package edu.illinois.ncsa.mmdb.web.client;

import java.util.Date;

import com.google.gwt.user.client.ui.Label;
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
	
	String shortenTitle(String title) {
		if(title.length()>15) {
			return title.substring(0,15)+"...";
		} else {
			return title;
		}
	}
	// misnomer here, when we get an "addRow" we're really adding a next
	// cell in a top-to-bottom, left-to-right traversal of the table.
	@Override
	public void addRow(String id, String title, String mimeType, Date date,
			String previewUri) {
		PreviewWidget pw = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id="+id);
		pw.setWidth("120px");
		pw.setMaxWidth(100);
		Label t = new Label(shortenTitle(title));
		t.addStyleName("smallText");
		t.setWidth("120px");
		int row = n / WIDTH;
		int col = n % WIDTH;
		setWidget(row*2, col, pw);
		setWidget((row*2)+1, col, t);
		n++;
	}

	public void doneAddingRows() {
		for(int i = n; i < getPageSize(); i++) {
			int row = i / WIDTH;
			int col = i % WIDTH;
			clearCell(row*2, col);
			clearCell((row*2)+1, col);
		}
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
