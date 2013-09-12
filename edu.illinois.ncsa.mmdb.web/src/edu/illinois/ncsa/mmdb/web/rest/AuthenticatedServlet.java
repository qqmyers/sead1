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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.util.Base64;

import edu.illinois.ncsa.mmdb.web.client.TextFormatter;
import edu.illinois.ncsa.mmdb.web.server.Authentication;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

public class AuthenticatedServlet extends HttpServlet {
    static Log                 log              = LogFactory.getLog(AuthenticatedServlet.class);

    public static final String AUTHENTICATED_AS = "edu.illinois.ncsa.mmdb.web.server.auth.authenticatedAs";
    public static final String SESSION_KEYS     = "edu.illinois.ncsa.mmdb.web.server.auth.sessionKeys";

    /***********************************************
     * // look up the user's session key.
     * protected String lookupSessionKey(String userId) {
     * for (Map.Entry<String, String> entry :
     * getSessionKeys(getServletContext()).entrySet() ) {
     * if (userId.equals(entry.getValue())) {
     * return entry.getKey();
     * }
     * }
     * log.info("LOGIN: no session key found for user " + userId);
     * return null;
     * }
     * 
     * @SuppressWarnings("unchecked")
     *                                public static Map<String, String>
     *                                getSessionKeys(ServletContext context) {
     *                                // FIXME need a way for session keys to
     *                                expire so this map doesn't take up more
     *                                and more memory
     *                                Map<String, String> sessionKeys =
     *                                (Map<String, String>)
     *                                context.getAttribute(SESSION_KEYS);
     *                                if (sessionKeys == null) {
     *                                sessionKeys = new HashMap<String,
     *                                String>();
     *                                context.setAttribute(SESSION_KEYS,
     *                                sessionKeys);
     *                                }
     *                                return sessionKeys;
     *                                }
     * 
     *                                public static String
     *                                setSessionKey(ServletContext context,
     *                                String userId) {
     *                                String sessionKey =
     *                                SecureHashMinter.getMinter().mint();
     *                                log.info("Generated new session key " +
     *                                sessionKey + " for user " + userId);
     *                                getSessionKeys(context).put(sessionKey,
     *                                userId);
     *                                return sessionKey;
     *                                }
     * 
     *                                public static String
     *                                getUserId(ServletContext context, String
     *                                sessionKey) {
     *                                return
     *                                getSessionKeys(context).get(sessionKey);
     *                                }
     * 
     *                                public static void
     *                                clearSessionKey(ServletContext context,
     *                                String sessionKey) {
     *                                Map<String, String> sk =
     *                                getSessionKeys(context);
     *                                if (sk.containsKey(sessionKey)) {
     *                                log.debug("Destroying session key " +
     *                                sessionKey);
     *                                getSessionKeys(context).remove(sessionKey)
     *                                ;
     *                                }
     *                                }
     * 
     *                                public static String
     *                                getSessionKey(HttpServletRequest request)
     *                                {
     *                                if (request.getCookies() == null) {
     *                                return null;
     *                                }
     *                                for (Cookie cookie : request.getCookies()
     *                                ) {
     *                                if (cookie.getName().equals("sessionKey"))
     *                                {
     *                                return cookie.getValue();
     *                                }
     *                                }
     *                                return null;
     *                                }
     * 
     *                                public void doLogout(HttpServletRequest
     *                                request, HttpServletResponse response) {
     *                                log.debug("Logout Path: " +
     *                                request.getPathInfo());
     *                                HttpSession session =
     *                                request.getSession(false);
     *                                if (session != null) {
     *                                String userId = (String)
     *                                session.getAttribute(AUTHENTICATED_AS);
     *                                session.setAttribute(AUTHENTICATED_AS,
     *                                null);
     *                                log.info(userId + " logged out");
     *                                }
     * 
     *                                String sessionKey =
     *                                getSessionKey(request);
     *                                if (sessionKey != null) {
     *                                clearSessionKey(getServletContext(),
     *                                sessionKey);
     *                                }
     *                                //Get rid of sessionKey cookie(s)
     *                                if (request.getCookies() != null) {
     *                                for (Cookie cookie : request.getCookies()
     *                                ) {
     *                                if (cookie.getName().equals("sessionKey"))
     *                                {
     *                                cookie.setValue("");
     *                                cookie.setPath(request.getContextPath());
     *                                cookie.setMaxAge(0);
     *                                response.addCookie(cookie);
     *                                }
     *                                }
     *                                }
     *                                }
     *****************************************/

