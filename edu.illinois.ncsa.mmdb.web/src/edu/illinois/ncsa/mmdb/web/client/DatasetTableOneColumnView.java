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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedHandler;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;

/**
 * List datasets in repository using a youtube-like list. A one column
 * table that makes it easier to read attributes of each element.
 * 
 * @author Luigi Marini
 */
public class DatasetTableOneColumnView extends DatasetTableView {

private final static DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getShortDateTimeFormat();
	
	private final ArrayList<Hyperlink> datasetLinks = new ArrayList<Hyperlink>();

	private final String BLOB_URL = "./api/image/";
	private final String PREVIEW_URL = "./api/image/preview/small/";
	
	public DatasetTableOneColumnView() {
		super();
		addStyleName("datasetTable");
	}

	public int getPageSize() {
		return 10;
	}
	
	@Override
	public void addRow(final String id, String name, String type, Date date, String preview) {
		
		final int row = this.getRowCount();
		
		GWT.log("Adding dataset " + name + " to row " + row, null);
		
		PreviewWidget pre = new PreviewWidget(id,GetPreviews.SMALL,"dataset?id="+id); 
		setWidget(row, 0, pre);
		
		VerticalPanel verticalPanel = new VerticalPanel();
		setWidget(row, 1, verticalPanel);
		
		Hyperlink hyperlink = new Hyperlink(name, "dataset?id=" + id);
		verticalPanel.add(hyperlink);	
		verticalPanel.add(new Label(DATE_TIME_FORMAT.format(date)));
		
		Button deleteButton = new Button("Delete");
		deleteButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				MMDB.dispatchAsync.execute(new DeleteDataset(id), new AsyncCallback<DeleteDatasetResult>() {
					public void onFailure(Throwable caught) {
					}
					public void onSuccess(DeleteDatasetResult result) {
						getRowFormatter().addStyleName(row, "hidden");
						MMDB.eventBus.fireEvent(new DatasetDeletedEvent(id));
					}
				});
			}
		});
		verticalPanel.add(deleteButton);
		
		// FIXME debug
		/*
		Anchor zoomLink = new Anchor("zoom", GWT.getHostPageBaseURL()+"pyramid/uri="+id);
		verticalPanel.add(zoomLink);
		*/
		// FIXME end debug
		
		getCellFormatter().addStyleName(row, 0, "leftCell");
		getCellFormatter().addStyleName(row, 1, "rightCell");
		getCellFormatter().setVerticalAlignment(row, 1, HasVerticalAlignment.ALIGN_TOP); // FIXME move to CSS
		getRowFormatter().addStyleName(row, "oddRow");
	}

	public void doneAddingRows() { }
	
	@Override
	public Widget asWidget() {
		return this;
	}

	public void addDatasetDeletedHandler(DatasetDeletedHandler handler) {
		this.addHandler(handler, DatasetDeletedEvent.TYPE);
	}
}
