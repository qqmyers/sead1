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

import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteAnnotation;
import edu.illinois.ncsa.mmdb.web.client.dispatch.DeleteAnnotationResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.AnnotationBeanUtil;

/**
 * Delete annotation. Unlinks it from the thing being annotated, but otherwise
 * leaves it untouched.
 * 
 */
public class DeleteAnnotationHandler implements
        ActionHandler<DeleteAnnotation, DeleteAnnotationResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(DeleteAnnotationHandler.class);

    @Override
    public DeleteAnnotationResult execute(DeleteAnnotation arg0, ExecutionContext arg1)
            throws ActionException {
        String thingUri = arg0.getUri();
        String annotationUri = arg0.getAnnotationUri();
        try {
            AnnotationBeanUtil abu = new AnnotationBeanUtil(null);
            Resource ap = abu.getAssociationPredicate();
            TupeloStore.getInstance().getContext().removeTriple(Resource.uriRef(thingUri), ap, Resource.uriRef(annotationUri));
            TupeloStore.getInstance().changed(thingUri);
            log.debug("Deleted annotation on " + thingUri);
            return new DeleteAnnotationResult(true);
        } catch (OperatorException e) {
            log.error("Error deleting annotation " + annotationUri + " from " + thingUri);
            return new DeleteAnnotationResult(false);
        }
    }

    @Override
    public Class<DeleteAnnotation> getActionType() {
        return DeleteAnnotation.class;
    }

    @Override
    public void rollback(DeleteAnnotation arg0, DeleteAnnotationResult arg1,
            ExecutionContext arg2) throws ActionException {
        String thingUri = arg0.getUri();
        String annotationUri = arg0.getAnnotationUri();
        try {
            AnnotationBeanUtil abu = new AnnotationBeanUtil(null);
            Resource ap = abu.getAssociationPredicate();
            TupeloStore.getInstance().getContext().addTriple(Resource.uriRef(thingUri), ap, Resource.uriRef(annotationUri));
        } catch (OperatorException e) {
            throw new ActionException("unable to undelete annotation " + annotationUri + " from " + thingUri);
        }
    }
}
