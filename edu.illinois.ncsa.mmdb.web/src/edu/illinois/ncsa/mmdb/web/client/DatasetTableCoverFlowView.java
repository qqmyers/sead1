package edu.illinois.ncsa.mmdb.web.client;

import java.util.Date;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;

public class DatasetTableCoverFlowView extends DatasetTableView {
	int n = 0;
	
	@Override
	public void removeAllRows() {
		super.removeAllRows();
		n = 0;
	}

	@Override
	public void addRow(String id, String title, String mimeType, Date date,	String previewUri) {
		VerticalPanel panel = new VerticalPanel();
		PreviewWidget preview = null;
		if(n++ == 1) {
			preview = new PreviewWidget(id, GetPreviews.LARGE, "dataset?id="+id);
		} else {
			preview = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id="+id);
		}
		panel.add(preview);
		panel.add(new Label(title));
		this.setWidget(0, n, panel);
	}

	public void doneAddingRows() { }
	
	@Override
	public int getPageSize() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Widget asWidget() {
		// TODO Auto-generated method stub
		return this;
	}

}
