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
import edu.illinois.ncsa.mmdb.web.client.event.ClearDatasetsEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ClearDatasetsHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.RefreshEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ShowItemEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ShowItemEventHandler;
import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;

/**
 * Show contents of a {@link DynamicTablePresenter} as a list. One item per row.
 * 
 * @author Luigi Marini
 * 
 */
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

        int insertItem(String id);

        void setTitle(int row, String title, String uri);

        void setDate(int row, Date date);

        void setAuthor(int row, String author);

        void setSize(int row, String size);

        void setType(int row, String type);

        void removeAllRows();
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
                String uri = datasetUnselectedEvent.getUri();
                if (items.containsKey(uri)) {
                    HasValue<Boolean> selected = display.getSelected(items.get(uri));
                    selected.setValue(false);
                }
            }
        });

        eventBus.addHandler(ShowItemEvent.TYPE, new ShowItemEventHandler() {

            @Override
            public void onShowItem(ShowItemEvent showItemEvent) {
                int row = addItem(showItemEvent.getId());
                display.setTitle(row, showItemEvent.getTitle(), showItemEvent.getId());
            }

        });

        eventBus.addHandler(ClearDatasetsEvent.TYPE, new ClearDatasetsHandler() {

            @Override
            public void onClearDatasets(ClearDatasetsEvent event) {
                display.removeAllRows();
                items.clear();
            }
        });

        eventBus.addHandler(DatasetDeletedEvent.TYPE, new DatasetDeletedHandler() {
            @Override
            public void onDeleteDataset(DatasetDeletedEvent event) {
                if (items.containsKey(event.getDatasetUri())) {
                    eventBus.fireEvent(new RefreshEvent());
                }
            }
        });
    }

    public int addItem(final String id) {
        int location = display.insertItem(id);
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
        return location;
    }

    @Override
    public View getView() {
        // TODO Auto-generated method stub
        return null;
    }

}
