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
import java.util.TreeMap;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sead.acr.common.utilities.Memoized;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Bag;
import org.tupeloproject.util.HashBag;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAllTags;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetTagsResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Return all tags in the system.
 *
 * @author Luigi Marini
 *
 */
public class GetAllTagsHandler implements ActionHandler<GetAllTags, GetTagsResult> {

    /** Commons logging **/
    private static Log                     log = LogFactory.getLog(GetAllTags.class);
    private static Memoized<GetTagsResult> cachedQuery;

    public GetAllTagsHandler() {
        super();
        if (cachedQuery == null) {
            cachedQuery = new Memoized<GetTagsResult>() {
                @Override
                public GetTagsResult computeValue() {
                    Unifier u = new Unifier();
                    u.setColumnNames("label", "resource");
                    u.addPattern("resource", Tags.TAGGED_WITH_TAG, "tag");
                    ///*u.addPattern("resource", Rdf.TYPE, Cet.DATASET);
                    u.addPattern("tag", Tags.HAS_TAG_TITLE, "label");

                    // only count one tag per label per non-deleted resource
                    Bag<String> tags = new HashBag<String>();
                    Set<String> uniq = new HashSet<String>();
                    try {
                        for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "resource") ) {
                            String label = row.get(0).getString();
                            String resource = row.get(1).getString();
                            String key = resource + " " + label;
                            if (!uniq.contains(key)) {
                                tags.add(label);
                            }
                            uniq.add(key);
                        }
                    } catch (OperatorException e) {
                        log.error("Error getting tags", e);
                    } catch (Exception e) {
                        log.error("Error getting tags", e);
                    }

                    TreeMap<String, Integer> sortedTags = new TreeMap<String, Integer>();
                    for (String tag : tags ) {
                        sortedTags.put(tag, tags.getCardinality(tag));
                    }

                    return new GetTagsResult(sortedTags);
                }
            };
            cachedQuery.setTtl(30 * 1000); // 30s
        }
    }

    @Override
    public GetTagsResult execute(GetAllTags arg0, ExecutionContext arg1)
            throws ActionException {

        return cachedQuery.getValue();
    }

    @Override
    public Class<GetAllTags> getActionType() {
        return GetAllTags.class;
    }

    @Override
    public void rollback(GetAllTags arg0, GetTagsResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
