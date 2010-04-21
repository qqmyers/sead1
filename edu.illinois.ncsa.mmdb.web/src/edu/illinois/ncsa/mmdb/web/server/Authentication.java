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

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.bard.jaas.UsernamePasswordContextHandler;

public class Authentication {
    private static final String JAAS_CONFIG = "/jaas.config";

    /** Commons logging **/
    private static Log          log         = LogFactory.getLog(Authentication.class);

    public boolean authenticate(String username, String password) {
        log.debug("LOGIN: Authenticating '" + username + "' via JAAS");

        if (username.length() == 0) {
            return false;
        }

        String loc = TupeloStore.findFile(JAAS_CONFIG).toExternalForm();

        // Workaround SUN's workaround, hopefully this is only a problem with a space.
        if (loc.startsWith("file:")) {
            loc = loc.replace("%20", " ");
        }

        System.setProperty("java.security.auth.login.config", loc);
        UsernamePasswordContextHandler handler = new UsernamePasswordContextHandler(username, password, TupeloStore.getInstance().getContext());

        Subject subject = new Subject();
        LoginContext ctx = null;

        try {
            ctx = new LoginContext("mmdb", subject, handler);
            ctx.login();
            log.debug("LOGIN: JAAS authentication suceeded for " + username);
        } catch (LoginException ex) {
            log.debug("LOGIN: JAAS authentication FAILED for " + username, ex);
            return false;
        }

        return true;
    }
}
