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
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.cet.search.Hit;
import edu.illinois.ncsa.cet.search.SearchableTextIndex;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Search;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SearchResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

/**
 * Text base search of the repository.
 * 
 * @author Luigi Marini
 * 
 */
public class SearchHandler implements ActionHandler<Search, SearchResult> {
    final int RESULT_COUNT_LIMIT = 20;                                    // FIXME this is a hack, we need paging

    Log       log                = LogFactory.getLog(SearchHandler.class);

    @Override
    public SearchResult execute(Search arg0, ExecutionContext arg1) throws ActionException {
        long then = System.currentTimeMillis();
        SearchResult searchResult = new SearchResult();

        String rawtext = "";
        Unifier uf = new Unifier();
        uf.setColumnNames("s");

        for (String s : arg0.getQuery().split("\\s+") ) {
            if (s.startsWith("tag:")) {
                uf.addPattern("s", Tags.TAGGED_WITH_TAG, TagEventBeanUtil.createTagUri(s.substring(4)));
            } else {
                rawtext += " " + s;
            }
        }

        // search for ids using RDF matcher.
        Set<String> idsfound = new HashSet<String>();
        if (uf.getPatterns().size() > 0) {
            try {
                for (Tuple<Resource> r : TupeloStore.getInstance().unifyExcludeDeleted(uf, "s") ) {
                    idsfound.add(r.get(0).getString());
                }
            } catch (OperatorException e) {
                log.error("Could not search for tags.", e);
            }
        }

        if (rawtext.trim().equals("")) {
            // only tag search
            for (String id : idsfound ) {
                searchResult.addHit(id);
                if (searchResult.getHits().size() > RESULT_COUNT_LIMIT) {
                    break;
                }
            }
        } else {
            // search for ids using lucene.
            SearchableTextIndex<String> search = TupeloStore.getInstance().getSearch();
            Iterable<Hit> result = search.search(rawtext);
            // merge lucene with tags
            for (Hit hit : result ) {
                if (!idsfound.contains(hit.getId())) {
                    continue;
                }
                searchResult.addHit(hit.getId());
                if (searchResult.getHits().size() > RESULT_COUNT_LIMIT) {
                    break;
                }
            }
        }

        long elapsed = System.currentTimeMillis() - then;
        log.debug("Search for '" + arg0.getQuery() + "' took " + elapsed + "ms");
        return searchResult;
    }

    @Override
    public Class<Search> getActionType() {
        return Search.class;
    }

    @Override
    public void rollback(Search arg0, SearchResult arg1, ExecutionContext arg2)
            throws ActionException {
        // TODO Auto-generated method stub

    }

}
