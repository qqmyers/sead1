/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Triggered when a dataset is selected.
 * 
 * @author Luigi Marini
 *
 */
public class DatasetSelectedEvent extends GwtEvent<DatasetSelectedHandler> {

	public static final GwtEvent.Type<DatasetSelectedHandler> TYPE = new GwtEvent.Type<DatasetSelectedHandler>();

	private DatasetBean dataset = new DatasetBean();
	
	@Override
	protected void dispatch(DatasetSelectedHandler handler) {
		handler.onDatasetSelected(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<DatasetSelectedHandler> getAssociatedType() {
		return TYPE;
	}

	public void setDataset(DatasetBean dataset) {
		this.dataset = dataset;
	}

	public DatasetBean getDataset() {
		return dataset;
	}



}
