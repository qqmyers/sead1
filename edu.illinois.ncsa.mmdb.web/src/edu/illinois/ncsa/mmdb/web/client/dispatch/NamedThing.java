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

    public NamedThing(String u, String n) {
        this();
        setUri(u);
        setName(n);
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

    public static <T extends NamedThing> SortedSet<T> orderByName(Collection<T> things) {
        SortedSet<T> sorted = new TreeSet<T>(new Comparator<T>() {
            @Override
            public int compare(T k1, T k2) {
                int c = 0;
                if ((k1.getName() != null) && (k2.getName() != null)) {
                    c = k1.getName().compareTo(k2.getName());
                }
                if ((c == 0) && (k1.getUri() != null) && (k2.getUri() != null)) {
                    c = k1.getUri().compareTo(k2.getUri());
                }
                return c;
            }
        });
        sorted.addAll(things);
        return sorted;
    }
}
