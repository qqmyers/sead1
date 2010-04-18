package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasValue;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.UserSessionState;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AddNewDatasetHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;

public class DynamicListPresenter implements Presenter {

    private final MyDispatchAsync      dispatch;
    private final HandlerManager       eventBus;
    private final Display              display;
    /** Map from uri to location in view **/
    private final Map<String, Integer> items;

    public interface Display {
        HasValue<Boolean> getSelected(int location);

        Set<HasValue<Boolean>> getCheckBoxes();

        int insertItem(String id, String name, String type, Date date, String preview, String size, String authorId);
    }

    public DynamicListPresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display) {
        this.dispatch = dispatch;
        this.eventBus = eventBus;
        this.display = display;
        this.items = new HashMap<String, Integer>();
    }

    @Override
    public void bind() {
        eventBus.addHandler(DatasetUnselectedEvent.TYPE, new DatasetUnselectedHandler() {

            @Override
            public void onDatasetUnselected(DatasetUnselectedEvent datasetUnselectedEvent) {
                HasValue<Boolean> selected = display.getSelected(items.get(datasetUnselectedEvent.getUri()));
                selected.setValue(false);
            }
        });

        eventBus.addHandler(AddNewDatasetEvent.TYPE, new AddNewDatasetHandler() {

            @Override
            public void onAddNewDataset(AddNewDatasetEvent event) {
                addItem(event.getDataset().getUri());
            }
        });
    }

    public void addItem(final String id) {
        int location = display.insertItem(id, "", "", new Date(), "", "", "");
        items.put(id, location);
        final HasValue<Boolean> selected = display.getSelected(location);
        selected.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (selected.getValue()) {
                    DatasetSelectedEvent datasetSelected = new DatasetSelectedEvent();
                    datasetSelected.setUri(id);
                    MMDB.eventBus.fireEvent(datasetSelected);
                } else {
                    DatasetUnselectedEvent datasetUnselected = new DatasetUnselectedEvent();
                    datasetUnselected.setUri(id);
                    MMDB.eventBus.fireEvent(datasetUnselected);
                }

            }
        });
        UserSessionState sessionState = MMDB.getSessionState();
        if (sessionState.getSelectedDatasets().contains(id)) {
            selected.setValue(true);
        } else {
            selected.setValue(false);
        }
    }

    @Override
    public View getView() {
        // TODO Auto-generated method stub
        return null;
    }

}
