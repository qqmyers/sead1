/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010 , NCSA.  All rights reserved.
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

import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;

import edu.illinois.ncsa.mmdb.web.client.dispatch.BatchResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasets;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

public class DeleteDatasetsHandler implements ActionHandler<DeleteDatasets, BatchResult> {

    @Override
    public BatchResult execute(DeleteDatasets arg0, ExecutionContext arg1) throws ActionException {
        BatchResult result = new BatchResult();
        TripleWriter mod = new TripleWriter();
        Context context = TupeloStore.getInstance().getContext();
        SEADRbac rbac = new SEADRbac(context);
        Resource userUri = arg0.getUser() == null ? PersonBeanUtil.getAnonymousURI() : Resource.uriRef(arg0.getUser());

        for (String datasetUri : arg0.getResources() ) {
            // check for authorization
            UriRef itemId = Resource.uriRef(datasetUri);
            TripleMatcher tm = new TripleMatcher();
            tm.match(itemId, Rdf.TYPE, Cet.DATASET);
            try {
                context.perform(tm);
                boolean isCollection = false;
                if (tm.getResult().isEmpty()) {
                    isCollection = true;
                }
                try {
                    if (!isCollection) {
                        if (!new SEADRbac(TupeloStore.getInstance().getContext()).checkAccessLevel(userUri, itemId)) {
                            result.setFailure(datasetUri, "no access to dataset");
                        } else if (!rbac.checkPermission(arg0.getUser(), datasetUri, Permission.DELETE_DATA)) {
                            result.setFailure(datasetUri, "Unauthorized");
                        } else {
                            mod.add(Triple.create(itemId, DcTerms.IS_REPLACED_BY, Rdf.NIL));
                            result.addSuccess(datasetUri);
                        }
                    } else { //Collection
                        if (!rbac.checkPermission(arg0.getUser(), datasetUri, Permission.DELETE_COLLECTION)) {
                            result.setFailure(datasetUri, "Unauthorized");
                        } else {
                            mod.add(Triple.create(itemId, DcTerms.IS_REPLACED_BY, Rdf.NIL));
                            TripleMatcher kidsMatcher = new TripleMatcher();
                            kidsMatcher.match(itemId, DcTerms.HAS_PART, null);
                            context.perform(kidsMatcher);
                            Set<Triple> kidsTriples = kidsMatcher.getResult();
                            //Mark kids as top level if they have no other parents
                            for (Triple t : kidsTriples ) {
                                Resource kid = t.getObject();
                                TripleMatcher rentsMatcher = new TripleMatcher();
                                rentsMatcher.match(null, DcTerms.HAS_PART, kid);
                                context.perform(rentsMatcher);
                                if (rentsMatcher.getResult().size() == 0) {
                                    mod.add(AddToCollectionHandler.TOP_LEVEL, DcTerms.HAS_PART, kid);
                                }
                            }
                            mod.setToRemove(kidsTriples);
                            result.addSuccess(datasetUri);
                        }
                    }
                } catch (RBACException x) {
                    result.setFailure(datasetUri, "access control error");
                }
            } catch (OperatorException oe) {
                result.setFailure(datasetUri, "Unable to determ type");
            }
        }
        try {
            context.perform(mod);
        } catch (OperatorException e) {
            throw new ActionException("delete failed completely", e);
        }

        return result;
    }

    @Override
    public Class<DeleteDatasets> getActionType() {
        return DeleteDatasets.class;
    }

    @Override
    public void rollback(DeleteDatasets arg0, BatchResult arg1, ExecutionContext arg2) throws ActionException {
        // FIXME implement
    }

}
