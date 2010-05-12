package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public class BatchResult implements Result {
    String              actionVerb = "modified";
    Set<String>         successes  = new HashSet<String>();        // uri's of sucessful ops
    Map<String, String> failures   = new HashMap<String, String>(); // uri -> message explaining why the op failed for that dataset

    public BatchResult() {
    }

    public BatchResult(String actionVerb) {
        this.actionVerb = actionVerb;
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

    public String getActionVerb() {
        return actionVerb;
    }
}
