package edu.illinois.ncsa.mmdb.web.client.dispatch;

public class Section extends NamedThing {
    // "name" is section marker.
    // "uri" is the section uri
    String datasetUri;

    public String getDatasetUri() {
        return datasetUri;
    }

    public void setDatasetUri(String datasetUri) {
        this.datasetUri = datasetUri;
    }

}
