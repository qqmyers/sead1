package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

public class IsReadyForPublicationResult implements Result {

    /**
     *
     */
    private static final long serialVersionUID = -5834653013760307146L;

    private boolean           ready            = false;

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

}
