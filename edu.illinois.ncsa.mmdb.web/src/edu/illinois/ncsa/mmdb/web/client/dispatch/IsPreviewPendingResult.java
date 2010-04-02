package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public class IsPreviewPendingResult implements Result {
    boolean isPending;
    boolean isReady;

    public boolean isPending() {
        return isPending;
    }
    public void setPending(boolean isPending) {
        this.isPending = isPending;
    }
    public boolean isReady() {
        return isReady;
    }
    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }
}
