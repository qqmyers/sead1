/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;

/**
 * Show information about a specific dataset.
 * 
 * @author Luigi Marini
 *
 */
public class DatasetPresenter extends BasePresenter<DatasetPresenter.DatasetPresenterDisplay> {

	public DatasetPresenter(DatasetPresenterDisplay display,
			HandlerManager eventBus) {
		super(display, eventBus);
	}

	interface DatasetPresenterDisplay extends View {
		
	}

}
