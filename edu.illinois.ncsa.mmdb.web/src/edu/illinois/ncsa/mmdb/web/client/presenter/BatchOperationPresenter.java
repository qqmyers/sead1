/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010 , NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.MMDB;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil;
import edu.illinois.ncsa.mmdb.web.client.PermissionUtil.PermissionCallback;
import edu.illinois.ncsa.mmdb.web.client.UserSessionState;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddToCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AddToCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.BatchResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveFromCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.RemoveFromCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetUserMetadata;
import edu.illinois.ncsa.mmdb.web.client.event.AllDatasetsUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.AllOnPageSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.BatchCompletedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.BatchCompletedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmEvent;
import edu.illinois.ncsa.mmdb.web.client.event.ConfirmHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetSelectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedEvent;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetUnselectedHandler;
import edu.illinois.ncsa.mmdb.web.client.event.DatasetsDeletedEvent;
import edu.illinois.ncsa.mmdb.web.client.mvp.BasePresenter;
import edu.illinois.ncsa.mmdb.web.client.ui.AddMetadataDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.AddToCollectionDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.ConfirmDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.DownloadDialog;
import edu.illinois.ncsa.mmdb.web.client.ui.SetLicenseDialog;
import edu.illinois.ncsa.mmdb.web.client.view.CreateCollectionDialogView;
import edu.illinois.ncsa.mmdb.web.client.view.TagDialogView;
import edu.illinois.ncsa.mmdb.web.common.Permission;

/**
 * @author LUigi Marini
 * 
 */
public class BatchOperationPresenter extends BasePresenter<BatchOperationPresenter.Display> {

    private final DispatchAsync          service;
    private static BatchCompletedHandler batchCompletedHandler;

    String title(String fmt) {
        return title(fmt, MMDB.getSessionState().getSelectedItems().size());
    }

    String title(String fmt, int n) {
        return fmt.replaceFirst("%s", n + " item" + (n > 1 ? "s" : ""));
    }

    boolean selectionEmpty() {
        if (MMDB.getSessionState().getSelectedItems().size() == 0) {
            ConfirmDialog d = new ConfirmDialog("Empty selection", "No items selected, no action will be taken.", false);
            d.getOkText().setText("OK");
            return true;
        }
        return false;
    }

