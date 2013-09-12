/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.server.resteasy;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.resteasy.util.Base64;
import org.jboss.resteasy.util.HttpResponseCodes;

import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.rest.AuthenticatedServlet;
import edu.illinois.ncsa.mmdb.web.server.Authentication;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * Check authentication and authorization:
 * Authentication requires having a valid username/password or session token
 * that indicates a previous successful authentication
 * Authorization requires that the user have
 * edu.illinois.ncsa.mmdb.common.Permission.USE_REMOTEAPI permission and that
 * the app send
 * the Medici-defined remoteAPIKey if it is defined (non-null)
 * 
 * @author Luigi Marini <lmarini@illinois.edu>, Jim Myers(myersjd@umich.edu)
 * 
 * 
 * 
 */
@Provider
@ServerInterceptor
public class AuthenticationInterceptor implements PreProcessInterceptor {

    /** Commons logging **/
    private static Log         log = LogFactory.getLog(AuthenticationInterceptor.class);

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method)
            throws UnauthorizedException {

        String mykey = TupeloStore.getInstance().getConfiguration(ConfigurationKey.RemoteAPIKey);
        if ((mykey != null) && (mykey.length() != 0)) {
            log.debug("Medici RemoteAPIKey = " + mykey);
            String keyName = ConfigurationKey.RemoteAPIKey.getPropertyKey();
            String theirkey = request.getFormParameters().getFirst(keyName);
            log.debug("Key from form: " + keyName + " : " + theirkey);
            if (theirkey == null) {
                theirkey = request.getUri().getQueryParameters().getFirst(keyName);
                log.debug("Key from query: " + keyName + " : " + theirkey);
            }
            if ((theirkey == null) || (!(mykey.equals(theirkey)))) {
                log.debug("RemoteAPIKey does not match - Access Forbidden");
                return forbiddenResponse(request.getPreprocessedPath());
            }
        }
        //remoteAPIKey matches or is not required...
        //Now identify the user

        //request.setAttribute("userid", PersonBeanUtil.getAnonymousURI().toString());
        //            return null;
        //    }
        String userid = null;
        //Retrieve userid from session if it exists
        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            userid = (String) session.getAttribute(AuthenticatedServlet.AUTHENTICATED_AS);
            log.debug("Found session - Sucessfully authenticated as " + userid);
        } else if (request.getHttpHeaders().getRequestHeader("Authorization") != null) {
            String token = request.getHttpHeaders().getRequestHeader("Authorization").get(0);
            if (token != null && ((userid = checkLoggedIn(token)) != null)) {
                log.debug("Authorization header found - Sucessfully authenticated as " + userid);
            }
        }
        if (userid == null) {
            return unauthorizedResponse(request.getPreprocessedPath());
        } else {
            request.setAttribute("userid", userid);

        }
        //Now determine whether userid is authorized to use the remoteAPI...

        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        try {
            if (!rbac.checkPermission(userid, Permission.USE_REMOTEAPI)) {
                log.debug("user: " + userid + "  forbidden");
                return forbiddenResponse(request.getPreprocessedPath());
            }
        } catch (RBACException e) {
            e.printStackTrace();
            return forbiddenResponse(request.getPreprocessedPath());
        }
        //Authenticated user with necessary remoteAPI permission...
        log.debug("User: " + userid + " successfully authenticated and authorized");
        return (null);
    }

    /**
     * Response in case of failed authentication.
     * 
     * @param preprocessedPath
     * @return
     */
    private ServerResponse unauthorizedResponse(String preprocessedPath) {
        ServerResponse response = new ServerResponse();
        response.setStatus(HttpResponseCodes.SC_UNAUTHORIZED);
        MultivaluedMap<String, Object> headers = new Headers<Object>();
        headers.add("Content-Type", "text/plain");
        response.setMetadata(headers);
        response.setEntity("Error 401 Unauthorized: "
                + preprocessedPath);
        return response;
    }

    /**
     * Response in case of failed authorization.
     * 
     * @param preprocessedPath
     * @return
     */
    private ServerResponse forbiddenResponse(String preprocessedPath) {
        ServerResponse response = new ServerResponse();
        response.setStatus(HttpResponseCodes.SC_FORBIDDEN);
        MultivaluedMap<String, Object> headers = new Headers<Object>();
        headers.add("Content-Type", "text/plain");
        response.setMetadata(headers);
        response.setEntity("Error 403 Forbidden: "
                + preprocessedPath);
        return response;
    }

    /**
     * Decode token and check against local user database.
     * 
     * @param token
     * @return the id of the user, or null if it failed
     */
    private String checkLoggedIn(String token) {
        try {
            String decoded = new String(Base64.decode(token.substring(6)));
            int lastIndex = decoded.lastIndexOf(":");
            if (lastIndex != -1) {
                String user = decoded.substring(0, lastIndex);
                String password = decoded.substring(lastIndex + 1);
                log.debug("U: " + user + " P: " + password);

                if ((new Authentication()).authenticate(user, password)) {
                    log.debug("REST Authentication successful");
                    return PersonBeanUtil.getPersonID(user);
                } else {
                    log.debug("REST Authentication failed");
                    return null;
                }
            } else {
                log.error("Authentication token not complete");
                return null;
            }
        } catch (IOException e) {
            log.error("Error decoding token");
        }
        return null;
    }
}