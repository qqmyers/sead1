package edu.illinois.ncsa.mmdb.web.client.dispatch;

public class NamedThing {
    String uri;
    String name;

    public NamedThing() {
    }

    public NamedThing(String uri, String name) {
        setUri(uri);
        setName(name);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
