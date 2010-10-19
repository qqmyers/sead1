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

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Result;
import edu.illinois.ncsa.mmdb.web.client.Permissions.Permission;

/**
 * @author Luigi Marini
 * 
 */
@SuppressWarnings("serial")
public class HasPermissionResult implements Result {

    private Map<Permission, Boolean> permitted;

    public HasPermissionResult() {
    }

    public HasPermissionResult(Permission p, boolean permitted) {
        setIsPermitted(p, permitted);
    }

    /**
     * This only works properly if the result contains a value for a
     * <b>single</b> permission.
     * Otherwise it returns true if <b>any</b> of the permission values is ALLOW
     * 
     * @return
     */
    public boolean isPermitted() {
        assert permitted.size() == 1;
        return permitted.values().contains(Boolean.TRUE);
    }

    /**
     * @return the result
     */
    public boolean isPermitted(Permission p) {
        return permitted.get(p);
    }

    public void setIsPermitted(Permission p, boolean ip) {
        getPermitted().put(p, ip);
    }

    public Map<Permission, Boolean> getPermitted() {
        if (permitted == null) {
            permitted = new HashMap<Permission, Boolean>();
        }
        return permitted;
    }

    public void setPermitted(Map<Permission, Boolean> p) {
        permitted = p;
    }
}
