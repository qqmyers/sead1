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

import edu.illinois.ncsa.mmdb.web.client.dispatch.AnnotateResource;
import edu.illinois.ncsa.mmdb.web.client.dispatch.AnnotateResourceResult;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.AnnotationBean;
import edu.uiuc.ncsa.cet.bean.tupelo.AnnotationBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Annotate any resource in repository. Given a sessionId, retrieve the
 * PersonBean and add set it as the creator of the annotation. Store the
 * annotation in the context.
 *
 * @author Luigi Marini
 *
 */
public class AnnotateResourceHandler implements
        ActionHandler<AnnotateResource, AnnotateResourceResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(AnnotateResourceHandler.class);

    @Override
    public AnnotateResourceResult execute(AnnotateResource arg0,
            ExecutionContext arg1) throws ActionException {

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        AnnotationBeanUtil abu = new AnnotationBeanUtil(beanSession);

        AnnotationBean annotation = arg0.getAnnotation();

        String resource = arg0.getUri();

        String sessionId = arg0.getSessionId();

        String personID = sessionId;

        PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance()
                .getBeanSession());

        // FIXME only required until sessionid stores the full uri and not the
        // email address
        if (!sessionId.startsWith(PersonBeanUtil.getPersonID(""))) {
            personID = PersonBeanUtil.getPersonID(sessionId);
        }

        try {
            annotation.setCreator(pbu.get(personID));
        } catch (Exception e1) {
            log.error("Error getting creator of annotation", e1);
        }

        if (annotation.getCreator() == null) {
            annotation.setCreator(PersonBeanUtil.getAnonymous());
        }

        // store annotation
        try {
            beanSession.register(annotation);
            beanSession.save(annotation);
            abu.addAssociationTo(resource, annotation);
            log.debug("Annotated " + resource);
            TupeloStore.getInstance().changed(resource);
        } catch (OperatorException e2) {
            log.error("Error saving and associating an annotation bean", e2);
        } catch (Exception e) {
            log.error("Error saving and associating an annotation bean", e);
        }

        return new AnnotateResourceResult();
    }

    @Override
    public Class<AnnotateResource> getActionType() {
        return AnnotateResource.class;
    }

    @Override
    public void rollback(AnnotateResource arg0, AnnotateResourceResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
