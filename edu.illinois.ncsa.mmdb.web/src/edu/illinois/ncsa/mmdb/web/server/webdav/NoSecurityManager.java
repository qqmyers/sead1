package edu.illinois.ncsa.mmdb.web.server.webdav;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;

/**
 * Simple implementation of SecurityManger which has no security (i.e. anybody
 * can see anything and do anything).
 * 
 * @author Rob Kooper
 * 
 */
public class NoSecurityManager implements SecurityManager
{
    @Override
    public String getRealm()
    {
        return "unsecure"; //$NON-NLS-1$
    }

    @Override
    public String authenticate( String user, String password )
    {
        return "OK"; //$NON-NLS-1$
    }

    @Override
    public String authenticate( DigestResponse digestRequest )
    {
        return "OK"; //$NON-NLS-1$
    }

    @Override
    public boolean authorise( Request request, Method method, Auth auth, Resource resource )
    {
        return true;
    }
}