    public BatchOperationPresenter(final DispatchAsync service, final HandlerManager eventBus, final Display display, boolean collections) {
        super(display, service, eventBus);
        this.service = service;
        // selected dataset
        final UserSessionState sessionState = MMDB.getSessionState();
        final int nSelected = sessionState.getSelectedItems().size();
        display.setNumSelected(nSelected);

        display.addMenuAction("Delete", new Command() {
            public void execute() {
                if (selectionEmpty()) {
                    return;
                }
                final Set<String> selectedItems = new HashSet<String>(sessionState.getSelectedItems());
                ConfirmDialog cd = new ConfirmDialog(title("Delete %s"), title("Do you really want to delete %s?"));
                cd.addConfirmHandler(new ConfirmHandler() {
                    public void onConfirm(ConfirmEvent event) {
                        final BatchCompletedEvent done = new BatchCompletedEvent(selectedItems.size(), "deleted");
                        service.execute(new DeleteDatasets(selectedItems), new AsyncCallback<BatchResult>() {
                            public void onFailure(Throwable caught) {
                                GWT.log("Error deleting items", caught);
                                for (String uri : selectedItems ) {
                                    done.setFailure(uri, "not deleted: " + caught.getMessage());
                                }
                                eventBus.fireEvent(done);
                            }

                            public void onSuccess(BatchResult result) {
                                for (String success : result.getSuccesses() ) {
                                    done.addSuccess(success);
                                    eventBus.fireEvent(new DatasetUnselectedEvent(success));
                                }
                                for (Map.Entry<String, String> failureEntry : result.getFailures().entrySet() ) {
                                    done.setFailure(failureEntry.getKey(), failureEntry.getValue());
                                }
                                eventBus.fireEvent(done);
                                eventBus.fireEvent(new DatasetsDeletedEvent(result.getSuccesses()));
                            }
                        });
                    }
                });
            }
        });

        if (!collections) {
            display.addMenuAction("Download", new Command() {
                @Override
                public void execute() {

                    final HashSet<String> selectedDatasets = new HashSet<String>(sessionState.getSelectedItems());
                    if (selectionEmpty()) {
                        return;
                    }

                    PermissionUtil rbac = new PermissionUtil(service);
                    rbac.doIfAllowed(Permission.DOWNLOAD, new PermissionCallback() {
                        @Override
                        public void onAllowed() {
                            new DownloadDialog(title("Download files"), selectedDatasets);
                        }

                        @Override
                        public void onDenied() {
                            ConfirmDialog okay = new ConfirmDialog("Error", "You do not have permission to download datasets", false);
                            okay.getOkText().setText("OK");
                        }
                    });

                }
            });
        }

        display.addMenuSeparator();

        display.addMenuAction("Add tag(s)", new Command() {

            @Override
            public void execute() {
                if (selectionEmpty()) {
                    return;
                }
                TagDialogView tagView = new TagDialogView(title("Add tag(s) to %s"));
                TagDialogPresenter tagPresenter = new TagDialogPresenter(service, eventBus, tagView);
                tagPresenter.bind();
                tagPresenter.setSelectedResources(sessionState.getSelectedItems());
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
                TagDialogPresenter tagPresenter = new TagDialogPresenter(service, eventBus, tagView, true);
                tagPresenter.bind();
                tagPresenter.setSelectedResources(sessionState.getSelectedItems());
                tagView.show();
            }
        });

        display.addMenuSeparator();

        display.addMenuAction("Add metadata", new Command() {
            @Override
            public void execute() {
                if (selectionEmpty()) {
                    return;
                }
                //BatchAddMetadataView view = new BatchAddMetadataView(title("Add metadata to %s"));
                //BatchAddMetadataPresenter presenter = new BatchAddMetadataPresenter(service, eventBus, view, sessionState.getSelectedDatasets());
                // presenter.bind();
                //view.show();
                new AddMetadataDialog(title("Add metadata to %s"), sessionState.getSelectedItems(), service, eventBus);

            }
        });

        display.addMenuAction("Add relationships", new Command() {
            @Override
            public void execute() {
                if (selectionEmpty()) {
                    return;
                }
                History.newItem("editRelationships");
            }
        });

        if (!collections) {
            display.addMenuAction("Change license", new Command() {
                public void execute() {
                    if (selectionEmpty()) {
                        return;
                    }
                    new SetLicenseDialog(title("Set license for %s"), sessionState.getSelectedItems(), service, eventBus);
                }
            });
        }

        display.addMenuSeparator();

        if (!collections) {
            display.addMenuAction("Create collection", new Command() {
                @Override
                public void execute() {
                    if (selectionEmpty()) {
                        return;
                    }
                    CreateCollectionDialogView view = new CreateCollectionDialogView(title("Create collection containing %s"));
                    CreateCollectionDialogPresenter presenter = new CreateCollectionDialogPresenter(service, eventBus, view);
                    presenter.bind();
                    presenter.setSelectedResources(sessionState.getSelectedItems());
                    view.show();
                }
            });
            display.addMenuAction("Add to collection", new Command() {
                @Override
                public void execute() {
                    if (selectionEmpty()) {
                        return;
                    }
                    final AddToCollectionDialog atc = new AddToCollectionDialog(service, title("Add %s to collection"));
                    atc.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {

                            PermissionUtil rbac = new PermissionUtil(service);
                            rbac.doIfAllowed(Permission.EDIT_COLLECTION, new PermissionCallback() {
                                @Override
                                public void onAllowed() {
                                    final String collectionUri = atc.getSelectedValue();
                                    final Set<String> selectedDatasets = new HashSet<String>(sessionState.getSelectedItems());
                                    final BatchCompletedEvent done = new BatchCompletedEvent(selectedDatasets.size(), "added");
                                    service.execute(new AddToCollection(collectionUri, selectedDatasets),
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

                                @Override
                                public void onDenied() {
                                    ConfirmDialog okay = new ConfirmDialog("Error", "You do not have permission to add datasets to a collection", false);
                                    okay.getOkText().setText("OK");
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
                    final AddToCollectionDialog atc = new AddToCollectionDialog(service, title("Remove %s from collection"));
                    atc.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {

                            PermissionUtil rbac = new PermissionUtil(service);
                            rbac.doIfAllowed(Permission.EDIT_COLLECTION, new PermissionCallback() {
                                @Override
                                public void onAllowed() {
                                    final String collectionUri = atc.getSelectedValue();
                                    final Set<String> selectedDatasets = new HashSet<String>(sessionState.getSelectedItems());
                                    final BatchCompletedEvent done = new BatchCompletedEvent(selectedDatasets.size(), "removed");
                                    service.execute(new RemoveFromCollection(collectionUri, selectedDatasets),
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

                                @Override
                                public void onDenied() {
                                    ConfirmDialog okay = new ConfirmDialog("Error", "You do not have permission to remove datasets from a collection", false);
                                    okay.getOkText().setText("OK");
                                }
                            });

                        }
                    });
                }
            });

            display.addMenuAction("Describes Collection", new Command() {
                @Override
                public void execute() {
                    if (selectionEmpty()) {
                        return;
                    }
                    final AddToCollectionDialog atc = new AddToCollectionDialog(service, title("Set %s as collection description/preview dataset(s)"));
                    atc.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent event) {

                            PermissionUtil rbac = new PermissionUtil(service);
                            rbac.doIfAllowed(Permission.EDIT_COLLECTION, new PermissionCallback() {
                                @Override
                                public void onAllowed() {
                                    final String collectionUri = atc.getSelectedValue();
                                    final Set<String> selectedDatasets = new HashSet<String>(sessionState.getSelectedItems());
<<<<<<< HEAD
                                    //DCTERMS:description is being overloaded - the user message is 'describes collection' but we will also be using this to select a collection preview image 
                                    SetUserMetadata sum = new SetUserMetadata(collectionUri, "http://purl.org/dc/terms/description", selectedDatasets, true);
=======
                                    SetUserMetadata sum = new SetUserMetadata(collectionUri, "http://cet.ncsa.uiuc.edu/2007/hasBadge", selectedDatasets, true);
>>>>>>> refs/remotes/origin/sead-1.2
                                    final BatchCompletedEvent done = new BatchCompletedEvent(selectedDatasets.size(), "set");
                                    service.execute(sum,
                                            new AsyncCallback<EmptyResult>() {

                                                @Override
                                                public void onFailure(Throwable arg0) {
                                                    GWT.log("Error setting dataset(s) as collection desciptions", arg0);
                                                    done.setFailure(selectedDatasets, arg0);
                                                    eventBus.fireEvent(done);
                                                }

                                                @Override
                                                public void onSuccess(EmptyResult arg0) {
                                                    GWT.log("Dataset(s) successfully set as collection descriptions", null);
                                                    done.addSuccesses(selectedDatasets);
                                                    eventBus.fireEvent(done);
                                                    atc.hide();
                                                }
                                            });
                                }

                                @Override
                                public void onDenied() {
                                    ConfirmDialog okay = new ConfirmDialog("Error", "You do not have permission to set datasets as collection descriptions", false);
                                    okay.getOkText().setText("OK");
                                }
                            });

                        }
                    });
                }
            });

            display.addMenuSeparator();
        }

        display.addMenuAction("Select all on page", new Command() {
            @Override
            public void execute() {
                selectAllOnPage();
            }
        });
        // unselect items
        display.addMenuAction("Unselect all", new Command() {
            @Override
            public void execute() {
                unselectAll();
            }
        });
    }

    void selectAllOnPage() {
        AllOnPageSelectedEvent select = new AllOnPageSelectedEvent();
        MMDB.eventBus.fireEvent(select);
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
                display.setNumSelected(MMDB.getSessionState().getSelectedItems().size());
            }
        });

        addHandler(DatasetUnselectedEvent.TYPE, new DatasetUnselectedHandler() {

            @Override
            public void onDatasetUnselected(DatasetUnselectedEvent event) {
                display.setNumSelected(MMDB.getSessionState().getSelectedItems().size());
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

        void addMenuSeparator();

        Widget asWidget();
    }

}
