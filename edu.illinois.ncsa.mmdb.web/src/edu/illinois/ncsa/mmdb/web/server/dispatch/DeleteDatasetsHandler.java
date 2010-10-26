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

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;

import edu.illinois.ncsa.mmdb.web.client.dispatch.BatchResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasets;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.medici.MediciRbac;

public class DeleteDatasetsHandler implements ActionHandler<DeleteDatasets, BatchResult> {

    @Override
    public BatchResult execute(DeleteDatasets arg0, ExecutionContext arg1) throws ActionException {
        BatchResult result = new BatchResult();
        TripleWriter mod = new TripleWriter();
        Context context = TupeloStore.getInstance().getContext();
        MediciRbac rbac = new MediciRbac(context);
        for (String datasetUri : arg0.getResources() ) {
            // check for authorization
            try {
                if (!rbac.checkPermission(arg0.getUser(), datasetUri, Permission.DELETE_DATA)) {
                    result.setFailure(datasetUri, "Unauthorized");
                } else {
                    mod.add(Triple.create(Resource.uriRef(datasetUri), DcTerms.IS_REPLACED_BY, Rdf.NIL));
                    result.addSuccess(datasetUri);
                }
            } catch (RBACException x) {
                result.setFailure(datasetUri, "access control error");
            }
        }
        try {
            TupeloStore.getInstance().getContext().perform(mod);
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
