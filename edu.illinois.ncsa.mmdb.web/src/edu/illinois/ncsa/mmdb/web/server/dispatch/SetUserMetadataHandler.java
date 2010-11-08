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

import java.util.Collection;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EmptyResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetUserMetadata;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.rbac.medici.Permission;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.medici.MediciRbac;

/**
 * TODO Add comments
 * 
 * @author Joe Futrelle
 * 
 */
public class SetUserMetadataHandler implements ActionHandler<SetUserMetadata, EmptyResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(SetUserMetadataHandler.class);

    @Override
    public EmptyResult execute(SetUserMetadata arg0, ExecutionContext arg1)
            throws ActionException {
        // only allow user to edit user metadata fields
        /* deprecating the static method on that handler, that's a kludge!
        if (!ListUserMetadataFieldsHandler.listUserMetadataFields().containsKey(arg0.getPropertyUri())) {
            throw new ActionException("specified property " + arg0.getPropertyUri() + " is not a user metadata field");
        }
        */
        MediciRbac rbac = new MediciRbac(TupeloStore.getInstance().getContext());
        try {
            if (!rbac.checkPermission(arg0.getUser(), arg0.getUri(), Permission.EDIT_USER_METADATA)) {
                throw new ActionException("Unauthorized");
            }
        } catch (RBACException e) {
            throw new ActionException("access control failure", e);
        }
        try {
            Resource subject = Resource.uriRef(arg0.getUri());
            Resource predicate = Resource.uriRef(arg0.getPropertyUri());
            Collection<String> values = arg0.getValues();

            //
            ThingSession ts = new ThingSession(TupeloStore.getInstance().getContext());
            ts.setValues(subject, predicate, values);
            ts.save();
            ts.close();

            // attempt to refetch the bean
            try {
                TupeloStore.refetch(subject);
            } catch (Exception x) {
                x.printStackTrace();
            }

            TupeloStore.getInstance().changed(subject.getString());

            return new EmptyResult();
        } catch (Exception x) {
            log.error("Error setting metadata on " + arg0.getUri(), x);
            throw new ActionException("failed", x);
        }
    }

    @Override
    public Class<SetUserMetadata> getActionType() {
        return SetUserMetadata.class;
    }

    @Override
    public void rollback(SetUserMetadata arg0, EmptyResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
