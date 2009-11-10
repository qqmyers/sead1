/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Triggered when a dataset is selected.
 * 
 * @author Luigi Marini
 *
 */
public interface DatasetSelectedHandler extends EventHandler {

	void onDatasetSelected(DatasetSelectedEvent event);
}
