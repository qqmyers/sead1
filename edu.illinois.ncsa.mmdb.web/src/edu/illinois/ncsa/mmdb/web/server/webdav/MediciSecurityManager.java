package edu.illinois.ncsa.mmdb.web.server.webdav;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
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

/**
 * Simple implementation of SecurityManger for medici. This will use the medici
 * to check if the username and password and if the user has access.
 * 
 * @author Rob Kooper
 * 
 */
public class MediciSecurityManager implements SecurityManager
{
    private static Log          log      = LogFactory.getLog( MediciSecurityManager.class );

    private Map<String, String> accepted = new HashMap<String, String>();
    private RBAC                rbac;

    public MediciSecurityManager( Context context )
    {
        rbac = new RBAC( context );
    }

    @Override
    public String getRealm()
    {
        return "medici"; //$NON-NLS-1$
    }

    @Override
    public String authenticate( String user, String password )
    {
        if ( new Authentication().authenticate( user, password ) ) {
            String token = UUID.randomUUID().toString();
            accepted.put( token, user );
            return token;
        }
        return null;
    }

    @Override
    public String authenticate( DigestResponse digestRequest )
    {
        return null;
        //com.bradmcevoy.http.http11.auth.DigestGenerator
        // Hex(MD5(username + ":" + realm + ":" + password))
        //                LogFactory.getLog( MediciSecurityManager.class ).info("Accepting digest blindly.");
        //                String token = UUID.randomUUID().toString();
        //                accepted.put( token, digestRequest.getUser() );
        //                return token;
    }

    @Override
    public boolean authorise( Request request, Method method, Auth auth, com.bradmcevoy.http.Resource resource )
    {
        if ( auth == null ) {
            return false;
        }
        if ( !accepted.containsKey( auth.getTag() ) ) {
            return false;
        }

        // check permissions
        String userid = PersonBeanUtil.getPersonID( accepted.get( auth.getTag() ) );
        try {
            return rbac.checkPermission( Resource.uriRef( userid ), MMDB.VIEW_MEMBER_PAGES );
        } catch ( OperatorException e ) {
            log.info( "Could not check permissions.", e );
            return false;
        }
    }
}
