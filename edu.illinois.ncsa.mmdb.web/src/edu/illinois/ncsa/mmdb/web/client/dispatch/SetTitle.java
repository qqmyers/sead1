package edu.illinois.ncsa.mmdb.web.client.dispatch;

public class SetTitle extends SubjectAction<EmptyResult> {
    private String title;

    public SetTitle() {
    }

    public SetTitle(String uri, String title) {
        super(uri);
        setTitle(title);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
