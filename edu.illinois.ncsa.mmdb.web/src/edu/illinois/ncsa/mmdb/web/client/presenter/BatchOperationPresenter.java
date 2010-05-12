/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.presenter;

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
import edu.illinois.ncsa.mmdb.web.client.event.AllDatasetsUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.BatchCompletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.BatchCompletedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.ui.AddToCollectionDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.ConfirmDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.SetLicenseDialog;
import edu.illinois.ncsa.mmdb.web.client.view.BatchAddMetadataView;
import edu.illinois.ncsa.mmdb.web.client.view.CreateCollectionDialogView;
import edu.illinois.ncsa.mmdb.web.client.view.TagDialogView;

/**
 * @author LUigi Marini
 * 
 */
public class BatchOperationPresenter extends BasePresenter<BatchOperationPresenter.Display> {

    private final MyDispatchAsync        dispatch;
    private static BatchCompletedHandler batchCompletedHandler;

    String title(String fmt) {
        return title(fmt, MMDB.getSessionState().getSelectedDatasets().size());
    }

    String title(String fmt, int n) {
        return fmt.replaceFirst("%s", n + " dataset" + (n > 1 ? "s" : ""));
    }

    boolean selectionEmpty() {
        if (MMDB.getSessionState().getSelectedDatasets().size() == 0) {
            ConfirmDialog d = new ConfirmDialog("Empty selection", "No datasets selected, no action will be taken.", false);
            d.getOkText().setText("OK");
            return true;
        }
        return false;
    }

