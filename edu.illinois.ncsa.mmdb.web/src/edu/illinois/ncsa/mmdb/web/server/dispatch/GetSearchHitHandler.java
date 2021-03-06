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
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSearchHit;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetSearchHitResult;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

/**
 * Retrieve a specific dataset.
 * 
 * @author Luigi Marini
 * 
 */
public class GetSearchHitHandler implements ActionHandler<GetSearchHit, GetSearchHitResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(GetSearchHitHandler.class);

    @SuppressWarnings("deprecation")
    @Override
    public GetSearchHitResult execute(GetSearchHit action, ExecutionContext arg1) throws ActionException {

        String beanURI = null;
        GetSearchHitResult result = new GetSearchHitResult();
        Resource userUri = action.getUser() == null ? PersonBeanUtil.getAnonymousURI() : Resource.uriRef(action.getUser());

        // get the type of the hit
        Unifier u = new Unifier();
        u.setColumnNames("type");
        u.addPattern(Resource.uriRef(action.getUri()), Rdf.TYPE, "type");
        try {
            TupeloStore.getInstance().getContext().perform(u);
        } catch (OperatorException e1) {
            log.error("Operator exception getting search hit: ", e1);
        }
        boolean isCollection = false;

        for (Tuple<Resource> row : u.getResult() ) {
            Resource type = row.get(0);
            if (Cet.DATASET.equals(type)) {
                beanURI = action.getUri();
            } else if (CollectionBeanUtil.COLLECTION_TYPE.equals(type)) {
                beanURI = action.getUri();
                isCollection = true;
            } else if (MMDB.SECTION_TYPE.equals(type)) {
                // get section info
                Unifier us = new Unifier();
                us.setColumnNames("dataset", "label", "marker");
                us.addPattern("dataset", MMDB.METADATA_HASSECTION, Resource.uriRef(action.getUri()));
                us.addPattern(Resource.uriRef(action.getUri()), MMDB.SECTION_LABEL, "label");
                us.addPattern(Resource.uriRef(action.getUri()), MMDB.SECTION_MARKER, "marker");
                try {
                    TupeloStore.getInstance().getContext().perform(us);
                } catch (OperatorException e) {
                    log.error("Error querying for information about section of dataset " + action.getUri(), e);
                }
                for (Tuple<Resource> row2 : us.getResult() ) {
                    beanURI = row2.get(0).getString();
                    result.setSectionUri(action.getUri());
                    result.setSectionLabel(row2.get(1).getString());
                    result.setSectionMarker(row2.get(2).getString());
                }
            }
        }
        if (beanURI != null) {
            if (!new SEADRbac(TupeloStore.getInstance().getContext()).checkAccessLevel(userUri, Resource.uriRef(beanURI))) {
                log.debug("No access to: " + beanURI);
                throw new ActionException("No access to dataset or collection.");
            }
            BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
            if (!isCollection) {
                try {
                    DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);

                    DatasetBean datasetBean = dbu.get(beanURI);
                    datasetBean = dbu.update(datasetBean);
                    result.setBean(datasetBean);
                    result.setPreviewUri(beanURI);

                } catch (Exception e) {
                    log.error("Error retrieving dataset " + action.getUri(), e);
                    throw new ActionException(e);
                }
            } else {
                try {
                    CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);
                    CollectionBean collectionBean = cbu.get(beanURI);
                    beanSession.update(collectionBean, true);
                    collectionBean.setMemberCount(TupeloStore.getInstance().countDatasets(beanURI, false));
                    result.setBean(collectionBean);

                    String badgeUri = TupeloStore.getInstance().getBadge(beanURI);
                    result.setPreviewUri(badgeUri);
                } catch (Exception e) {
                    log.error("Error retrieving collection " + action.getUri(), e);
                    throw new ActionException(e);
                }

            }
        }
        log.debug("Returning hit: " + result.getBean().getUri());
        return result;
    }

    @Override
    public Class<GetSearchHit> getActionType() {
        return GetSearchHit.class;
    }

    @Override
    public void rollback(GetSearchHit arg0, GetSearchHitResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
