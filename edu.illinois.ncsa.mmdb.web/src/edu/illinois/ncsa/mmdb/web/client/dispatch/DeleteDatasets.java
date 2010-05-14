package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;

@SuppressWarnings("serial")
public class DeleteDatasets extends BatchAction<BatchResult> {

    public DeleteDatasets() {
        super();
    }

    public DeleteDatasets(Collection<String> resources) {
        super(resources);
    }

}
