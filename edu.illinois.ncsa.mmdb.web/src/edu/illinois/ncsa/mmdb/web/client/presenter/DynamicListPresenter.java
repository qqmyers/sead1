package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasValue;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.UserSessionState;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.ClearDatasetsEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ClearDatasetsHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.RefreshEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ShowItemEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ShowItemEventHandler;
import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.ui.DatasetSelectionCheckboxHandler;

/**
 * Show contents of a {@link DynamicTablePresenter} as a list. One item per row.
 * 
 * @author Luigi Marini
 * 
 */
public class DynamicListPresenter extends BasePresenter<DynamicListPresenter.Display> {

    private final MyDispatchAsync      dispatch;
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
        super(display, eventBus);
        this.dispatch = dispatch;
        this.items = new HashMap<String, Integer>();
    }

    @Override
    public void bind() {
        addHandler(DatasetUnselectedEvent.TYPE, new DatasetUnselectedHandler() {

            @Override
            public void onDatasetUnselected(DatasetUnselectedEvent datasetUnselectedEvent) {
                String uri = datasetUnselectedEvent.getUri();
                if (items.containsKey(uri)) {
                    HasValue<Boolean> selected = display.getSelected(items.get(uri));
                    selected.setValue(false);
                }
            }
        });

        addHandler(ShowItemEvent.TYPE, new ShowItemEventHandler() {

            @Override
            public void onShowItem(ShowItemEvent showItemEvent) {
                int row = addItem(showItemEvent.getId());
                display.setTitle(row, showItemEvent.getTitle(), showItemEvent.getId());
                if (showItemEvent.getAuthor() != null) {
                    display.setAuthor(row, showItemEvent.getAuthor());
                }
                if (showItemEvent.getDate() != null) {
                    display.setDate(row, showItemEvent.getDate());
                }
                if (showItemEvent.getType() != null) {
                    display.setType(row, showItemEvent.getType());
                }
                if (showItemEvent.getSize() != null) {
                    display.setSize(row, showItemEvent.getSize());
                }
            }

        });

        addHandler(ClearDatasetsEvent.TYPE, new ClearDatasetsHandler() {

            @Override
            public void onClearDatasets(ClearDatasetsEvent event) {
                display.removeAllRows();
                items.clear();
            }
        });

        addHandler(DatasetDeletedEvent.TYPE, new DatasetDeletedHandler() {
            @Override
            public void onDeleteDataset(DatasetDeletedEvent event) {
                if (items.containsKey(event.getDatasetUri())) {
                    eventBus.fireEvent(new RefreshEvent());
                }
            }
        });

        addHandler(DatasetDeletedEvent.TYPE, new DatasetDeletedHandler() {
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
        selected.addValueChangeHandler(new DatasetSelectionCheckboxHandler(id, MMDB.eventBus));
        UserSessionState sessionState = MMDB.getSessionState();
        if (sessionState.getSelectedDatasets().contains(id)) {
            selected.setValue(true);
        } else {
            selected.setValue(false);
        }
        return location;
    }
}
