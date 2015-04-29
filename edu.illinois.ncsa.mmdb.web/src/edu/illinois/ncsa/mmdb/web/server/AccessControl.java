/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010 , NCSA.  All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.Dc;

import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

// static utilities for making access control decisions
public class AccessControl {
    static Log log = LogFactory.getLog(AccessControl.class);

    /** Is this person a dc:creator of this dataset? */
    public static boolean isCreator(String personUri, DatasetBean dataset) {
        if (personUri == null || dataset == null) {
            log.warn("isCreator called with null value");
            return false;
        }
        return personUri.equals(dataset.getCreator().getUri());
    }

    /** Is this person a dc:creator of this resource? */
    public static boolean isCreator(String personUri, String resourceUri) {
        if (personUri == null || resourceUri == null) {
            log.warn("isCreator called with null value");
            return false;
        }
        try {
            for (Triple t : TupeloStore.getInstance().getContext().match(Resource.uriRef(resourceUri), Dc.CREATOR, null) ) {
                if (t.getObject().getString().equals(personUri)) {
                    return true;
                }
            }
            return false;
        } catch (OperatorException x) {
            log.error("unable to determine ownership", x);
            return false;
        }
    }

    public static boolean isAdmin(String personUri) {
        if (personUri == null) {
            log.warn("isAdmin called with null value");
            return false;
        }
        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        try {
            return rbac.checkPermission(personUri, Permission.VIEW_ADMIN_PAGES);
        } catch (RBACException e) {
            log.error("unable to check if user is admin", e);
            return false;
        }
    }
}
