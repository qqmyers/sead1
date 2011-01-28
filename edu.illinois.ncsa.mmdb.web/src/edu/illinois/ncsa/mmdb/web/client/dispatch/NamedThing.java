package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

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

    public static SortedSet<NamedThing> orderByName(Collection<NamedThing> things) {
        return new TreeSet<NamedThing>(new Comparator<NamedThing>() {
            @Override
            public int compare(NamedThing k1, NamedThing k2) {
                int c = k1.getName().compareTo(k2.getName());
                if (c == 0) {
                    return k1.getUri().compareTo(k2.getUri());
                } else {
                    return c;
                }
            }
        });

    }
}
