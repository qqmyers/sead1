/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Show information about a specific dataset.
 * 
 * @author Luigi Marini
 *
 */
public class DatasetPresenter extends BasePresenter<DatasetPresenter.DatasetPresenterDisplay> {

	private final MyDispatchAsync dispatchAsync;

	public DatasetPresenter(DatasetPresenterDisplay display,
			HandlerManager eventBus, MyDispatchAsync dispatchAsync) {
		super(display, eventBus);
		this.dispatchAsync = dispatchAsync;
	}

	interface DatasetPresenterDisplay extends View {
		HasText getTitleText();
	}

	public void showDataset(DatasetBean dataset) {
		// TODO Auto-generated method stub
		
	}

	public void showDataset(String id) {
		
		dispatchAsync.execute(new GetDataset(id), new AsyncCallback<GetDatasetResult>() {

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("Failed getting dataset", caught);
			}

			@Override
			public void onSuccess(GetDatasetResult result) {
				DatasetBean dataset = result.getDataset();
				display.getTitleText().setText(dataset.getTitle());
			}
			
		});
	}

}
