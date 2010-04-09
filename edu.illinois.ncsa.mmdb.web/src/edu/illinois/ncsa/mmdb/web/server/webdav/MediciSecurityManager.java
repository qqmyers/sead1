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
package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.rdf.Resource;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;

import edu.illinois.ncsa.mmdb.web.server.Authentication;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBAC;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * Simple implementation of SecurityManger for medici. This will use the medici
 * to check if the username and password and if the user has access.
 * 
 * @author Rob Kooper
 * 
 */
public class MediciSecurityManager implements SecurityManager {
    private static Log                log      = LogFactory.getLog(MediciSecurityManager.class);

    private final Map<String, String> accepted = new HashMap<String, String>();
    private final RBAC                rbac;
    private final boolean             allowDelete;

    public MediciSecurityManager(Context context, boolean allowDelete) {
        this.rbac = new RBAC(context);
        this.allowDelete = allowDelete;
    }

    @Override
    public String getRealm() {
        return "medici"; //$NON-NLS-1$
    }

    @Override
    public String authenticate(String user, String password) {
        if (new Authentication().authenticate(user, password)) {
            String token = UUID.randomUUID().toString();
            accepted.put(token, user);
            return token;
        }
        return null;
    }

    @Override
    public String authenticate(DigestResponse digestRequest) {
        return null;
        //com.bradmcevoy.http.http11.auth.DigestGenerator
        // Hex(MD5(username + ":" + realm + ":" + password))
        //                LogFactory.getLog( MediciSecurityManager.class ).info("Accepting digest blindly.");
        //                String token = UUID.randomUUID().toString();
        //                accepted.put( token, digestRequest.getUser() );
        //                return token;
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth, com.bradmcevoy.http.Resource resource) {
        if (auth == null) {
            return false;
        }
        if (!accepted.containsKey(auth.getTag())) {
            return false;
        }

        // check permissions
        String userid = PersonBeanUtil.getPersonID(accepted.get(auth.getTag()));
        try {
            if (!rbac.checkPermission(Resource.uriRef(userid), MMDB.VIEW_MEMBER_PAGES)) {
                return false;
            }
        } catch (RBACException e) {
            log.info("Could not check permissions.", e);
            return false;
        }

        // no delete
        if (!allowDelete && method.equals(Method.DELETE)) {
            return false;
        }

        // done
        return true;
    }
}
