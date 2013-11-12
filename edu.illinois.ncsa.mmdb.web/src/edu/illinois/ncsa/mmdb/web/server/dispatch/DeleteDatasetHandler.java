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

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;

import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDataset;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteDatasetResult;
import edu.illinois.ncsa.mmdb.web.server.AccessControl;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * Delete dataset. Marks dataset as deleted but content still remains in the
 * repository.
 * 
 * @author Luigi Marini
 * 
 */
public class DeleteDatasetHandler implements
        ActionHandler<DeleteDataset, DeleteDatasetResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(DeleteDatasetHandler.class);

    @Override
    public DeleteDatasetResult execute(DeleteDataset arg0, ExecutionContext arg1)
            throws ActionException {
        String datasetUri = arg0.getUri();
        // check for authorization
        if (!new SEADRbac(TupeloStore.getInstance().getContext()).checkAccessLevel(Resource.uriRef(arg0.getUser()), Resource.uriRef(arg0.getUri()))) {
            throw new ActionException("No access to dataset");
        }
        if (!AccessControl.isAdmin(arg0.getUser()) && !AccessControl.isCreator(arg0.getUser(), datasetUri)) {
            throw new ActionException("Unauthorized");
        }
        try {
            TupeloStore.getInstance().getContext().addTriple(
                    Resource.uriRef(datasetUri), DcTerms.IS_REPLACED_BY, Rdf.NIL);
            TupeloStore.getInstance().deleted(datasetUri);
            log.debug("Dataset deleted" + datasetUri);
            return new DeleteDatasetResult(true);
        } catch (OperatorException e) {
            log.error("Error deleting dataset " + datasetUri, e);
            return new DeleteDatasetResult(false);
        }
    }

    @Override
    public Class<DeleteDataset> getActionType() {
        return DeleteDataset.class;
    }

    @Override
    public void rollback(DeleteDataset arg0, DeleteDatasetResult arg1,
            ExecutionContext arg2) throws ActionException {
        String datasetUri = arg0.getUri();
        try {
            TupeloStore.getInstance().getContext().removeTriple(
                    Resource.uriRef(datasetUri), DcTerms.IS_REPLACED_BY, Rdf.NIL);
        } catch (OperatorException e) {
            throw new ActionException("unable to undelete dataset "
                    + datasetUri, e);
        }
    }
}
