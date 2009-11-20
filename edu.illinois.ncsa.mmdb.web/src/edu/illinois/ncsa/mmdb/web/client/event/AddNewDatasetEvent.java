/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Triggered when a new dataset is added to the interface.
 * 
 * @author Luigi Marini
 * 
 */
public class AddNewDatasetEvent extends GwtEvent<AddNewDatasetHandler> {

	public static final GwtEvent.Type<AddNewDatasetHandler> TYPE = new GwtEvent.Type<AddNewDatasetHandler>();

	private DatasetBean dataset = new DatasetBean();
	
	@Override
	protected void dispatch(AddNewDatasetHandler handler) {
		handler.onAddNewDataset(this);
	}

	@Override
	public GwtEvent.Type<AddNewDatasetHandler> getAssociatedType() {
		return TYPE;
	}

	public void setDataset(DatasetBean dataset) {
		this.dataset = dataset;
	}

	public DatasetBean getDataset() {
		return dataset;
	}

}
