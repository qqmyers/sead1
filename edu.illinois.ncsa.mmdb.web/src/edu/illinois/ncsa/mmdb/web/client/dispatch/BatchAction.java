package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;

import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public abstract class BatchAction<T extends Result> extends AuthorizedAction<T> {
    Collection<String> resources;

    public BatchAction() {
    }

    public BatchAction(Collection<String> resources) {
        setResources(resources);
    }

    public Collection<String> getResources() {
        return resources;
    }

    public void setResources(Collection<String> resources) {
        this.resources = resources;
    }
}
