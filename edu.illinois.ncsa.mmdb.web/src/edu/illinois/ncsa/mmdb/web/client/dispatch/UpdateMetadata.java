package edu.illinois.ncsa.mmdb.web.client.dispatch;

@SuppressWarnings("serial")
public class UpdateMetadata extends SubjectAction<MetadataTermResult> {
    String label;
    String description;

    public UpdateMetadata() {
    }

    public UpdateMetadata(String uri, String label, String description) {
        super(uri);
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
