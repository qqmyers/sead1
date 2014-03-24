package edu.illinois.ncsa.mmdb.web.server.util;

import java.util.Iterator;

import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.Tuple;

public class FilteredIterator<T> implements Iterator<T> {

    private Iterator<Tuple<T>> rawIterator    = null;
    private final int          filterOn;
    private T                  nextGoodResult = null;

    public FilteredIterator(Iterator<Tuple<T>> iterator, int filterColumn) {
        rawIterator = iterator;
        filterOn = filterColumn;
        while (rawIterator.hasNext() && (nextGoodResult == null)) {
            getGoodResult();

        }
    }

    private void getGoodResult() {
        nextGoodResult = null;
        Tuple<Resource> row = null;
        if (rawIterator.hasNext()) {
            row = (Tuple<Resource>) rawIterator.next();
        }
        if (row != null) {
            while ((row.get(filterOn) != null) && rawIterator.hasNext()) {
                row = (Tuple<Resource>) rawIterator.next();
            }
            if (row.get(filterOn) == null) {
                nextGoodResult = (T) row;
            }
        }
    }

    public boolean hasNext() {

        if (nextGoodResult == null) {
            getGoodResult();
        }
        return (nextGoodResult != null);
    }

    public T next() {
        T result = nextGoodResult;
        getGoodResult();
        return result;
    }

    public void remove() {
        rawIterator.remove();

    }

}
