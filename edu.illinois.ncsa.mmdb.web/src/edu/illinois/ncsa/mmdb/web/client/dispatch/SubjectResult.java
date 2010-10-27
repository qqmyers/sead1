package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

public class SubjectResult implements Result {
    String uri;

    public SubjectResult() {
    }

    public SubjectResult(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }
}
