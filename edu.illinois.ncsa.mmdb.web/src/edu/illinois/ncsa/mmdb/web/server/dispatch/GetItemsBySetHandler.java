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

import java.util.Collection;
import java.util.HashSet;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetItemsBySet;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetItemsBySetResult;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;

/**
 * Retrieve datasets from a set of datasets
 * 
 * @author Luis Mendez
 * 
 */
public class GetItemsBySetHandler implements ActionHandler<GetItemsBySet, GetItemsBySetResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(GetItemsBySetHandler.class);

    @Override
    public GetItemsBySetResult execute(GetItemsBySet action, ExecutionContext arg1) throws ActionException {

        log.info("Retrieving selected items for display");

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
        CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);

        HashSet<DatasetBean> datasets = new HashSet<DatasetBean>();
        HashSet<CollectionBean> collections = new HashSet<CollectionBean>();

        try {
            SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
            for (String uri : action.getItems() ) {
                Collection<Resource> types = beanSession.getRDFTypes(Resource.uriRef(uri));
                for (Resource type : types ) {
                    if (type.equals(cbu.getType())) {
                        CollectionBean bean = cbu.get(uri);
                        bean = cbu.update(bean);
                        collections.add(bean);
                        break;
                    }
                    if (type.equals(dbu.getType())) {
                        if (!rbac.checkAccessLevel(Resource.uriRef(action.getUser()), Resource.uriRef(uri))) {
                            break;
                        }
                        DatasetBean bean = dbu.get(uri);
                        bean = dbu.update(bean);
                        datasets.add(bean);
                        break;
                    }
                }
            }

            // return dataset and preview

        } catch (Exception e) {
            log.error("Error retrieving datasets ", e);
            throw new ActionException(e);
        }

        return new GetItemsBySetResult(datasets, collections);

    }

    @Override
    public Class<GetItemsBySet> getActionType() {
        return GetItemsBySet.class;
    }

    @Override
    public void rollback(GetItemsBySet arg0, GetItemsBySetResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
