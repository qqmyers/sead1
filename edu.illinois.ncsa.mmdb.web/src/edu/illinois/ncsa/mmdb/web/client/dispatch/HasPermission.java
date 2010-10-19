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
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.customware.gwt.dispatch.shared.Action;
import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;

/**
 * @author Luigi Marini
 * 
 */
@SuppressWarnings("serial")
public class HasPermission implements Action<HasPermissionResult> {

    private String          user;
    private Set<Permission> permissions;

    public HasPermission() {
    }

    public HasPermission(String user, Permission... permissions) {
        this.user = user;
        for (Permission p : permissions ) {
            addPermission(p);
        }
    }

    public HasPermission(String user, Collection<Permission> permissions) {
        this.user = user;
        for (Permission p : permissions ) {
            addPermission(p);
        }
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    public void addPermission(Permission p) {
        getPermissions().add(p);
    }

    /**
     * @return the permission
     */
    public Set<Permission> getPermissions() {
        if (permissions == null) {
            permissions = new HashSet<Permission>();
        }
        return permissions;
    }

    public void setPermissions(Set<Permission> p) {
        permissions = p;
    }
}
