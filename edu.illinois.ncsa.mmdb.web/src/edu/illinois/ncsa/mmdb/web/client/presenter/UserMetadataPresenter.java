package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.TreeSet;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;

public class UserMetadataPresenter implements Presenter {
    protected final MyDispatchAsync dispatch;
    protected final Display         display;

    public UserMetadataPresenter(MyDispatchAsync dispatch, Display display) {
        this.dispatch = dispatch;
        this.display = display;
    }

    @Override
    public void bind() {
        dispatch.execute(new ListUserMetadataFields(),
                new AsyncCallback<ListUserMetadataFieldsResult>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(ListUserMetadataFieldsResult result) {
                TreeSet<String> sortedUris = new TreeSet<String>();
                sortedUris.addAll(result.getFieldLabels().keySet());
                for (String key : sortedUris ) {
                    String predicate = key;
                    String label = result.getFieldLabels().get(key);
                    display.addMetadataField(predicate, label);
                }
            }
        });
    }

    public interface Display {
        /**
         * Indicate to the display the name and URI of a user metadata predicate
         */
        void addMetadataField(String uri, String name);

        /**
         * Indicate to the display that a given user metadata predicate has a
         * given value (for whatever content the presenter is presenting)
         */
        void addMetadataValue(String uri, String value);
    }
}