    public BatchOperationPresenter(final MyDispatchAsync theDispatch, final HandlerManager eventBus, final Display display) {
        super(display, eventBus);
        this.dispatch = theDispatch;
        // selected dataset
        final UserSessionState sessionState = MMDB.getSessionState();
        final int nSelected = sessionState.getSelectedDatasets().size();
        display.setNumSelected(nSelected);
        // delete items
        display.addMenuAction("Delete", new Command() {
            public void execute() {
                if (selectionEmpty()) {
                    return;
                }
                final Set<String> selectedDatasets = new HashSet<String>(sessionState.getSelectedDatasets());
                ConfirmDialog cd = new ConfirmDialog(title("Delete %s"), title("Do you really want to delete %s?"));
                cd.addConfirmHandler(new ConfirmHandler() {
                    public void onConfirm(ConfirmEvent event) {
                        final BatchCompletedEvent done = new BatchCompletedEvent(selectedDatasets.size(), "deleted");
                        for (final String dataset : selectedDatasets ) {
                            dispatch.execute(new DeleteDataset(dataset), new AsyncCallback<DeleteDatasetResult>() {
                                public void onFailure(Throwable caught) {
                                    GWT.log("Error deleting dataset", caught);
                                    done.setFailure(dataset, "not deleted: " + caught.getMessage());
                                    if (done.readyToFire()) {
                                        eventBus.fireEvent(done);
                                    }
                                }

                                public void onSuccess(DeleteDatasetResult result) {
                                    // FIXME what to do?
                                    DatasetDeletedEvent dde = new DatasetDeletedEvent(dataset);
                                    eventBus.fireEvent(dde);
                                    done.addSuccess(dataset);
                                    if (done.readyToFire()) {
                                        eventBus.fireEvent(done);
                                    }
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
                if (selectionEmpty()) {
                    return;
                }
                new SetLicenseDialog(title("Set license for %s"), sessionState.getSelectedDatasets(), eventBus);
            }
        });
        // add tag action
        display.addMenuAction("Add tag(s)", new Command() {

            @Override
            public void execute() {
                if (selectionEmpty()) {
                    return;
                }
                TagDialogView tagView = new TagDialogView(title("Add tag(s) to %s"));
                TagDialogPresenter tagPresenter = new TagDialogPresenter(dispatch, eventBus, tagView);
                tagPresenter.bind();
                tagPresenter.setSelectedResources(sessionState.getSelectedDatasets());
                tagView.show();
            }
        });
        display.addMenuAction("Remove tag(s)", new Command() {
            @Override
            public void execute() {
                if (selectionEmpty()) {
                    return;
                }
                TagDialogView tagView = new TagDialogView(title("Remove tag(s) from %s"));
                TagDialogPresenter tagPresenter = new TagDialogPresenter(dispatch, eventBus, tagView, true);
                tagPresenter.bind();
                tagPresenter.setSelectedResources(sessionState.getSelectedDatasets());
                tagView.show();
            }
        });
        display.addMenuAction("Add metadata", new Command() {
            @Override
            public void execute() {
                if (selectionEmpty()) {
                    return;
                }
                BatchAddMetadataView view = new BatchAddMetadataView(title("Add metadata to %s"));
                BatchAddMetadataPresenter presenter = new BatchAddMetadataPresenter(dispatch, eventBus, view, sessionState.getSelectedDatasets());
                presenter.bind();
                view.show();
            }
        });
        display.addMenuAction("Create collection", new Command() {
            @Override
            public void execute() {
                if (selectionEmpty()) {
                    return;
                }
                CreateCollectionDialogView view = new CreateCollectionDialogView(title("Create collection containing %s"));
                CreateCollectionDialogPresenter presenter = new CreateCollectionDialogPresenter(dispatch, eventBus, view);
                presenter.bind();
                presenter.setSelectedResources(sessionState.getSelectedDatasets());
                view.show();
            }
        });
        display.addMenuAction("Add to collection", new Command() {
            @Override
            public void execute() {
                if (selectionEmpty()) {
                    return;
                }
                final AddToCollectionDialog atc = new AddToCollectionDialog(dispatch, title("Add %s to collection"));
                atc.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        final String collectionUri = atc.getSelectedValue();
                        final Set<String> selectedDatasets = new HashSet<String>(sessionState.getSelectedDatasets());
                        final BatchCompletedEvent done = new BatchCompletedEvent(selectedDatasets.size(), "added");
                        dispatch.execute(new AddToCollection(collectionUri, selectedDatasets),
                                new AsyncCallback<AddToCollectionResult>() {

                            @Override
                            public void onFailure(Throwable arg0) {
                                GWT.log("Error adding dataset(s) to collection", arg0);
                                done.setFailure(selectedDatasets, arg0);
                                eventBus.fireEvent(done);
                            }

                            @Override
                            public void onSuccess(AddToCollectionResult arg0) {
                                // FIXME AddToCollectionResult doesn't tell us how many were actually added
                                GWT.log("Dataset(s) successfully added to collection", null);
                                done.addSuccesses(selectedDatasets);
                                eventBus.fireEvent(done);
                                atc.hide();
                            }
                        });
                    }
                });
            }
        });
        display.addMenuAction("Remove from collection", new Command() {
            // FIXME tile
            @Override
            public void execute() {
                if (selectionEmpty()) {
                    return;
                }
                final AddToCollectionDialog atc = new AddToCollectionDialog(MMDB.dispatchAsync, title("Remove %s from collection"));
                atc.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        final String collectionUri = atc.getSelectedValue();
                        final Set<String> selectedDatasets = new HashSet<String>(sessionState.getSelectedDatasets());
                        final BatchCompletedEvent done = new BatchCompletedEvent(selectedDatasets.size(), "removed");
                        MMDB.dispatchAsync.execute(new RemoveFromCollection(collectionUri, selectedDatasets),
                                new AsyncCallback<RemoveFromCollectionResult>() {

                            @Override
                            public void onFailure(Throwable arg0) {
                                GWT.log("Error adding dataset(s) to collection", arg0);
                                done.setFailure(selectedDatasets, arg0);
                                eventBus.fireEvent(done);
                            }

                            @Override
                            public void onSuccess(RemoveFromCollectionResult result) {
                                // FIXME AddToCollectionResult doesn't tell us how many were actually removed
                                GWT.log("Dataset(s) successfully removed from collection", null);
                                done.addSuccesses(selectedDatasets);
                                atc.hide();
                                eventBus.fireEvent(done);
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
        AllDatasetsUnselectedEvent unselect = new AllDatasetsUnselectedEvent();
        MMDB.eventBus.fireEvent(unselect);
    }

    void unselect(String dataset) {
        DatasetUnselectedEvent datasetUnselected = new DatasetUnselectedEvent();
        datasetUnselected.setUri(dataset);
        MMDB.eventBus.fireEvent(datasetUnselected);
    }

    @Override
    public void bind() {

        addHandler(DatasetSelectedEvent.TYPE, new DatasetSelectedHandler() {

            @Override
            public void onDatasetSelected(DatasetSelectedEvent event) {
                display.setNumSelected(MMDB.getSessionState().getSelectedDatasets().size());
            }
        });

        addHandler(DatasetUnselectedEvent.TYPE, new DatasetUnselectedHandler() {

            @Override
            public void onDatasetUnselected(DatasetUnselectedEvent event) {
                display.setNumSelected(MMDB.getSessionState().getSelectedDatasets().size());
            }
        });

        // only add one of these handlers globally, because we want to see batch results even if
        // we have already unattached the batch operations menu
        if (batchCompletedHandler == null) {
            batchCompletedHandler = new BatchCompletedHandler() {
                @Override
                public void onBatchCompleted(BatchCompletedEvent event) {
                    String failureClause = event.getFailures().size() > 0 ? " (" + event.getFailures().size() + " errors(s))" : "";
                    String content = title("%s " + event.getActionVerb() + failureClause, event.getSuccesses().size());
                    ConfirmDialog d = new ConfirmDialog("Action complete", content, false);
                    d.getOkText().setText("OK");
                }
            };
            eventBus.addHandler(BatchCompletedEvent.TYPE, batchCompletedHandler);
        }
    }

    public interface Display {
        void setNumSelected(int num);

        void addMenuAction(String name, Command command);

        Widget asWidget();
    }

}
