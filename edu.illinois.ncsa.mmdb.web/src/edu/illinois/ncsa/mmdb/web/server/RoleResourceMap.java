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
package edu.illinois.ncsa.mmdb.web.server;

import java.util.HashMap;
import java.util.Map;

import org.tupeloproject.rdf.Resource;

import edu.illinois.ncsa.mmdb.web.client.Role;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;

/**
 * Map between elements of an enum and resources representing a specific
 * role. In the future this can be extended to load the map from an
 * external resource, be it a file or a Tupelo context.
 * 
 * @author Luigi Marini
 * 
 */
public class RoleResourceMap {

    private static Map<Role, Resource> toResource;
    private static Map<Resource, Role> toRole;

    static {
        toResource = new HashMap<Role, Resource>();
        toResource.put(Role.ADMIN, RBAC.ADMIN_ROLE);
        toResource.put(Role.MEMBER, RBAC.REGULAR_MEMBER_ROLE);

        toRole = new HashMap<Resource, Role>();
        toRole.put(RBAC.ADMIN_ROLE, Role.ADMIN);
        toRole.put(RBAC.REGULAR_MEMBER_ROLE, Role.MEMBER);
    }

    public static final Resource getResource(Role role) {
        return toResource.get(role);
    }

    public static final Role getRole(Resource role) {
        return toRole.get(role);
    }
}
