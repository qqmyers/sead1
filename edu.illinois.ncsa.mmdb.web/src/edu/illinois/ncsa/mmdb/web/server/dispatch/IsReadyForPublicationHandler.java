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
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;

import edu.illinois.ncsa.mmdb.web.client.dispatch.IsReadyForPublication;
import edu.illinois.ncsa.mmdb.web.client.dispatch.IsReadyForPublicationResult;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * Retrieve a specific dataset.
 *
 * @author Luigi Marini
 *
 */
public class IsReadyForPublicationHandler implements ActionHandler<IsReadyForPublication, IsReadyForPublicationResult> {

    /** Commons logging **/
    private static Log   log                       = LogFactory.getLog(IsReadyForPublicationHandler.class);

    static public UriRef proposedForPublicationRef = Resource.uriRef("http://sead-data.net/terms/ProposedForPublication");

    @Override
    public IsReadyForPublicationResult execute(IsReadyForPublication action, ExecutionContext arg1) throws ActionException {
        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        IsReadyForPublicationResult result = new IsReadyForPublicationResult();
        log.trace("User: " + action.getUser());
        log.trace("Coll: " + action.getUri());
        Resource userUri = action.getUser() == null ? PersonBeanUtil.getAnonymousURI() : Resource.uriRef(action.getUser());
        try {
            if (rbac.checkPermission(userUri, Resource.uriRef(Permission.VIEW_MEMBER_PAGES.getUri()))) {

                TripleMatcher tMatcher = new TripleMatcher();
                tMatcher.setSubject(Resource.uriRef(action.getUri()));
                tMatcher.setPredicate(proposedForPublicationRef);
                TupeloStore.getInstance().getContext().perform(tMatcher);
                if (!(tMatcher.getResult().isEmpty())) {
                    result.setReady(true);
                    log.debug(action.getUri() + " is ready to publish");
                }

            }
        } catch (RBACException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;

    }

    @Override
    public void rollback(IsReadyForPublication action, IsReadyForPublicationResult result, ExecutionContext context) throws ActionException {
        // TODO Auto-generated method stub

    }

    @Override
    public Class<IsReadyForPublication> getActionType() {
        return IsReadyForPublication.class;
    }

}
