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
import java.util.Timer;
import java.util.TimerTask;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tables;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryDatasets;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

/**
 * TODO Add comments
 * 
 * @author Joe Futrelle
 * @author Luigi Marini
 * 
 */
public class ListQueryDatasetsHandler implements
        ActionHandler<ListQueryDatasets, ListQueryResult<DatasetBean>> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(ListQueryDatasetsHandler.class);

    @Override
    public ListQueryResult<DatasetBean> execute(ListQueryDatasets arg0, ExecutionContext arg1) throws ActionException {

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

        String sortkey = arg0.getOrderBy();
        boolean desc = !sortkey.endsWith("-asc");
        int idx = sortkey.indexOf('-');
        if (idx != -1) {
            sortkey = sortkey.substring(0, idx);
        }

        ListQueryResult<DatasetBean> queryResult = new ListQueryResult<DatasetBean>();
        queryResult.setResults(listDatasets(sortkey, desc, arg0.getLimit(), arg0.getOffset(), arg0.getInCollection(), arg0.getWithTag(), dbu));

        queryResult.setTotalCount(TupeloStore.getInstance().countDatasets(arg0.getInCollection(), arg0.getWithTag(), false));

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
    private static Table<Resource> list(String orderBy, boolean desc, int limit, int offset, String inCollection, String withTag, DatasetBeanUtil dbu) throws OperatorException {
        Unifier u = new Unifier();
        u.setColumnNames("s", "o");
        if (inCollection != null) {
            u.addPattern(Resource.uriRef(inCollection), DcTerms.HAS_PART, "s");
        }
        if (withTag != null) {
            u.addPattern("s", Tags.HAS_TAGGING_EVENT, "_te");
            u.addPattern("_te", Tags.HAS_TAG_OBJECT, "_to");
            u.addPattern("_to", Tags.HAS_TAG_TITLE, Resource.literal(withTag)); // FIXME normalize?
        } else {
            if (limit > 0) {
                u.setLimit(limit);
            }
            u.setOffset(offset);
        }
        u.addPattern("s", Rdf.TYPE, dbu.getType());

        // translate orderBy to the right sort
        if (orderBy.equals("date")) {
            u.addPattern("s", Dc.DATE, "o");
        } else if (orderBy.equals("title")) {
            u.addPattern("s", Dc.TITLE, "o");
        } else if (orderBy.equals("category")) {
            u.addPattern("s", Dc.FORMAT, "f");
            u.addPattern("m", Dc.FORMAT, "f");
            u.addPattern("m", MimeMap.MIME_CATEGORY, "o");
        }

        // ascending or descending ordering
        if (desc) {
            u.addOrderByDesc("o");
        } else {
            u.addOrderBy("o");
        }

        return TupeloStore.getInstance().unifyExcludeDeleted(u, "s");
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
    public static List<String> listDatasetUris(String orderBy, boolean desc, int limit, int offset, String inCollection, String withTag, DatasetBeanUtil dbu) {
        try {
            List<String> result = new LinkedList<String>();
            for (Resource r : Tables.getColumn(list(orderBy, desc, limit, offset, inCollection, withTag, dbu), 0) ) {
                if (!result.contains(r.getString())) {
                    result.add(r.getString());
                }
            }
            if (withTag != null) {
                if (offset > result.size()) {
                    result.clear();
                } else {
                    result = result.subList(offset, Math.min(offset + limit, result.size()));
                }
            }
            log.info(result.size() + " elements retured");
            return result;
        } catch (OperatorException x) {
            log.error("Error listing dataset URIs", x);
            return new LinkedList<String>();
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
    public static List<DatasetBean> listDatasets(String orderBy, boolean desc, int limit, int offset, String inCollection, String withTag, DatasetBeanUtil dbu) {
        return listDatasets(orderBy, desc, limit, offset, inCollection, withTag, dbu, true);
    }

    public static List<DatasetBean> listDatasets(final String orderBy, final boolean desc, final int limit, final int offset, final String inCollection, final String withTag, final DatasetBeanUtil dbu, boolean prefetch) {
        try {
            List<String> uris;
            long then = System.currentTimeMillis(); //
            try {
                uris = listDatasetUris(orderBy, desc, limit, offset, inCollection, withTag, dbu);
            } catch (Exception x) {
                log.error("unable to list datasets", x);
                throw x;
            }
            long between = System.currentTimeMillis();
            try {
                final List<DatasetBean> result = dbu.get(uris, true); // we know they're not deleted already, hence getDeleted=true
                long now = System.currentTimeMillis();
                log.debug("listed " + result.size() + " dataset(s) in "
                        + (now - then) + "ms (" + (between - then) + "/"
                        + (now - between) + " u/b)");
                if (prefetch) {
                    final Timer t = new Timer(true);
                    t.schedule(new TimerTask() {
                        public void run() {
                            // prefetch the previews on this page
                            for (DatasetBean ds : result ) {
                                TupeloStore.getInstance().getPreview(ds.getUri(), GetPreviews.SMALL);
                            }
                            List<String> previewsToFetch = new LinkedList<String>();
                            // prefetch 1 more pages in each direction
                            for (int i = 1; i <= 1; i++ ) {
                                for (String ds : listDatasetUris(orderBy, desc, limit, offset + (limit * i), inCollection, withTag, dbu) ) {
                                    previewsToFetch.add(ds);
                                }
                                if (offset - (limit * i) > 0) {
                                    for (String ds : listDatasetUris(orderBy, desc, limit, offset - (limit * i), inCollection, withTag, dbu) ) {
                                        previewsToFetch.add(ds);
                                    }
                                }
                            }
                            // now fetch their previews, so they'll be cached
                            for (String ds : previewsToFetch ) {
                                TupeloStore.getInstance().getPreview(ds, GetPreviews.SMALL);
                            }
                            t.cancel();
                        }
                    }, 1000);
                }
                return result;
            } catch (OperatorException x) {
                log.error("unable to list datasets " + uris);
                throw x;
            }
        } catch (Exception x) {
            log.error("Error listing datasets", x);
            return new LinkedList<DatasetBean>();
        }
    }

    @Override
    public Class<ListQueryDatasets> getActionType() {
        return ListQueryDatasets.class;
    }

    @Override
    public void rollback(ListQueryDatasets arg0, ListQueryResult<DatasetBean> arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
