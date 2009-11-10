/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Triggered when a new dataset is added to the interface.
 * 
 * @author Luigi Marini
 *
 */
public interface AddNewDatasetHandler extends EventHandler {

	void onAddNewDataset(AddNewDatasetEvent event);
}
