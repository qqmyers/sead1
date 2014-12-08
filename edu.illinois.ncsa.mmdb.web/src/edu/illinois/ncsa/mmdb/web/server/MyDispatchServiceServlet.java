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
package edu.illinois.ncsa.mmdb.web.server;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.customware.gwt.dispatch.server.service.DispatchServiceServlet;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.Result;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.mmdb.web.client.dispatch.AuthorizedAction;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetConfiguration;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetMimeTypeCategories;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetOauth2ServerFlowState;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GoogleOAuth2Props;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GoogleUserInfo;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Oauth2ServerFlowTokenRequest;
import edu.illinois.ncsa.mmdb.web.client.dispatch.Oauth2ServerFlowUserInfo;
import edu.illinois.ncsa.mmdb.web.rest.AuthenticatedServlet;
import edu.illinois.ncsa.mmdb.web.server.dispatch.GoogleUserInfoHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.Oauth2ServerFlowTokenRequestHandler;
import edu.illinois.ncsa.mmdb.web.server.dispatch.Oauth2ServerFlowUserInfoHandler;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;

/**
 * Default dispatch servlet.
 *
 * @author Luigi Marini
 * @author Jim Myers
 *
 */

public class MyDispatchServiceServlet extends DispatchServiceServlet {

    static Log                log              = LogFactory.getLog(MyDispatchServiceServlet.class);

    private static final long serialVersionUID = 2464722364321662618L;

    public MyDispatchServiceServlet() {
        super(DispatchUtil.getDispatch());
    }

    public static String getUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute(AuthenticatedServlet.AUTHENTICATED_AS);
        }
        return null;
    }

    @Override
    protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        // here we capture the URL prefix of the request, for use in canonicalization later

        TupeloStore.getInstance().getUriCanonicalizer(arg0);
        super.service(arg0, arg1);
    }

    @Override
    public Result execute(Action<?> action) throws ActionException {

        HttpServletRequest request = getThreadLocalRequest();
        String user = getUser(request);
        if (user != null) {
            user = PersonBeanUtil.getPersonID(user);
        }
        //FIXME elsewhere - should not trust that client has sent the current user's name in the actions
        //Shouldn't send user at all in AuthorizedAction
        if (action instanceof AuthorizedAction) {
            ((AuthorizedAction<?>) action).setUser(user);
        }

        if (user == null) {

            log.debug("executing action: " + action.getClass().getName());
            //If you're not authenticated to the server, don't do the dispatch
            //These are the only actions that should be allowed when there are no credentials
            if (!((action instanceof GoogleOAuth2Props) ||
                    (action instanceof GetConfiguration) ||
                    (action instanceof GoogleUserInfo) ||
                    (action instanceof GetMimeTypeCategories) ||
                    (action instanceof GetOauth2ServerFlowState) ||
                    (action instanceof Oauth2ServerFlowTokenRequest) || (action instanceof Oauth2ServerFlowUserInfo))) {
                log.debug("Refusing a dispatch request due to lack of credentials: " + action.getClass().getName());

                //FIXME: Is there a lighter-weight option, e.g. to send an ActionException from here?
                throw new ActionException("User has no server credentials");
            }
        }

        // Required to login user for restful API
        if (action instanceof GoogleUserInfo) {
            log.debug("GoogleUserInfoHandler is being called");
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            GoogleUserInfoHandler.setSession(request.getSession(true));
            GoogleUserInfoHandler.setServer(getServer(request));
        } else if (action instanceof Oauth2ServerFlowTokenRequest) {
            log.debug("Oauth2ServerFlowTokenRequestHandle is being called");
            Oauth2ServerFlowTokenRequestHandler.setSession(request.getSession(true));
        } else if (action instanceof Oauth2ServerFlowUserInfo) {
            log.debug("Oauth2ServerFlowUserInfoHandler is being called");
            HttpSession session = request.getSession(false);
            if (session != null) {
                String id = (String) session.getAttribute("orcid_id");
                int expires_at = (Integer) session.getAttribute("expires_at");
                Oauth2ServerFlowUserInfoHandler.setId(id);
                Oauth2ServerFlowUserInfoHandler.setExpiresAt(expires_at);
                log.debug(getServer(request));
                Oauth2ServerFlowUserInfoHandler.setServer(getServer(request));
                session.invalidate();
            } else {
                log.debug("Null Session");
            }
            Oauth2ServerFlowUserInfoHandler.setSession(request.getSession(true));
        }

        Result execute = super.execute(action);

        if (action instanceof GoogleUserInfo) {
            GoogleUserInfoHandler.setSession(null);
        } else if (action instanceof Oauth2ServerFlowTokenRequest) {
            Oauth2ServerFlowTokenRequestHandler.setSession(null);
            @SuppressWarnings("unchecked")
            Enumeration<String> enumeration = request.getSession(false).getAttributeNames();
            while (enumeration.hasMoreElements()) {
                log.debug("Session has: " + enumeration.nextElement());
            }
        } else if (action instanceof Oauth2ServerFlowUserInfo) {
            @SuppressWarnings("unchecked")
            Enumeration<String> enumeration = request.getSession(false).getAttributeNames();
            while (enumeration.hasMoreElements()) {
                log.debug("Session has: " + enumeration.nextElement());
            }
            Oauth2ServerFlowUserInfoHandler.setSession(null);
        }
        return execute;
    }

    private String getServer(HttpServletRequest request) {
        StringBuffer sBuffer = request.getRequestURL();
        String sp = request.getServletPath();
        String pi = request.getPathInfo();
        int end = sBuffer.length();
        if (sp != null) {
            end -= sp.length();
        }
        if (pi != null) {
            end -= pi.length();
        }
        return sBuffer.substring(0, end);
    }
}
