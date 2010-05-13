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

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryDatasetsForTag;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Given a tag get all datasets tagged with it.
 * 
 * @author Luigi Marini
 * 
 */
public class ListQueryDatasetsForTagHandler implements
        ActionHandler<ListQueryDatasetsForTag, ListQueryResult<DatasetBean>> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(ListQueryDatasetsForTagHandler.class);

    @Override
    public ListQueryResult<DatasetBean> execute(ListQueryDatasetsForTag action, ExecutionContext arg1)
            throws ActionException {

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
        String tagName = action.getTagName();
        log.trace("Getting datasets tagged with " + tagName);
        List<DatasetBean> datasets = new ArrayList<DatasetBean>();

        Unifier uf = new Unifier();
        uf.addPattern("dataset", Tags.HAS_TAGGING_EVENT, "event");
        uf.addPattern("event", Tags.HAS_TAG_OBJECT, "tag");
        uf.addPattern("tag", Tags.HAS_TAG_TITLE, Resource.literal(tagName));
        uf.addPattern("dataset", Rdf.TYPE, Cet.DATASET);
        uf.setColumnNames("dataset");

        try {
            // TODO modifiy to support sorting and paging
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(uf, "dataset") ) {
                if (row.get(0) != null) {
                    datasets.add(dbu.get(row.get(0)));
                }
            }
        } catch (OperatorException e1) {
            log.error("Error retrieving datasets with tag " + tagName);
            e1.printStackTrace();
        }

        log.debug("Found " + datasets.size() + " datasets with tag '"
                + tagName + "'");

        ListQueryResult<DatasetBean> queryResult = new ListQueryResult<DatasetBean>();
        queryResult.setResults(datasets);

        queryResult.setTotalCount(datasets.size());

        return queryResult;
    }

    @Override
    public Class<ListQueryDatasetsForTag> getActionType() {
        return ListQueryDatasetsForTag.class;
    }

    @Override
    public void rollback(ListQueryDatasetsForTag arg0, ListQueryResult<DatasetBean> arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }
}
