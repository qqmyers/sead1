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
/**
 *
 */
package edu.illinois.ncsa.mmdb.web.server.search;

import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.SearchResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Text base search of the repository with filter
 *
 * @author Luis Mendez
 *
 */
public class SearchWithFilterHandler {

    Log log = LogFactory.getLog(SearchWithFilterHandler.class);

    public SearchResult performQuery(String query, String predicate)
            throws ActionException {

        log.info("Trying to search");

        SearchResult searchResult = new SearchResult();

        long then = System.currentTimeMillis();

        Unifier u = new Unifier();
        Resource filter = Resource.uriRef(predicate);
        u.setColumnNames("dataset", "query");
        u.addPattern("dataset", filter, "query"); // determine the target dataset/collection uri from the relationship triple

        try {
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "dataset") ) {
                if (query.equals(row.get(1).getString())) {
                    searchResult.addHit(row.get(0).getString());
                }

            }
        } catch (OperatorException e) {
            log.info("Search with filter failed");
            e.printStackTrace();
        }

        long elapsed = System.currentTimeMillis() - then;
        log.debug("Search for '" + query + "' with filter: '" + predicate + "' took " + elapsed + "ms");
        return searchResult;
    }
}
