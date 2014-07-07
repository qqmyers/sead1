/*******************************************************************************
 * Copyright 2014 University of Michigan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