    public void doLogout(HttpServletRequest request, HttpServletResponse response) {
        log.debug("Logout Path: " + request.getPathInfo());
        HttpSession session = request.getSession(false);
        if (session != null) {
            String userId = (String) session.getAttribute(AUTHENTICATED_AS);
            session.invalidate();
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

    public static boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        //        // first, see if this HTTP session is already authenticated // FIXME need expiration mechanism!
        String validUser = getHttpSessionUser(request);
        log.debug("In doAuthenticate()");
        log.debug("Path: " + request.getPathInfo());
        //        // to authenticate we need either 1) a "sessionKey" cookie, or 2) credentials
        if (validUser != null) {
            log.debug("Authenticated as " + validUser + " via session");
        } else {
            String auth = request.getHeader("Authorization");
            if (auth != null) {
                // attempt to authenticate provided u/p credentials
                //log.info("raw basic creds = " + auth); // FIXME debug
                String ap[] = auth.split(" ");
                if (ap.length != 2 && !ap[0].equalsIgnoreCase("basic")) {
                    log.warn("can't parse basic creds " + auth); // FIXME debug
                    return unauthorized(request, response);
                }
                //log.info("authorization credentials = " + Base64.decodeToString(ap[1])); // FIXME debug
                String up[] = Base64.decodeToString(ap[1]).split(":");
                if (up.length != 2) {
                    return unauthorized(request, response);
                }
                String username = TextFormatter.unescapeEmailAddress(up[0]);
                String password = up[1];
                if (new Authentication().authenticate(username, password)) {
                    // set the session attribute indicating that we're authenticated
                    validUser = username;
                    // we're authenticating, so we need to generate a session key and put it in the context
                    //                    String sessionKey = setSessionKey(context, validUser);
                    //                    log.info("User " + username + " logged in with valid u/p, sessionKey=" + sessionKey);
                    //                    Cookie cookie = new Cookie("sessionKey", sessionKey);
                    //                    cookie.setPath(request.getContextPath());
                    //                    response.addCookie(cookie);

                    // yes. record the user id in the http session
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }
                    session = request.getSession(true);
                    log.info("User " + validUser + " is now authenticated in HTTP session " + session.getId());
                    session.setAttribute(AUTHENTICATED_AS, validUser);
                }
            }
        }
        // now are we authenticated?
        if (validUser != null) {
            //            // yes. record the user id in the http session
            //            HttpSession session = request.getSession(true);
            //            if (session.getAttribute(AUTHENTICATED_AS) == null) {
            //                log.info("User " + validUser + " is now authenticated in HTTP session " + session.getId());
            //                session.setAttribute(AUTHENTICATED_AS, validUser);
            //            }
            return authorized(request);
        } else if (request.getRequestURI().contains("api/video")) {
            if (request.getHeader("User-Agent").startsWith("AppleCoreMedia") || request.getHeader("User-Agent").startsWith("QuickTime")) {
                // FIXME : special case for quicktime player.
                validUser = PersonBeanUtil.getAnonymous().getName().toLowerCase();
                HttpSession session = request.getSession(true);
                if (session.getAttribute(AUTHENTICATED_AS) == null) {
                    log.info("Special Apple case, validated as anonymous in HTTP session " + session.getId());
                    session.setAttribute(AUTHENTICATED_AS, validUser);
                }
                return authorized(request);
            } else {
                log.info("Video request from non authenticated user with User-Agent=" + request.getHeader("User-Agent"));
            }
        } else if (request.getRequestURI().contains("api/image/preview/")) {
            // FIXME : special image case for nced data
            validUser = PersonBeanUtil.getAnonymous().getName().toLowerCase();
            HttpSession session = request.getSession(true);
            if (session.getAttribute(AUTHENTICATED_AS) == null) {
                log.info("Special image case, validated as " + validUser + " in HTTP session " + session.getId());
                session.setAttribute(AUTHENTICATED_AS, validUser);
            }
            return authorized(request);
        }
        // no. reject
        log.info("Client provided no credentials, returning 403 Unauthorized");
        return unauthorized(request, response);
    }

    static boolean unauthorized(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "BASIC realm=\"Medici REST service @ " + request.getContextPath() + "\""); // FIXME need webapp-specific realm
        return false;
    }

    static boolean authorized(HttpServletRequest request) {
        return true;
    }

    protected boolean authenticate(HttpServletRequest request, HttpServletResponse response) {
        return doAuthenticate(request, response, getServletContext());
    }
}
