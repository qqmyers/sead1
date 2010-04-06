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
                        String previewUri = "/api/image/preview/small/" + id;
                        String size = TextFormatter.humanBytes(dataset.getSize());
                        String authorsId = dataset.getCreator().getName();
                        display.addRow(id, title, type, date, previewUri, size, authorsId);
                    }
                });
    }

    interface Display extends View {
        /** add a row to this multi-dataset view */
        void addRow(String id, String title, String mimeType, Date date, String previewUri, String size, String authorsId);

        void insertRow(int position, String id, String title, String mimeType, Date date, String previewUri, String size, String authorsId);

        /** signal that no more rows will be added on this page */
        void doneAddingRows();

        /** return the optimal page size for this view */
        int getPageSize();
    }

    public Widget getWidget() {
        return (Widget) this.display;
    }
}
