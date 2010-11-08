package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Utilities for dealing with maps
 * 
 * @author futrelle
 */
public class MapUtil {
    /**
     * Sort a map by value
     * 
     * @param <K>
     *            the key type
     * @param <V>
     *            the value type
     * @param map
     *            the map to sort
     * @return a map of the same key/value pairs, sorted by value
     */
    public static <K extends Comparable<K>, V extends Comparable<V>> SortedMap<K, V> sortByValue(final Map<K, V> map) {
        SortedMap<K, V> result = new TreeMap<K, V>(new Comparator<K>() {
            @Override
            public int compare(K k1, K k2) {
                int c = map.get(k1).compareTo(map.get(k2));
                if (c == 0) { // handle value collision by sorting by key
                    return k1.compareTo(k2);
                } else {
                    return c;
                }
            }
        });
        result.putAll(map);
        return result;
    }
}
