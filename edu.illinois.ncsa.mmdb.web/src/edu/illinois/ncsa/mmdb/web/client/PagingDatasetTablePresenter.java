package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetHandler;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class PagingDatasetTablePresenter extends PagingTablePresenter<DatasetBean> {

    public PagingDatasetTablePresenter(Display<DatasetBean> display, HandlerManager eventBus) {
        super(display, eventBus);
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
                        if (event.getPosition() == -1) {
                            display.addItem(id, dataset);
                        } else {
                            display.addItem(id, dataset, event.getPosition());
                        }
                    }
                });
    }
}
