package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.io.Serializable;

public class NamedThing implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -1597715944154418196L;

    String                    uri;
    String                    name;

    public NamedThing() {
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
