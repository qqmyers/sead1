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
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tables;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryCollections;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

/**
 * TODO Add comments
 * 
 * @author Joe Futrelle
 * @author Luigi Marini
 * 
 */
public class ListQueryCollectionsHandler implements
        ActionHandler<ListQueryCollections, ListQueryResult<CollectionBean>> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(ListQueryCollectionsHandler.class);

    @Override
    public ListQueryResult<CollectionBean> execute(ListQueryCollections arg0, ExecutionContext arg1) throws ActionException {
        long l = System.currentTimeMillis();
        ListQueryResult<CollectionBean> queryResult = new ListQueryResult<CollectionBean>();

        String sortkey = arg0.getOrderBy();
        boolean desc = !sortkey.endsWith("-asc");
        int idx = sortkey.indexOf('-');
        if (idx != -1) {
            sortkey = sortkey.substring(0, idx);
        }

        queryResult.setResults(listCollections(sortkey, desc, arg0.getLimit(), arg0.getOffset(), arg0.getWithTag()));
        queryResult.setTotalCount(countCollections(arg0.getWithTag()));
        log.debug("Tupelo fetch results : " + (System.currentTimeMillis() - l));
        l = System.currentTimeMillis();

        return queryResult;
    }

    /**
     * 
     * @param orderBy
     * @param desc
     * @param limit
     * @param offset
     * @param inCollection
     * @return
     * @throws OperatorException
     */
    private static Table<Resource> list(String orderBy, boolean desc, int limit, int offset, String withTag) throws OperatorException {
        Unifier u = new Unifier();
        u.addColumnName("s");
        if (withTag != null) {
            u.addPattern("s", Tags.TAGGED_WITH_TAG, TagEventBeanUtil.createTagUri(withTag));
        } else {
            if (limit > 0) {
                u.setLimit(limit);
            }
            u.setOffset(offset);
        }
        u.addPattern("s", DcTerms.HAS_PART, "p");
        u.addColumnName("p");
        u.addPattern("s", Rdf.TYPE, Cet.cet("Collection"));

        // translate orderBy to the right sort
        if (orderBy.equals("date")) {
            u.addPattern("s", DcTerms.DATE_CREATED, "d");
            u.addColumnName("d");
            u.addOrderBy("d", !desc);
        } else if (orderBy.equals("title")) {
            u.addPattern("s", Dc.TITLE, "t");
            u.addColumnName("t");
            u.addOrderBy("t", !desc);
        }

        return TupeloStore.getInstance().unifyExcludeDeleted(u, "s");
    }

    public static int countCollections(String withTag) {
        Unifier u = new Unifier();
        u.addColumnName("s");
        if (withTag != null) {
            u.addPattern("s", Tags.TAGGED_WITH_TAG, TagEventBeanUtil.createTagUri(withTag));
        }
        u.addPattern("s", Rdf.TYPE, Cet.cet("Collection"));

        try {
            return TupeloStore.getInstance().unifyExcludeDeleted(u, "s").getRows().size();
        } catch (OperatorException e) {
            log.error("Error counting collections", e);
            return 0;
        }
    }

    /**
     * 
     * @param orderBy
     * @param desc
     * @param limit
     * @param offset
     * @param inCollection
     * @param dbu
     * @return
     */
    public static Map<String, Integer> listCollectionUris(String orderBy, boolean desc, int limit, int offset, String withTag) {
        try {
            Map<String, Integer> result = new HashMap<String, Integer>();
            List<String> list = new ArrayList<String>();
            for (Resource r : Tables.getColumn(list(orderBy, desc, limit, offset, withTag), 0) ) {
                if (!result.containsKey(r.getString())) {
                    result.put(r.getString(), new Integer(1));
                    list.add(r.getString());
                } else {
                    result.put(r.getString(), new Integer(result.get(r.getString()) + 1));
                }
            }
            if (withTag != null) {
                if (offset > list.size()) {
                    result.clear();
                } else {
                    list = list.subList(offset, Math.min(offset + limit, result.size()));
                    Map<String, Integer> temp = result;
                    result = new HashMap<String, Integer>();
                    for(String x : list) {
                        result.put(x, temp.get(x));
                    }
                }
            }
            log.info(result.size() + " elements retured");
            return result;
        } catch (OperatorException x) {
            log.error("Error listing collection URIs", x);
            return new HashMap<String, Integer>();
        }
    }

    public static List<CollectionBean> listCollections(final String orderBy, final boolean desc, final int limit, final int offset, final String withTag) {
        try {
            Map<String, Integer> uris;
            long then = System.currentTimeMillis(); //
            try {
                uris = listCollectionUris(orderBy, desc, limit, offset, withTag);
            } catch (Exception x) {
                log.error("unable to list collections", x);
                throw x;
            }
            long between = System.currentTimeMillis();
            try {
                CollectionBeanUtil cbu = new CollectionBeanUtil(TupeloStore.getInstance().getBeanSession());
                List<CollectionBean> result = new ArrayList<CollectionBean>();
                for(Map.Entry<String, Integer> entry : uris.entrySet()) {
                    CollectionBean cb = cbu.get(entry.getKey(), true); // we know they're not deleted already, hence getDeleted=true
                    cb.setMemberCount(entry.getValue());
                    result.add(cb);
                }
                long now = System.currentTimeMillis();
                log.debug("listed " + result.size() + " dataset(s) in "
                        + (now - then) + "ms (" + (between - then) + "/"
                        + (now - between) + " u/b)");
                return result;
            } catch (OperatorException x) {
                log.error("unable to list datasets " + uris);
                throw x;
            }
        } catch (Exception x) {
            log.error("Error listing datasets", x);
            return new LinkedList<CollectionBean>();
        }
    }

    @Override
    public Class<ListQueryCollections> getActionType() {
        return ListQueryCollections.class;
    }

    @Override
    public void rollback(ListQueryCollections arg0, ListQueryResult<CollectionBean> arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
