package edu.illinois.ncsa.mmdb.web.client.dispatch;

public class GetSection extends SubjectAction<GetSectionResult> {
    String marker;

    public GetSection() {
    }

    public GetSection(String datasetUri, String sectionMarker) {
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }
}
