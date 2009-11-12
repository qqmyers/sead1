/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client;

import java.util.Date;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetHandler;
import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

/**
 * List datasets in repository.
 * 
 * @author Luigi Marini
 */
public class DatasetTablePresenter extends
		BasePresenter<DatasetTablePresenter.Display> {

	public DatasetTablePresenter(Display widget, HandlerManager eventBus) {
		super(widget, eventBus);
	}

	public void bind() {

		super.bind();

		eventBus.addHandler(AddNewDatasetEvent.TYPE,
				new AddNewDatasetHandler() {

					@Override
					public void onAddNewDataset(AddNewDatasetEvent event) {
						DatasetBean dataset = event.getDataset();
						String id = dataset.getUri();
						String title = dataset.getTitle();
						String type = dataset.getMimeType();
						Date date = dataset.getDate();
						display.addRow(id, title, type, date);
					}
				});
	}

	interface Display extends View {
		void addRow(String id, String text, String string, Date date);
	}

	public Widget getWidget() {
		return (Widget) this.display;
	}
}
