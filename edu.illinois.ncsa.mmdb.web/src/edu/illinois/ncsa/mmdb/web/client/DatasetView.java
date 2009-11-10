/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.DatasetPresenter.DatasetPresenterDisplay;

/**
 * Show information about a specific dataset.
 * 
 * @author Luigi Marini
 */
public class DatasetView extends Composite implements DatasetPresenterDisplay {

	private FlowPanel flowPanel = new FlowPanel();
	
	public DatasetView() {
		initWidget(flowPanel);
		flowPanel.add(new Label("Dataset"));
	}
	
	@Override
	public Widget asWidget() {
		return flowPanel;
	}

}
