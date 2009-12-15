/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.DatasetTablePresenter.Display;

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
		
		GWT.log("Adding dataset " + name, null);
		
		int row = this.getRowCount();
		
		Image previewImage = new Image(PREVIEW_URL + id);
		previewImage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				History.newItem("dataset?id="+id);
			}
		});
		setWidget(row, 0, previewImage);
		/*
		if (preview != null) {
			setWidget(row, 0, new Image(BLOB_URL + preview));
		} else {
			setWidget(row, 0, new Image("images/preview-100.gif"));
		}
		*/
		
		VerticalPanel verticalPanel = new VerticalPanel();
		setWidget(row, 1, verticalPanel);
		
		Hyperlink hyperlink = new Hyperlink(name, "dataset?id=" + id);
		verticalPanel.add(hyperlink);	
		verticalPanel.add(new Label(DATE_TIME_FORMAT.format(date)));
		
		getCellFormatter().addStyleName(row, 0, "cell");
		getCellFormatter().addStyleName(row, 1, "cell");
		getCellFormatter().setVerticalAlignment(row, 1, HasVerticalAlignment.ALIGN_TOP);
		getRowFormatter().addStyleName(row, "oddRow");
	}

	@Override
	public Widget asWidget() {
		return this;
	}

}
