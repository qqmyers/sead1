/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.mmdb.web.server.search;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.tupeloproject.rdf.terms.Dc;

import edu.illinois.ncsa.cet.search.Hit;
import edu.illinois.ncsa.cet.search.SearchableTextIndex;
import edu.illinois.ncsa.cet.search.StringHit;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.dispatch.ListDatasetsHandler;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * TODO Add comments
 * 
 * @author Joe Futrelle
 */
public class Search extends SearchableTextIndex<String> {

    @Override
    public Iterable<Hit> search(String searchString) {
        // here we just list datasets
        return search(searchString, 30, 0);
    }

    @Override
    public Iterable<Hit> search(String searchString, int limit) {
        // TODO Auto-generated method stub
        return search(searchString, limit, 0);
    }

    @Override
    public Iterable<Hit> search(String searchString, int limit, int offset) {
        // TODO Auto-generated method stub
        List<Hit> result = new LinkedList<Hit>();
        // TODO don't rely on ListDatasetsHandler, move that impl code into a common class
        DatasetBeanUtil dbu = new DatasetBeanUtil(TupeloStore.getInstance().getBeanSession());
        for (String uri : ListDatasetsHandler.listDatasetUris(Dc.DATE.getString(), true, limit, offset, null, null, dbu) ) {
            result.add(new StringHit(uri));
        }
        return result;
    }

    @Override
    public void deindexId(String id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void index(String id, String text) {
        // TODO Auto-generated method stub

    }

    @Override
    public Iterator<String> iterator() throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }
}
