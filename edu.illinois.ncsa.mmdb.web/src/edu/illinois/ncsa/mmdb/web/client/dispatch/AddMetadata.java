package edu.illinois.ncsa.mmdb.web.client.dispatch;

/**
 * Add the metadata attached to a resource.
 * 
 * @author Ashwini Vaidya
 * 
 */

@SuppressWarnings("serial")
public class AddMetadata extends SubjectAction<MetadataTermResult> {
    String label;
    String description;

    public AddMetadata() {
    }

    public AddMetadata(String uri, String label, String description) {
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
