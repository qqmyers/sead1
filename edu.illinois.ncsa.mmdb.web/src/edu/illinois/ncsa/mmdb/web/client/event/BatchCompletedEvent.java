package edu.illinois.ncsa.mmdb.web.client.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.shared.GwtEvent;

public class BatchCompletedEvent extends GwtEvent<BatchCompletedHandler> {
    public static final Type<BatchCompletedHandler> TYPE       = new Type<BatchCompletedHandler>();

    String                                          actionVerb = "modified";
    Set<String>                                     successes  = new HashSet<String>();            // uri's of sucessful ops
    Map<String, String>                             failures   = new HashMap<String, String>();    // uri -> message explaining why the op failed for that dataset

    int                                             readyAt;

    public BatchCompletedEvent() {
    }

    public BatchCompletedEvent(int batchSize, String actionVerb) {
        readyAt = batchSize;
        this.actionVerb = actionVerb;
    }

    @Override
    protected void dispatch(BatchCompletedHandler handler) {
        handler.onBatchCompleted(this);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<BatchCompletedHandler> getAssociatedType() {
        return TYPE;
    }

    public void addSuccess(String uri) {
        successes.add(uri);
    }

    public void addSuccesses(Collection<String> batch) {
        successes.addAll(batch);
    }

    public Set<String> getSuccesses() {
        return successes;
    }

    public void setFailure(String uri, String message) {
        failures.put(uri, message);
    }

    public void setFailure(Collection<String> batch, String message) {
        for (String uri : batch ) {
            setFailure(uri, message);
        }
    }

    /** put a default message in based on the given exception */
    public void setFailure(String uri, Throwable exception) {
        failures.put(uri, "failed: " + exception.getMessage());
    }

    public void setFailure(Collection<String> batch, Throwable exception) {
        for (String uri : batch ) {
            setFailure(uri, exception);
        }
    }

    public String getFailure(String uri) {
        return failures.get(uri);
    }

    public Map<String, String> getFailures() {
        return failures;
    }

    public boolean readyToFire() {
        return successes.size() + failures.size() == readyAt;
    }

    public String getActionVerb() {
        return actionVerb;
    }
}
