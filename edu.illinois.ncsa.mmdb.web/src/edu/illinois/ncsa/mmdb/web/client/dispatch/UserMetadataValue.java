package edu.illinois.ncsa.mmdb.web.client.dispatch;

public class UserMetadataValue extends NamedThing {
    String sectionUri;
    String sectionMarker;

    public UserMetadataValue() {
        super();
    }

    public UserMetadataValue(String u, String n) {
        super(u, n);
    }

    public UserMetadataValue(String sectionUri, String u, String n) {
        super(u, n);
        setSectionUri(sectionUri);
    }

    public String getSectionUri() {
        return sectionUri;
    }

    public void setSectionUri(String sectionUri) {
        this.sectionUri = sectionUri;
    }

    public String getSectionMarker() {
        return sectionMarker;
    }

    public void setSectionMarker(String sectionMarker) {
        this.sectionMarker = sectionMarker;
    }

}
