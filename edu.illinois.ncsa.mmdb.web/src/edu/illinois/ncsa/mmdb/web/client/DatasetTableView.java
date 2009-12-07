/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.DatasetTablePresenter.Display;

/**
 * List datasets in repository.
 * 
 * @author Luigi Marini
 */
public class DatasetTableView extends FlexTable implements Display {

	private final static DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getShortDateTimeFormat();
	
	private ArrayList<Hyperlink> datasetLinks = new ArrayList<Hyperlink>();
	
	public DatasetTableView() {
		super();
		this.setWidget(0, 0, new Label("Name"));
		this.setWidget(0, 1, new Label("Type"));
		this.setWidget(0, 2, new Label("Date"));
		addStyleName("datasetTable");
		getRowFormatter().addStyleName(0, "topRow");
	}

	@Override
	public void addRow(String id, String name, String type, Date date) {
		GWT.log("Adding dataset " + name, null);
		int row = this.getRowCount();
		Hyperlink hyperlink = new Hyperlink(name, "dataset?id=" + id);
		datasetLinks.add(hyperlink);
		this.setWidget(row, 0, hyperlink);
		
		this.setWidget(row, 1, new Label(type));
		this.setWidget(row, 2, new Label(DATE_TIME_FORMAT.format(date)));
		
		for (int col=0; col<3; col++) {
			getCellFormatter().addStyleName(row, col, "cell");
		}
		
		if (row % 2 == 0) {
			getRowFormatter().addStyleName(row, "evenRow");
		} else {
			getRowFormatter().addStyleName(row, "oddRow");
		}
	}

	@Override
	public Widget asWidget() {
		return this;
	}

}
