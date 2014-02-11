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
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAccessLevelResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetAccessLevel;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * Set access level for dataset
 * 
 * @author Rob Kooper
 * 
 */
public class SetAccessLevelHandler implements ActionHandler<SetAccessLevel, GetAccessLevelResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(SetAccessLevelHandler.class);

    @Override
    public GetAccessLevelResult execute(SetAccessLevel arg0, ExecutionContext arg1) throws ActionException {
        Context context = TupeloStore.getInstance().getContext();
        SEADRbac rbac = new SEADRbac(context);
        try {
            if (rbac.checkPermission(arg0.getUser(), arg0.getUri(), Permission.EDIT_METADATA)) {
                try {
                    Resource pred = Resource.uriRef(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelPredicate));
                    rbac.setAccessLevel(Resource.uriRef(arg0.getUri()), pred, arg0.getLevel());
                } catch (OperatorException x) {
                    throw new ActionException("failed to set access level on " + arg0.getUri(), x);
                }
            } else {
                log.debug("no permission to set access level on " + arg0.getUri());
            }
        } catch (RBACException x) {
            throw new ActionException("failed to check set metadata permission", x);
        }
        return GetAccessLevelHandler.getResult(arg0.getUri());
    }

    @Override
    public Class<SetAccessLevel> getActionType() {
        return SetAccessLevel.class;
    }

    @Override
    public void rollback(SetAccessLevel arg0, GetAccessLevelResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub
    }

}
