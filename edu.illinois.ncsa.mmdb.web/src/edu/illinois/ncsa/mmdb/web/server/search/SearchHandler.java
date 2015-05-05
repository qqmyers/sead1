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

import java.util.HashSet;
import java.util.Set;

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
import edu.illinois.ncsa.mmdb.web.client.dispatch.SearchResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

/**
 * Text base search of the repository.
 *
 * @author Luigi Marini
 *
 */
public class SearchHandler {

    Log log = LogFactory.getLog(SearchHandler.class);

    public SearchResult performQuery(String query) throws ActionException {
        long then = System.currentTimeMillis();
        SearchResult searchResult = new SearchResult();

        String rawtext = "";
        Unifier uf = new Unifier();
        uf.setColumnNames("s");

        for (String s : query.split("\\s+") ) {
            if (s.startsWith("tag:")) {
                uf.addPattern("s", Tags.TAGGED_WITH_TAG, TagEventBeanUtil.createTagUri(s.substring(4)));
            } else {
                rawtext += " *" + s + "*";
            }
        }
        rawtext = rawtext.trim();
        log.debug("Rawtext search: " + rawtext);

        // search for ids using RDF matcher.
        Set<String> idsfound = null;
        if (uf.getPatterns().size() > 0) {
            try {
                idsfound = new HashSet<String>();
                for (Tuple<Resource> r : TupeloStore.getInstance().unifyExcludeDeleted(uf, "s") ) {
                    idsfound.add(r.get(0).getString());
                }
            } catch (OperatorException e) {
                log.error("Could not search for tags.", e);
                idsfound = null;
            }
        }

        if (rawtext.equals("")) {
            // only tag search
            for (String id : idsfound ) {
                searchResult.addHit(id);
            }
        } else {
            // search for ids using lucene.
            SearchableTextIndex<String> search = TupeloStore.getInstance().getSearch();
            Iterable<Hit> result = search.search(rawtext);
            // merge lucene with tags
            for (Hit hit : result ) {
                log.debug("Hit: " + hit.getId());
                //FixMe - this code tries to AND between a tag and text search, but if the text search returns a section( and not the dataset),
                //the AND test fails and the hit is excluded - probably not what we want
                if ((idsfound != null) && !idsfound.contains(hit.getId())) {
                    log.debug("Not AND tag(s)");
                    continue;
                }
                searchResult.addHit(hit.getId());
            }
        }

        long elapsed = System.currentTimeMillis() - then;
        log.debug("Search for '" + query + "' took " + elapsed + "ms to find " + searchResult.getHits().size() + " hits");
        return searchResult;
    }

}