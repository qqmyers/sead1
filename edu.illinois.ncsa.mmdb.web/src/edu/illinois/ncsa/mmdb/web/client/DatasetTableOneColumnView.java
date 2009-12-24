/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.DatasetTablePresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;

/**
 * List datasets in repository using a youtube-like list. A one column
 * table that makes it easier to read attributes of each element.
 * 
 * @author Luigi Marini
 */
public class DatasetTableOneColumnView extends FlexTable implements Display {

private final static DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getShortDateTimeFormat();
	
	private final ArrayList<Hyperlink> datasetLinks = new ArrayList<Hyperlink>();

	private final String BLOB_URL = "./api/image/";
	private final String PREVIEW_URL = "./api/image/preview/small/";
	
	public DatasetTableOneColumnView() {
		super();
		addStyleName("datasetTable");
	}

	@Override
	public void addRow(final String id, String name, String type, Date date, String preview) {
		
		int row = this.getRowCount();
		
		GWT.log("Adding dataset " + name + " to row " + row, null);
		
		PreviewWidget pre = new PreviewWidget(id,GetPreviews.SMALL,"dataset?id="+id); 
		setWidget(row, 0, pre);
		setWidget(row, 0, pre);
		
		VerticalPanel verticalPanel = new VerticalPanel();
		setWidget(row, 1, verticalPanel);
		
		Hyperlink hyperlink = new Hyperlink(name, "dataset?id=" + id);
		verticalPanel.add(hyperlink);	
		verticalPanel.add(new Label(DATE_TIME_FORMAT.format(date)));
		
		// FIXME debug
		Anchor zoomLink = new Anchor("zoom", GWT.getHostPageBaseURL()+"pyramid/uri="+id);
		verticalPanel.add(zoomLink);
		// FIXME end debug
		
		getCellFormatter().addStyleName(row, 0, "leftCell");
		getCellFormatter().addStyleName(row, 1, "rightCell");
		getCellFormatter().setVerticalAlignment(row, 1, HasVerticalAlignment.ALIGN_TOP);
		getRowFormatter().addStyleName(row, "oddRow");
	}

	@Override
	public Widget asWidget() {
		return this;
	}

}
