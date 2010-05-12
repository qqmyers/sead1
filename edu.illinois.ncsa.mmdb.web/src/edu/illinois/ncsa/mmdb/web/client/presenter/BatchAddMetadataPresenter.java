package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetProperty;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPropertyResult;
import edu.illinois.ncsa.mmdb.web.client.event.BatchCompletedEvent;

public class BatchAddMetadataPresenter extends EditableUserMetadataPresenter {
    Set<String> selectedResources = new HashSet<String>();

    public BatchAddMetadataPresenter(MyDispatchAsync dispatch, HandlerManager eventBus, Display display, Set<String> selectedResources) {
        super(dispatch, eventBus, display);
        this.selectedResources = selectedResources;
    }

    @Override
    protected void onSetMetadataField(String predicate, String value) {
        final BatchCompletedEvent done = new BatchCompletedEvent(selectedResources.size(), "modified");
        for (final String dataset : selectedResources ) {
            dispatch.execute(new SetProperty(dataset, predicate, value), new AsyncCallback<SetPropertyResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Failed modifying resource", caught);
                    editDisplay.onFailure();
                    done.setFailure(dataset, caught);
                    if (done.readyToFire()) {
                        eventBus.fireEvent(done);
                    }
                }

                @Override
                public void onSuccess(SetPropertyResult result) {
                    GWT.log("Resource successfully modified", null);
                    editDisplay.onSuccess();
                    done.addSuccess(dataset);
                    if (done.readyToFire()) {
                        eventBus.fireEvent(done);
                    }
                }
            });
        }
    }
}
