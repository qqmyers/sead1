package edu.illinois.ncsa.mmdb.web.server.webdav;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;

/**
 * Interface used for security. This is used by the AbstractResource to check if
 * the user is logged in and has permissions to see a specific resource.
 * 
 * @author Rob Kooper
 * 
 */
public interface SecurityManager
{
    /**
     * Return the security realm for this resource. Just any string identifier.
     * 
     * This will be used to contruct authorization challenges and will be used
     * on Digest authentication to construct the expected response.
     */
    public String getRealm();

    /**
     * Check the given credentials, and return a relevant object if accepted.
     * 
     * Returning null indicates credentials were not accpeted
     * 
     * @param user
     *            - the username provided by the user's agent
     * @param password
     *            - the password provided by the user's agent
     * @return - if credentials are accepted, some object to attach to the Auth
     *         object. otherwise null
     */
    public String authenticate( String user, String password );

    /**
     * Check the given credentials, and return a relevant object if accepted.
     * 
     * Returning null indicates credentials were not accpeted
     * 
     * You SHOULD use com.bradmcevoy.http.http11.auth.DigestGenerator to
     * implement digest calculation, and then compare that to the given request
     * digest.
     * 
     * @param digestRequest
     *            - the digest authentication information provided by the client
     * @return - if credentials are accepted, some object to attach to the Auth
     *         object. otherwise null
     */
    public String authenticate( DigestResponse digestRequest );

    /**
     * Return true if the current user is permitted to access this resource
     * using the specified method.
     * 
     * Note that the current user may be determined by the Auth associated with
     * the request, or by a seperate, application specific, login mechanism such
     * as a session variable or cookie based system. This method should
     * correctly interpret all such mechanisms
     * 
     * The auth given as a parameter will be null if authentication failed. The
     * auth associated with the request will still exist
     */
    public boolean authorise( Request request, Method method, Auth auth );
}
