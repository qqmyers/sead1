package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

public class GetSubclassesResult implements Result {
    private List<NamedThing> subclasses;

    public void setSubclasses(List<NamedThing> subclasses) {
        this.subclasses = subclasses;
    }

    public List<NamedThing> getSubclasses() {
        return subclasses;
    }
}
