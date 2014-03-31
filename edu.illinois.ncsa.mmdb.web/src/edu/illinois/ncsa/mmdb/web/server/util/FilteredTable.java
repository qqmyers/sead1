package edu.illinois.ncsa.mmdb.web.server.util;

import java.util.Iterator;

import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;

public class FilteredTable<T> implements Table<T> {

    private Table<T>  resultTable = null;
    private final int filterOn;

    public FilteredTable(Table<T> result, int filterColumn) {
        resultTable = result;
        filterOn = filterColumn;
    }

    @SuppressWarnings("unchecked")
    public Iterator<Tuple<T>> iterator() {
        // TODO Auto-generated method stub
        Iterator<Tuple<T>> fi = (Iterator<Tuple<T>>) new FilteredIterator<T>(resultTable.iterator(), filterOn);
        return fi;
    }

    public Tuple<String> getColumnNames() {
        //FixMe - remove deleted column
        return resultTable.getColumnNames();
    }

}
