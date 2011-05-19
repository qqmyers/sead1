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

import java.util.ArrayList;
import java.util.LinkedList;
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
import org.tupeloproject.rdf.query.OrderBy;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRecentActivity;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetRecentActivityResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Find out recent activity for a particular user.
 * 
 * @author Luigi Marini
 * 
 */
public class GetRecentActivityHandler implements ActionHandler<GetRecentActivity, GetRecentActivityResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(GetRecentActivityHandler.class);

    @Override
    public GetRecentActivityResult execute(GetRecentActivity getRecentActivity,
            ExecutionContext arg1) throws ActionException {

        String user = getRecentActivity.getUser();

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

        List<DatasetBean> datasets = new ArrayList<DatasetBean>();

        log.debug("Getting recent activity for user " + user);

        // TODO implement query
        Unifier uf = new Unifier();
        uf.addPattern("dataset", Rdf.TYPE, Cet.DATASET);
        uf.addPattern("dataset", Dc.CREATOR, Resource.uriRef(user));
        uf.addPattern("dataset", Dc.DATE, "date");
        uf.setColumnNames("dataset", "date");

        List<OrderBy> listOrderBy = new LinkedList<OrderBy>();
        OrderBy orderBy = new OrderBy();
        orderBy.setName("date");
        orderBy.setAscending(false);
        listOrderBy.add(orderBy);
        uf.setOrderBy(listOrderBy);

        try {
            int showIndex = 0;
            int equalCounter = 0;
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(uf, "dataset") ) {
                //First time home page is loaded
                if (getRecentActivity.getFirst()) {
                    if (showIndex < getRecentActivity.getMaxNum() && row.get(0) != null) {
                        datasets.add(dbu.get(row.get(0)));
                        showIndex++;
                    }
                    //Every time user presses "View More"
                } else {
                    long compareDate = dbu.get(row.get(0)).getDate().getTime();
                    long staticDate = getRecentActivity.getDate().getTime();
                    //Used in the instance of datasets having exact same date
                    if (staticDate == compareDate) {
                        if (equalCounter > 0) {
                            staticDate = staticDate + 1;
                        }
                        equalCounter++;
                    }
                    if (showIndex < getRecentActivity.getMaxNum() && row.get(0) != null && staticDate > compareDate) {
                        datasets.add(dbu.get(row.get(0)));
                        showIndex++;
                    }

                }
            }
        } catch (OperatorException e1) {
            log.error("Error getting recent activity for user" + user, e1);
        }

        return new GetRecentActivityResult(datasets);
    }

    @Override
    public Class<GetRecentActivity> getActionType() {
        return GetRecentActivity.class;
    }

    @Override
    public void rollback(GetRecentActivity arg0, GetRecentActivityResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
