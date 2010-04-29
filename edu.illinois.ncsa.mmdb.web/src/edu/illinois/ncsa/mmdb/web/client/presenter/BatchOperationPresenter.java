/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.UserSessionState;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddToCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddToCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasetResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveFromCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveFromCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;
import edu.illinois.ncsa.mmdb.web.client.mvp.View;
import edu.illinois.ncsa.mmdb.web.client.ui.AddToCollectionDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.ConfirmDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.SetLicenseDialog;
import edu.illinois.ncsa.mmdb.web.client.view.TagDialogView;

/**
 * @author LUigi Marini
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
        // delete items
        display.addMenuAction("Delete", new Command() {
            public void execute() {
                final Set<String> selectedDatasets = new HashSet<String>(sessionState.getSelectedDatasets());
                ConfirmDialog cd = new ConfirmDialog("Delete selected datasets", "Do you really want to delete " + selectedDatasets.size() + " dataset(s)?");
                cd.addConfirmHandler(new ConfirmHandler() {
                    public void onConfirm(ConfirmEvent event) {
                        for (final String dataset : selectedDatasets ) {
                            MMDB.dispatchAsync.execute(new DeleteDataset(dataset), new AsyncCallback<DeleteDatasetResult>() {
                                public void onFailure(Throwable caught) {
                                    GWT.log("Error deleting dataset", caught);
                                }

                                public void onSuccess(DeleteDatasetResult result) {
                                    // FIXME what to do?
                                    DatasetDeletedEvent dde = new DatasetDeletedEvent(dataset);
                                    MMDB.eventBus.fireEvent(dde);
                                    unselect(dataset);
                                }
                            });
                        }
                    }
                });
            }
        });
        display.addMenuAction("Change license", new Command() {
            public void execute() {
                Collection<String> batch = sessionState.getSelectedDatasets();
                new SetLicenseDialog("Set license for " + batch.size() + " dataset(s)", sessionState.getSelectedDatasets());
            }
        });
        // add tag action
        display.addMenuAction("Add tag(s)", new Command() {

            @Override
            public void execute() {
                TagDialogView tagView = new TagDialogView();
                TagDialogPresenter tagPresenter = new TagDialogPresenter(dispatch, eventBus, tagView);
                tagPresenter.bind();
                tagPresenter.setSelectedResources(sessionState.getSelectedDatasets());
                tagView.show();
            }
        });
        display.addMenuAction("Remove tag(s)", new Command() {
            @Override
            public void execute() {
                TagDialogView tagView = new TagDialogView();
                TagDialogPresenter tagPresenter = new TagDialogPresenter(dispatch, eventBus, tagView, true);
                tagPresenter.bind();
                tagPresenter.setSelectedResources(sessionState.getSelectedDatasets());
                tagView.show();
            }
        });
        display.addMenuAction("Add to collection", new Command() {
            @Override
            public void execute() {
                final AddToCollectionDialog atc = new AddToCollectionDialog(MMDB.dispatchAsync);
                atc.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        final String collectionUri = atc.getSelectedValue();
                        final Set<String> selectedDatasets = new HashSet<String>(sessionState.getSelectedDatasets());
                        MMDB.dispatchAsync.execute(new AddToCollection(collectionUri, selectedDatasets),
                                new AsyncCallback<AddToCollectionResult>() {

                            @Override
                            public void onFailure(Throwable arg0) {
                                GWT.log("Error adding dataset(s) to collection", arg0);
                            }

                            @Override
                            public void onSuccess(AddToCollectionResult arg0) {
                                GWT.log("Dataset(s) successfully added to collection", null);
                                atc.hide();
                            }
                        });
                    }
                });
            }
        });
        display.addMenuAction("Remove from collection", new Command() {
            @Override
            public void execute() {
                final AddToCollectionDialog atc = new AddToCollectionDialog(MMDB.dispatchAsync);
                atc.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        final String collectionUri = atc.getSelectedValue();
                        final Set<String> selectedDatasets = new HashSet<String>(sessionState.getSelectedDatasets());
                        MMDB.dispatchAsync.execute(new RemoveFromCollection(collectionUri, selectedDatasets),
                                new AsyncCallback<RemoveFromCollectionResult>() {

                            @Override
                            public void onFailure(Throwable arg0) {
                                GWT.log("Error adding dataset(s) to collection", arg0);
                            }

                            @Override
                            public void onSuccess(RemoveFromCollectionResult result) {
                                GWT.log("Dataset(s) successfully removed from collection", null);
                                atc.hide();
                            }
                        });
                    }
                });
            }
        });
        // unselect items
        display.addMenuAction("Unselect All", new Command() {
            @Override
            public void execute() {
                unselectAll();
            }
        });
    }

    void unselectAll() {
        UserSessionState sessionState = MMDB.getSessionState();
        Set<String> selectedDatasets = new HashSet<String>(sessionState.getSelectedDatasets());
        for (String dataset : selectedDatasets ) {
            unselect(dataset);
        }
    }

    void unselect(String dataset) {
        DatasetUnselectedEvent datasetUnselected = new DatasetUnselectedEvent();
        datasetUnselected.setUri(dataset);
        MMDB.eventBus.fireEvent(datasetUnselected);
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
