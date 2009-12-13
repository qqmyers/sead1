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
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;

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

	@Override
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
						String previewUri = null;
						for (PreviewImageBean preview : event.getPreviews()) {
							if (preview.getWidth() == 100) {
								previewUri = preview.getUri();
							}
						}
						
						display.addRow(id, title, type, date, previewUri);
					}
				});
	}

	interface Display extends View {
		void addRow(String id, String text, String string, Date date, String previewUri);
	}

	public Widget getWidget() {
		return (Widget) this.display;
	}
}
