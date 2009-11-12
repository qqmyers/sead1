/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * Main presenter mananges list of datasets and single datasets.
 * 
 * @author Luigi Marini
 * 
 */
public class MainPresenter extends BasePresenter<MainView> {

	private final DatasetTablePresenter tablePresenter;
	private final DatasetPresenter datasetPresenter;
	private Presenter currentPresenter;
	
	public MainPresenter(MainView display, HandlerManager eventBus, 
			DatasetTablePresenter tablePresenter,
			DatasetPresenter datasetPresenter) {
		super(display, eventBus);
		this.tablePresenter = tablePresenter;
		this.datasetPresenter = datasetPresenter;
		
		switchPresenter(tablePresenter);
	}

	interface MainViewInterface extends View {
		
	}
	
	@Override
	public void bind() {
		super.bind();
		eventBus.addHandler(DatasetSelectedEvent.TYPE, new DatasetSelectedHandler() {
			
			@Override
			public void onDatasetSelected(DatasetSelectedEvent event) {
				doShowDataset(event.getDataset());
			}
		});
	}
	
	protected void doShowDataset(DatasetBean dataset) {
		datasetPresenter.showDataset(dataset);
		switchPresenter(datasetPresenter);
	}

	private void switchPresenter(Presenter presenter) {

		if (this.currentPresenter != null) {
//			this.currentPresenter.unbind();
			display.removeContent();
		}

		this.currentPresenter = presenter;

		if (presenter != null) {
			display.addContent(presenter.getView().asWidget());
			this.currentPresenter.bind();
		}

	}

}
