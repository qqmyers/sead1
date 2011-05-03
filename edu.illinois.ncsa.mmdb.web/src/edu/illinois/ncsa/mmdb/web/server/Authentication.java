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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.rdf.Resource;

import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.Anonymous;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.AuthenticationException;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.ContextAuthentication;

public class Authentication {
    /** Commons logging **/
    private static Log log = LogFactory.getLog(Authentication.class);

    public boolean authenticate(String username, String password) {
        log.debug("LOGIN: Authenticating attempt for " + username);

        if (username.length() == 0) {
            return false;
        }

        try {
            Resource person = Resource.uriRef(PersonBeanUtil.getPersonID(username));
            if (person.getString().equals(Anonymous.USER)) {
                log.debug("LOGIN: anonymous login successful");
                return true;
            }
            ContextAuthentication ca = new ContextAuthentication(TupeloStore.getInstance().getContext());
            if (ca.checkPassword(person, password)) {
                log.debug("LOGIN: authentication suceeded for " + username);
                return true;
            } else {
                log.debug("LOGIN: authentication failed for " + username);
                return false;
            }
        } catch (AuthenticationException ex) {
            log.debug("LOGIN: authentication FAILED for " + username, ex);
            return false;
        }
    }
}
