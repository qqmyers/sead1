package edu.illinois.ncsa.mmdb.web.client;

import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetHandler;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

public class PagingSearchResultsTablePresenter extends PagingTablePresenter<DatasetBean> {

    public PagingSearchResultsTablePresenter(Display<DatasetBean> display, HandlerManager eventBus) {
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
                        int position = event.getPosition();
                        display.addItem(id, dataset, position);
                    }
                });
    }
}
