package edu.illinois.ncsa.mmdb.web.client.dispatch;

@SuppressWarnings("serial")
public class SetInfo extends SubjectAction<EmptyResult> {
    private String fname;
    private String mimetype;

    public enum Type {
        FILENAME, MIMETYPE, CATEGORY, NONE
    };

    public SetInfo() {
    }

    public SetInfo(String uri, String info, Type t) {
        super(uri);
        if (t == Type.FILENAME) {
            setFilename(info);
        }
        if (t == Type.MIMETYPE) {
            setMimetype(info);
        }

    }

    public String getFilename() {
        return fname;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setFilename(String fname) {
        this.fname = fname;
    }

    public void setMimetype(String mime) {
        this.mimetype = mime;
    }
}