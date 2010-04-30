package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetProperty;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetPropertyResult;

public class BatchAddMetadataPresenter extends EditableUserMetadataPresenter {
    Set<String> selectedResources = new HashSet<String>();

    public BatchAddMetadataPresenter(MyDispatchAsync dispatch, Display display, Set<String> selectedResources) {
        super(dispatch, display);
        this.selectedResources = selectedResources;
    }

    @Override
    protected void onSetMetadataField(String predicate, String value) {
        for (String dataset : selectedResources ) {
            dispatch.execute(new SetProperty(dataset, predicate, value), new AsyncCallback<SetPropertyResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Failed tagging resource", caught);
                    editDisplay.onFailure();
                }

                @Override
                public void onSuccess(SetPropertyResult result) {
                    GWT.log("Resource successfully tagged", null);
                    editDisplay.onSuccess();
                }
            });
        }
    }
}
