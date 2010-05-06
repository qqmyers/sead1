package edu.illinois.ncsa.mmdb.web.client.ui;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;

import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;

public class DatasetSelectionCheckboxHandler implements ValueChangeHandler<Boolean> {
    private final String         datasetUri;
    private final HandlerManager eventBus;

    public DatasetSelectionCheckboxHandler(String datasetUri, HandlerManager eventBus) {
        this.datasetUri = datasetUri;
        this.eventBus = eventBus;
    }

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
        if (event.getValue()) {
            DatasetSelectedEvent datasetSelected = new DatasetSelectedEvent();
            datasetSelected.setUri(datasetUri);
            eventBus.fireEvent(datasetSelected);
        } else {
            DatasetUnselectedEvent datasetUnselected = new DatasetUnselectedEvent();
            datasetUnselected.setUri(datasetUri);
            eventBus.fireEvent(datasetUnselected);
        }
    }
}
