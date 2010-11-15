package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public class MintResult implements Result {
    String uri;

    public MintResult() {
    }

    public MintResult(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }
}
