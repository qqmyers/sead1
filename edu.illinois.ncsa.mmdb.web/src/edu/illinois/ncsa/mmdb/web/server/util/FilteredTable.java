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
