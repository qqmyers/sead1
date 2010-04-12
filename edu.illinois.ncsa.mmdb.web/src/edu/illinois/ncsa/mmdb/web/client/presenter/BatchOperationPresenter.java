/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.UserSessionState;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;
import edu.illinois.ncsa.mmdb.web.client.view.TagDialogView;

/**
 * @author lmarini
 * 
 */
public class BatchOperationPresenter implements Presenter {

    private final HandlerManager  eventBus;
    private final MyDispatchAsync dispatch;
    private final Display         display;

    public BatchOperationPresenter(final MyDispatchAsync dispatch, final HandlerManager eventBus, final Display display) {
        this.dispatch = dispatch;
        this.eventBus = eventBus;
        this.display = display;
        // selected dataset
        final UserSessionState sessionState = MMDB.getSessionState();
        display.setNumSelected(sessionState.getSelectedDatasets().size());
        // add tag action
        display.addMenuAction("Tag", new Command() {

            @Override
            public void execute() {
                TagDialogView tagView = new TagDialogView();
                TagDialogPresenter tagPresenter = new TagDialogPresenter(dispatch, eventBus, tagView);
                tagPresenter.bind();
                tagPresenter.setSelectedResources(sessionState.getSelectedDatasets());
                tagView.show();
            }
        });
        // unselect items
        display.addMenuAction("Unselect All", new Command() {

            @Override
            public void execute() {
                Set<String> selectedDatasets = new HashSet<String>(sessionState.getSelectedDatasets());
                for (String dataset : selectedDatasets ) {
                    DatasetUnselectedEvent datasetUnselected = new DatasetUnselectedEvent();
                    datasetUnselected.setUri(dataset);
                    MMDB.eventBus.fireEvent(datasetUnselected);
                }
            }
        });

    }

    @Override
    public void bind() {

        eventBus.addHandler(DatasetSelectedEvent.TYPE, new DatasetSelectedHandler() {

            @Override
            public void onDatasetSelected(DatasetSelectedEvent event) {
                UserSessionState sessionState = MMDB.getSessionState();
                sessionState.datasetSelected(event.getUri());
                display.setNumSelected(sessionState.getSelectedDatasets().size());
            }
        });

        eventBus.addHandler(DatasetUnselectedEvent.TYPE, new DatasetUnselectedHandler() {

            @Override
            public void onDatasetUnselected(DatasetUnselectedEvent event) {
                UserSessionState sessionState = MMDB.getSessionState();
                sessionState.datasetUnselected(event.getUri());
                display.setNumSelected(sessionState.getSelectedDatasets().size());
            }
        });
    }

    @Override
    public View getView() {
        // TODO Auto-generated method stub
        return null;
    }

    public interface Display {
        void setNumSelected(int num);

        void addMenuAction(String name, Command command);

        Widget asWidget();
    }

}
