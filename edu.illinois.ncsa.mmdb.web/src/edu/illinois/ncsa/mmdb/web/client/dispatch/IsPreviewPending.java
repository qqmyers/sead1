package edu.illinois.ncsa.mmdb.web.client.dispatch;

public class IsPreviewPending extends SubjectAction<IsPreviewPendingResult> {
    String size;
    public IsPreviewPending() { }

    public IsPreviewPending(String uri, String size) {
        super(uri);
        setSize(size);
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }


}
