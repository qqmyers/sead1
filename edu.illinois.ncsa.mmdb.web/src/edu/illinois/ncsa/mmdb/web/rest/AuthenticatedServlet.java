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
package edu.illinois.ncsa.mmdb.web.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.util.Base64;

import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.Authentication;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

public class AuthenticatedServlet extends HttpServlet {
    /**
     * 
     */
    private static final long  serialVersionUID = -4256332408054511050L;

    static Log                 log              = LogFactory.getLog(AuthenticatedServlet.class);

    public static final String AUTHENTICATED_AS = "edu.illinois.ncsa.mmdb.web.server.auth.authenticatedAs";
    public static final String _anonymousId     = PersonBeanUtil.getAnonymous().getUri();                   ;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        postAuthenticate(request, response);
    }

    public void postAuthenticate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //FIXME .equals("/authenticate") ?
        log.debug("POST Authenticate");
        String validUser = null;
        if (request.getRequestURL().toString().endsWith("authenticate")) {

            //Expect two application/x-www-form-urlencoded parameters

            String username = request.getParameter("username");
            String password = request.getParameter("password");
            log.debug("u: " + username);
            log.debug("p: " + password);
            if ((username != null) && (!username.equals("")) && (password != null) && (!password.equals(""))) {
                if (new Authentication().authenticate(username, password)) {
                    // set the session attribute indicating that we're authenticated
                    validUser = username;
                    // we're authenticating, so we need to create a session and put it in the context
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        log.debug("Invalidating: " + session.getId());
                        session.invalidate();
                    }
                    session = request.getSession(true);
                    log.info("User " + validUser + " is now authenticated in HTTP session " + session.getId());
                    session.setAttribute(AUTHENTICATED_AS, validUser);
                    response.getWriter().print(session.getId());
                    /*To allow webapps such as ACR Dashboard, Discovery, and Geobrowse to start a session/
                     * set a cookie for mmdb in the user's browser when they are not hosted on the same server as medici/mmdb, 
                     * it looks like two steps would work:
                     * 1) add the headers below allowing the webapp to make a client-side request to the mmdb domain
                     *    (w/o headers, the call can be made, but the response can't be read. I've verified that the headers
                     *    work to let the app see the response(i.e. the sessionKey). They do not allow the set cookie request to succeed
                     *    (request.getSession(true))
                     * 2) launch an iframe that, on load, makes a request to set the mmdb cookie - 
                     *    i.e. from calling this method, the app can get the seesionKey - the iframe could make a request
                     *    (for post on load) to mediciURL/joinsession/sessionKey which could then respond with a valid session.
                     *    I have not tried step 2, but the web says it works...
                     *    
                     *    For v1, we'll turn this off. After that, we can consider adding it if other forms of sso don't obviate the need.
                     */
                    //response.addHeader("Access-Control-Allow-Origin", "*");
                    //response.addHeader("Access-Control-Allow-Methods", "PUT, POST");

                }
            }
            if (validUser == null) {
                response.getWriter().print("");
                //Set 404 - we don't want to invoke the browser authentication attempt we'd get from sending 401...

                response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            }
            dontCache(response);
            response.flushBuffer();

        }
        //else - pass through to derived classes w/o handling anything
    }

    public void doLogout(HttpServletRequest request, HttpServletResponse response) {
        log.debug("Logout Path: " + request.getPathInfo());
        HttpSession session = request.getSession(false);
        if (session != null) {
            String userId = (String) session.getAttribute(AUTHENTICATED_AS);
            session.invalidate();
            //  response.setHeader("Connection", "close");
            log.info(userId + " logged out");
        }
    }

    /*******************************************************/
    protected void logout(HttpServletRequest request, HttpServletResponse response) {
        doLogout(request, response);
    }

    protected static String getHttpSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute(AUTHENTICATED_AS);
        } else {
            return null;
        }
    }

    public static String getUserUri(HttpServletRequest req) {
        String username = getHttpSessionUser(req);
        if (username != null) {
            return PersonBeanUtil.getPersonID(username);
        } else {
            return null;
        }
    }

    /* This method checks credentials and returns the users identity - 'anonymous' if no other credentials are presented
     * It does not change state by storing the credentials in the session (which requires a call to /api/authenticate
     * It's primary use is to retrieve the ID from the session, but it also supports the case where a URL is requested outside
     * an app/session context - i.e. when the browser sends a Basic auth header in response to a 401 
     * 
     */
    public static String doBasicAuthenticate(HttpServletRequest request, HttpServletResponse response) throws HTTPException {

        log.debug("In doBasicAuthenticate()");
        log.debug("Path: " + request.getPathInfo());
        String validUser = null;

        String auth = request.getHeader("Authorization");
        if (auth != null) {
            // attempt to authenticate provided u/p credentials
            //log.info("raw basic creds = " + auth); // FIXME debug
            String ap[] = auth.split(" ");
            if (ap.length != 2 && !ap[0].equalsIgnoreCase("basic")) {
                log.warn("can't parse basic creds " + auth);
                throw (new HTTPException(HttpServletResponse.SC_UNAUTHORIZED));
            }
            log.debug("authorization credentials = " + Base64.decodeToString(ap[1])); // FIXME debug
            String up[] = Base64.decodeToString(ap[1]).split(":");
            if (up.length != 2) {
                throw (new HTTPException(HttpServletResponse.SC_UNAUTHORIZED));
            }
            String username = TextFormatter.unescapeEmailAddress(up[0]);
            String password = up[1];
            log.debug("Authing u: " + username + " p: " + password);
            if (new Authentication().authenticate(username, password)) {
                // set the session attribute indicating that we're authenticated
                validUser = username;
            }
            else {
                throw (new HTTPException(HttpServletResponse.SC_UNAUTHORIZED));
            }
        }

        // now are we authenticated?
        if (validUser != null) {
            log.debug("Returning with valid user: " + validUser);
            return PersonBeanUtil.getPersonID(validUser);
        } else {
            //There was no auth header, try anonymous and let derived classes decide what to return
            return PersonBeanUtil.getAnonymous().getUri();
        }
    }

    void dontCache(HttpServletResponse response) {
        // OK, we REALLY don't want the browser to cache this. For reals
        response.addHeader("cache-control", "no-store, no-cache, must-revalidate, max-age=-1"); // don't cache
        response.addHeader("cache-control", "post-check=0, pre-check=0, false"); // really don't cache
        response.addHeader("pragma", "no-cache, no-store"); // no, we mean it, really don't cache
        response.addHeader("expires", "-1"); // if you cache, we're going to be very, very angry
    }

    Context getContext() {
        return TupeloStore.getInstance().getContext();
    }

    protected boolean isAllowed(String userId, String objectUri, Permission permission) {
        SEADRbac rbac = new SEADRbac(getContext());
        Resource userUri = Resource.uriRef(userId);
        Resource permissionUri = Resource.uriRef(permission.getUri());
        Resource oUri = objectUri != null ? Resource.uriRef(objectUri) : null; // how I long for implicit type conversion
        try {
            if (!rbac.checkPermission(userUri, oUri, permissionUri)) {
                return false;
            }
        } catch (RBACException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected boolean isAllowed(String userId, Permission permission) {
        return isAllowed(userId, null, permission);
    }

    static void unauthorized(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        log.debug("Setting WWW-Authenticate header");
        response.setHeader("WWW-Authenticate", "BASIC realm=\"SEAD ACR Services @ " + request.getContextPath() + "\"");
    }

}
