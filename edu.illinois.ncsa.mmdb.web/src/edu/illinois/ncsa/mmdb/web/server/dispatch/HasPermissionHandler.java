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
import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermission;
import edu.illinois.ncsa.mmdb.web.client.dispatch.HasPermissionResult;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * Check if a user has a specific permission.
 * 
 * @author Luigi Marini
 * 
 */
public class HasPermissionHandler implements
        ActionHandler<HasPermission, HasPermissionResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(HasPermissionHandler.class);

    @Override
    public HasPermissionResult execute(HasPermission action,
            ExecutionContext arg1) throws ActionException {

        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());

        Resource userUri = createUserURI(action.getUser());
        Resource objectUri = action.getObject() != null ? Resource.uriRef(action.getObject()) : null;

        HasPermissionResult result = new HasPermissionResult();

        for (Permission permission : action.getPermissions() ) {
            Resource permissionUri = Resource.uriRef(permission.getUri());
            try {
                boolean hasPermission = false;
                hasPermission = rbac.checkPermission(userUri, objectUri, permissionUri);
                result.setIsPermitted(permission, hasPermission);
                log.debug("User " + userUri + " " + (hasPermission ? "has" : "does not have") + " permission " + permission.getLabel());
            } catch (RBACException e) {
                log.error("Error checking user permissions", e);
                result.setIsPermitted(permission, false);
            }
        }

        return result;
    }

    /**
     * Create the proper user uri.
     * 
     * FIXME this seems like a hack
     * FIXME is this used?
     * 
     * @param user
     * @return
     */
    private Resource createUserURI(String user) {
        if (user.startsWith("http://cet.ncsa.uiuc.edu/")) {
            Resource userURI = Resource.uriRef(user);
            return userURI;
        } else {
            Resource userURI = Resource.uriRef(PersonBeanUtil.getPersonID(user));
            return userURI;
        }
    }

    @Override
    public Class<HasPermission> getActionType() {
        return HasPermission.class;
    }

    @Override
    public void rollback(HasPermission arg0, HasPermissionResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
